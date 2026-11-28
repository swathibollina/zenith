package com.pavan.zenith.catalog.domain.exception;

import java.util.UUID;

public class EventAlreadyPublishedException extends DomainException {

  public EventAlreadyPublishedException(final UUID eventId) {
    super("event is already published: " + eventId);
  }
}
