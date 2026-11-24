# Event storm — every business event in zenith's life

Rough time order. Past tense. No filtering, no grouping yet.

## Raj sets things up

1. OrganizerSignedUp — Raj created his login account on zenith.
2. OrganizationCreated — Raj registered his jazz club as a business on the platform.
3. OrganizerLoggedIn — Raj signed in and proved who he is.
4. VenueCreated — Raj added the hall where his shows happen.
5. EventCreated — Raj started a new show entry ("Jazz Night, 14 March"); still a private draft.
6. EventDescriptionEdited — Raj changed the wording of his draft show.
7. PriceTierDefined — Raj set a price level, like "front rows ₹2000, back rows ₹800".
8. PriceTierChanged — Raj later changed one of those prices.
9. SeatMapFileUploaded — Raj uploaded the spreadsheet listing every seat in the hall.
10. FileVirusScanPassed — the platform checked an uploaded file is safe; it was.
11. FileRejected — an uploaded file was refused (unsafe, or not what it claimed to be).
12. SeatMapImported — the platform finished reading the spreadsheet and now knows every seat.
13. SeatMapImportFailed — the spreadsheet could not be read (broken rows, wrong format).
14. PosterImageUploaded — Raj uploaded the show's poster picture.
15. PosterThumbnailGenerated — a small preview version of the poster was created for list pages.
16. EventPublished — Raj pressed Publish; the show became visible to the public.
17. SeatRecordsCreated — one record per seat (all 500) was created, so each seat can be sold exactly once.
18. EventDetailsUpdated — Raj edited the show after it was already public.
19. EventRescheduled — the show's date or time was moved.
20. TicketSalesOpened — the clock hit the announced sale minute; buying became allowed.

## Priya gets ready to buy

21. CustomerSignedUp — Priya created her account.
22. CustomerLoggedIn — Priya signed in.
23. PasswordResetRequested — Priya forgot her password and asked for a reset link.
24. PasswordResetCompleted — Priya set a new password.
25. AccountAccessRevoked — an account's access was cut off (for example, it was stolen).
26. WaitingRoomJoined — sale day is crowded; Priya entered the online queue.
27. QueuePositionAdvanced — her place in the queue moved up ("you are number 4,312… 4,311…").
28. CustomerAdmittedFromQueue — her turn came; she was let in to buy.

## The purchase

29. SeatHeld — the seats Priya picked were reserved for her for 10 minutes while she pays.
30. SeatHoldRejected — she asked for a seat someone else got first; told "sorry, gone".
31. HoldExpired — her 10 minutes ran out unpaid; the seats went back on sale by themselves.
32. OrderPlaced — Priya pressed Buy; her order now exists (money not yet taken).
33. PaymentAttempted — the platform started trying to take her money.
34. PaymentCaptured — money actually left her card.
35. PaymentFailed — the charge did not go through (the bank said no).
36. PaymentOutcomeUnknown — the call to the card company got no answer in time; we honestly
    do not know whether money moved. (Rule: find out the truth, never guess.)
37. PaymentReconciledWithProvider — we asked the card company "what actually happened?"
    and recorded the true answer.
38. PaymentWebhookReceived — the card company itself called us back later to report a result.
39. OrderConfirmed — payment succeeded; the order is final; tickets are on their way.
40. SeatAllocated — her held seats became permanently hers.
41. HoldReleased — a hold was deliberately undone early (payment failed, so the seats
    return to sale immediately instead of waiting out the 10 minutes).
42. OrderFailed — the purchase as a whole did not happen; everything already done for it was undone.
43. OrderCancelled — an existing order was called off on purpose.
44. RefundIssued — money was sent back to Priya.
45. PartialRefundIssued — part of the money was sent back, not all of it.

## Tickets and messages

46. TicketIssued — Priya's ticket now officially exists.
47. TicketPdfGenerated — the printable PDF with the QR code was created.
48. ConfirmationEmailSent — the "here are your tickets" email went out.
49. EmailDeliveryFailed — one attempt to deliver an email did not work.
50. NotificationRetryScheduled — the platform decided to try sending again later
    (waiting a little longer after each failure).
51. NotificationParkedForHumanReview — after too many failed tries, the message was set
    aside for a person to look at, instead of being silently thrown away.
52. DuplicateNotificationSuppressed — the platform noticed it had already sent this exact
    message and refused to send it twice.
53. EventSoldOut — the last seat was taken; nothing left to sell.

## At the venue door

54. TicketScanned — gate staff pointed the scanner at a ticket's QR code.
55. EntryGranted — the ticket was real and unused; the person walked in.
56. DuplicateScanRejected — the same ticket was scanned a second time; refused (already used).
57. InvalidTicketRejected — a fake or unknown ticket was refused.

## When things change, and the nightly tidy-up

58. EventCancelled — the whole show was called off; everyone who bought must be told and repaid.
59. DunningReminderSent — a chasing message about money that failed or is overdue
    ("your payment didn't go through — please try again"). Dunning = politely chasing owed money.
60. NightlyReconciliationCompleted — the nightly comparison of our money records against
    the card company's records finished.
61. PaymentDiscrepancyFlagged — that comparison found a mismatch (we say paid, they say
    not — or the reverse); flagged for a human.
62. ScanLogRollupCompleted — the nightly job that turns thousands of individual door scans
    into summary numbers finished.
63. DailySalesReportGenerated — Raj's "how did yesterday go" numbers were prepared.

Identity — 1, 2, 3, 21, 22, 23, 24, 25 (accounts, logins, resets, revocation)
Catalog — 4, 5, 6, 7, 8, 16, 18, 19, 20, 58 (the show's own life: created, described, priced, published, moved, cancelled, sales opened)
Inventory — 17, 29, 30, 31, 40, 41, 53 (seats: created, held, rejected, expired, allocated, released, sold out)
Access control — 54, 55, 56, 57 (the door: scans and their verdicts) — see the note below.
Orders — 32, 39, 42, 43, 46 (the purchase's life, including "ticket officially exists")
Payments — 33, 34, 35, 36, 37, 38, 44, 45, 59, 60, 61 (money in, money back, the unknown case, the nightly truth-checks, the chasing reminders)
Media — 9, 10, 11, 12, 13, 14, 15, 47 (files: uploads, scans, imports, thumbnails, the ticket PDF)
Notifications — 48, 49, 50, 51, 52 (messages: sent, failed, retried, parked, deduplicated)
Realtime — 26, 27, 28 (the sale-day queue)
Reporting — 62, 63 (yesterday's numbers)


## Words that change meaning between contexts

**ticket**
- Catalog: a kind of ticket — a price level with a name and a price (no owner, nothing to scan)
- Orders: one line of a purchase — this seat at this price in this order
- Access control: the scannable credential — a QR code, real-or-fake, used-or-unused

**event**
- Catalog: the show itself — Jazz Night, 14 March, with description and poster
- Everywhere else in our docs: a fact that happened — OrderPlaced, SeatHeld

**seat**
- Catalog: a position on the venue map — row A number 12, balcony tier
- Inventory: a sellable unit with a status — free, held (by whom, until when), or sold

**customer**
- Identity: an account that can log in — email and password
- Orders: the buyer — who owes or paid money
- Notifications: a recipient — a name and an address to send messages to