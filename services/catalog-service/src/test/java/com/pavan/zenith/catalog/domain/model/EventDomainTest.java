package com.pavan.zenith.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pavan.zenith.catalog.domain.event.EventPublished;
import com.pavan.zenith.catalog.domain.event.EventRescheduled;
import com.pavan.zenith.catalog.domain.event.PriceTierChanged;
import com.pavan.zenith.catalog.domain.exception.DomainException;
import com.pavan.zenith.catalog.domain.exception.DuplicateTierCodeException;
import com.pavan.zenith.catalog.domain.exception.EventAlreadyCancelledException;
import com.pavan.zenith.catalog.domain.exception.EventAlreadyPublishedException;
import com.pavan.zenith.catalog.domain.exception.MissingPriceTierException;
import com.pavan.zenith.catalog.domain.exception.SalesStartInPastException;
import com.pavan.zenith.common.values.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

// The domain suite for the Event aggregate: plain JUnit, no Spring, no
// database. Every invariant sentence in docs/architecture/aggregates.md has a
// failing-case test here. Named *DomainTest so it can be run alone and fast:
// mvn -pl services/catalog-service test -Dtest='*DomainTest'
class EventDomainTest {

  private static final UUID ID = UUID.fromString("7e57d004-2b97-0e7a-b45f-5387367791cd");
  private static final UUID OTHER_ID = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
  private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
  private static final PriceTier GOLD =
      new PriceTier(new TierCode("GOLD"), new Money(new BigDecimal("2000.00"), "INR"));

  private static Event draftEvent(final UUID id) {
    Instant showStart = Instant.now().plus(30, ChronoUnit.DAYS);
    return Event.draft(
        id,
        "Jazz Night",
        KOLKATA,
        new DateRange(showStart, showStart.plus(3, ChronoUnit.HOURS)),
        Instant.now().plus(7, ChronoUnit.DAYS));
  }

  private static Event publishable(final UUID id) {
    Event event = draftEvent(id);
    event.addPriceTier(GOLD);
    return event;
  }

