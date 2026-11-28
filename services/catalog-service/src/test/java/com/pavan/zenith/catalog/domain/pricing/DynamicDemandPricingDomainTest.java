package com.pavan.zenith.catalog.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

// DynamicDemandPricing alone: the demand curve, plus the three shared base
// behaviours (foreign currency refused, rounding applied, floor enforced).
class DynamicDemandPricingDomainTest {

  private static final TierCode GOLD = new TierCode("GOLD");
  private static final Money INR_2000 = new Money(new BigDecimal("2000.00"), "INR");

  private static PricingContext goldWith(final int sold, final int capacity) {
    return new PricingContext(GOLD, INR_2000, sold, capacity);
  }

  @Test
  void raisesThePriceAsSeatsSell() {
    DynamicDemandPricing rule = new DynamicDemandPricing();

    // empty: listed price. 80% sold: x1.40. sold out: x1.50.
    assertEquals(new Money(new BigDecimal("2000.00"), "INR"), rule.priceFor(goldWith(0, 100)));
    assertEquals(new Money(new BigDecimal("2800.00"), "INR"), rule.priceFor(goldWith(80, 100)));
    assertEquals(new Money(new BigDecimal("3000.00"), "INR"), rule.priceFor(goldWith(100, 100)));
  }

  @Test
  void roundsToExactlyTwoDecimals() {
    // 10% sold: factor 1.05. 999.99 x 1.05 = 1049.9895 -> rounded to 1049.99.
    DynamicDemandPricing rule = new DynamicDemandPricing();
    PricingContext context =
        new PricingContext(GOLD, new Money(new BigDecimal("999.99"), "INR"), 10, 100);

    assertEquals(new Money(new BigDecimal("1049.99"), "INR"), rule.priceFor(context));
  }

  @Test
  void refusesAnEventListedInAForeignCurrency() {
    DynamicDemandPricing rule = new DynamicDemandPricing();
    PricingContext usdEvent =
        new PricingContext(GOLD, new Money(new BigDecimal("40.00"), "USD"), 0, 100);

    assertThrows(IllegalArgumentException.class, () -> rule.priceFor(usdEvent));
  }

  @Test
  void neverPricesBelowTheMinimumFloor() {
    // an empty event sells at the listed price; 20.00 is below the 50.00 floor.
    DynamicDemandPricing rule = new DynamicDemandPricing();
    PricingContext context =
        new PricingContext(GOLD, new Money(new BigDecimal("20.00"), "INR"), 0, 100);

    assertEquals(new Money(new BigDecimal("50.00"), "INR"), rule.priceFor(context));
  }
}
