package com.pavan.zenith.catalog.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

// TieredPricing alone: its own lookup behaviour, its constructor guards,
// and the three shared base behaviours (foreign currency refused, rounding
// applied, floor enforced).
class TieredPricingDomainTest {

  private static final TierCode GOLD = new TierCode("GOLD");
  private static final TierCode SILVER = new TierCode("SILVER");
  private static final Money INR_2000 = new Money(new BigDecimal("2000.00"), "INR");
  private static final Money INR_1200 = new Money(new BigDecimal("1200.00"), "INR");

  private static PricingContext buying(final TierCode tier) {
    return new PricingContext(tier, INR_2000, 0, 100);
  }

  @Test
  void looksUpThePriceOfTheTierBeingBought() {
    TieredPricing rule = new TieredPricing(Map.of(GOLD, INR_2000, SILVER, INR_1200));

    assertEquals(INR_2000, rule.priceFor(buying(GOLD)));
    assertEquals(INR_1200, rule.priceFor(buying(SILVER)));
  }

  @Test
  void refusesATierItWasNeverGivenAPriceFor() {
    TieredPricing rule = new TieredPricing(Map.of(SILVER, INR_1200));

    assertThrows(IllegalArgumentException.class, () -> rule.priceFor(buying(GOLD)));
  }

  @Test
  void refusesNullOrEmptyTierPrices() {
    assertThrows(IllegalArgumentException.class, () -> new TieredPricing(null));
    assertThrows(IllegalArgumentException.class, () -> new TieredPricing(Map.of()));
  }

  @Test
  void keepsItsOwnCopyOfTheTierPrices() {
    Map<TierCode, Money> source = new HashMap<>();
    source.put(GOLD, INR_2000);
    TieredPricing rule = new TieredPricing(source);

    // too late: the rule copied the entries at construction time.
    source.put(SILVER, INR_1200);

    assertThrows(IllegalArgumentException.class, () -> rule.priceFor(buying(SILVER)));
  }

  @Test
  void refusesAnEventListedInAForeignCurrency() {
    TieredPricing rule = new TieredPricing(Map.of(GOLD, INR_2000));
    PricingContext usdEvent =
        new PricingContext(GOLD, new Money(new BigDecimal("40.00"), "USD"), 0, 100);

    assertThrows(IllegalArgumentException.class, () -> rule.priceFor(usdEvent));
  }

  @Test
  void roundsTheConfiguredTierPriceToTwoDecimals() {
    TieredPricing rule =
        new TieredPricing(Map.of(GOLD, new Money(new BigDecimal("1234.567"), "INR")));

    assertEquals(new Money(new BigDecimal("1234.57"), "INR"), rule.priceFor(buying(GOLD)));
  }

  @Test
  void neverPricesBelowTheMinimumFloor() {
    TieredPricing rule = new TieredPricing(Map.of(GOLD, new Money(new BigDecimal("25.00"), "INR")));

    assertEquals(new Money(new BigDecimal("50.00"), "INR"), rule.priceFor(buying(GOLD)));
  }
}
