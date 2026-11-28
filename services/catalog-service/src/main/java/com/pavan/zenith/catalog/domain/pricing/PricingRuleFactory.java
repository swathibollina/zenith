package com.pavan.zenith.catalog.domain.pricing;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.util.Map;

// Factory Method: the single place in the whole codebase that turns the
// persisted rule type into a concrete PricingRule object. Nobody else is
// allowed to branch on PricingRuleType - callers ask here and get back the
// interface, never a concrete class name.
public final class PricingRuleFactory {

  private PricingRuleFactory() {
    // a toolbox, not a thing to build: one static method, no objects needed.
  }

  // flatPrice is used by FLAT, priceByTier by TIERED; DYNAMIC_DEMAND needs
  // neither. Each rule's constructor still guards the value it actually uses,
  // so a missing amount is caught there, not silently accepted here.
  public static PricingRule ruleFor(
      final PricingRuleType type, final Money flatPrice, final Map<TierCode, Money> priceByTier) {
    if (type == null) {
      throw new IllegalArgumentException("type must not be null");
    }
    return switch (type) {
      case FLAT -> new FlatPricing(flatPrice);
      case TIERED -> new TieredPricing(priceByTier);
      case DYNAMIC_DEMAND -> new DynamicDemandPricing();
    };
  }
}
