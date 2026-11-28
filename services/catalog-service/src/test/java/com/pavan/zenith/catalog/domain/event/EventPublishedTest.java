package com.pavan.zenith.catalog.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventPublishedTest {

  private static final UUID EVENT_ID = UUID.fromString("7e57d004-2b97-0e7a-b45f-5387367791cd");
  private static final Instant WHEN = Instant.parse("2026-03-01T10:00:00Z");

  @Test
  void nullEventIdIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new EventPublished(null, WHEN));
  }

  @Test
  void nullOccurredAtIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new EventPublished(EVENT_ID, null));
  }

  @Test
  void carriesTheIdAndTheMoment() {
    EventPublished fact = new EventPublished(EVENT_ID, WHEN);
    assertEquals(EVENT_ID, fact.eventId());
    assertEquals(WHEN, fact.occurredAt());
    assertEquals(new EventPublished(EVENT_ID, WHEN), fact);
  }
}
