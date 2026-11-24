package com.pavan.zenith.common.values;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SeatCoordinateTest {

  @Test
  void blankRowIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new SeatCoordinate(" ", 12));
  }

  @Test
  void nullRowIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new SeatCoordinate(null, 12));
  }

  @Test
  void seatNumberZeroIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new SeatCoordinate("A", 0));
  }

  @Test
  void aWellFormedCoordinateIsAccepted() {
    SeatCoordinate seat = new SeatCoordinate("A", 12);
    assertEquals("A", seat.row());
    assertEquals(12, seat.number());
  }
}
