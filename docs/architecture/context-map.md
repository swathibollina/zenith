# Context map — zenith's bounded contexts and how they talk

Boxes are bounded contexts (from the event storm). Arrows point the way requests
or announcements travel. Every arrow carries exactly one relationship kind:

- **shared-nothing + events** — no shared database, no shared classes; one side
  announces facts, the other listens.
- **customer/supplier** — the consuming side's needs genuinely shape what the
  supplying side provides.
- **anti-corruption layer** — a translation wall: their model is converted into
  ours at the border, so their vocabulary never leaks in.

```mermaid
graph LR
  Identity -- "customer/supplier" --> Catalog
  Catalog -- "customer/supplier" --> Inventory
  Media -- "customer/supplier" --> Catalog
  Orders -- "customer/supplier" --> Inventory
  Orders -- "shared-nothing + events" --> Payments
  Payments -- "shared-nothing + events" --> Orders
  Orders -- "shared-nothing + events" --> Media
  Orders -- "shared-nothing + events" --> Notifications
  Orders -- "shared-nothing + events" --> Realtime
  Orders -- "shared-nothing + events" --> Reporting
  Orders -- "shared-nothing + events" --> AccessControl["Access control"]
  Inventory -- "shared-nothing + events" --> Catalog
  Inventory -- "shared-nothing + events" --> Realtime
  Catalog -- "shared-nothing + events" --> Notifications
  AccessControl -- "shared-nothing + events" --> Reporting
  Payments -- "anti-corruption layer" --> CardProvider["Card provider (external)"]