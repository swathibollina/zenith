# Aggregates and their invariants

An aggregate is a small family of objects that is always saved together in one
all-or-nothing database commit. An invariant is a rule about that family that
must be true every single time the family is saved.

Rule for this document: every invariant is one sentence that a save could check
by reading the stored data. "Allocated never exceeds capacity" passes the test.
"Customers are happy" does not, because happiness is not stored data.

## Catalog context

### Event
(contains its price tiers)

- An event cannot be published unless it has at least one price tier.
- An event cannot be published unless its sales start is in the future.
- No two price tiers of the same event share the same tier code.
- An event that has been cancelled stays cancelled.

### SeatMap

- Within one section, every seat label is unique.
- Every seat belongs to exactly one section.

## Inventory context

### SeatInventoryBlock

- Allocated never exceeds capacity.
- Neither allocated nor capacity is ever negative.

### SeatHold

- A hold always has an expiry time.
- A hold covers at least one seat.
- A hold is for exactly one event.

## Orders context

### Order
(contains its lines)

- An order has at least one line.
- Every line says which event, which tier, and how many seats - and how many
  is at least one.
- The order's stored total equals the sum of its own lines.
- An order's status only moves forward: placed can become confirmed or failed,
  and neither confirmed nor failed ever changes again.

## Payments context (lives inside order-service for now)

### Payment

- A payment belongs to exactly one order, referenced by id.
- One payment never captures money more than once.
- A captured payment always records the exact amount and currency captured.

## Access control context (lives inside inventory-service for now)

### Ticket

- A ticket points at the order that bought it, referenced by id.
- A ticket is used at most once, and the moment of use is recorded.

## Contexts with no rich aggregate

Identity, Media, Notifications, Realtime, and Reporting mostly store or move
data without business rules of this kind. If one of them grows a real rule
later, it earns a section here.

## Boundary decisions — what must commit together

The only question that draws a boundary: must these facts become true at the
same instant, in one all-or-nothing commit? "They feel related" and "they
share a noun" decide nothing.

- Event | PriceTier — **same aggregate.** "Cannot publish without a tier" and
  "no two tiers share a code" compare the event's status and its tiers in the
  same instant, so the family must be saved as one.
- Event | SeatMap — separate. A show being published and a hall's seat labels
  being corrected never need one commit; one seat map also serves many events
  at the same venue.
- Event | SeatInventoryBlock — separate; full paragraph below.
- SeatInventoryBlock | SeatHold — **one consistency boundary.** "One seat, one
  buyer" means granting a hold and the seat's taken-state must be decided in
  the same instant, so both live in Inventory. The exact internal shape is
  measured and chosen in Phase 5 (Task 5.1).
- Order | Payment — separate. An order may honestly sit at "placed, awaiting
  payment" while the charge is attempted; the gap is designed. Payment points
  at its order by id.
- Order | SeatHold — separate. The hold itself protects the gap between
  placing the order and allocating the seats.
- Order | Ticket — separate. A confirmed order and its ticket may be moments
  apart; nobody is harmed by the gap.

### Why Event and SeatInventoryBlock are different aggregates

Publishing an event's description and allocating one of its seats never need
to commit in the same instant: Raj publishes weeks before Priya buys, and
"the words on the show page changed" never has to be true at the same moment
as "seat A12 is taken". If they shared one aggregate, every seat purchase
would have to lock the entire event — on sale day, thousands of buyers of
unrelated seats would stand in a single-file line behind one lock. Kept
separate, each lock stays tiny: buying A12 waits only on A12's block, never
on the show's description and never on seat Z999. This argument uses only
"what must commit together" — no technology, no feelings of relatedness.
(The one-big-aggregate mistake becomes a measured throughput problem in
Task 5.1.)

## Aggregate roots — one door per family

The root is the only object outside code may hold or call; everything inside
is reached through it. Across families, only ids travel.

| Aggregate | Root | Inside the family | Points at others by id |
| --- | --- | --- | --- |
| Event | Event | its PriceTiers | organizerId, venueId, seatMapId |
| SeatMap | SeatMap | its sections and seat labels | venueId |
| SeatInventoryBlock | SeatInventoryBlock | its capacity/allocated counts | eventId |
| SeatHold | SeatHold | the list of held seats | eventId, customerId |
| Order | Order | its OrderLines | customerId; eventId and tierCode per line |
| Payment | Payment | its capture/refund entries | orderId |
| Ticket | Ticket | (single object) | orderId, eventId |

## Cross-aggregate rules — eventual consistency, saga in Phase 6

Rules whose facts live in different aggregates. No single commit can guarantee
them, so the other side catches up shortly after; Phase 6 builds the saga (a
sequence of local transactions with compensating undo steps) that coordinates
them.

1. An order's total matches the sum of the current tier prices (Order +
   Event). A price may change mid-checkout; checkout re-verifies, and the gap
   is chased, never assumed away.
2. A confirmed order always has a captured payment (Order + Payment). For a
   few seconds after the charge they may disagree; on failure the undo step
   fails the order shortly after.
3. The browse page's "seats remaining" equals the true count (Catalog's copy +
   SeatInventoryBlock). Allowed to lag by design; corrected on the next
   announcement.