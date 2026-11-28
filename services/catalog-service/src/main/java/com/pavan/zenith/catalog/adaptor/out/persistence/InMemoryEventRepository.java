package com.pavan.zenith.catalog.adapter.out.persistence;

import com.pavan.zenith.catalog.application.port.in.SearchEventsCriteria;
import com.pavan.zenith.catalog.application.port.out.EventRepository;
import com.pavan.zenith.catalog.domain.model.Event;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

// A fake: a real, working, lightweight implementation of the port - not a
// mock. The whole service runs end to end on this before any database
// exists. Task 1.10 (Flyway + JPA) replaces it as the production bean;
// the fake stays forever for use-case tests.
@Component
public class InMemoryEventRepository implements EventRepository {

  // ConcurrentHashMap: a HashMap that stays correct when several web
  // requests touch it at the same time.
  private final Map<UUID, Event> store = new ConcurrentHashMap<>();
  private final Map<UUID, Long> versions = new ConcurrentHashMap<>();

  @Override
  public Optional<Event> findById(final UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void save(final Event event) {
    store.put(event.id(), event);
    // Counts saves, like the version column a real database will keep in
    // Task 1.10 to detect two people editing the same event in parallel.
    // merge: store 1 if absent, otherwise combine the old value with 1
    // using Long::sum (addition) - all in one safe step.
    versions.merge(event.id(), 1L, Long::sum);
  }

  @Override
  public List<Event> search(final SearchEventsCriteria criteria) {
    return store.values().stream()
        .filter(event -> matchesTitle(event, criteria.titleContains()))
        .filter(event -> matchesStatus(event, criteria.status()))
        .toList();
  }

  // Not on the port: the application never needs this. A fake may offer
  // extra inspection helpers for tests; the port stays shaped by use cases.
  public long versionOf(final UUID id) {
    return versions.getOrDefault(id, 0L);
  }

  private static boolean matchesTitle(final Event event, final String titleContains) {
    if (titleContains == null) {
      return true;
    }
    return event.title().toLowerCase(Locale.ROOT).contains(titleContains.toLowerCase(Locale.ROOT));
  }

  private static boolean matchesStatus(final Event event, final Event.Status status) {
    return status == null || event.status() == status;
  }
}
