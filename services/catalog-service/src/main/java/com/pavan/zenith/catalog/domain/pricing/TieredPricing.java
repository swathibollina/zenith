package com.pavan.zenith.catalog.domain.pricing;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.util.Map;

// Strategy 2: each tier has its own price; the rule looks the tier up.
public final class TieredPricing extends AbstractPricingRule {

  private final Map<TierCode, Money> priceByTier;

  public TieredPricing(final Map<TierCode, Money> priceByTier) {
    if (priceByTier == null || priceByTier.isEmpty()) {
      throw new IllegalArgumentException("priceByTier must hold at least one tier");
    }
    // Map.copyOf: our own unchangeable copy - later edits to the caller's map cannot reach us.
    this.priceByTier = Map.copyOf(priceByTier);
  }

  @Override
  protected Money calculate(final PricingContext context) {
    Money price = priceByTier.get(context.tier());
    if (price == null) {
      throw new IllegalArgumentException("no price configured for tier " + context.tier().value());
    }
    return price;
  }
}
