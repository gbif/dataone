package org.gbif.d1.mn.backend.model;

import java.util.Date;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;
import org.dataone.ns.service.types.v1.Event;

@Immutable
public final class MNLogEntry {

  private final String key;
  private final String identifier;
  private final String ipAddress;
  private final String userAgent;
  private final String subject;
  private final Event event;
  private final Date dateLogged;
  private final String nodeIdentifier;

  public MNLogEntry(String key, String identifier, String ipAddress, String userAgent, String subject, Event event,
    Date dateLogged, String nodeIdentifier) {
    this.key = key;
    this.identifier = identifier;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.subject = subject;
    this.event = event;
    this.dateLogged = dateLogged;
    this.nodeIdentifier = nodeIdentifier;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof MNLogEntry) {
      MNLogEntry that = (MNLogEntry) obj;
      return Objects.equal(this.key, that.key)
        && Objects.equal(this.identifier, that.identifier)
        && Objects.equal(this.ipAddress, that.ipAddress)
        && Objects.equal(this.userAgent, that.userAgent)
        && Objects.equal(this.subject, that.subject)
        && Objects.equal(this.event, that.event)
        && Objects.equal(this.dateLogged, that.dateLogged)
        && Objects.equal(this.nodeIdentifier, that.nodeIdentifier);
    }
    return false;
  }

  public Date getDateLogged() {
    return dateLogged;
  }

  public Event getEvent() {
    return event;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getKey() {
    return key;
  }

  public String getNodeIdentifier() {
    return nodeIdentifier;
  }

  public String getSubject() {
    return subject;
  }

  public String getUserAgent() {
    return userAgent;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, identifier, ipAddress, userAgent, subject, event, dateLogged, nodeIdentifier);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("key", key)
      .add("identifier", identifier)
      .add("ipAddress", ipAddress)
      .add("userAgent", userAgent)
      .add("subject", subject)
      .add("event", event)
      .add("dateLogged", dateLogged)
      .add("nodeIdentifier", nodeIdentifier)
      .toString();
  }
}
