package com.pavan.zenith.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

  @Test
  void localStartTimeViewsTheMomentThroughTheVenueZone() {
    EventSchedule schedule = new EventSchedule(MOMENT, KOLKATA);
    assertEquals(ZonedDateTime.of(2026, 3, 14, 23, 30, 0, 0, KOLKATA), schedule.localStartTime());
  }

  @Test
  void springForwardNightInBerlinRendersTheRealWallClock() {
    // 2026-03-29, Europe/Berlin: at 01:00 UTC the wall clock jumps 02:00 -> 03:00,
    // so the readings 02:00..02:59 never happen that night. Winter arithmetic
    // (+01:00) would claim this instant reads 02:30 - a time that does not exist.
    // The zone rules know summer time (+02:00) already applies: 03:30.
    ZoneId berlin = ZoneId.of("Europe/Berlin");
    EventSchedule schedule = new EventSchedule(Instant.parse("2026-03-29T01:30:00Z"), berlin);
    assertEquals(ZonedDateTime.of(2026, 3, 29, 3, 30, 0, 0, berlin), schedule.localStartTime());
  }

  @Test
  void kolkataRendersExactlyFiveAndAHalfHoursAheadOfUtc() {
    // Not all offsets are whole hours: IST is UTC+5:30. Code that models an
    // offset as "hours" breaks here; the zone id handles it with no special case.
    EventSchedule schedule = new EventSchedule(MOMENT, KOLKATA);
    ZonedDateTime local = schedule.localStartTime();
    assertEquals(ZoneOffset.ofHoursMinutes(5, 30), local.getOffset());
    assertEquals(
        MOMENT.atZone(ZoneOffset.UTC).toLocalDateTime().plusHours(5).plusMinutes(30),
        local.toLocalDateTime());
  }
}
