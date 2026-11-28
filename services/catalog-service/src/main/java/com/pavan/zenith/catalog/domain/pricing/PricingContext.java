package com.pavan.zenith.catalog.domain.pricing;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;

// The facts about one sale moment that a pricing rule may look at:
// which tier is being bought, what that tier normally advertises,
// and how full the event already is (sold out of capacity).
// A rule is free to ignore fields it does not care about.
public record PricingContext(TierCode tier, Money listedPrice, int sold, int capacity) {

  public PricingContext {
    if (tier == null) {
      throw new IllegalArgumentException("tier must not be null");
    }
    if (listedPrice == null) {
      throw new IllegalArgumentException("listedPrice must not be null");
    }
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be at least 1: " + capacity);
    }
    if (sold < 0 || sold > capacity) {
      throw new IllegalArgumentException(
          "sold must be between 0 and capacity (" + capacity + "): " + sold);
    }
  }
}
