package com.pavan.zenith.catalog.domain.exception;

import java.util.UUID;

public class EventAlreadyCancelledException extends DomainException {

  public EventAlreadyCancelledException(final UUID eventId) {
    super("a cancelled event stays cancelled: " + eventId);
  }
}
