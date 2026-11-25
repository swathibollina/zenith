package com.pavan.zenith.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TierCodeTest {

  @Test
  void nullIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new TierCode(null));
  }

  @Test
  void emptyIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new TierCode(""));
  }

  @Test
  void lowercaseIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new TierCode("gold"));
  }

  @Test
  void spacesAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new TierCode("VIP FRONT"));
  }

  @Test
  void aWellFormedCodeIsAccepted() {
    TierCode code = new TierCode("VIP-FRONT");
    assertEquals("VIP-FRONT", code.value());
    assertEquals(new TierCode("VIP-FRONT"), code);
  }
}
