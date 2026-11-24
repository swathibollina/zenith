# ADR-0005: Aggregate boundaries and consistency model

## Context

Zenith sells seats to live events. The domain model needs units of change:
which objects are saved together in one all-or-nothing commit, and which may
drift apart for a moment. An aggregate is both the unit of consistency and
the unit of locking, so this choice decides both correctness under concurrent
buyers and throughput on sale day.

## Options considered

1. **One big Event aggregate** owning description, tiers, seat map, and all
   seats. Every rule checkable in one commit — but every seat purchase locks
   the entire event; on sale day thousands of buyers of unrelated seats queue
   behind one lock. Rejected: lock contention no hardware can fix (measured
   in Task 5.1).
2. **Tiny aggregates everywhere**, even an order's total split from its
   lines. Maximum parallelism — but rules like "total equals sum of lines"
   lose their guard and break silently. Rejected: corruption you cannot
   detect.
3. **Aggregates exactly as big as their strongest invariant** (chosen):
   Event (with its PriceTiers), SeatMap, SeatInventoryBlock, SeatHold,
   Order (with its OrderLines), Payment, Ticket.

## Decision

Strong consistency inside an aggregate, eventual consistency across
aggregates. One transaction changes one aggregate. Aggregates reference each
other by id only. Rules spanning aggregates (listed in aggregates.md) are
coordinated by sagas with compensating steps, built in Phase 6.

## Consequences

- Good: purchases of unrelated seats run in parallel; sale day scales.
- Good: every invariant has exactly one guard, enforced at one door (the
  root).
- Bad, accepted: user-visible staleness — a browse page may show a seat as
  available moments after someone bought it, corrected on the next update.
- Bad, accepted: an order and its payment may disagree for seconds; a
  reconciling check chases mismatches instead of one simple transaction.
- Bad, accepted: cross-aggregate rules need sagas, retries, and nightly
  checks — real machinery arriving in Phase 6.