package com.pavan.zenith.catalog.application.port.in;

import com.pavan.zenith.catalog.domain.model.Event;

// Inbound port: "show me one event". A query returns data and changes
// nothing. What happens for an unknown id is decided by the implementation
// in Step 5.
public interface GetEventUseCase {

  Event get(GetEventQuery query);
}
