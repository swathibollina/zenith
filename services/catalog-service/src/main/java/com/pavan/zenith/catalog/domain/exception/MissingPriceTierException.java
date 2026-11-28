package com.pavan.zenith.catalog.domain.exception;

import java.util.UUID;

public class MissingPriceTierException extends DomainException {

  public MissingPriceTierException(final UUID eventId) {
    super("an event cannot be published without at least one price tier: " + eventId);
  }
}
