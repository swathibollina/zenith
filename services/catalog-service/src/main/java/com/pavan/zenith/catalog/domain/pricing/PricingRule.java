package com.pavan.zenith.catalog.domain.pricing;

import com.pavan.zenith.common.values.Money;

// Strategy interface: one family of interchangeable pricing algorithms.
// Callers hold a PricingRule and never know (or care) which concrete
// rule is behind it - swapping rules never changes the caller.
public interface PricingRule {

  Money priceFor(PricingContext context);
}
