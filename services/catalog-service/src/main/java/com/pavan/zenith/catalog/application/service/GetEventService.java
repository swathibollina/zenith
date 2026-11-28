package com.pavan.zenith.catalog.application.service;

import com.pavan.zenith.catalog.application.port.in.GetEventQuery;
import com.pavan.zenith.catalog.application.port.in.GetEventUseCase;
import com.pavan.zenith.catalog.application.port.out.EventRepository;
import com.pavan.zenith.catalog.domain.exception.EventNotFoundException;
import com.pavan.zenith.catalog.domain.model.Event;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Use case (query): show one event. Returns data, changes nothing - note
// the dependency list: no publisher, no clock, just the reading port.
@Service
public class GetEventService implements GetEventUseCase {

  private final EventRepository repository;

  public GetEventService(final EventRepository repository) {
    this.repository = repository;
  }

  // Propagation REQUIRED (the default), readOnly = true: a query promises
  // no writes; the JPA adapter in Task 1.10 can use that to optimize and
  // to refuse accidental writes inside this boundary.
  @Override
  @Transactional(readOnly = true)
  public Event get(final GetEventQuery query) {
    return repository
        .findById(query.eventId())
        .orElseThrow(() -> new EventNotFoundException(query.eventId()));
  }
}
