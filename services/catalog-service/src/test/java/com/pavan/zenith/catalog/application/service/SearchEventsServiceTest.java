package com.pavan.zenith.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pavan.zenith.catalog.adapter.out.persistence.InMemoryEventRepository;
import com.pavan.zenith.catalog.application.port.in.SearchEventsCriteria;
import com.pavan.zenith.catalog.domain.model.DateRange;
import com.pavan.zenith.catalog.domain.model.Event;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

// Query test: the use case delegates to the reading side of the port; the
// filtering behaviour itself is covered by the fake's own test.
class SearchEventsServiceTest {

  private final InMemoryEventRepository repository = new InMemoryEventRepository();
  private final SearchEventsService service = new SearchEventsService(repository);

  @Test
  void unfilteredSearchReturnsEverythingStored() {
    repository.save(draftEvent("Aurora Nights"));
    repository.save(draftEvent("MoonStage Acoustic Evening"));
    repository.save(draftEvent("Aurora Daybreak"));

    List<Event> found = service.search(SearchEventsCriteria.unfiltered());

    assertEquals(3, found.size());
  }

  @Test
  void titleFilterNarrowsTheResult() {
    repository.save(draftEvent("Aurora Nights"));
    repository.save(draftEvent("MoonStage Acoustic Evening"));
    repository.save(draftEvent("Aurora Daybreak"));

    List<Event> found = service.search(new SearchEventsCriteria("aurora", null));

    assertEquals(2, found.size());
    assertTrue(found.stream().allMatch(event -> event.title().contains("Aurora")));
  }

  private static Event draftEvent(final String title) {
    return Event.draft(
        UUID.randomUUID(),
        title,
        ZoneId.of("Asia/Kolkata"),
        new DateRange(Instant.parse("2027-03-01T12:00:00Z"), Instant.parse("2027-03-01T15:00:00Z")),
        Instant.parse("2027-01-01T00:00:00Z"));
  }
}
