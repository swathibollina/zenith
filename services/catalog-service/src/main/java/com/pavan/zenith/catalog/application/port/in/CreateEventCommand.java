package com.pavan.zenith.catalog.application.port.in;

import com.pavan.zenith.catalog.domain.model.DateRange;
import java.time.Instant;
import java.time.ZoneId;

// The inputs of "create an event", bundled into one named record instead of
// four loose parameters. The caller does NOT choose the id - the application
// generates identity and returns it.
public record CreateEventCommand(
    String title, ZoneId venueZone, DateRange dates, Instant salesStart) {

  public CreateEventCommand {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (venueZone == null) {
      throw new IllegalArgumentException("venueZone must not be null");
    }
    if (dates == null) {
      throw new IllegalArgumentException("dates must not be null");
    }
    if (salesStart == null) {
      throw new IllegalArgumentException("salesStart must not be null");
    }
  }
}
