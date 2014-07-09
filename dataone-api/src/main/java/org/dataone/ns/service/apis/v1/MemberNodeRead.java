package org.dataone.ns.service.apis.v1;

import java.io.InputStream;
import java.util.Date;

import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.SynchronizationFailed;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.ObjectList;
import org.omg.CosNaming.NamingContextPackage.NotFound;

/**
 * Interface definition for the Tier 1 services.
 * TODO: Used String for Identifier; consider using Identifier instead
 * 
 * @see <a
 *      href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html</a>
 */
public interface MemberNodeRead extends SystemMetadataProvider {

  /**
   * This method provides a lighter weight mechanism than {@link MemberNodeRead.getSystemMetadata()} for a client to
   * determine basic properties of the referenced object. The response should indicate properties that are typically
   * returned in a HTTP HEAD request: the date late modified, the size of the object, the type of the object (the
   * SystemMetadata.formatId).
   */
  DescribeResponse describe(String pid) throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure, NotFound;

  /**
   * Retrieve an object identified by id from the node.
   */
  InputStream get(String pid) throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure, NotFound,
    InsufficientResources;

  /**
   * Returns a document describing the capabilities of the Member Node.
   */
  Node getCapabilities() throws NotImplemented, ServiceFailure;

  /**
   * Returns {@link Checksum} for the specified object using an accepted hashing algorithm. The result is used to
   * determine if two instances referenced by a PID are identical, hence it is necessary that MNs can ensure that the
   * returned checksum is valid for the referenced object either by computing it on the fly or by using a cached value
   * that is certain to be correct.
   */
  Checksum getChecksum(String pid, String checksumAlgorithm) throws InvalidRequest, InvalidToken, NotAuthorized,
    NotImplemented, ServiceFailure, NotFound;

  /**
   * Retrieve log information from the Member Node for the specified slice parameters. Log entries will only return
   * PIDs.
   */
  Log getLogRecords(Date fromDate, Date toDate, Event event, String pidFilter, Integer start, Integer count)
    throws InvalidRequest, InvalidToken, NotAuthorized, NotImplemented, ServiceFailure;

  /**
   * Called by a target Member Node to fullfill the replication request originated by a Coordinating Node calling
   * {@link MemberNodeReplication.replicate()}. This is a request to make a replica copy of the object, and differs from
   * a call to GET /object in that it should be logged as a replication event rather than a read event on that object.
   */
  InputStream getReplica(String pid) throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure, NotFound,
    InsufficientResources;

  /**
   * Retrieve the list of objects present on the MN that match the calling parameters.
   * <p>
   * This method is required to support the process of Member Node synchronization. At a minimum, this method MUST be
   * able to return a list of objects that match "fromDate < SystemMetadata.dateSysMetadataModified". but is expected to
   * also support date range (by also specifying toDate), and should also support slicing of the matching set of records
   * by indicating the starting index of the response (where 0 is the index of the first item) and the count of elements
   * to be returned.
   * <p>
   * Note that date time precision is limited to one millisecond. If no timezone information is provided, the UTC will
   * be assumed.
   * <p>
   * Access control for this method MUST be configured to allow calling by Coordinating Nodes and MAY be configured to
   * allow more general access.
   */
  ObjectList listObjects(Date fromDate, Date toDate, String formatId, Boolean replicaStatus, Integer start,
    Integer count) throws InvalidRequest, InvalidToken, NotAuthorized, NotImplemented, ServiceFailure;

  /**
   * Returns a human readable form of the time for easy debugging since the specification is ambiguous.
   */
  String ping() throws NotImplemented, ServiceFailure, InsufficientResources;

  /**
   * This is a callback method used by a CN to indicate to a MN that it cannot complete synchronization of the science
   * metadata identified by pid. When called, the MN should take steps to record the problem description and notify an
   * administrator or the data owner of the issue.
   */
  boolean synchronizationFailed(SynchronizationFailed message) throws InvalidToken, NotAuthorized, NotImplemented,
    ServiceFailure;
}