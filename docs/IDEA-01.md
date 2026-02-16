# IDEA-01: Partial Offline Mode for Invoice Creation

Date: 2026-02-16
Related Trello card: `IDEA-01 - offline mode`

## Goal

Allow users to create invoices (receipts) while offline and synchronize them automatically when network access is restored.

## Scope (Phase 1)

In scope:
- Offline creation of invoice draft/final payload in local storage.
- Queueing unsent invoices for synchronization.
- Background synchronization when network is available.
- Basic UI feedback: pending/synced/failed status.

Out of scope (initial phase):
- Full offline mode for all app features.
- Offline edits/deletes for all entities.
- Complex conflict resolution with manual merge UI.

## Functional Requirements

1. If API is unavailable, invoice submission should not be lost.
2. Invoice should be saved locally with status `PENDING`.
3. App should retry synchronization automatically when network is back.
4. User should see synchronization status for offline-created invoices.
5. Failed synchronization should be visible and retryable.

## Proposed Technical Design

### 1) Local Persistence (Room)

Add local database tables/entities:
- `PendingInvoiceEntity`
  - `localId` (PK)
  - `clientRequestId` (UUID, unique)
  - `accountId`, `shopId`, `date`, `number`, `description`
  - serialized invoice items payload (or normalized child table)
  - `status` (`PENDING`, `SYNCING`, `SYNCED`, `FAILED`)
  - `retryCount`, `lastError`, `createdAt`, `updatedAt`

Optional:
- `PendingInvoiceItemEntity` for normalized line items.

### 2) Submission Strategy

Unify invoice submission pipeline:
- UI sends invoice request to a use case/repository layer.
- If online and API succeeds -> mark as `SYNCED` (or skip queue write).
- If offline/API fails with network issue -> persist as `PENDING`.

### 3) Synchronization Worker

Use WorkManager:
- Network constraint: `CONNECTED`.
- Backoff policy for retry.
- Worker loads pending/failed items and attempts API submission.
- On success: update status to `SYNCED`.
- On failure: `FAILED` + increment `retryCount` + save `lastError`.

### 4) Idempotency / Duplicate Protection

Recommended:
- Send `clientRequestId` with API payload (or in request headers) if backend can support idempotency.
- If backend cannot support idempotency initially, use conservative retry policy and duplicate detection where possible.

### 5) UI/UX Additions

In invoice/account outcome flows:
- Show status badge for offline-created invoices.
- Add lightweight "Pending sync" / "Sync failed" indicators.
- Add action: "Retry sync" for failed items.

### 6) Offline Autocomplete

For invoice creation fields (shop/product):
- Use local cached data as autocomplete source when offline.
- Keep autocomplete available even without API connectivity.
- Refresh local autocomplete cache when online sync succeeds.

### 7) Offline Navigation Strategy

When app is offline, some sections may be unavailable.

Recommended approach:
- Keep navigation items visible for orientation.
- Disable unavailable tabs/actions with a clear "Available online only" hint.
- Optionally hide selected advanced tabs only if disabled state causes UX confusion.

Rationale:
- Fully hiding tabs can make the app feel inconsistent and unpredictable.
- Disabled + explanation usually gives better user trust and discoverability.

## Risks and Mitigations

1. Duplicate invoices due to retries.
   - Mitigation: idempotency key (`clientRequestId`) and backend support.
2. Inconsistent local/remote state after app restart/crash.
   - Mitigation: transactional Room writes and robust worker state machine.
3. Poor UX if failures are silent.
   - Mitigation: explicit status and retry actions.

## Test Strategy

### Unit Tests
- Queue status transitions (`PENDING -> SYNCING -> SYNCED/FAILED`).
- Mapper tests for local entity <-> API request.
- Retry/error handling rules.

### Integration Tests
- Repository behavior in offline/online scenarios.
- Worker processing pending queue with mocked API responses.

### UI Tests (androidTest)
- User can create invoice offline and sees pending state.
- Retry action updates status when network/API is available.

## Rollout Plan

1. Implement storage + queue model.
2. Integrate submit flow with offline fallback.
3. Add worker sync and status transitions.
4. Add minimal user-visible status in UI.
5. Add tests and documentation.

## Acceptance Criteria

- Offline invoice creation works and data is persisted locally.
- Pending invoices are synchronized automatically on reconnect.
- Failed sync is visible and retryable.
- No invoice data loss when offline.
- Build and tests pass.

## Client Recommendations and MVP Scope

### Recommended MVP Boundary

To reduce delivery risk, start with:
1. Offline invoice creation (local queue).
2. Background sync with retry/backoff.
3. User-visible sync status (`PENDING`, `FAILED`, `SYNCED`).

Defer full offline support for all sections until MVP quality is validated.

### Additional Caching Recommendations

In addition to pending invoices, cache lightweight dictionaries used by forms:
- shops,
- shop items,
- income types,
- media types,
- accounts/categories used in selectors.

Optional read-only snapshots for key list screens (budgets/accounts/categories) can improve offline UX significantly.

### Data Freshness Policy

Define explicit cache freshness (TTL), for example:
- dictionary data: 24h,
- dashboard/list snapshots: 1-6h.

Always display "last synced" timestamp for offline-visible data.

### Backend Contract Recommendation

Introduce idempotency support for invoice creation (for example `clientRequestId`) to prevent duplicates during retries.

This should be treated as a high-priority API alignment item.

### Offline UX Recommendation

For unavailable sections in offline mode:
- prefer **disabled with explanation** ("Available online only"),
- avoid hiding tabs by default, because hidden navigation can be confusing.

Hiding selected advanced areas can be considered later if UX testing confirms it is better.

### Operational Recommendation

Track basic sync metrics after rollout:
- pending queue size,
- failed sync count/rate,
- average sync time,
- duplicate detection incidents.

Suggested success indicators for first release window:
- no offline data loss,
- near-zero duplicate invoices after retries,
- low failed-sync rate after reconnect.
