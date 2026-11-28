package com.pavan.zenith.catalog.domain.pricing;

import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

// Template Method: this base class fixes the sequence every pricing rule runs
// through - currency check, then the calculation, then rounding, then the
// minimum-price floor - and leaves exactly one step (the calculation) abstract
// for subclasses to fill in. The shared steps live here once, instead of being
// copied into every rule.
public abstract class AbstractPricingRule implements PricingRule {

  // Zenith sells in INR only for now; this is also the lowest allowed price.
  private static final Money MINIMUM_PRICE = new Money(new BigDecimal("50.00"), "INR");

  // final: no subclass may override this method, so the sequence is locked.
  @Override
  public final Money priceFor(final PricingContext context) {
    // Step 1 - currency check. Money.plus refuses to add across currencies,
    // so adding zero rupees changes no amount but borrows that refusal: an
    // event listed in any other currency is rejected before any math runs.
    context.listedPrice().plus(new Money(BigDecimal.ZERO, MINIMUM_PRICE.currency()));

    // Step 2 - the calculation: the only step that differs per rule.
    Money calculated = calculate(context);

    // Step 3 - rounding mode: every price leaves with exactly two decimals.
    BigDecimal rounded = calculated.amount().setScale(2, RoundingMode.HALF_UP);

    // Step 4 - minimum-price floor: nothing on Zenith sells below the floor.
    BigDecimal floored = rounded.max(MINIMUM_PRICE.amount());

    return new Money(floored, calculated.currency());
  }

  // The hole subclasses fill: the raw price, before rounding and the floor.
  protected abstract Money calculate(PricingContext context);
}
