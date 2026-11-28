package com.pavan.zenith.catalog.domain.model;

import com.pavan.zenith.common.values.Money;

public record PriceTier(TierCode code, Money price) {

  public PriceTier {
    if (code == null) {
      throw new IllegalArgumentException("code must not be null");
    }
    if (price == null) {
      throw new IllegalArgumentException("price must not be null");
    }
  }
}
