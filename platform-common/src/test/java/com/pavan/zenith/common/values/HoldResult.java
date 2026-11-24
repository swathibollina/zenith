package com.pavan.zenith.common.values;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class HoldResultTest {

  private static final SeatCoordinate A1 = new SeatCoordinate("A", 1);
  private static final SeatCoordinate A2 = new SeatCoordinate("A", 2);

  @Test
  void grantedIsDescribed() {
    assertEquals("granted 2 seats", describe(new Granted(List.of(A1, A2))));
  }

  @Test
  void rejectedIsDescribed() {
    assertEquals("rejected: sold out", describe(new Rejected("sold out")));
  }

  @Test
  void partiallyGrantedIsDescribed() {
    assertEquals("granted 1, refused 1", describe(new PartiallyGranted(List.of(A1), List.of(A2))));
  }

  @Test
  void rejectionWithoutAReasonIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Rejected(" "));
  }

  private static String describe(final HoldResult result) {
    return switch (result) {
      case Granted granted -> "granted " + granted.seats().size() + " seats";
      case Rejected rejected -> "rejected: " + rejected.reason();
      case PartiallyGranted partial ->
          "granted " + partial.granted().size() + ", refused " + partial.refused().size();
    };
  }
}
