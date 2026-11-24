package com.pavan.zenith.common.values;

public record Rejected(String reason) implements HoldResult {

  public Rejected {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("reason must not be blank");
    }
  }
}
