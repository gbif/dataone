package org.gbif.d1.mn.auth;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.ImmutableSet;
import org.dataone.ns.service.types.v1.Permission;

@ThreadSafe
final class Permissions {

  // Not for instantiation
  private Permissions() {
  }

  /**
   * Expands permissions which are hierarchical i.e. CHANGE permits WRITE permits READ.
   */
  static ImmutableSet<Permission> expand(Iterable<Permission> permissions) {
    ImmutableSet.Builder<Permission> builder = new ImmutableSet.Builder<Permission>();
    // expand the higher permissions to include what they also grant
    for (Permission p : permissions) {
      builder.add(p);
      if (Permission.WRITE == p) {
        builder.add(Permission.READ);
      }
      if (Permission.CHANGE_PERMISSION == p) {
        builder.add(Permission.WRITE);
        builder.add(Permission.READ);
      }
    }
    return builder.build();
  }
}
