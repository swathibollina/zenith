package com.pavan.zenith.catalog.domain.pricing;

import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

// Strategy 3: the price climbs as the event fills up.
// Recipe: ratio = sold / capacity (0.00 when empty, 1.00 when sold out)
//         raw price = listedPrice x (1 + ratio / 2)
// So an empty event sells at the listed price, and the very last seat
// sells at 1.5x the listed price. The base class rounds and floors it.
public final class DynamicDemandPricing extends AbstractPricingRule {

  private static final BigDecimal HALF = new BigDecimal("0.5");

  @Override
  protected Money calculate(final PricingContext context) {
    BigDecimal ratio =
        BigDecimal.valueOf(context.sold())
            .divide(BigDecimal.valueOf(context.capacity()), 4, RoundingMode.HALF_UP);
    BigDecimal factor = BigDecimal.ONE.add(ratio.multiply(HALF));
    BigDecimal amount = context.listedPrice().amount().multiply(factor);
    return new Money(amount, context.listedPrice().currency());
  }
}
