package com.pavan.zenith.catalog.application.service;

import com.pavan.zenith.catalog.application.port.in.PublishEventCommand;
import com.pavan.zenith.catalog.application.port.in.PublishEventUseCase;
import com.pavan.zenith.catalog.application.port.out.DomainEventPublisher;
import com.pavan.zenith.catalog.application.port.out.EventRepository;
import com.pavan.zenith.catalog.domain.exception.EventNotFoundException;
import com.pavan.zenith.catalog.domain.model.Event;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Use case (command): publish a draft so tickets can go on sale. Changes
// state, returns nothing. The conductor's rhythm: load through a port, call
// the intention method, save through a port, hand the drained facts to the
// publisher port.
@Service
public class PublishEventService implements PublishEventUseCase {

  private final EventRepository repository;
  private final DomainEventPublisher publisher;
  private final Clock clock;

  public PublishEventService(
      final EventRepository repository, final DomainEventPublisher publisher, final Clock clock) {
    this.repository = repository;
    this.publisher = publisher;
    this.clock = clock;
  }

  // Propagation REQUIRED (the default): load-change-save is one unit; it
  // joins a caller's transaction if one exists, and never commits half.
  @Override
  @Transactional
  public void publish(final PublishEventCommand command) {
    Event event =
        repository
            .findById(command.eventId())
            .orElseThrow(() -> new EventNotFoundException(command.eventId()));
    event.publish(Instant.now(clock));
    repository.save(event);
    publisher.publish(event.pullDomainEvents());
  }
}
