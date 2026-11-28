package com.pavan.zenith.catalog.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

// Cross-rule suite: the properties that hold for the whole FAMILY, not for
// any single rule. Per-rule behaviour lives in FlatPricingDomainTest,
// TieredPricingDomainTest and DynamicDemandPricingDomainTest; the factory's
// choice lives in PricingRuleFactoryDomainTest.
//
// The defence sentence, worth saying from memory: with strategies, tests
// grow one class per rule; with an if/else chain, every new branch
// multiplies against the old ones.
class PricingRuleDomainTest {

  private static final TierCode GOLD = new TierCode("GOLD");
  private static final Money INR_2000 = new Money(new BigDecimal("2000.00"), "INR");
  private static final Money INR_1200 = new Money(new BigDecimal("1200.00"), "INR");

  // THE caller. It holds only the interface. Each rule below passes through
  // this one method, and the method never changes - that is the whole point
  // of the Strategy pattern.
  private static Money quote(final PricingRule rule, final PricingContext context) {
    return rule.priceFor(context);
  }

  @Test
  void theSameCallerWorksWithEveryRuleWithoutChanging() {
    PricingContext context = new PricingContext(GOLD, INR_2000, 50, 100);

    Money flat = quote(new FlatPricing(INR_1200), context);
    Money tiered = quote(new TieredPricing(Map.of(GOLD, INR_2000)), context);
    Money dynamic = quote(new DynamicDemandPricing(), context);

    assertEquals(INR_1200, flat);
    assertEquals(INR_2000, tiered);
    assertEquals(new Money(new BigDecimal("2500.00"), "INR"), dynamic);
  }

  @Test
  void contextRefusesNonsenseOccupancy() {
    assertThrows(IllegalArgumentException.class, () -> new PricingContext(GOLD, INR_2000, 5, 0));
    assertThrows(IllegalArgumentException.class, () -> new PricingContext(GOLD, INR_2000, -1, 100));
    assertThrows(
        IllegalArgumentException.class, () -> new PricingContext(GOLD, INR_2000, 101, 100));
  }
}
