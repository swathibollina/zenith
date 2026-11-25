package com.pavan.zenith.catalog.domain.model;

import java.time.Instant;
import java.time.ZoneId;

// Stores the fact: one exact moment plus the venue's named timezone.
// Deliberately no LocalDateTime and no ZoneOffset field: a wall-clock reading
// without a zone is ambiguous, and a fixed offset is wrong half the year
// wherever DST applies.
public record EventSchedule(Instant start, ZoneId venueZone) {

  public EventSchedule {
    if (start == null) {
      throw new IllegalArgumentException("start must not be null");
    }
    if (venueZone == null) {
      throw new IllegalArgumentException("venueZone must not be null");
    }
  }
}
