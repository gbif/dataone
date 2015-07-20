package org.gbif.d1.mn.service;

class ReplicateEvent {

  private final String identifier;
  private final String sourceNode;

  ReplicateEvent(String identifier, String sourceNode) {
    this.identifier = identifier;
    this.sourceNode = sourceNode;
  }

  String getIdentifier() {
    return identifier;
  }

  String getSourceNode() {
    return sourceNode;
  }
}
