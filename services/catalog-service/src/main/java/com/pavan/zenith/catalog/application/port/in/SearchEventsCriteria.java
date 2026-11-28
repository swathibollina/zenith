package com.pavan.zenith.catalog.application.port.in;

import com.pavan.zenith.catalog.domain.model.Event;

// The optional filters of "find events". A null field means "do not filter
// by this"; that meaning is the contract, so a blank title filter (which can
// only be a mistake) is refused.
public record SearchEventsCriteria(String titleContains, Event.Status status) {

  public SearchEventsCriteria {
    if (titleContains != null && titleContains.isBlank()) {
      throw new IllegalArgumentException("titleContains must be null or non-blank");
    }
  }

  public static SearchEventsCriteria unfiltered() {
    return new SearchEventsCriteria(null, null);
  }
}
