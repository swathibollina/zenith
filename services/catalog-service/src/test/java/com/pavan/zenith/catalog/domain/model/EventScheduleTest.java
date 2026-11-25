package com.pavan.zenith.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class EventScheduleTest {

  private static final Instant MOMENT = Instant.parse("2026-03-14T18:00:00Z");
  private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");

  @Test
  void nullStartIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new EventSchedule(null, KOLKATA));
  }

  @Test
  void nullZoneIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new EventSchedule(MOMENT, null));
  }

  @Test
  void aWellFormedScheduleIsAccepted() {
    EventSchedule schedule = new EventSchedule(MOMENT, KOLKATA);
    assertEquals(MOMENT, schedule.start());
    assertEquals(KOLKATA, schedule.venueZone());
  }
}
