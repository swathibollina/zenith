package com.pavan.zenith.catalog.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

// FlatPricing alone: its own calculation plus the three shared base
// behaviours (foreign currency refused, rounding applied, floor enforced).
// A fourth pricing rule gets a fourth small file like this one - tests
// grow linearly, one class per rule.
class FlatPricingDomainTest {

  private static final TierCode GOLD = new TierCode("GOLD");
  private static final TierCode SILVER = new TierCode("SILVER");
  private static final Money INR_2000 = new Money(new BigDecimal("2000.00"), "INR");
  private static final Money INR_1200 = new Money(new BigDecimal("1200.00"), "INR");

  private static PricingContext goldEvent(final Money listedPrice) {
    return new PricingContext(GOLD, listedPrice, 0, 100);
  }

  @Test
  void chargesTheSameForEveryTierAndAnyDemand() {
    FlatPricing rule = new FlatPricing(INR_1200);

    assertEquals(INR_1200, rule.priceFor(new PricingContext(GOLD, INR_2000, 0, 100)));
    assertEquals(INR_1200, rule.priceFor(new PricingContext(SILVER, INR_2000, 99, 100)));
  }

  @Test
  void refusesANullConfiguredPrice() {
    assertThrows(IllegalArgumentException.class, () -> new FlatPricing(null));
  }

  @Test
  void refusesAnEventListedInAForeignCurrency() {
    FlatPricing rule = new FlatPricing(INR_1200);
    PricingContext usdEvent = goldEvent(new Money(new BigDecimal("40.00"), "USD"));

    assertThrows(IllegalArgumentException.class, () -> rule.priceFor(usdEvent));
  }

  @Test
  void roundsTheConfiguredAmountToTwoDecimals() {
    FlatPricing rule = new FlatPricing(new Money(new BigDecimal("99.999"), "INR"));

    assertEquals(new Money(new BigDecimal("100.00"), "INR"), rule.priceFor(goldEvent(INR_2000)));
  }

  @Test
  void neverPricesBelowTheMinimumFloor() {
    FlatPricing rule = new FlatPricing(new Money(new BigDecimal("10.00"), "INR"));

    assertEquals(new Money(new BigDecimal("50.00"), "INR"), rule.priceFor(goldEvent(INR_2000)));
  }
}
