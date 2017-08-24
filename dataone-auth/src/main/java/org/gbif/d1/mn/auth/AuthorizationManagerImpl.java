package org.gbif.d1.mn.auth;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.apis.v1.SystemMetadataProvider;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
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

  @VisibleForTesting
  AuthorizationManagerImpl(SystemMetadataProvider systemMetadataProvider, CoordinatingNode cn, Node self) {
    this(systemMetadataProvider, cn, self, ImmutableList.of(AuthorizationManager.DEFAULT_OID_SUBJECT_INFO));
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

  @Override
  public Session checkIsAuthorized(HttpServletRequest request, String id, Permission permission) {
    return checkIsAuthorized(certificateUtils.newSession(request), id, permission);
  }

  @Override
  public Session checkIsAuthorized(HttpServletRequest request, Permission permission) {
    return checkIsAuthorized(certificateUtils.newSession(request), permission);
  }

  @Override
  public Session checkIsAuthorized(Session session, String id, Permission permission) {
    Preconditions.checkNotNull(session, "A session must be provided");
    Preconditions.checkNotNull(id, "An identifier must be provided");
    Preconditions.checkNotNull(permission, "A permission must be provided");

    LOG.debug("Checking permission for {}", id);
    SystemMetadata sysMetadata = systemMetadataProvider.getSystemMetadata(session,
                                                                          Identifier.builder().withValue(id).build());
    if (sysMetadata == null) {
      throw new NotFound("Cannot perform action since object not found", id);
    }

    if (!checkIsAuthorized(session, sysMetadata, permission)) {
      throw new NotAuthorized("No subject represented by the certificate have permission to perform action");
    }
    return session;
  }

  /**
   * Retir
   */
  @Override
  public Session checkIsAuthorized(Session session, Permission permission) {
    Preconditions.checkNotNull(session, "A session must be provided");
    Preconditions.checkNotNull(permission, "A permission must be provided");

    // call the coordinating node and get a list of all nodes including all their alias subjects
    // if the original request comes from a CN then it is granted
    try {
      for (Node node : cn.listNodes().getNode()) {
        if (NodeType.CN == node.getType()
            && contains(node.getSubject(), session.getSubject().toString())) {
          LOG.debug("Request received from a known alias[{}] of a CN[{}]", session.getSubject(), node.getSubject());
          return session;
        }
      }
    } catch (ServiceFailure e) {
      throw new ServiceFailure("Unable to call the CN for the list of nodes", e);
    }

    throw new NotAuthorized("Only coordinating nodes are permitted to perform this action");
  }

  /**
   * @return true if subjects contains the target
   */
  private static boolean contains(List<Subject> subjects, final String target) {
    return subjects.stream().anyMatch(subject -> target.equals(subject.getValue()));
  }

  /**
   * Returns true if the rights holder in the system metadata is one of the subjects passed in.
   */
  private static boolean isRightsHolder(SystemMetadata sysMetadata, Set<String> subjects) {
    Subject rightsHolder = sysMetadata.getRightsHolder();
    Preconditions.checkNotNull(rightsHolder, "An object cannot exist without a rights holder");
    boolean approved = subjects.contains(rightsHolder.getValue());
    LOG.debug("Subject[{}] does not contain the rights holder[{}]", subjects, rightsHolder.getValue());
    return approved;
  }

  /**
   * Runs through the procedure of verification returning whether approved or not.
   * Procedure is executed in an order to ensure that local calls are executed before calls requiring network calls.
   *
   * @throws ServiceFailure If it is not possible to connect to the coordinating node
   */
  @VisibleForTesting
  boolean checkIsAuthorized(Session session, SystemMetadata sysMetadata, Permission permission) {
    String primary = Subjects.primary(session);
    LOG.info("Subject {}", primary);
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
      LOG.info("The rights holder named in the system metadata[{}] is found in the session[{}]", sysMetadata, session);
      return true;
    }

    // any CN or the authoritative MN is granted permission, but requires a network call to a CN
    if (isAuthorityNodeOrCN(primary, sysMetadata)) {
      LOG.debug("The session[{}] originates from the CN or the authoritative member node", session);
      return true;
    }

    LOG.debug("The session[{}] is not permitted", session);
    return false;
  }

  /**
   * Looks up if the subject is the authority member node for the object or a CN.
   * This is combined into a single operation to minimize network calls to the CN.
   *
   * @throws ServiceFailure If it is not possible to connect to the coordinating node
   */
  @VisibleForTesting
  boolean isAuthorityNodeOrCN(String subject, SystemMetadata sysMetadata) {
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
    try {
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
    } catch (ServiceFailure e) {
      throw new ServiceFailure("Unable to call the CN for the list of nodes", e);
    }
    return false;
  }

  /**
   * Inspects the system metadata access rules and returns true if a subject passed in is explicitly granted the
   * permission sought.
   */
  @VisibleForTesting
  static boolean isGrantedByAccessPolicy(SystemMetadata sysMetadata, Set<String> subjects, Permission permission) {
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
