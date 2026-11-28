package com.pavan.zenith.catalog.domain.exception;

import com.pavan.zenith.catalog.domain.model.TierCode;
import java.util.UUID;

public class DuplicateTierCodeException extends DomainException {

  public DuplicateTierCodeException(final UUID eventId, final TierCode code) {
    super("tier code already exists on event " + eventId + ": " + code.value());
  }
}
