package com.pavan.zenith.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventTest {

  @Test
  void sameIdMeansSameEventEvenWithDifferentTitles() {
    UUID id = UUID.randomUUID();
    Event first = new Event(id, "Coldplay Mumbai");
    Event second = new Event(id, "Coldplay Mumbai rescheduled");
    assertTrue(first.equals(second));
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void differentIdMeansDifferentEventEvenWithSameTitle() {
    Event first = new Event(UUID.randomUUID(), "Coldplay Mumbai");
    Event second = new Event(UUID.randomUUID(), "Coldplay Mumbai");
    assertFalse(first.equals(second));
  }

  @Test
  void notEqualToNullOrOtherTypes() {
    Event event = new Event(UUID.randomUUID(), "Coldplay Mumbai");
    assertFalse(event.equals(null));
    assertTrue(event.equals(event));
  }

  @Test
  void renamingChangesNeitherEqualityNorHashCode() {
    UUID id = UUID.randomUUID();
    Event event = new Event(id, "Old title");
    int hashBefore = event.hashCode();
    event.rename("New title");
    assertEquals("New title", event.title());
    assertEquals(hashBefore, event.hashCode());
    assertTrue(event.equals(new Event(id, "Something else entirely")));
  }

  @Test
  void publishMovesADraftToPublished() {
    Event event = new Event(UUID.randomUUID(), "Coldplay Mumbai");
    assertEquals(Event.Status.DRAFT, event.status());
    event.publish();
    assertEquals(Event.Status.PUBLISHED, event.status());
  }

  @Test
  void publishRefusesWhenAlreadyPublished() {
    Event event = new Event(UUID.randomUUID(), "Coldplay Mumbai");
    event.publish();
    assertThrows(IllegalStateException.class, event::publish);
  }

  @Test
  void constructorRefusesNullId() {
    assertThrows(IllegalArgumentException.class, () -> new Event(null, "Coldplay Mumbai"));
  }

  @Test
  void constructorRefusesBlankTitle() {
    assertThrows(IllegalArgumentException.class, () -> new Event(UUID.randomUUID(), " "));
  }

  @Test
  void renameRefusesBlankTitle() {
    Event event = new Event(UUID.randomUUID(), "Coldplay Mumbai");
    assertThrows(IllegalArgumentException.class, () -> event.rename(" "));
  }
}
