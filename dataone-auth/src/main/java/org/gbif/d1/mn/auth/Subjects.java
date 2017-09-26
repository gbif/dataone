package org.gbif.d1.mn.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.dataone.ns.service.types.v1.Group;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Person;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SubjectInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utilities for dealing with Subjects.
 * <p>
 * This functionality is core to the authorization model and hence has highly restricted access.
 * <p>
 * Methods marked with @VisibleForTesting are not intended to be used outside of this class.
 */
final class Subjects {

  private static final Logger LOG = LoggerFactory.getLogger(Subjects.class);

  // symbolic subjects (visible for testing)
  static final String PUBLIC_SUBJECT = "public";
  static final String AUTHENTICATED_SUBJECT = "authenticatedUser";
  static final String VERIFIED_SUBJECT = "verifiedUser";

  // Not for instantiation
  private Subjects() {
  }

  /**
   * Utility to add subjects provided they exist and contain a value otherwise skips silently.
   */
  private static void appendIfNotNull(Subject s, Set<String> collection) {
    Preconditions.checkNotNull(collection, "Cannot append to collection if it is null");
    if (s != null && s.getValue() != null) {
      collection.add(s.getValue());
    }
  }

  /**
   * Recursive calling over the people list looking for the target identity. When found the person is added to the
   * subjects, all groups they belong to are added and then we find any equivalent identities for them and start over
   * until exhausted.
   */
  private static void recurseAllPeople(List<Person> people, String target, Set<String> subjects,
    Map<String, Set<String>> groupMembership) {
    Preconditions.checkNotNull(people, "Cannot recurse over missing lists");
    Preconditions.checkNotNull(target, "Cannot find a person if non specified");
    Preconditions.checkNotNull(subjects, "Cannot append to subjects if missing");
    Preconditions.checkNotNull(groupMembership, "Cannot find groups with no index");

    LOG.debug("Recursing subject: {}", target);

    // always ensure the target is stored [it may have no Person objects]
    subjects.add(target);

    // For safety: the model supports a membership on the group object in addition to on the person.
    // The target might actually be an equivalent identity with no person object.
    if (groupMembership.containsKey(target)) {
      subjects.addAll(groupMembership.get(target));
    }

    for (Person person : people) {
      if (person.getSubject() != null && target.equals(person.getSubject().getValue())) {

        // add the symbolic subject if verified
        if (Boolean.TRUE.equals(person.isVerified())) {
          subjects.add(VERIFIED_SUBJECT);
        }

        // add group membership listed on the person
        for (Subject group : person.getIsMemberOf()) {
          appendIfNotNull(group, subjects);
        }

        // For safety again: the model supports a membership on the group object in addition to on the person.
        // Add any group the subject is associated with here, noting that SubjectInfo can come from
        // the certificate, so the generation of it is outside of our control.
        if (groupMembership.containsKey(person.getSubject().getValue())) {
          subjects.addAll(groupMembership.get(person.getSubject().getValue()));
        }

        // recurse into equivalent identities (aliases)
        for (Subject alias : person.getEquivalentIdentity()) {
          if (alias != null && alias.getValue() != null && !subjects.contains(alias.getValue())) {
            // start over, this time using the alias
            recurseAllPeople(people, alias.getValue(), subjects, groupMembership);
          }
        }
      }
    }
  }

  /**
   * The session object holds a primary subject and can have extra information indicating alternative identities for the
   * subject and their groups. This returns all subjects that can be expanded upon for the principle subject
   * based on the session object provided.
   *
   * @return The subjects from the session in an immutable set
   */
  static ImmutableSet<String> allSubjects(Session session) {
    Preconditions.checkNotNull(session, "Session required to extract subjects");
    LOG.info("Session {}", session.getSubjectInfo());
    // public symbolic subject is always added
    Set<String> subjects = Sets.newHashSet(PUBLIC_SUBJECT);

    Subject primary = session.getSubject();
    if (primary != null && !PUBLIC_SUBJECT.equals(primary.getValue())) {

      // a real subject was found: add them and the symbolic subject
      appendIfNotNull(primary, subjects);
      subjects.add(AUTHENTICATED_SUBJECT);

      // recurse into the subject info finding all groups, equivalent identities for the person (and their groups)
      SubjectInfo info = session.getSubjectInfo();
      if (info != null) {

        // index groups by membership once for lookup later
        Map<String, Set<String>> groupMembership = indexGroupMembership(info);

        // recursively inspect the people adding groups and equivalent identities
        recurseAllPeople(info.getPerson(), primary.getValue(), subjects, groupMembership);
      }

    }

    return ImmutableSet.copyOf(subjects);
  }

  /**
   * @return The subjects as an immutable Set
   */
  static ImmutableSet<String> fromNode(Node node) {
    Preconditions.checkNotNull(node, "Node is required");
    Preconditions.checkNotNull(node.getSubject(), "Node subject is required");
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (Subject subject : node.getSubject()) {
      if (subject.getValue() != null) { // for safety
        builder.add(subject.getValue());
      }
    }
    return builder.build();
  }

  /**
   * Builds an index of any groups with a key for the subject and the value listing the groups they are contained in.
   */
  @VisibleForTesting
  static ImmutableMap<String, Set<String>> indexGroupMembership(SubjectInfo info) {
    Map<String, Set<String>> index = Maps.newHashMap();
    if (info != null) {
      for (Group group : info.getGroup()) {

        // only accept groups with valid subjects
        Subject groupSubject = group.getSubject();
        if (groupSubject != null && groupSubject.getValue() != null) {

          // index each member in the group
          for (Subject member : group.getHasMember()) {
            if (member != null && member.getValue() != null) {
              index.computeIfAbsent(member.getValue(), k -> Sets.newHashSet()).add(groupSubject.getValue());
            }
          }
        }
      }
    }
    return ImmutableMap.copyOf(index);
  }

  /**
   * @return The primary subject from the session or null
   */
  static String primary(Session session) {
    Preconditions.checkNotNull(session, "Session required to extract primary subject");
    return Optional.ofNullable(session.getSubject()).map(Subject::getValue).orElse(null);
  }
}
