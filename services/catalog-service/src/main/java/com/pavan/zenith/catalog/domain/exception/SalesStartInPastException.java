package com.pavan.zenith.catalog.domain.exception;

import java.time.Instant;
import java.util.UUID;

public class SalesStartInPastException extends DomainException {

  public SalesStartInPastException(final UUID eventId, final Instant salesStart) {
    super("sales start must be in the future to publish event " + eventId + ": " + salesStart);
  }
}
