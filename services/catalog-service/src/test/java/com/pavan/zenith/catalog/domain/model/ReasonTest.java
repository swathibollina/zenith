package com.pavan.zenith.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ReasonTest {

  @Test
  void blankReasonIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Reason(" "));
  }

  @Test
  void nullReasonIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Reason(null));
  }

  @Test
  void aWellFormedReasonIsAccepted() {
    assertEquals("venue flooded", new Reason("venue flooded").text());
  }
}
