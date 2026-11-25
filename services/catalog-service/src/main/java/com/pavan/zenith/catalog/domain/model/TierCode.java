package com.pavan.zenith.catalog.domain.model;

public record TierCode(String value) {

  public TierCode {
    if (value == null || !value.matches("[A-Z0-9-]+")) {
      throw new IllegalArgumentException(
          "tier code must be uppercase letters, digits, and hyphens: " + value);
    }
  }
}
