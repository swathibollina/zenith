package com.pavan.zenith.catalog.domain.event;

import com.pavan.zenith.catalog.domain.model.EventSchedule;
import java.time.Instant;
import java.util.UUID;

public record EventRescheduled(UUID eventId, Instant occurredAt, EventSchedule newSchedule) {

  public EventRescheduled {
    if (eventId == null) {
      throw new IllegalArgumentException("eventId must not be null");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt must not be null");
    }
    if (newSchedule == null) {
      throw new IllegalArgumentException("newSchedule must not be null");
    }
  }
}
