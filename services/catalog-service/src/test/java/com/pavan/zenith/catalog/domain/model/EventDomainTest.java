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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

// The domain suite for the Event aggregate: plain JUnit, no Spring, no
// database - and since Task 1.6 Step 4, no wall clock either. Every moment
// in this file is a chosen constant, so every run is identical. Named
// *DomainTest so it can be run alone and fast:
// mvn -pl services/catalog-service test -Dtest='*DomainTest'
class EventDomainTest {

  private static final UUID ID = UUID.fromString("7e57d004-2b97-0e7a-b45f-5387367791cd");
  private static final UUID OTHER_ID = UUID.fromString("16fd2706-8baf-433b-82eb-8c7fada847da");
  private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
  private static final PriceTier GOLD =
      new PriceTier(new TierCode("GOLD"), new Money(new BigDecimal("2000.00"), "INR"));

  // The one "current moment" of this whole suite - a constant, not a reading.
  private static final Instant NOW = Instant.parse("2026-11-28T12:00:00Z");
  private static final Instant SALES_START = NOW.plus(7, ChronoUnit.DAYS);
  private static final Instant SHOW_START = NOW.plus(30, ChronoUnit.DAYS);

  private static Event draftEvent(final UUID id) {
    return Event.draft(
        id,
        "Jazz Night",
        KOLKATA,
        new DateRange(SHOW_START, SHOW_START.plus(3, ChronoUnit.HOURS)),
        SALES_START);
  }

  private static Event publishable(final UUID id) {
    Event event = draftEvent(id);
    event.addPriceTier(GOLD, NOW);
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
    DateRange dates = new DateRange(SHOW_START, SHOW_START.plus(2, ChronoUnit.HOURS));
    assertThrows(
        IllegalArgumentException.class, () -> Event.draft(null, "T", KOLKATA, dates, SALES_START));
    assertThrows(
        IllegalArgumentException.class, () -> Event.draft(ID, " ", KOLKATA, dates, SALES_START));
    assertThrows(
        IllegalArgumentException.class, () -> Event.draft(ID, "T", null, dates, SALES_START));
    assertThrows(
        IllegalArgumentException.class, () -> Event.draft(ID, "T", KOLKATA, null, SALES_START));
    assertThrows(IllegalArgumentException.class, () -> Event.draft(ID, "T", KOLKATA, dates, null));
  }

  @Test
  void publishRefusesWithoutAnyPriceTier() {
    Event event = draftEvent(ID);
    assertThrows(MissingPriceTierException.class, () -> event.publish(NOW));
  }

  @Test
  void publishRefusesWhenSalesStartIsInThePast() {
    Event event = publishable(ID);
    Instant afterSalesStart = SALES_START.plus(1, ChronoUnit.HOURS);
    assertThrows(SalesStartInPastException.class, () -> event.publish(afterSalesStart));
  }

  @Test
  void publishRefusesWhenSalesStartIsExactlyNow() {
    // The boundary moment: impossible to pin down with a real clock,
    // trivial with an injected one.
    Event event = publishable(ID);
    assertThrows(SalesStartInPastException.class, () -> event.publish(SALES_START));
  }

  @Test
  void publishMovesAWellFormedDraftToPublished() {
    Event event = publishable(ID);
    event.publish(NOW);
    assertEquals(Event.Status.PUBLISHED, event.status());
  }

  @Test
  void publishRefusesASecondTime() {
    Event event = publishable(ID);
    event.publish(NOW);
    assertThrows(EventAlreadyPublishedException.class, () -> event.publish(NOW));
  }

  @Test
  void publishRecordsTheExactMomentTheClockShows() {
    // THE Step-4 proof: with Clock.fixed, the recorded timestamp can be
    // asserted exactly. With a real clock this assertion would be flaky.
    Clock clock = Clock.fixed(Instant.parse("2026-11-28T18:30:00Z"), ZoneOffset.UTC);
    Event event = publishable(ID);
    event.pullDomainEvents(); // drain the note recorded while adding the setup tier

    event.publish(Instant.now(clock));

    EventPublished fact = assertInstanceOf(EventPublished.class, event.pullDomainEvents().get(0));
    assertEquals(Instant.parse("2026-11-28T18:30:00Z"), fact.occurredAt());
  }

  @Test
  void addPriceTierRefusesADuplicateCode() {
    Event event = publishable(ID);
    PriceTier goldAgain =
        new PriceTier(new TierCode("GOLD"), new Money(new BigDecimal("1500.00"), "INR"));
    assertThrows(DuplicateTierCodeException.class, () -> event.addPriceTier(goldAgain, NOW));
  }

  @Test
  void rescheduleReplacesTheDates() {
    Event event = draftEvent(ID);
    Instant newStart = NOW.plus(60, ChronoUnit.DAYS);
    DateRange newDates = new DateRange(newStart, newStart.plus(3, ChronoUnit.HOURS));
    event.reschedule(newDates, NOW);
    assertEquals(newDates, event.dates());
  }

  @Test
  void cancelRecordsTheReasonAndFreezesTheEvent() {
    Event event = publishable(ID);
    event.publish(NOW);
    event.cancel(new Reason("venue flooded"));
    assertEquals(Event.Status.CANCELLED, event.status());
    assertEquals(new Reason("venue flooded"), event.cancellationReason());
    assertThrows(EventAlreadyCancelledException.class, () -> event.publish(NOW));
    assertThrows(EventAlreadyCancelledException.class, () -> event.addPriceTier(GOLD, NOW));
    assertThrows(EventAlreadyCancelledException.class, () -> event.reschedule(event.dates(), NOW));
    assertThrows(EventAlreadyCancelledException.class, () -> event.cancel(new Reason("twice")));
    assertThrows(EventAlreadyCancelledException.class, () -> event.rename("New Name"));
  }

