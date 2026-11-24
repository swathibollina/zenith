package com.pavan.zenith.common.values;

import java.util.List;

public record PartiallyGranted(List<SeatCoordinate> granted, List<SeatCoordinate> refused)
    implements HoldResult {

  public PartiallyGranted {
    granted = List.copyOf(granted);
    refused = List.copyOf(refused);
  }
}
