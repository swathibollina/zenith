package com.pavan.zenith.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.adapter.out.persistence.InMemoryEventRepository;
import com.pavan.zenith.catalog.application.port.in.GetEventQuery;
import com.pavan.zenith.catalog.domain.exception.EventNotFoundException;
import com.pavan.zenith.catalog.domain.model.DateRange;
import com.pavan.zenith.catalog.domain.model.Event;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

// Query test: no mock at all - a query has nothing to announce, so there is
// no publisher to listen with. Fake repository only.
class GetEventServiceTest {

  private static final UUID ID = UUID.fromString("7e57d004-2b97-0e7a-b45f-5387367791cd");

  private final InMemoryEventRepository repository = new InMemoryEventRepository();
  private final GetEventService service = new GetEventService(repository);

  @Test
  void getReturnsTheStoredEvent() {
    repository.save(draftEvent(ID, "Aurora Nights"));

    Event found = service.get(new GetEventQuery(ID));

    assertEquals(ID, found.id());
    assertEquals("Aurora Nights", found.title());
  }

  @Test
  void getRefusesAnUnknownId() {
    assertThrows(EventNotFoundException.class, () -> service.get(new GetEventQuery(ID)));
  }

  private static Event draftEvent(final UUID id, final String title) {
    return Event.draft(
        id,
        title,
        ZoneId.of("Asia/Kolkata"),
        new DateRange(Instant.parse("2027-03-01T12:00:00Z"), Instant.parse("2027-03-01T15:00:00Z")),
        Instant.parse("2027-01-01T00:00:00Z"));
  }
}
