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

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Objects;

/**
 * A DataONEException is the root of all DataONE service class exception messages.
 * <p>
 * All D1 exceptions are checked. Arguably some might be served as unchecked exceptions as they could be unrecoverable
 * by any client, but the design is to <b>enforce</b> that clients code defensively for these scenarios.
 */
@ThreadSafe
public class DataONEException extends Exception {

  private static final long serialVersionUID = -8001672483615361690L;

  private final String detailCode;

  @Nullable
  private final String pid; // the persistent identifier for the object if applicable

  @Nullable
  private final String nodeId; // node identifier of the machine that raised the exception

  protected DataONEException(String message, String detailCode) {
    super(message);
    this.detailCode = detailCode;
    this.nodeId = null;
    this.pid = null;
  }

  protected DataONEException(String message, String detailCode, String nodeId) {
    super(message);
    this.detailCode = detailCode;
    this.nodeId = nodeId;
    this.pid = null;
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

  protected DataONEException(String message, String detailCode, String nodeId, Throwable cause) {
    super(message, cause);
    this.detailCode = detailCode;
    this.nodeId = nodeId;
    this.pid = null;
  }

  protected DataONEException(String message, String detailCode, Throwable cause) {
    super(message, cause);
    this.detailCode = detailCode;
    this.nodeId = null;
    this.pid = null;
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
    return super.toString() + Objects.toStringHelper(this)
      .add("detailCode", detailCode)
      .add("pid", pid)
      .add("nodeId", nodeId)
      .toString();
  }
}
