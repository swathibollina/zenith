package com.pavan.zenith.catalog.application.port.in;

import java.util.UUID;

// The single input of "show me this event".
public record GetEventQuery(UUID eventId) {

  public GetEventQuery {
    if (eventId == null) {
      throw new IllegalArgumentException("eventId must not be null");
    }
  }
}
