package com.pavan.zenith.catalog.application.port.out;

import com.pavan.zenith.catalog.application.port.in.SearchEventsCriteria;
import com.pavan.zenith.catalog.domain.model.Event;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Outbound port: what the application NEEDS from storage, shaped by the use
// cases and nothing else. The application owns this interface; technology
// adapters implement it later (Step 3: an in-memory fake, Task 1.10: JPA).
// That direction - business code declares, infrastructure implements - is
// the dependency inversion this whole architecture rests on.
public interface EventRepository {

  // Publish and Get need one event that might not exist; Optional says
  // "might not be there" honestly, where null could be forgotten.
  Optional<Event> findById(UUID id);

  // Create and Publish need to store the current state.
  void save(Event event);

  // Search needs exactly the filters the use case offers, nothing more.
  // No findAll, no saveAll: a port shaped by what JPA happens to offer
  // would be pointing the wrong way.
  List<Event> search(SearchEventsCriteria criteria);
}
