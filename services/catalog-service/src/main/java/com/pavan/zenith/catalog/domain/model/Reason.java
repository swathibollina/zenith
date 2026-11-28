package com.pavan.zenith.catalog.domain.model;

public record Reason(String text) {

  public Reason {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("reason must not be blank");
    }
  }
}
