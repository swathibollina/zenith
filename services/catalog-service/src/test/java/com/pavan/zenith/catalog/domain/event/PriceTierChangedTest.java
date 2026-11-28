package com.pavan.zenith.catalog.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PriceTierChangedTest {

  private static final UUID EVENT_ID = UUID.fromString("7e57d004-2b97-0e7a-b45f-5387367791cd");
  private static final Instant WHEN = Instant.parse("2026-03-02T10:00:00Z");
  private static final TierCode GOLD = new TierCode("GOLD");
  private static final Money PRICE = new Money(new BigDecimal("2000.00"), "INR");

  @Test
  void nullEventIdIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new PriceTierChanged(null, WHEN, GOLD, PRICE));
  }

  @Test
  void nullOccurredAtIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new PriceTierChanged(EVENT_ID, null, GOLD, PRICE));
  }

  @Test
  void nullTierIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new PriceTierChanged(EVENT_ID, WHEN, null, PRICE));
  }

  @Test
  void nullPriceIsRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> new PriceTierChanged(EVENT_ID, WHEN, GOLD, null));
  }

  @Test
  void carriesExactlyWhatAConsumerNeeds() {
    PriceTierChanged fact = new PriceTierChanged(EVENT_ID, WHEN, GOLD, PRICE);
    assertEquals(EVENT_ID, fact.eventId());
    assertEquals(WHEN, fact.occurredAt());
    assertEquals(GOLD, fact.tier());
    assertEquals(PRICE, fact.newPrice());
  }
}
