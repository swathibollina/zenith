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
}
