package org.gbif.d1.mn.service;

class SystemMetadataUpdateEvent {

  private final String identifier;

  SystemMetadataUpdateEvent(String identifier) {
    this.identifier = identifier;
  }

  String getIdentifier() {
    return identifier;
  }
}
