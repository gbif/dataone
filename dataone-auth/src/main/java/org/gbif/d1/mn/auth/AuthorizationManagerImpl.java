package org.gbif.d1.mn.auth;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.apis.v1.SystemMetadataProvider;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidCredentials;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.AccessPolicy;
import org.dataone.ns.service.types.v1.AccessRule;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.NodeType;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enforces DataONE based authorization rules.
 * <p>
 * All methods in this class will throw {@link NullPointerException} if any parameter passed in is null.
 * <p>
 * Methods marked @VisibleForTesting are not intended for use outside of this class.
 * <p>
 * Inheritance of this class is disallowed since this is core to the authorization model within DataONE.
 * <p>
 * This class is conditionally thread-safe and depends on the thread-safety of the provided
 * {@link SystemMetadataProvider} and {@link CoordinatingNode}. If they are thread-safe as they should be, this class is
 * unconditionally thread-safe. If they are not then users must synchronize access.
 */
final class AuthorizationManagerImpl implements AuthorizationManager {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizationManager.class);

  private final SystemMetadataProvider systemMetadataProvider;
  private final CoordinatingNode cn;
  private final Set<String> selfSubjects; // identifying ourselves, a member node
  private final CertificateUtils certificateUtils;

  /**
   * Builds a new instance using the default OID.
   */
  AuthorizationManagerImpl(SystemMetadataProvider systemMetadataProvider, CoordinatingNode cn, Node self) {
    this(systemMetadataProvider, cn, self, ImmutableList.of(DEFAULT_OID_SUBJECT_INFO));
  }

  /**
   * @param systemMetadataProvider The provider of system metadata
   * @param cn The coordinating node
   * @param self The Node object representing ourselves as a running Node in DataONE
   * @param subjectInfoExtensionOIDs The names of extensions within the certificate to use to find SubjectInfo
   *        information
   */
  AuthorizationManagerImpl(SystemMetadataProvider systemMetadataProvider, CoordinatingNode cn, Node self,
    List<String> subjectInfoExtensionOIDs) {
    Preconditions.checkNotNull(systemMetadataProvider, "The systemMetadataProvider is required");
    Preconditions.checkNotNull(cn, "Access to a coordinating node (e.g. a ws client) is required");
    Preconditions.checkNotNull(self, "The Node representing the running installation is required");
    Preconditions.checkNotNull(self.getSubject(), "The subject identifiers for this running node are required");
    Preconditions.checkState(!self.getSubject().isEmpty(),
      "The subject identifiers for this running node are required to be populated");
    Preconditions.checkNotNull(subjectInfoExtensionOIDs,
      "The OIDs to the subject info extensions within certificates is required");

    this.systemMetadataProvider = systemMetadataProvider;
    this.cn = cn;
    certificateUtils = CertificateUtils.newInstance(subjectInfoExtensionOIDs);
    selfSubjects = Subjects.fromNode(self);
    LOG.debug("MN is configured with subject identifications: {}", selfSubjects);
  }

  /**
   * TODO: document this complex procedure
   * Extracts the certificate from the request, which must be present and runs the authentication routine.
   * This will either complete indicating that the subject is authorized or a NotAuthorized exception will be thrown.
   * 
   * @throws InsufficientResources
   * @throws NotImplemented
   * @throws InvalidToken
   * @throws ServiceFailure
   * @throws NotFound
   * @throws NotAuthorized
   * @throws InvalidCredentials
   * @throws InvalidRequest
   */
  @Override
  public void checkIsAuthorized(HttpServletRequest request, Identifier identifier, Permission permission,
    String detailCode) throws NotAuthorized, NotFound, ServiceFailure, InvalidToken, InsufficientResources,
    InvalidRequest, InvalidCredentials {
    Preconditions.checkNotNull(request, "A request must be provided");
    Preconditions.checkNotNull(identifier, "An identifier must be provided");
    Preconditions.checkNotNull(identifier.getValue(), "An identifier must be provided");
    Preconditions.checkNotNull(permission, "A permission must be provided");

    SystemMetadata sysMetadata = systemMetadataProvider.getSystemMetadata(identifier.getValue());
    if (sysMetadata == null) {
      throw new NotFound("Cannot perform action since object not found", detailCode, identifier.getValue());
    }

    Session session = certificateUtils.newSession(request, detailCode); // throws exception if could not be built
    boolean approved = checkIsAuthorized(session, sysMetadata, permission, detailCode);
    if (!approved) {
      throw new NotAuthorized("No subject represented by the certificate have permission to perform action", detailCode);
    }
  }

  /**
   * @return true if subjects contains the target
   */
  private boolean contains(List<Subject> subjects, final String target) {
    return Iterables.any(subjects, new Predicate<Subject>() {

      @Override
      public boolean apply(Subject input) {
        return target.equals(input.getValue());
      }
    });
  }

  /**
   * Returns true if the rights holder in the system metadata is one of the subjects passed in.
   */
  private boolean isRightsHolder(SystemMetadata sysMetadata, Set<String> subjects) {
    Subject rightsHolder = sysMetadata.getRightsHolder();
    Preconditions.checkNotNull(rightsHolder, "An object cannot exist without a rights holder");
    boolean approved = subjects.contains(rightsHolder.getValue());
    LOG.debug("Subject[{}] does not contain the rights holder[{}]", subjects, rightsHolder.getValue());
    return approved;
  }

  /**
   * Runs through the procedure of verification returning whether approved or not.
   * Procedure is executed in an order to ensure that local calls are executed before calls requiring network calls.
   */
  @VisibleForTesting
  boolean checkIsAuthorized(Session session, SystemMetadata sysMetadata, Permission permission, String detailCode)
    throws ServiceFailure {
    try {
      String primary = Subjects.primary(session);

      // Is this call coming with our own credentials?
      // Perhaps we've got local applications using the same certificate for example.
      if (selfSubjects.contains(primary)) {
        LOG.debug("Session[{}] is approved since request came from ourselves", session);
        return true;
      }

      // get the primary, alternative identities and all groups from the caller
      Set<String> subjects = Subjects.allSubjects(session);
      // is there an access rule covering the session (e.g. PUBLIC READ must be very common)
      if (isGrantedByAccessPolicy(sysMetadata, subjects, permission)) {
        LOG.debug("System metadata[{}] access rules grant access to session[{}]", sysMetadata, session);
        return true;
      }

      // the rights holder is granted permission
      if (isRightsHolder(sysMetadata, subjects)) {
        LOG
          .info("The rights holder named in the system metadata[{}] is found in the session[{}]", sysMetadata, session);
        return true;
      }

      // any CN or the authoritative MN is granted permission, but requires a network call to a CN
      if (isAuthorityNodeOrCN(primary, sysMetadata)) {
        LOG.debug("The session[{}] originates from the CN or the authoritative member node", session);
        return true;
      }

      LOG.debug("The session[{}] is not permitted", session);
      return false;

    } catch (ServiceFailure e) {
      throw new ServiceFailure(e.getMessage(), detailCode, e);
    }
  }

  /**
   * Looks up if the subject is the authority member node for the object or a CN.
   * This is combined into a single operation to minimize network calls to the CN.
   */
  @VisibleForTesting
  boolean isAuthorityNodeOrCN(String subject, SystemMetadata sysMetadata) throws ServiceFailure {
    NodeReference authNode = sysMetadata.getAuthoritativeMemberNode();
    Preconditions.checkNotNull(authNode, "The authoritative member node cannot be null on an object");
    String authoritativeMN = authNode.getValue();

    // short cut: if the sysMetadata lists the subject as the authoritative member node we can grant immediately
    if (authoritativeMN.equals(subject)) {
      LOG.debug("System metadata lists subject as authoritative MN: {}", subject);
      return true;
    }

    // call the coordinating node and get a list of all nodes including all their alias subjects
    // if the original request comes from a CN or the named authoritative MN then it is granted
    for (Node node : cn.listNodes().getNode()) {

      if (NodeType.CN == node.getType()
        && contains(node.getSubject(), subject)) {
        LOG.debug("Request received from a known alias[{}] of a CN[{}]", subject, node.getSubject());
        return true;

      } else if (NodeType.MN == node.getType()
        && contains(node.getSubject(), authoritativeMN)
        && contains(node.getSubject(), subject)) {
        LOG.debug("Request received from a known alias[{}] of the listed authoritative MN[{}]", subject,
          node.getSubject());
        return true;

      }
    }
    return false;
  }

  /**
   * Inspects the system metadata access rules and returns true if a subject passed in is explicitly granted the
   * permission sought.
   */
  @VisibleForTesting
  boolean isGrantedByAccessPolicy(SystemMetadata sysMetadata, Set<String> subjects, Permission permission) {
    AccessPolicy accessPolicy = sysMetadata.getAccessPolicy();
    if (accessPolicy != null) {
      for (AccessRule rule : accessPolicy.getAllow()) {

        // rules cascade such that CHANGE grants WRITE automatically and WRITE grants READ
        // expand the rule permissions to explicitly list all permissions
        Set<Permission> permissions = Permissions.expand(rule.getPermission());

        // verify the rule covers the level of access requested first
        if (permissions.contains(permission)) {
          for (Subject subject : rule.getSubject()) {
            // check if a subject passed in is explicitly named in the rule
            if (subject.getValue() != null && subjects.contains(subject.getValue())) {
              LOG.debug("An explicit access rule has granted {} permission to subject[{}]", permission,
                subject.getValue());
              return true;
            }
          }

        } else {
          LOG.debug("{} not granted by rule[{}]", permission, rule);
        }
      }
    }
    return false;
  }

}
