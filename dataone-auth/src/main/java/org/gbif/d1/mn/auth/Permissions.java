package org.gbif.d1.mn.auth;

import java.util.Collection;
import java.util.Set;

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
  static Set<Permission> expand(Collection<Permission> permissions) {
    ImmutableSet.Builder<Permission> builder = new ImmutableSet.Builder<Permission>();
    for (Permission p : permissions) {
      switch (p) {
        case CHANGE_PERMISSION:
          builder.add(Permission.CHANGE_PERMISSION); // continue to cascade
        case WRITE:
          builder.add(Permission.WRITE); // continue to cascade
        case READ:
          builder.add(Permission.READ); // no more cascades
          break;
        default:
          break; // no permission
      }
    }
    return builder.build();
  }
}
