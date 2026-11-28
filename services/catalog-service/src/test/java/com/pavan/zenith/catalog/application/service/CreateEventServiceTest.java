package com.pavan.zenith.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.pavan.zenith.catalog.adapter.out.persistence.InMemoryEventRepository;
import com.pavan.zenith.catalog.application.port.in.CreateEventCommand;
import com.pavan.zenith.catalog.application.port.out.DomainEventPublisher;
import com.pavan.zenith.catalog.domain.model.DateRange;
import com.pavan.zenith.catalog.domain.model.Event;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

// Application-layer test: the real use case wired by hand - fake repository,
// mock publisher. No Spring, no database, milliseconds.
class CreateEventServiceTest {

  private final InMemoryEventRepository repository = new InMemoryEventRepository();
  private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);
  private final CreateEventService service = new CreateEventService(repository, publisher);

  @Test
  void createReturnsTheNewIdAndSavesADraft() {
    UUID id = service.create(command("Aurora Nights"));

    Event saved = repository.findById(id).orElseThrow();
    assertEquals("Aurora Nights", saved.title());
    assertEquals(Event.Status.DRAFT, saved.status());
    assertEquals(1, repository.versionOf(id));
  }

  @Test
  void createRelaysAnEmptyFactListBecauseDraftingRecordsNothing() {
    service.create(command("Aurora Nights"));

    ArgumentCaptor<List<Object>> captor = ArgumentCaptor.captor();
    verify(publisher).publish(captor.capture());
    assertEquals(List.of(), captor.getValue());
  }

  private static CreateEventCommand command(final String title) {
    return new CreateEventCommand(
        title,
        ZoneId.of("Asia/Kolkata"),
        new DateRange(Instant.parse("2027-03-01T12:00:00Z"), Instant.parse("2027-03-01T15:00:00Z")),
        Instant.parse("2027-01-01T00:00:00Z"));
  }
}
