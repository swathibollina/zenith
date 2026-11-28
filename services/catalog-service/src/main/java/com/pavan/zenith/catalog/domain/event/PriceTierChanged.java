package com.pavan.zenith.catalog.domain.event;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.time.Instant;
import java.util.UUID;

public record PriceTierChanged(UUID eventId, Instant occurredAt, TierCode tier, Money newPrice) {

  public PriceTierChanged {
    if (eventId == null) {
      throw new IllegalArgumentException("eventId must not be null");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt must not be null");
    }
    if (tier == null) {
      throw new IllegalArgumentException("tier must not be null");
    }
    if (newPrice == null) {
      throw new IllegalArgumentException("newPrice must not be null");
    }
  }
}
