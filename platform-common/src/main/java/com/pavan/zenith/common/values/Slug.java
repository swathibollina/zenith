package com.pavan.zenith.common.values;

public record Slug(String value) {
  public Slug {
    if (value == null || !value.matches("[a-z0-9-]+"))
      throw new IllegalArgumentException(
          "Slug must be lower case letters, digits and hyphens: " + value);
  }
}
