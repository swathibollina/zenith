package com.pavan.zenith.catalog.domain.exception;

import java.util.UUID;

public class EventNotFoundException extends DomainException {

  public EventNotFoundException(final UUID eventId) {
    super("no event exists with id: " + eventId);
  }
}
