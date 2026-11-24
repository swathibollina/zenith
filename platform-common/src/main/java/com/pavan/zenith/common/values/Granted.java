package com.pavan.zenith.common.values;

import java.util.List;

public record Granted(List<SeatCoordinate> seats) implements HoldResult {

  public Granted {
    seats = List.copyOf(seats);
  }
}
