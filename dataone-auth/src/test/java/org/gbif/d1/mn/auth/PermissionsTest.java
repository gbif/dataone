package org.gbif.d1.mn.auth;

import com.google.common.collect.ImmutableSet;
import org.dataone.ns.service.types.v1.Permission;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This tests the behavior of the logic within the authorization manager.
 */
public class PermissionsTest {

  @Test
  public void testExpand() {
    assertEquals(
      ImmutableSet.of(Permission.READ),
      Permissions.expand(ImmutableSet.of(Permission.READ)));

    assertEquals(
      ImmutableSet.of(Permission.READ, Permission.WRITE),
      Permissions.expand(ImmutableSet.of(Permission.WRITE)));

    assertEquals(
      ImmutableSet.of(Permission.READ, Permission.WRITE, Permission.CHANGE_PERMISSION),
      Permissions.expand(ImmutableSet.of(Permission.CHANGE_PERMISSION)));

    assertEquals(
      ImmutableSet.of(Permission.READ, Permission.WRITE, Permission.CHANGE_PERMISSION),
      Permissions.expand(ImmutableSet.of(Permission.CHANGE_PERMISSION, Permission.READ))); // write missing
  }
}
