package com.pavan.zenith.common.values;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

  @Test
  void plusAddsAmountsInTheSameCurrency() {
    Money total =
        new Money(new BigDecimal("500.00"), "INR").plus(new Money(new BigDecimal("250.50"), "INR"));
    assertEquals(new Money(new BigDecimal("750.50"), "INR"), total);
  }

  @Test
  void plusReturnsANewMoneyAndLeavesTheOriginalsUntouched() {
    Money first = new Money(new BigDecimal("10.00"), "INR");
    Money second = new Money(new BigDecimal("5.00"), "INR");
    first.plus(second);
    assertEquals(new Money(new BigDecimal("10.00"), "INR"), first);
    assertEquals(new Money(new BigDecimal("5.00"), "INR"), second);
  }

  @Test
  void multipliedByComputesAnOrderLinePrice() {
    Money linePrice = new Money(new BigDecimal("800.00"), "INR").multipliedBy(3);
    assertEquals(new Money(new BigDecimal("2400.00"), "INR"), linePrice);
  }

  @Test
  void multiplyingIntoANegativeAmountIsRejectedByTheConstructor() {
    Money price = new Money(new BigDecimal("800.00"), "INR");
    assertThrows(IllegalArgumentException.class, () -> price.multipliedBy(-1));
  }

  @Test
  void addingDifferentCurrenciesThrowsAndNamesBoth() {
    Money dollars = new Money(new BigDecimal("10.00"), "USD");
    Money euros = new Money(new BigDecimal("10.00"), "EUR");
    assertThatThrownBy(() -> dollars.plus(euros))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("USD")
        .hasMessageContaining("EUR");
  }
}
