## The time rule

**Store `Instant` + IANA zone id; render `ZonedDateTime`; `LocalDateTime` is
almost always the wrong type for a stored timestamp.**

Why: "2026-03-29 02:30" does not even exist in zones that skip it on
spring-forward night - a stored wall-clock reading can be a time that never
happened, while a stored `Instant` is always a real moment.

## Domain events vs integration events

A domain event is an internal fact ("EventPublished"), seen only inside its
own service, so its shape may be reshaped freely in any refactor. An
integration event is the version published to other services; the moment
another team consumes it, its shape is a contract that cannot be changed
alone — so it is deliberately a separate class, translated from the internal
fact at the border, even on the day the two shapes happen to look identical.