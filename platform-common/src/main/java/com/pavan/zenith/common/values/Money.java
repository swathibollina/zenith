package com.pavan.zenith.common.values;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, String currency) {
  public Money {
    if (amount == null) throw new IllegalArgumentException("amount must not be null");
    if (amount.signum() < 0)
      throw new IllegalArgumentException("amount must not be negative: " + amount);
    if (currency == null) throw new IllegalArgumentException("currency must not be null");
    Currency.getInstance(currency);
  }

  public Money plus(final Money other) {
    if (!currency.equals(other.currency))
      throw new IllegalArgumentException(
          "cannot add " + other.currency + " to " + currency + " without an exchange rate");
    return new Money(amount.add(other.amount), currency);
  }

  public Money multipliedBy(final int quantity) {
    return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
  }
}
