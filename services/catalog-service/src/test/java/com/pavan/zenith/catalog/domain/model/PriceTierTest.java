package com.pavan.zenith.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PriceTierTest {

  private static final Money PRICE = new Money(new BigDecimal("800.00"), "INR");

  @Test
  void nullCodeIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new PriceTier(null, PRICE));
  }

  @Test
  void nullPriceIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new PriceTier(new TierCode("BACK"), null));
  }

  @Test
  void aWellFormedTierIsAccepted() {
    PriceTier tier = new PriceTier(new TierCode("BACK"), PRICE);
    assertEquals(new TierCode("BACK"), tier.code());
    assertEquals(PRICE, tier.price());
  }
}
