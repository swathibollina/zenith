package com.pavan.zenith.common.values;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void negativeAmountIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("-1"), "USD"));
  }

  @Test
  void nullAmountIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Money(null, "USD"));
  }

  @Test
  void nullCurrencyIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("500"), null));
  }

  @Test
  void nonsenseCurrencyIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("500"), "ZZZ"));
  }

  @Test
  void sameValuesAreEqualAndShareAHashCode() {
    Money first = new Money(new BigDecimal("500"), "INR");
    Money second = new Money(new BigDecimal("500"), "INR");
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    assertEquals(new BigDecimal("500"), first.amount());
    assertEquals("INR", first.currency());
  }
}
