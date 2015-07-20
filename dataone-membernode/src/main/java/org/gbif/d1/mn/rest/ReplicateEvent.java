package org.gbif.d1.mn.rest;

import org.dataone.ns.service.types.v1.Subject;

final class ReplicateEvent {

  private final String identifier;
  private final String sourceNode;
  private final String ip;
  private final String userAgent;
  private final Subject subject;

  ReplicateEvent(String identifier, String sourceNode, String ip, String userAgent, Subject subject) {
    this.identifier = identifier;
    this.sourceNode = sourceNode;
    this.ip = ip;
    this.userAgent = userAgent;
    this.subject = subject;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getIp() {
    return ip;
  }

  public String getSourceNode() {
    return sourceNode;
  }

  public Subject getSubject() {
    return subject;
  }

  public String getUserAgent() {
    return userAgent;
  }
}
