package com.pavan.zenith.catalog.application.service;

import com.pavan.zenith.catalog.application.port.in.CreateEventCommand;
import com.pavan.zenith.catalog.application.port.in.CreateEventUseCase;
import com.pavan.zenith.catalog.application.port.out.DomainEventPublisher;
import com.pavan.zenith.catalog.application.port.out.EventRepository;
import com.pavan.zenith.catalog.domain.model.Event;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Use case (command): create a draft event. Changes state; returns only the
// new id, the one return value CQS permits a command - without it the caller
// could never find what it just created. One class, one business operation,
// one public method. Depends only on ports; it has no idea the repository is
// a HashMap today.
@Service
public class CreateEventService implements CreateEventUseCase {

  private final EventRepository repository;
  private final DomainEventPublisher publisher;

  public CreateEventService(
      final EventRepository repository, final DomainEventPublisher publisher) {
    this.repository = repository;
    this.publisher = publisher;
  }

  // Propagation REQUIRED (the default): this method IS the transaction
  // boundary; if a future caller has already opened one, we join it, because
  // two halves of one business operation must never commit separately.
  @Override
  @Transactional
  public UUID create(final CreateEventCommand command) {
    Event event =
        Event.draft(
            UUID.randomUUID(),
            command.title(),
            command.venueZone(),
            command.dates(),
            command.salesStart());
    repository.save(event);
    publisher.publish(event.pullDomainEvents());
    return event.id();
  }
}
