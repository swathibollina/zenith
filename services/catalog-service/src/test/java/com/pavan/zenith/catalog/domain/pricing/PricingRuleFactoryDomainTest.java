package com.pavan.zenith.catalog.domain.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

// Step 3 suite: the factory is the one place that turns the persisted rule
// type into a concrete PricingRule. Everyone else holds the interface.
class PricingRuleFactoryDomainTest {

  private static final TierCode GOLD = new TierCode("GOLD");
  private static final Money INR_2000 = new Money(new BigDecimal("2000.00"), "INR");
  private static final Money INR_1200 = new Money(new BigDecimal("1200.00"), "INR");

  @Test
  void factoryReturnsTheMatchingRuleForEachPersistedType() {
    assertInstanceOf(
        FlatPricing.class, PricingRuleFactory.ruleFor(PricingRuleType.FLAT, INR_1200, null));
    assertInstanceOf(
        TieredPricing.class,
        PricingRuleFactory.ruleFor(PricingRuleType.TIERED, null, Map.of(GOLD, INR_2000)));
    assertInstanceOf(
        DynamicDemandPricing.class,
        PricingRuleFactory.ruleFor(PricingRuleType.DYNAMIC_DEMAND, null, null));
  }

  @Test
  void factoryBuiltRulesBehaveExactlyLikeHandBuiltOnes() {
    PricingContext context = new PricingContext(GOLD, INR_2000, 50, 100);

    PricingRule viaFactory = PricingRuleFactory.ruleFor(PricingRuleType.FLAT, INR_1200, null);

    assertEquals(new FlatPricing(INR_1200).priceFor(context), viaFactory.priceFor(context));
  }

  @Test
  void factoryRefusesAMissingType() {
    assertThrows(
        IllegalArgumentException.class, () -> PricingRuleFactory.ruleFor(null, INR_1200, null));
  }

  @Test
  void factoryLetsEachConstructorGuardItsOwnAmount() {
    // FLAT stored without a flat amount: FlatPricing's own guard speaks.
    assertThrows(
        IllegalArgumentException.class,
        () -> PricingRuleFactory.ruleFor(PricingRuleType.FLAT, null, null));
  }
}
