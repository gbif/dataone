package org.gbif.d1.mn.rest;

import org.dataone.ns.service.types.v1.Subject;

final class SystemMetadataUpdateEvent {

  private final String identifier;
  private final String ip;
  private final String userAgent;
  private final Subject subject;

  SystemMetadataUpdateEvent(String identifier, String ip, String userAgent, Subject subject) {
    this.identifier = identifier;
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

  public Subject getSubject() {
    return subject;
  }

  public String getUserAgent() {
    return userAgent;
  }
}
