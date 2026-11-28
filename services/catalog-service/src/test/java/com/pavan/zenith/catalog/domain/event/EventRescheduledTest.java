package com.pavan.zenith.catalog.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.domain.model.EventSchedule;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventRescheduledTest {

  private static final UUID EVENT_ID = UUID.fromString("7e57d004-2b97-0e7a-b45f-5387367791cd");
  private static final Instant WHEN = Instant.parse("2026-03-03T10:00:00Z");
  private static final EventSchedule NEW_SCHEDULE =
      new EventSchedule(Instant.parse("2026-04-04T15:30:00Z"), ZoneId.of("Asia/Kolkata"));

  @Test
  void nullEventIdIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new EventRescheduled(null, WHEN, NEW_SCHEDULE));
  }

  @Test
  void nullOccurredAtIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new EventRescheduled(EVENT_ID, null, NEW_SCHEDULE));
  }

  @Test
  void nullScheduleIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new EventRescheduled(EVENT_ID, WHEN, null));
  }

  @Test
  void carriesTheNewScheduleAsAValueObject() {
    EventRescheduled fact = new EventRescheduled(EVENT_ID, WHEN, NEW_SCHEDULE);
    assertEquals(EVENT_ID, fact.eventId());
    assertEquals(WHEN, fact.occurredAt());
    assertEquals(NEW_SCHEDULE, fact.newSchedule());
  }
}
