package com.pavan.zenith.catalog.domain.model;

import com.pavan.zenith.catalog.domain.event.EventPublished;
import com.pavan.zenith.catalog.domain.event.EventRescheduled;
import com.pavan.zenith.catalog.domain.event.PriceTierChanged;
import com.pavan.zenith.catalog.domain.exception.DuplicateTierCodeException;
import com.pavan.zenith.catalog.domain.exception.EventAlreadyCancelledException;
import com.pavan.zenith.catalog.domain.exception.EventAlreadyPublishedException;
import com.pavan.zenith.catalog.domain.exception.MissingPriceTierException;
import com.pavan.zenith.catalog.domain.exception.SalesStartInPastException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// The Event aggregate root. All changes go through intention methods that
// enforce the invariants from docs/architecture/aggregates.md before touching
// any state. No setters, no framework imports. Time-sensitive methods take
// "now" as an argument: the aggregate judges time, it never reads the clock.
public class Event {

  public enum Status {
    DRAFT,
    PUBLISHED,
    CANCELLED
  }

  private final UUID id;
  private final ZoneId venueZone;
  private final Instant salesStart;
  private final List<PriceTier> tiers = new ArrayList<>();
  // Facts recorded by successful intention methods, waiting to be pulled.
  // The domain only writes these notes; it never sends them anywhere itself.
  private final List<Object> domainEvents = new ArrayList<>();
  private String title;
  private DateRange dates;
  private Status status;
  private Reason cancellationReason;

  // Creation: a named, validating front door for brand-new drafts.
  public static Event draft(
      final UUID id,
      final String title,
      final ZoneId venueZone,
      final DateRange dates,
      final Instant salesStart) {
    return new Event(id, title, venueZone, dates, salesStart);
  }

  // Reconstruction: the persistence adapter rebuilds a stored Event in any
  // state. Transition rules (like "sales start must be in the future to
  // publish") are NOT re-run here - they guarded the change when it happened;
  // structural rules (nulls, duplicate tiers, cancelled-needs-reason) are.
  public static Builder builder() {
    return new Builder();
  }

  private Event(
      final UUID id,
      final String title,
      final ZoneId venueZone,
      final DateRange dates,
      final Instant salesStart) {
    if (id == null) {
      throw new IllegalArgumentException("id must not be null");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (venueZone == null) {
      throw new IllegalArgumentException("venueZone must not be null");
    }
    if (dates == null) {
      throw new IllegalArgumentException("dates must not be null");
    }
    if (salesStart == null) {
      throw new IllegalArgumentException("salesStart must not be null");
    }
    this.id = id;
    this.title = title;
    this.venueZone = venueZone;
    this.dates = dates;
    this.salesStart = salesStart;
    this.status = Status.DRAFT;
  }

  private Event(final Builder builder) {
    this(builder.id, builder.title, builder.venueZone, builder.dates, builder.salesStart);
    if (builder.status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
    boolean cancelled = builder.status == Status.CANCELLED;
    if (cancelled != (builder.cancellationReason != null)) {
      throw new IllegalArgumentException(
          "a cancelled event has a reason, and only a cancelled event may have one");
    }
    for (PriceTier tier : builder.tiers) {
      storeTier(tier);
    }
    this.status = builder.status;
    this.cancellationReason = builder.cancellationReason;
    // Rebuilding is not a change: tiers are stored without recording notes,
    // and this wipe guards that promise if a future builder step ever records.
    this.domainEvents.clear();
  }

  public static final class Builder {

    private UUID id;
    private String title;
    private ZoneId venueZone;
    private DateRange dates;
    private Instant salesStart;
    private Status status;
    private final List<PriceTier> tiers = new ArrayList<>();
    private Reason cancellationReason;

    public Builder id(final UUID value) {
      this.id = value;
      return this;
    }

    public Builder title(final String value) {
      this.title = value;
      return this;
    }

    public Builder venueZone(final ZoneId value) {
      this.venueZone = value;
      return this;
    }

    public Builder dates(final DateRange value) {
      this.dates = value;
      return this;
    }

    public Builder salesStart(final Instant value) {
      this.salesStart = value;
      return this;
    }

    public Builder status(final Status value) {
      this.status = value;
      return this;
    }

    public Builder addTier(final PriceTier value) {
      this.tiers.add(value);
      return this;
    }

    public Builder cancellationReason(final Reason value) {
      this.cancellationReason = value;
      return this;
    }

    public Event build() {
      return new Event(this);
    }
  }

  // Invariants: only a draft can be published; needs at least one price tier
  // and a sales start still in the future - judged against the "now" the
  // caller supplies, so the rule is testable at any chosen moment.
  public void publish(final Instant now) {
    requireNow(now);
    refuseIfCancelled();
    if (status == Status.PUBLISHED) {
      throw new EventAlreadyPublishedException(id);
    }
    if (tiers.isEmpty()) {
      throw new MissingPriceTierException(id);
    }
    if (!salesStart.isAfter(now)) {
      throw new SalesStartInPastException(id, salesStart);
    }
    this.status = Status.PUBLISHED;
    domainEvents.add(new EventPublished(id, now));
  }

  // Invariant: no two tiers of the same event share a tier code.
  public void addPriceTier(final PriceTier tier, final Instant now) {
    requireNow(now);
    refuseIfCancelled();
    storeTier(tier);
    domainEvents.add(new PriceTierChanged(id, now, tier.code(), tier.price()));
  }

  public void reschedule(final DateRange newDates, final Instant now) {
    if (newDates == null) {
      throw new IllegalArgumentException("newDates must not be null");
    }
    requireNow(now);
    refuseIfCancelled();
    this.dates = newDates;
    domainEvents.add(new EventRescheduled(id, now, new EventSchedule(newDates.start(), venueZone)));
  }

  // Invariant: an event that has been cancelled stays cancelled.
  public void cancel(final Reason reason) {
    if (reason == null) {
      throw new IllegalArgumentException("reason must not be null");
    }
    refuseIfCancelled();
    this.status = Status.CANCELLED;
    this.cancellationReason = reason;
  }

  public void rename(final String newTitle) {
    if (newTitle == null || newTitle.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    refuseIfCancelled();
    this.title = newTitle;
  }

  // Hands out the recorded facts and wipes the list in the same move, so the
  // same fact can never be handed out twice. Someone outside the domain does
  // the actual telling; this class has no idea how messages are sent.
  public List<Object> pullDomainEvents() {
    List<Object> pulled = List.copyOf(domainEvents);
    domainEvents.clear();
    return pulled;
  }

  private void refuseIfCancelled() {
    if (status == Status.CANCELLED) {
      throw new EventAlreadyCancelledException(id);
    }
  }

  private static void requireNow(final Instant now) {
    if (now == null) {
      throw new IllegalArgumentException("now must not be null");
    }
  }

  // Validation and storage shared by addPriceTier and the Builder: enforces
  // the duplicate-code invariant and stores the tier, recording nothing.
  private void storeTier(final PriceTier tier) {
    if (tier == null) {
      throw new IllegalArgumentException("tier must not be null");
    }
    boolean codeTaken = tiers.stream().anyMatch(existing -> existing.code().equals(tier.code()));
    if (codeTaken) {
      throw new DuplicateTierCodeException(id, tier.code());
    }
    tiers.add(tier);
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

  public ZoneId venueZone() {
    return venueZone;
  }

  public DateRange dates() {
    return dates;
  }

  public Instant salesStart() {
    return salesStart;
  }

  public List<PriceTier> tiers() {
    return List.copyOf(tiers);
  }

  public Reason cancellationReason() {
    return cancellationReason;
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
