package org.gbif.d1.mn.auth;

import com.google.common.collect.ImmutableSet;
import org.dataone.ns.service.apis.v1.CoordinatingNode;
import org.dataone.ns.service.apis.v1.SystemMetadataProvider;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Builders;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This tests the behavior of the logic within the authorization manager implementation.
 */
public class AuthorizationManagerTest {

  private static Node selfNode; // ourselves as a member node
  private SystemMetadataProvider systemMetadataProvider;
  private CoordinatingNode cn;

  /**
   * Creates the node representing our own capabilities - immutable content so created once per suite run.
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    selfNode = Builders.newNode("org/gbif/d1/mn/auth/node-1.xml");
  }

  /**
   * Creates new mock objects created for each run allowing behavior to be customized per test.
   */
  @Before
  public void setup() throws Exception {
    systemMetadataProvider = mock(SystemMetadataProvider.class);
    cn = mock(CoordinatingNode.class);
  }

  /**
   * Tests that ServiceFailure propagates.
   */
  @Test(expected = ServiceFailure.class)
  public void testCNDownBehavior() throws Exception {
    when(cn.listNodes()).thenThrow(new ServiceFailure("1", "I'm not available"));
    AuthorizationManagerImpl auth = new AuthorizationManagerImpl(systemMetadataProvider, cn, selfNode);
    SystemMetadata sysMetadata = Builders.newSystemMetadata("org/gbif/d1/mn/auth/sysMeta-1.xml");
    // this will force a CN call to list nodes
    auth.isAuthorityNodeOrCN("CN=Nobody", sysMetadata);
  }

  @Test
  public void testIsAuthorityNodeOrCN1() throws Exception {
    when(cn.listNodes()).thenReturn(Builders.newNodeList("org/gbif/d1/mn/auth/nodeList-1.xml"));
    AuthorizationManagerImpl auth = new AuthorizationManagerImpl(systemMetadataProvider, cn, selfNode);
    SystemMetadata sysMetadata = Builders.newSystemMetadata("org/gbif/d1/mn/auth/sysMeta-1.xml");

    // ensure the authority MN is granted permission if listed explicitly
    assertTrue(auth.isAuthorityNodeOrCN("CN=MemberNode_2", sysMetadata));
    verify(cn, times(0)).listNodes(); // should not have required CN to deduce this

    // ensure the authority MN is granted permission if listed by an alias
    assertTrue(auth.isAuthorityNodeOrCN("CN=AliasForMN2", sysMetadata));
    verify(cn, times(1)).listNodes(); // required the cn node to find aliases

    // check that CNs get granted
    assertTrue(auth.isAuthorityNodeOrCN("CN=CoordinatingNode_1", sysMetadata));
    verify(cn, times(2)).listNodes();
    assertTrue(auth.isAuthorityNodeOrCN("CN=CoordinatingNode_2", sysMetadata));
    verify(cn, times(3)).listNodes();

    // ensure that just anybody doesn't get granted
    assertFalse(auth.isAuthorityNodeOrCN("CN=Nobody", sysMetadata));
    verify(cn, times(4)).listNodes();
  }

