package org.dataone.ns.service.apis.v1;

/**
 * A full Tier 4 member node.
 * It is a <strong>requirement</strong> that implementations of this be thread-safe and document as such.
 * 
 * @see <a
 *      href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html</a>
 */
public interface MemberNode extends MemberNodeRead, MemberNodeAuthorization, MemberNodeStorage, MemberNodeReplication {
}
