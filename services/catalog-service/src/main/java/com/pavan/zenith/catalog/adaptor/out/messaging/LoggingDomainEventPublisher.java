package com.pavan.zenith.catalog.adapter.out.messaging;

import com.pavan.zenith.catalog.application.port.out.DomainEventPublisher;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// The first, simplest adapter for the publisher port: every fact becomes a
// log line. Phase 6 swaps this for a Kafka adapter; the port - and every
// use case behind it - stays untouched when that happens.
@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

  @Override
  public void publish(final List<Object> events) {
    for (Object event : events) {
      LOG.info("domain event: {}", event);
    }
  }
}