  @Test
  public void testIsAuthorized() throws Exception {
    when(cn.listNodes()).thenReturn(Builders.newNodeList("org/gbif/d1/mn/auth/nodeList-1.xml"));
    AuthorizationManagerImpl auth = new AuthorizationManagerImpl(systemMetadataProvider, cn, selfNode);

    // use a complex system metadata that we can use to test various subjects against
    SystemMetadata sysMetadata = Builders.newSystemMetadata("org/gbif/d1/mn/auth/sysMeta-1.xml");

    // the subject is ourselves
    Session session = Builders.newSession("org/gbif/d1/mn/auth/session-8.xml");
    assertTrue(auth.checkIsAuthorized(session, sysMetadata, Permission.CHANGE_PERMISSION, "1"));
    verify(cn, times(0)).listNodes(); // no CN calls needed

    // the subject is the rights holder
    session = Builders.newSession("org/gbif/d1/mn/auth/session-1.xml");
    assertTrue(auth.checkIsAuthorized(session, sysMetadata, Permission.CHANGE_PERMISSION, "1"));
    verify(cn, times(0)).listNodes(); // no CN calls needed

    // the subject is part of an org listed in the access rules for READ only
    session = Builders.newSession("org/gbif/d1/mn/auth/session-6.xml");
    assertFalse(auth.checkIsAuthorized(session, sysMetadata, Permission.CHANGE_PERMISSION, "1"));
    verify(cn, times(1)).listNodes(); // failed, so ran through to the end and called a CN
    assertTrue(auth.checkIsAuthorized(session, sysMetadata, Permission.READ, "1"));
    verify(cn, times(1)).listNodes(); // did not require another call

    // session is a CN
    session = Builders.newSession("org/gbif/d1/mn/auth/session-9.xml");
    assertTrue(auth.checkIsAuthorized(session, sysMetadata, Permission.CHANGE_PERMISSION, "1"));
    verify(cn, times(2)).listNodes(); // required a call to the CN

    // session is an alias of the listed authority MN
    session = Builders.newSession("org/gbif/d1/mn/auth/session-10.xml");
    assertTrue(auth.checkIsAuthorized(session, sysMetadata, Permission.CHANGE_PERMISSION, "1"));
    verify(cn, times(3)).listNodes(); // required a call to the CN
  }

  /**
   * Ensure that the detail code is correctly surfaced in the exception.
   */
  @Test
  public void testIsAuthorizedCNDown() throws Exception {
    // tests that detail codes are correctly passed through during failures
    String expectedDetailCode = "Unit test code";
    try {
      // note this throws with detailCode "1" which is internal and should not be surfaced
      when(cn.listNodes()).thenThrow(new ServiceFailure("1", "I'm not available"));
      AuthorizationManagerImpl auth = new AuthorizationManagerImpl(systemMetadataProvider, cn, selfNode);
      SystemMetadata sysMetadata = Builders.newSystemMetadata("org/gbif/d1/mn/auth/sysMeta-1.xml");
      // session-9.xml represents a CN, so this will force a CN call during auth
      Session session = Builders.newSession("org/gbif/d1/mn/auth/session-9.xml");
      auth.checkIsAuthorized(session, sysMetadata, Permission.READ, expectedDetailCode);
      fail("Expected a ServiceFailure exception");
    } catch (ServiceFailure e) { // expected
      e.printStackTrace();
      assertEquals(expectedDetailCode, e.getDetailCode());
    }
  }

  @Test
  public void testIsGrantedByAccessPolicy() throws Exception {
    AuthorizationManagerImpl auth = new AuthorizationManagerImpl(systemMetadataProvider, cn, selfNode);
    SystemMetadata sysMetadata = Builders.newSystemMetadata("org/gbif/d1/mn/auth/sysMeta-1.xml");

    // READ
    assertTrue(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("CN=Dave Vieglas"), Permission.READ));
    assertTrue(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("CN=Matt Jones"), Permission.READ));
    assertTrue(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("O=DataONE"), Permission.READ));
    assertTrue(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("O=Microsoft"), Permission.READ));
    assertFalse(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("CN=Nobody"), Permission.READ));

    // WRITE
    assertTrue(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("CN=Dave Vieglas"), Permission.WRITE));
    assertTrue(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("CN=Matt Jones"), Permission.WRITE));
    assertFalse(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("O=DataONE"), Permission.WRITE));
    assertFalse(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("O=Microsoft"), Permission.WRITE));
    assertFalse(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("CN=Nobody"), Permission.WRITE));

    // MODIFY
    assertTrue(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("CN=Dave Vieglas"),
      Permission.CHANGE_PERMISSION));
    assertFalse(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("CN=Matt Jones"),
      Permission.CHANGE_PERMISSION));
    assertFalse(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("O=DataONE"), Permission.CHANGE_PERMISSION));
    assertFalse(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("O=Microsoft"), Permission.CHANGE_PERMISSION));
    assertFalse(auth.isGrantedByAccessPolicy(sysMetadata, ImmutableSet.of("O=Microsoft"), Permission.CHANGE_PERMISSION));
  }
}
