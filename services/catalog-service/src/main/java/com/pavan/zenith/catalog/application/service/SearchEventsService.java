package com.pavan.zenith.catalog.application.service;

import com.pavan.zenith.catalog.application.port.in.SearchEventsCriteria;
import com.pavan.zenith.catalog.application.port.in.SearchEventsQuery;
import com.pavan.zenith.catalog.application.port.out.EventRepository;
import com.pavan.zenith.catalog.domain.model.Event;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Use case (query): find events matching the filters. Returns data,
// changes nothing.
@Service
public class SearchEventsService implements SearchEventsQuery {

  private final EventRepository repository;

  public SearchEventsService(final EventRepository repository) {
    this.repository = repository;
  }

  // Propagation REQUIRED (the default), readOnly = true: a query promises
  // no writes inside its boundary.
  @Override
  @Transactional(readOnly = true)
  public List<Event> search(final SearchEventsCriteria criteria) {
    return repository.search(criteria);
  }
}
