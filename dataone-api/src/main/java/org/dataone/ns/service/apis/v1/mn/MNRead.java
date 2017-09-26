package org.dataone.ns.service.apis.v1.mn;

import java.io.InputStream;
import java.util.Date;

import javax.annotation.Nullable;

import org.dataone.ns.service.exceptions.ExceptionDetail;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * The MNRead API implements methods that enable object management operations on a Member Node.
 */
public interface MNRead {

  /**
   * Gets a stream to the identified object.
   *
   * @param identifier for the object
   * @return the stream or null if not found
   */
  InputStream get(Identifier identifier);

  /**
   * Describes the object identified by id by returning the associated system metadata object.
   *
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   * @throws NotFound if the DataONE object is not present on this node
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws InsufficientResources if the system determines that resource are exhausted
   */
  SystemMetadata getSystemMetadata(Identifier pid);

  /**
   * Returns a description of the identified object.
   *
   * @param identifier for the object
   * @return the description or null if not found
   */
  DescribeResponse describe(Identifier identifier);


  /**
   * Performs a search filtering by the provided parameters.
   */
  ObjectList listObjects(Date fromDate, @Nullable Date toDate, @Nullable String formatId,
                         @Nullable Boolean replicaStatus, @Nullable Integer start, @Nullable Integer count);

  /**
   * This is a callback method used by a CN to indicate to a MN that it cannot complete synchronization of the science
   * metadata identified by pid. When called, the MN should take steps to record the problem description and notify an
   * administrator or the data owner of the issue.
   *
   * The specification mandates we return a boolean and HTTP 200 on success although this is pointless as anything else
   * would surface as an Exception here and be returned as a non HTTP 200 code.
   *
   * @throws InvalidToken if the credentials in the request are not correctly presented
   * @throws NotAuthorized if the credentials presented do not have permission to perform the action
   */
  boolean synchronizationFailed(ExceptionDetail detail);

  /**
   * Called by a target Member Node to fulfill the replication request originated by a Coordinating Node calling
   * MNReplication.replicate(). This is a request to make a replica copy of the object, and differs from a call to
   * GET /object in that it should be logged as a replication event rather than a read event on that object.
   * If the object being retrieved is restricted access, then a Tier 2 or higher Member Node MUST make a call to
   * CNReplication.isNodeAuthorized() to verify that the Subject of the caller is authorized to retrieve the content.
   * A successful operation is indicated by a HTTP status of 200 on the response.
   * Failure of the operation MUST be indicated by returning an appropriate exception.
   * @param pid The identifier of the object to get as a replica Transmitted as part of the URL path and must be
   *            escaped accordingly.
   * @return Bytes of the specified object.
   */
  InputStream getReplica(Identifier pid);
}
