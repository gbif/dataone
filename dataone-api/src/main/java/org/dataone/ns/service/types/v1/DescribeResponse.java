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

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;

/**
 * The DataONE Type to represent the metadata returned from a describe request.
 */
@Immutable
public class DescribeResponse {

  private final String objectFormatID;
  private final BigInteger contentLength;
  private final Date lastModified;
  private final Checksum checksum;
  private final BigInteger serialVersion;

  public DescribeResponse(String objectFormatID, BigInteger contentLength, Date lastModified, Checksum checksum,
    BigInteger serialVersion) {
    this.objectFormatID = objectFormatID;
    this.contentLength = contentLength;
    this.lastModified = lastModified;
    this.checksum = checksum;
    this.serialVersion = serialVersion;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DescribeResponse) {
      DescribeResponse that = (DescribeResponse) obj;
      return Objects.equal(this.objectFormatID, that.objectFormatID)
        && Objects.equal(this.contentLength, that.contentLength)
        && Objects.equal(this.lastModified, that.lastModified)
        && Objects.equal(this.checksum, that.checksum)
        && Objects.equal(this.serialVersion, that.serialVersion);
    }
    return false;
  }

  public Checksum getChecksum() {
    return checksum;
  }

  public BigInteger getContentLength() {
    return contentLength;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public String getObjectFormatID() {
    return objectFormatID;
  }

  public BigInteger getSerialVersion() {
    return serialVersion;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(objectFormatID, contentLength, lastModified, checksum, serialVersion);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("objectFormatID", objectFormatID)
      .add("contentLength", contentLength)
      .add("lastModified", lastModified)
      .add("checksum", checksum)
      .add("serialVersion", serialVersion)
      .toString();
  }

}
