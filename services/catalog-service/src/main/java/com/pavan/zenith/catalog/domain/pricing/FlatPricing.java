package com.pavan.zenith.catalog.domain.pricing;

import com.pavan.zenith.common.values.Money;

// Strategy 1: every ticket costs the same fixed amount,
// no matter the tier and no matter how full the event is.
public final class FlatPricing extends AbstractPricingRule {

  private final Money flatPrice;

  public FlatPricing(final Money flatPrice) {
    if (flatPrice == null) {
      throw new IllegalArgumentException("flatPrice must not be null");
    }
    this.flatPrice = flatPrice;
  }

  @Override
  protected Money calculate(final PricingContext context) {
    return flatPrice;
  }
}
