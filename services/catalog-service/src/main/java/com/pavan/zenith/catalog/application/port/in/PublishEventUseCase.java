package com.pavan.zenith.catalog.application.port.in;

// Inbound port: "publish this draft so tickets can go on sale".
// A command that needs no answer returns nothing.
public interface PublishEventUseCase {

  void publish(PublishEventCommand command);
}
