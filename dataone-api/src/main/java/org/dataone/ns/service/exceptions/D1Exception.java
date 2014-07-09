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

/**
 * A D1Exception is the root of all DataONE service class exception messages.
 * <p>
 * All D1 exceptions are checked. Arguably some might be served as unchecked exceptions as they could be unrecoverable
 * by any client, but the design is to <b>enforce</b> that clients code defensively for these scenarios.
 */
public abstract class D1Exception extends Exception {

  private static final long serialVersionUID = -8001672483615361690L;
  private final int code; // maps to http code when thrown over the REST API

  private final String message;

  private final String detailCode; // currently required, but an RFC is lodged to make this optional

  @Nullable
  private final String pid; // the persistent identifier for the object

  @Nullable
  private final String nodeId; // node identifier of the machine that raised the exception

  public D1Exception(int code, String message, String detailCode) {
    super();
    this.code = code;
    this.message = message;
    this.detailCode = detailCode;
    this.pid = null;
    this.nodeId = null;
  }

  public D1Exception(int code, String message, String detailCode, String pid, String nodeId) {
    super();
    this.code = code;
    this.message = message;
    this.detailCode = detailCode;
    this.pid = pid;
    this.nodeId = nodeId;
  }

  public int getCode() {
    return code;
  }

  public String getDetailCode() {
    return detailCode;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public String getNodeId() {
    return nodeId;
  }

  public String getPid() {
    return pid;
  }
}
