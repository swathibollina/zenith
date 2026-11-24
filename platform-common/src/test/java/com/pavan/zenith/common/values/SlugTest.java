package com.pavan.zenith.common.values;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SlugTest {

  @Test
  void uppercaseLettersAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Slug("Coldplay-Mumbai-2026"));
  }

  @Test
  void spacesAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Slug("coldplay mumbai 2026"));
  }

  @Test
  void nullIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Slug(null));
  }

  @Test
  void emptyIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Slug(""));
  }

  @Test
  void aWellFormedSlugIsAccepted() {
    Slug slug = new Slug("coldplay-mumbai-2026");
    assertEquals("coldplay-mumbai-2026", slug.value());
  }
}
