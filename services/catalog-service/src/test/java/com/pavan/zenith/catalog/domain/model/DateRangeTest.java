package com.pavan.zenith.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DateRangeTest {

  private static final Instant EARLIER = Instant.parse("2026-03-14T18:00:00Z");
  private static final Instant LATER = Instant.parse("2026-03-14T21:00:00Z");

  @Test
  void nullStartIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new DateRange(null, LATER));
  }

  @Test
  void nullEndIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new DateRange(EARLIER, null));
  }

  @Test
  void endBeforeStartIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new DateRange(LATER, EARLIER));
  }

  @Test
  void aWellFormedRangeIsAccepted() {
    DateRange range = new DateRange(EARLIER, LATER);
    assertEquals(EARLIER, range.start());
    assertEquals(LATER, range.end());
    assertEquals(new DateRange(EARLIER, LATER), range);
  }

  @Test
  void rangesThatShareTimeOverlap() {
    DateRange first = new DateRange(at("18:00"), at("21:00"));
    DateRange second = new DateRange(at("20:00"), at("22:00"));
    assertTrue(first.overlaps(second));
    assertTrue(second.overlaps(first));
  }

  @Test
  void disjointRangesDoNotOverlap() {
    DateRange first = new DateRange(at("18:00"), at("19:00"));
    DateRange second = new DateRange(at("20:00"), at("21:00"));
    assertFalse(first.overlaps(second));
    assertFalse(second.overlaps(first));
  }

  @Test
  void rangesThatOnlyTouchDoNotOverlap() {
    DateRange first = new DateRange(at("18:00"), at("20:00"));
    DateRange second = new DateRange(at("20:00"), at("21:00"));
    assertFalse(first.overlaps(second));
    assertFalse(second.overlaps(first));
  }

  @Test
  void containsIncludesTheStartAndExcludesTheEnd() {
    DateRange range = new DateRange(at("18:00"), at("21:00"));
    assertTrue(range.contains(at("18:00")));
    assertTrue(range.contains(at("19:30")));
    assertFalse(range.contains(at("21:00")));
  }

  private static Instant at(final String time) {
    return Instant.parse("2026-03-14T" + time + ":00Z");
  }
}
