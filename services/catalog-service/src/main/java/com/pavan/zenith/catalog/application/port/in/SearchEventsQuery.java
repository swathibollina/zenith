package com.pavan.zenith.catalog.application.port.in;

import com.pavan.zenith.catalog.domain.model.Event;
import java.util.List;

// Inbound port: "find events matching these filters". Named ...Query rather
// than ...UseCase because it is a pure read - it returns data and must never
// change state.
public interface SearchEventsQuery {

  List<Event> search(SearchEventsCriteria criteria);
}