  @Test
  void sameIdMeansSameEventEvenWithDifferentTitles() {
    Event first = draftEvent(ID);
    Event second = draftEvent(ID);
    second.rename("Different Title");
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void differentIdMeansDifferentEventEvenWithSameTitle() {
    assertNotEquals(draftEvent(ID), draftEvent(OTHER_ID));
  }

  @Test
  void draftRefusesNullsAndBlankTitle() {
    Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
    DateRange dates = new DateRange(start, start.plus(2, ChronoUnit.HOURS));
    Instant sales = Instant.now().plus(1, ChronoUnit.HOURS);
    assertThrows(
        IllegalArgumentException.class, () -> Event.draft(null, "T", KOLKATA, dates, sales));
    assertThrows(IllegalArgumentException.class, () -> Event.draft(ID, " ", KOLKATA, dates, sales));
    assertThrows(IllegalArgumentException.class, () -> Event.draft(ID, "T", null, dates, sales));
    assertThrows(IllegalArgumentException.class, () -> Event.draft(ID, "T", KOLKATA, null, sales));
    assertThrows(IllegalArgumentException.class, () -> Event.draft(ID, "T", KOLKATA, dates, null));
  }

  @Test
  void publishRefusesWithoutAnyPriceTier() {
    Event event = draftEvent(ID);
    assertThrows(MissingPriceTierException.class, event::publish);
  }

  @Test
  void publishRefusesWhenSalesStartIsNotInTheFuture() {
    Instant showStart = Instant.now().plus(30, ChronoUnit.DAYS);
    Event event =
        Event.draft(
            ID,
            "Jazz Night",
            KOLKATA,
            new DateRange(showStart, showStart.plus(3, ChronoUnit.HOURS)),
            Instant.now().minus(1, ChronoUnit.HOURS));
    event.addPriceTier(GOLD);
    assertThrows(SalesStartInPastException.class, event::publish);
  }

  @Test
  void publishMovesAWellFormedDraftToPublished() {
    Event event = publishable(ID);
    event.publish();
    assertEquals(Event.Status.PUBLISHED, event.status());
  }

  @Test
  void publishRefusesASecondTime() {
    Event event = publishable(ID);
    event.publish();
    assertThrows(EventAlreadyPublishedException.class, event::publish);
  }

  @Test
  void addPriceTierRefusesADuplicateCode() {
    Event event = publishable(ID);
    PriceTier goldAgain =
        new PriceTier(new TierCode("GOLD"), new Money(new BigDecimal("1500.00"), "INR"));
    assertThrows(DuplicateTierCodeException.class, () -> event.addPriceTier(goldAgain));
  }

  @Test
  void rescheduleReplacesTheDates() {
    Event event = draftEvent(ID);
    Instant newStart = Instant.now().plus(60, ChronoUnit.DAYS);
    DateRange newDates = new DateRange(newStart, newStart.plus(3, ChronoUnit.HOURS));
    event.reschedule(newDates);
    assertEquals(newDates, event.dates());
  }

  @Test
  void cancelRecordsTheReasonAndFreezesTheEvent() {
    Event event = publishable(ID);
    event.publish();
    event.cancel(new Reason("venue flooded"));
    assertEquals(Event.Status.CANCELLED, event.status());
    assertEquals(new Reason("venue flooded"), event.cancellationReason());
    assertThrows(EventAlreadyCancelledException.class, event::publish);
    assertThrows(EventAlreadyCancelledException.class, () -> event.addPriceTier(GOLD));
    assertThrows(EventAlreadyCancelledException.class, () -> event.reschedule(event.dates()));
    assertThrows(EventAlreadyCancelledException.class, () -> event.cancel(new Reason("twice")));
    assertThrows(EventAlreadyCancelledException.class, () -> event.rename("New Name"));
  }

  @Test
  void intentionMethodsRefuseNullArguments() {
    Event event = draftEvent(ID);
    assertThrows(IllegalArgumentException.class, () -> event.addPriceTier(null));
    assertThrows(IllegalArgumentException.class, () -> event.reschedule(null));
    assertThrows(IllegalArgumentException.class, () -> event.cancel(null));
    assertThrows(IllegalArgumentException.class, () -> event.rename(" "));
  }

  @Test
  void tiersListCannotBeModifiedFromOutside() {
    Event event = publishable(ID);
    assertThrows(UnsupportedOperationException.class, () -> event.tiers().add(GOLD));
  }

  @Test
  void draftStartsInDraftStateWithNoTiers() {
    Event event = draftEvent(ID);
    assertEquals(Event.Status.DRAFT, event.status());
    assertEquals(0, event.tiers().size());
  }

  @Test
  void builderRebuildsAPublishedEventEvenAfterSalesStarted() {
    Instant pastSalesStart = Instant.now().minus(2, ChronoUnit.DAYS);
    Instant showStart = Instant.now().plus(10, ChronoUnit.DAYS);
    Event rebuilt =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(new DateRange(showStart, showStart.plus(3, ChronoUnit.HOURS)))
            .salesStart(pastSalesStart)
            .status(Event.Status.PUBLISHED)
            .addTier(GOLD)
            .build();
    assertEquals(Event.Status.PUBLISHED, rebuilt.status());
    assertEquals(1, rebuilt.tiers().size());
  }

  @Test
  void builderRefusesAMissingStatus() {
    Instant showStart = Instant.now().plus(10, ChronoUnit.DAYS);
    Event.Builder builder =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(new DateRange(showStart, showStart.plus(3, ChronoUnit.HOURS)))
            .salesStart(Instant.now());
    assertThrows(IllegalArgumentException.class, builder::build);
  }

  @Test
  void builderRefusesCancelledWithoutReasonAndReasonWithoutCancelled() {
    Instant showStart = Instant.now().plus(10, ChronoUnit.DAYS);
    DateRange dates = new DateRange(showStart, showStart.plus(3, ChronoUnit.HOURS));
    Event.Builder cancelledNoReason =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(dates)
            .salesStart(Instant.now())
            .status(Event.Status.CANCELLED);
    assertThrows(IllegalArgumentException.class, cancelledNoReason::build);
    Event.Builder reasonNotCancelled =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(dates)
            .salesStart(Instant.now())
            .status(Event.Status.DRAFT)
            .cancellationReason(new Reason("oops"));
    assertThrows(IllegalArgumentException.class, reasonNotCancelled::build);
  }

  @Test
  void publishRecordsExactlyOneEventPublishedAndPullingAgainReturnsEmpty() {
    Event event = publishable(ID);
    event.pullDomainEvents(); // drain the note recorded while adding the setup tier
    event.publish();
    List<Object> pulled = event.pullDomainEvents();
    assertEquals(1, pulled.size());
    EventPublished published = assertInstanceOf(EventPublished.class, pulled.get(0));
    assertEquals(ID, published.eventId());
    assertEquals(0, event.pullDomainEvents().size());
  }

  @Test
  void addPriceTierRecordsAPriceTierChangedCarryingCodeAndPrice() {
    Event event = draftEvent(ID);
    event.addPriceTier(GOLD);
    List<Object> pulled = event.pullDomainEvents();
    assertEquals(1, pulled.size());
    PriceTierChanged changed = assertInstanceOf(PriceTierChanged.class, pulled.get(0));
    assertEquals(ID, changed.eventId());
    assertEquals(new TierCode("GOLD"), changed.tier());
    assertEquals(GOLD.price(), changed.newPrice());
  }

  @Test
  void rescheduleRecordsAnEventRescheduledCarryingTheNewSchedule() {
    Event event = draftEvent(ID);
    Instant newStart = Instant.now().plus(90, ChronoUnit.DAYS);
    event.reschedule(new DateRange(newStart, newStart.plus(3, ChronoUnit.HOURS)));
    List<Object> pulled = event.pullDomainEvents();
    assertEquals(1, pulled.size());
    EventRescheduled rescheduled = assertInstanceOf(EventRescheduled.class, pulled.get(0));
    assertEquals(ID, rescheduled.eventId());
    assertEquals(newStart, rescheduled.newSchedule().start());
    assertEquals(KOLKATA, rescheduled.newSchedule().venueZone());
  }

  @Test
  void aRefusedPublishRecordsNothing() {
    Event event = draftEvent(ID);
    assertThrows(MissingPriceTierException.class, event::publish);
    assertEquals(0, event.pullDomainEvents().size());
  }

  @Test
  void rebuildingThroughTheBuilderRecordsNothing() {
    Instant showStart = Instant.now().plus(10, ChronoUnit.DAYS);
    Event rebuilt =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(new DateRange(showStart, showStart.plus(3, ChronoUnit.HOURS)))
            .salesStart(Instant.now().minus(2, ChronoUnit.DAYS))
            .status(Event.Status.PUBLISHED)
            .addTier(GOLD)
            .build();
    assertEquals(0, rebuilt.pullDomainEvents().size());
  }

  @Test
  void builderRefusesDuplicateTierCodes() {
    Instant showStart = Instant.now().plus(10, ChronoUnit.DAYS);
    Event.Builder builder =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(new DateRange(showStart, showStart.plus(3, ChronoUnit.HOURS)))
            .salesStart(Instant.now())
            .status(Event.Status.DRAFT)
            .addTier(GOLD)
            .addTier(new PriceTier(new TierCode("GOLD"), new Money(new BigDecimal("1.00"), "INR")));
    assertThrows(DuplicateTierCodeException.class, builder::build);
  }

  @Test
  void everyBusinessRuleRefusalCanBeCaughtAsADomainException() {
    Event draft = draftEvent(ID);
    assertThrows(DomainException.class, draft::publish); // missing price tier
    Event published = publishable(OTHER_ID);
    published.publish();
    assertThrows(DomainException.class, published::publish); // already published
    published.cancel(new Reason("venue flooded"));
    assertThrows(
        DomainException.class, () -> published.rename("New Name")); // cancelled stays cancelled
  }
}
