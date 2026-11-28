package com.pavan.zenith.catalog.application.port.in;

import java.util.UUID;

// The single input of "publish this event". Still a record, even for one
// field: adding a second input later is one change here, not a signature
// break in every caller.
public record PublishEventCommand(UUID eventId) {

  public PublishEventCommand {
    if (eventId == null) {
      throw new IllegalArgumentException("eventId must not be null");
    }
  }
}
