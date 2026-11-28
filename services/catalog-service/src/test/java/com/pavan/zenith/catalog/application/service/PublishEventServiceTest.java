package com.pavan.zenith.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.pavan.zenith.catalog.adapter.out.persistence.InMemoryEventRepository;
import com.pavan.zenith.catalog.application.port.in.PublishEventCommand;
import com.pavan.zenith.catalog.application.port.out.DomainEventPublisher;
import com.pavan.zenith.catalog.domain.event.EventPublished;
import com.pavan.zenith.catalog.domain.exception.EventNotFoundException;
import com.pavan.zenith.catalog.domain.exception.MissingPriceTierException;
import com.pavan.zenith.catalog.domain.model.DateRange;
import com.pavan.zenith.catalog.domain.model.Event;
import com.pavan.zenith.catalog.domain.model.PriceTier;
import com.pavan.zenith.catalog.domain.model.TierCode;
import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

// The full conductor's rhythm under test: load, intend, save, relay - with
// a frozen clock so the recorded moment can be asserted exactly.
class PublishEventServiceTest {

  private static final Instant FROZEN_NOW = Instant.parse("2026-11-29T10:00:00Z");
  private static final Clock CLOCK = Clock.fixed(FROZEN_NOW, ZoneOffset.UTC);
  private static final UUID ID = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");

  private final InMemoryEventRepository repository = new InMemoryEventRepository();
  private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);
  private final PublishEventService service = new PublishEventService(repository, publisher, CLOCK);

  @Test
  void publishFlipsStatusAndSavesThroughThePort() {
    repository.save(publishableDraft(ID));

    service.publish(new PublishEventCommand(ID));

    assertEquals(Event.Status.PUBLISHED, repository.findById(ID).orElseThrow().status());
    // Version 2 proves save() ran a second time - asserting status alone
    // would pass even if the use case forgot to save, because the fake
    // hands back the same object it stored.
    assertEquals(2, repository.versionOf(ID));
  }

  @Test
  void publishHandsTheRecordedFactToThePublisher() {
    repository.save(publishableDraft(ID));

    service.publish(new PublishEventCommand(ID));

    ArgumentCaptor<List<Object>> captor = ArgumentCaptor.captor();
    verify(publisher).publish(captor.capture());
    assertEquals(1, captor.getValue().size());
    EventPublished fact = assertInstanceOf(EventPublished.class, captor.getValue().get(0));
    assertEquals(ID, fact.eventId());
    assertEquals(FROZEN_NOW, fact.occurredAt());
  }

  @Test
  void publishRefusesAnUnknownIdAndStaysSilent() {
    assertThrows(EventNotFoundException.class, () -> service.publish(new PublishEventCommand(ID)));

    verifyNoInteractions(publisher);
  }

  @Test
  void domainRefusalsPassThroughUntouched() {
    repository.save(draftWithoutTiers(ID));

    assertThrows(
        MissingPriceTierException.class, () -> service.publish(new PublishEventCommand(ID)));

    assertEquals(1, repository.versionOf(ID)); // nothing was saved after the refusal
    verifyNoInteractions(publisher);
  }

  private static Event publishableDraft(final UUID id) {
    Event event = draftWithoutTiers(id);
    event.addPriceTier(
        new PriceTier(new TierCode("GOLD"), new Money(new BigDecimal("1500.00"), "INR")),
        FROZEN_NOW);
    event.pullDomainEvents(); // drain the setup fact; tests watch only what publish records
    return event;
  }

  private static Event draftWithoutTiers(final UUID id) {
    Instant showStart = FROZEN_NOW.plus(30, ChronoUnit.DAYS);
    return Event.draft(
        id,
        "Aurora Nights",
        ZoneId.of("Asia/Kolkata"),
        new DateRange(showStart, showStart.plus(3, ChronoUnit.HOURS)),
        FROZEN_NOW.plus(7, ChronoUnit.DAYS));
  }
}
