/**
 * This work was created by participants in the DataONE project, and is
 * jointly copyrighted by participating institutions in DataONE. For
 * more information on DataONE, see our web site at http://dataone.org.
 * Copyright ${year}
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataone.ns.service.types.v1;

import java.math.BigInteger;
import java.util.Date;

/**
 * The DataONE Type to represent the metadata returned from a 'describe' request.
 * Describe provides a lighter weight mechanism than MN_crud.getSystemMetadata()
 * for a client to determine basic properties of the referenced object.
 * The response should indicate properties that are typically returned in a
 * HTTP HEAD request: the date late modified, the size of the object,
 * the type of the object (the SystemMetadata.objectFormat).
 * It is not serializable
 * Example of a HEAD response on object “ABC123”:
 * curl -I http://mn1.dataone.org/mn/object/ABC123
 * HTTP/1.1 200 OK
 * Last-Modified: Wed, 16 Dec 2009 13:58:34 GMT
 * Content-Length: 10400
 * Content-Type: application/octet-stream
 * DataONE-fmtid: eml://ecoinformatics.org/eml-2.0.1
 * DataONE-Checksum: SHA-1,2e01e17467891f7c933dbaa00e1459d23db3fe4f
 * DataONE-SerialVersion: 1234
 * 
 * @author Matthew Jones
 */
// String replaced to String!!!
public class DescribeResponse
{

  private final String dataONE_ObjectFormatID;
  private final BigInteger content_Length;
  private final Date last_Modified;
  private final Checksum dataONE_Checksum;
  private final BigInteger serialVersion;

  /**
   * instantiate a DescribeResponse object
   * 
   * @author Robert Waltz
   * @param format value of the SystemMetadata.objectFormat entry available in the SystemMetadata.
   * @param content_length Size of the object in bytes, the value of SystemMetadata.size from SystemMetadata.
   * @param last_modified DateTime value that indicates when the system metadata associated with the object was last
   *        modified, i.e. the value of SystemMetadata.dateSysMetadataModified for the object.
   * @param checksum The algorithm (SystemMetadata.algorithm) and Checksum (SystemMetadata.checksum) of the object being
   *        examined, drawn from the SystemMetadata. The algorithm and checksum are separated by a single comma with the
   *        algorithm first.
   * @param serialVersion The serialVersion of the object's SystemMetadata
   */
  public DescribeResponse(String objectFormatID, BigInteger content_length, Date last_modified,
    Checksum checksum, BigInteger serialVersion) {
    this.dataONE_ObjectFormatID = objectFormatID;
    this.content_Length = content_length;
    this.last_Modified = last_modified;
    this.dataONE_Checksum = checksum;
    this.serialVersion = serialVersion;
  }

  /**
   * get the Size of the object in bytes, the value of SystemMetadata.size from SystemMetadata.
   * 
   * @return Size of the object in bytes
   */
  public BigInteger getContent_Length() {
    return content_Length;
  }

  /**
   * get The algorithm (SystemMetadata.algorithm) and Checksum (SystemMetadata.checksum) of the object being examined,
   * drawn from the SystemMetadata. The algorithm and checksum are separated by a single comma with the algorithm first.
   * 
   * @return Checksum of the object
   */
  public Checksum getDataONE_Checksum() {
    return dataONE_Checksum;
  }

  /**
   * get The value of the SystemMetadata.String entry available in the SystemMetadata.
   * 
   * @return objectFormat of the object
   */
  public String getDataONE_String() {
    return dataONE_ObjectFormatID;
  }

  /**
   * get DateTime value that indicates when the system metadata associated with the object was last modified, i.e. the
   * value of SystemMetadata.dateSysMetadataModified for the object.
   * 
   * @return last modified date of object
   */
  public Date getLast_Modified() {
    return last_Modified;
  }

  /**
   * get the SerialVersion of the object SystemMetadata
   * 
   * @return serialVersion of the object
   */
  public BigInteger getSerialVersion() {
    return serialVersion;
  }

}
