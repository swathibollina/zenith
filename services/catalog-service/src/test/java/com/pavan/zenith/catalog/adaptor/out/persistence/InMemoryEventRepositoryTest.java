package com.pavan.zenith.catalog.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pavan.zenith.catalog.application.port.in.SearchEventsCriteria;
import com.pavan.zenith.catalog.domain.model.DateRange;
import com.pavan.zenith.catalog.domain.model.Event;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

// The fake behaves like storage: save, find, search, and a version that
// counts saves. Plain JUnit - no Spring context, no database.
class InMemoryEventRepositoryTest {

  private static Event draftEvent(final String title) {
    return Event.draft(
        UUID.randomUUID(),
        title,
        ZoneId.of("Asia/Kolkata"),
        new DateRange(Instant.parse("2027-03-01T12:00:00Z"), Instant.parse("2027-03-01T15:00:00Z")),
        Instant.parse("2027-01-01T00:00:00Z"));
  }

  @Test
  void findsWhatWasSaved() {
    InMemoryEventRepository repository = new InMemoryEventRepository();
    Event event = draftEvent("StarLive Arena Night");

    repository.save(event);

    assertEquals(Optional.of(event), repository.findById(event.id()));
  }

  @Test
  void findingAnUnknownIdReturnsEmpty() {
    assertTrue(new InMemoryEventRepository().findById(UUID.randomUUID()).isEmpty());
  }

  @Test
  void versionGoesUpByOneOnEverySave() {
    InMemoryEventRepository repository = new InMemoryEventRepository();
    Event event = draftEvent("StarLive Arena Night");

    assertEquals(0L, repository.versionOf(event.id()));
    repository.save(event);
    assertEquals(1L, repository.versionOf(event.id()));
    repository.save(event);
    assertEquals(2L, repository.versionOf(event.id()));
  }

  @Test
  void searchFiltersByTitleFragmentAndStatus() {
    InMemoryEventRepository repository = new InMemoryEventRepository();
    Event arena = draftEvent("StarLive Arena Night");
    Event moon = draftEvent("MoonStage Acoustic Evening");
    repository.save(arena);
    repository.save(moon);

    assertEquals(List.of(arena), repository.search(new SearchEventsCriteria("arena", null)));
    assertEquals(2, repository.search(SearchEventsCriteria.unfiltered()).size());
    assertTrue(repository.search(new SearchEventsCriteria(null, Event.Status.PUBLISHED)).isEmpty());
  }
}
