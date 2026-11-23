package com.pavan.zenith.catalog.domain.model;

import java.util.UUID;

// sketch — the real Event aggregate is built in Phase 1
public class Event {

  public enum Status {
    DRAFT,
    PUBLISHED
  }

  private final UUID id;
  private String title;
  private Status status;

  public Event(final UUID id, final String title) {
    if (id == null) {
      throw new IllegalArgumentException("id must not be null");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    this.id = id;
    this.title = title;
    this.status = Status.DRAFT;
  }

  public void rename(final String newTitle) {
    if (newTitle == null || newTitle.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    this.title = newTitle;
  }

  public void publish() {
    if (status != Status.DRAFT) {
      throw new IllegalStateException("only a draft event can be published, was: " + status);
    }
    this.status = Status.PUBLISHED;
  }

  public UUID id() {
    return id;
  }

  public String title() {
    return title;
  }

  public Status status() {
    return status;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Event event)) {
      return false;
    }
    return id.equals(event.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
