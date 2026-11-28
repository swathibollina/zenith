package com.pavan.zenith.catalog.application.port.out;

import java.util.List;

// Outbound port: "tell the rest of the world what just happened". The
// application drains an aggregate's recorded facts (Event.pullDomainEvents)
// and hands them here - this interface is the "someone outside the domain
// does the actual telling" that the aggregate's comment promised. How the
// facts travel (a log line now, Kafka in Phase 6) is an adapter's secret:
// no technology is named here.
public interface DomainEventPublisher {

  void publish(List<Object> events);
}