  @Test
  void intentionMethodsRefuseNullArguments() {
    Event event = draftEvent(ID);
    assertThrows(IllegalArgumentException.class, () -> event.addPriceTier(null, NOW));
    assertThrows(IllegalArgumentException.class, () -> event.reschedule(null, NOW));
    assertThrows(IllegalArgumentException.class, () -> event.cancel(null));
    assertThrows(IllegalArgumentException.class, () -> event.rename(" "));
  }

  @Test
  void timeSensitiveMethodsRefuseANullNow() {
    Event event = publishable(ID);
    assertThrows(IllegalArgumentException.class, () -> event.publish(null));
    assertThrows(IllegalArgumentException.class, () -> event.addPriceTier(GOLD, null));
    assertThrows(IllegalArgumentException.class, () -> event.reschedule(event.dates(), null));
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
    Instant pastSalesStart = NOW.minus(2, ChronoUnit.DAYS);
    Event rebuilt =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(new DateRange(SHOW_START, SHOW_START.plus(3, ChronoUnit.HOURS)))
            .salesStart(pastSalesStart)
            .status(Event.Status.PUBLISHED)
            .addTier(GOLD)
            .build();
    assertEquals(Event.Status.PUBLISHED, rebuilt.status());
    assertEquals(1, rebuilt.tiers().size());
  }

  @Test
  void builderRefusesAMissingStatus() {
    Event.Builder builder =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(new DateRange(SHOW_START, SHOW_START.plus(3, ChronoUnit.HOURS)))
            .salesStart(SALES_START);
    assertThrows(IllegalArgumentException.class, builder::build);
  }

  @Test
  void builderRefusesCancelledWithoutReasonAndReasonWithoutCancelled() {
    DateRange dates = new DateRange(SHOW_START, SHOW_START.plus(3, ChronoUnit.HOURS));
    Event.Builder cancelledNoReason =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(dates)
            .salesStart(SALES_START)
            .status(Event.Status.CANCELLED);
    assertThrows(IllegalArgumentException.class, cancelledNoReason::build);
    Event.Builder reasonNotCancelled =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(dates)
            .salesStart(SALES_START)
            .status(Event.Status.DRAFT)
            .cancellationReason(new Reason("oops"));
    assertThrows(IllegalArgumentException.class, reasonNotCancelled::build);
  }

  @Test
  void publishRecordsExactlyOneEventPublishedAndPullingAgainReturnsEmpty() {
    Event event = publishable(ID);
    event.pullDomainEvents(); // drain the note recorded while adding the setup tier
    event.publish(NOW);
    List<Object> pulled = event.pullDomainEvents();
    assertEquals(1, pulled.size());
    EventPublished published = assertInstanceOf(EventPublished.class, pulled.get(0));
    assertEquals(ID, published.eventId());
    assertEquals(NOW, published.occurredAt());
    assertEquals(0, event.pullDomainEvents().size());
  }

  @Test
  void addPriceTierRecordsAPriceTierChangedCarryingCodeAndPrice() {
    Event event = draftEvent(ID);
    event.addPriceTier(GOLD, NOW);
    List<Object> pulled = event.pullDomainEvents();
    assertEquals(1, pulled.size());
    PriceTierChanged changed = assertInstanceOf(PriceTierChanged.class, pulled.get(0));
    assertEquals(ID, changed.eventId());
    assertEquals(NOW, changed.occurredAt());
    assertEquals(new TierCode("GOLD"), changed.tier());
    assertEquals(GOLD.price(), changed.newPrice());
  }

  @Test
  void rescheduleRecordsAnEventRescheduledCarryingTheNewSchedule() {
    Event event = draftEvent(ID);
    Instant newStart = NOW.plus(90, ChronoUnit.DAYS);
    event.reschedule(new DateRange(newStart, newStart.plus(3, ChronoUnit.HOURS)), NOW);
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
    assertThrows(MissingPriceTierException.class, () -> event.publish(NOW));
    assertEquals(0, event.pullDomainEvents().size());
  }

  @Test
  void rebuildingThroughTheBuilderRecordsNothing() {
    Event rebuilt =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(new DateRange(SHOW_START, SHOW_START.plus(3, ChronoUnit.HOURS)))
            .salesStart(NOW.minus(2, ChronoUnit.DAYS))
            .status(Event.Status.PUBLISHED)
            .addTier(GOLD)
            .build();
    assertEquals(0, rebuilt.pullDomainEvents().size());
  }

  @Test
  void builderRefusesDuplicateTierCodes() {
    Event.Builder builder =
        Event.builder()
            .id(ID)
            .title("Jazz Night")
            .venueZone(KOLKATA)
            .dates(new DateRange(SHOW_START, SHOW_START.plus(3, ChronoUnit.HOURS)))
            .salesStart(SALES_START)
            .status(Event.Status.DRAFT)
            .addTier(GOLD)
            .addTier(new PriceTier(new TierCode("GOLD"), new Money(new BigDecimal("1.00"), "INR")));
    assertThrows(DuplicateTierCodeException.class, builder::build);
  }

  @Test
  void everyBusinessRuleRefusalCanBeCaughtAsADomainException() {
    Event draft = draftEvent(ID);
    assertThrows(DomainException.class, () -> draft.publish(NOW)); // missing price tier
    Event published = publishable(OTHER_ID);
    published.publish(NOW);
    assertThrows(DomainException.class, () -> published.publish(NOW)); // already published
    published.cancel(new Reason("venue flooded"));
    assertThrows(
        DomainException.class, () -> published.rename("New Name")); // cancelled stays cancelled
  }
}
