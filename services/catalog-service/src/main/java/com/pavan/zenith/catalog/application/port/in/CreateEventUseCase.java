package com.pavan.zenith.catalog.application.port.in;

import java.util.UUID;

// Inbound port: one thing the outside world may ask of this application -
// "create a draft event". A command changes state, so it returns at most
// the new identity, never the whole object.
public interface CreateEventUseCase {

  UUID create(CreateEventCommand command);
}
