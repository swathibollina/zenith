# Design pattern register

One row per pattern, filled in only when a real class starts using it. No row
is written from theory alone.

| Pattern | Where used | What problem it solved |
| --- | --- | --- |
| Static Factory | `Event.draft(...)` in catalog-service | A named, validating front door for new drafts; unlike `new`, it can refuse to return an object and its name says what it creates. |
| Builder | `Event.Builder` in catalog-service | Reconstruction from storage needs eight fields; eight positional constructor arguments invite silent swaps, named builder steps do not. |