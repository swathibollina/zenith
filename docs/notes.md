# Engineering notes

Running answers to "ask AI" prompts and design questions, tied to this
codebase. One dated entry per question.

## 2026-11-28 - When is Strategy over-engineering? (Task 1.5, Step 6)

Question asked: when is a Strategy pattern over-engineering compared to a
simple switch or a map of lambdas?

The three honest designs for picking a pricing behaviour:

1. A `switch` inside one method - zero ceremony, all logic in one place.
2. A map of lambdas - `Map<PricingRuleType, Function<PricingContext, Money>>`:
   look the function up by type, call it, no classes at all.
3. A Strategy hierarchy - what catalog-service has now.

Strategy earns its keep only when at least one of these is true:

- **Variants carry state.** FlatPricing holds its configured Money;
  TieredPricing holds a validated, defensively copied map with a price for
  each tier. Constructors with guards need classes; a lambda cannot refuse
  bad configuration at creation time.
- **Variants share steps.** The currency check, rounding and floor live once
  in AbstractPricingRule. A switch or a lambda map has no home for shared
  steps except "every branch must remember to call the helper".
- **The set is expected to grow.** A new rule is a new class plus one enum
  value and one factory line; nothing existing is reopened, and tests grow
  one file per rule instead of multiplying against old branches.
- **Variants deserve isolated tests.** Each rule has its own DomainTest file,
  so a failure names the guilty class.

All four are true in catalog-service, so the hierarchy is justified.

Where it would be over-engineering: two or three stateless one-liners with no
shared steps and no growth expected - say, if pricing were only "listed price"
versus "listed price times two". Then a switch inside one method is the honest
design, and five files of hierarchy would be ceremony. The factory keeping a
switch is the reminder: switch was never the disease; scattered, duplicated
switches were.

One-line criterion to say out loud: state, shared steps, growth, isolated
tests - no tick, no hierarchy.