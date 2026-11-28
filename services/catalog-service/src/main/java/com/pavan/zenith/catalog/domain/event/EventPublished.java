package com.pavan.zenith.catalog.domain.event;

import java.time.Instant;
import java.util.UUID;

// Domain event: a fact recorded inside this service, in business language.
// occurredAt = when the business fact happened, not when any message is sent.
public record EventPublished(UUID eventId, Instant occurredAt) {

  public EventPublished {
    if (eventId == null) {
      throw new IllegalArgumentException("eventId must not be null");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt must not be null");
    }
  }
}
