package com.pavan.zenith.common.values;

public record SeatCoordinate(String row, int number) {
  public SeatCoordinate {
    if (row == null || row.isBlank()) throw new IllegalArgumentException("row must not be blank");
    if (number < 1) throw new IllegalArgumentException("seat number must be atleast 1: " + number);
  }

  // The label printed on tickets and seat maps: row A, seat 12 -> "A12".
  public String label() {
    return row + number;
  }
}
