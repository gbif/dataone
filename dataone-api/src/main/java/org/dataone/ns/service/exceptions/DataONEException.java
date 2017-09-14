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

package org.dataone.ns.service.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.MoreObjects;

/**
 * A DataONEException is the root of all DataONE service class exception messages.
 * <p>
 * Because DataONE specify that exceptional cases are associated with a detail codes which vary depending on the
 * execution (e.g. method being called) we cannot use the more familiar existing Java exceptions such as
 * {@link UnsupportedOperationException}.
 * <p>
 * All D1 exceptions are unchecked. Arguably some might be served as checked exceptions but it is anticipated that all
 * are unrecoverable in practice. These exceptions are low level exceptions and not intended for end users.
 * <ul>
 * <li>A server implementation will pass all exceptions through and serve as HTTP errors</li>
 * <li>A client implementation will likely throw a more suitable higher level checked exception</li>
 * </ul>
 */
@ThreadSafe
public class DataONEException extends RuntimeException {

  private static final long serialVersionUID = -8001672483615361690L;

  /**
   * Nullable to allow deferred setting, but note that an exception is not valid to serialize as XML across the wire
   * unless the detailCode is set.
   */
  @Nullable
  private final String detailCode;

  @Nullable
  private final String pid; // the persistent identifier for the object if applicable

  @Nullable
  private final String nodeId; // node identifier of the machine that raised the exception

  // Note: deliberately limiting options in construction as it is very easy to misuse String order
  protected DataONEException(String message) {
    super(message);
    detailCode = null;
    nodeId = null;
    pid = null;
  }

  protected DataONEException(String message, String detailCode, String nodeId, String pid) {
    super(message);
    this.detailCode = detailCode;
    this.nodeId = nodeId;
    this.pid = pid;
  }

  protected DataONEException(String message, String detailCode, String nodeId, String pid, Throwable cause) {
    super(message, cause);
    this.detailCode = detailCode;
    this.nodeId = nodeId;
    this.pid = pid;
  }

  protected DataONEException(String message, Throwable cause) {
    super(message, cause);
    detailCode = null;
    nodeId = null;
    pid = null;
  }

  public String getDetailCode() {
    return detailCode;
  }

  /**
   * The identifier for the node in the DataONE network that raised the exception. If the exception is created from a
   * response to a call over the network (e.g. in a web service client), this will always be populated otherwise it will
   * likely be null.
   */
  @Nullable
  public String getNodeId() {
    return nodeId;
  }

  /**
   * The identifier for the object this exception relates to if applicable.
   */
  @Nullable
  public String getPid() {
    return pid;
  }

  @Override
  public String toString() {
    // append to super to inherit the familiar default "classname: message" format first
    return super.toString() + MoreObjects.toStringHelper(this)
      .add("detailCode", detailCode)
      .add("pid", pid)
      .add("nodeId", nodeId);
  }

  public Map<String, String> toHeaderMap() {
    Map<String, String> headers = new HashMap<>();
    headers.put("DataONE-Exception-Name", getClass().getSimpleName());
    Optional.ofNullable(detailCode)
      .ifPresent(detailCodeValue -> headers.put("DataONE-Exception-DetailCode", detailCodeValue));
    Optional.ofNullable(pid).ifPresent(pidValue ->headers.put("DataONE-Exception-PID", pid));
    Optional.ofNullable(getMessage())
      .ifPresent(message ->headers.put("DataONE-Exception-Description", message));
    return headers;
  }
}
