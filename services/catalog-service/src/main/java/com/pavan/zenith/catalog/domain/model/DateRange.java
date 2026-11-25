package com.pavan.zenith.catalog.domain.model;

import java.time.Instant;

public record DateRange(Instant start, Instant end) {

  public DateRange {
    if (start == null) {
      throw new IllegalArgumentException("start must not be null");
    }
    if (end == null) {
      throw new IllegalArgumentException("end must not be null");
    }
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("end must not be before start: " + start + " .. " + end);
    }
  }

  // Edge rule: the start belongs to the range, the end does not. Two ranges
  // that only touch at an edge therefore do not overlap.
  public boolean overlaps(final DateRange other) {
    return start.isBefore(other.end) && other.start.isBefore(end);
  }

  public boolean contains(final Instant moment) {
    return !moment.isBefore(start) && moment.isBefore(end);
  }
}
