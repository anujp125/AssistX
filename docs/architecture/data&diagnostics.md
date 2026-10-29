AssistX — Data and Diagnostics Specification
Document ID: NUDGE-DATA-001
Version: 0.1
Status: Draft — owner review required
Last updated: 2026-10-29
Related documents: PRODUCT_BRIEF.md, SRS.md, SYSTEM_ARCHITECTURE.md, STATE_AND_LIFECYCLE.md, PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md, FAILURE_AND_RECOVERY_MATRIX.md, FAILURE_INJECTION_PLAN.md, CODING_STANDARDS.md, DECISION_LOG.md
Status note: This document is a proposed specification, not a record of an implemented storage design. Decisions marked [OWNER DECISION REQUIRED] or [ENGINEERING PROPOSAL] must be resolved and reflected in DECISION_LOG.md before the relevant implementation is treated as final.

1. Purpose
Define the data AssistX stores locally, the minimum diagnostic evidence needed to troubleshoot failures, the privacy boundaries around that data, and the behavior expected when storage is unavailable or corrupted.

AssistX's MVP is a single Android app with a floating assistant and a Studio/settings interface. They share local state. The MVP has no required backend, cloud account, telemetry service, or remote diagnostic upload.
2. Data Principles
1. Local by default. Keep MVP settings and diagnostics on the device. Do not transmit them to a server.
2. Minimum necessary data. Record only what is needed to explain app state and troubleshoot supported operations.
3. State accuracy over apparent continuity. Persisted user intent is not evidence that an overlay, service, or Accessibility service is currently alive.
4. No secret collection. Never store passwords, authentication tokens, unrelated app content, contacts, messages, or keystrokes.
5. No Accessibility content capture. If Accessibility is used by an approved feature, do not collect or log text/content from other apps, window contents, typed text, or unrelated accessibility events.
6. Bounded storage. Settings and diagnostics must not grow without limits.
7. Graceful degradation. Diagnostic-write failure must not create a restart loop or usually prevent the primary UI/action from reporting its result.
8. Explicit export. No automatic upload or sharing. Any future export must be user-initiated and reviewed for sensitive content.
9. Clear evidence labels. Distinguish observed facts, inferred state, unknown state, and test-only simulated failures.
10. No fabricated history. Never backfill imagined events or mark an operation successful when the platform did not confirm it.
3. Data Inventory
Category	Examples	MVP storage expectation	Sensitivity / notes
User settings	Preferred appearance, button visibility/position if approved, haptics if approved	Local preferences	Do not store a value until the related setting is approved
Desired assistant state	User's request for enabled/disabled	Local preferences	This is intent, not proof of runtime health
Runtime state	Current service/overlay status and last observed transition	Prefer runtime memory; persist only minimal diagnostic snapshots if justified	Must be revalidated after process recreation
Permission/service observations	Overlay permission available, Accessibility enabled/connected when applicable	Runtime observation; selected transitions may be logged	Do not infer current status from stale saved values
Operation outcomes	Overlay attach/remove result, startup outcome, action result category	Bounded local diagnostics	Include structured reason codes; avoid raw exception payloads
Recovery decisions	Retry scheduled/suppressed/exhausted, permission blocker	Bounded local diagnostics	Include attempt number and reason; no unbounded event flood
App/build context	App version, build type, app-side schema version	Local diagnostic event metadata	Do not collect a persistent device fingerprint
Test evidence	Test run ID, failure-injection case ID	Test reports / developer workflow	Do not put test-only secrets into production storage
Optional user-facing notes	User-entered incident note, if later approved	Out of MVP unless approved	Free text can contain sensitive data; do not add casually


4. Proposed Storage Boundaries
The storage implementation has not been finalized. The following is an engineering proposal for review, not a requirement to add every technology.
4.1 User settings and desired state
Proposal: Use Android's Preferences DataStore for small, typed settings if compatible with the chosen architecture and dependency policy.

Possible keys:
- desired_assistant_enabled: boolean, default false unless a different onboarding policy is approved.
- button_position_x_fraction: optional normalized horizontal position if drag/position persistence is approved.
- button_position_y_fraction: optional normalized vertical position if drag/position persistence is approved.
- settings_schema_version: integer used only for controlled migrations.
Only include keys for features approved in the MVP. Do not create preferences for speculative features.
4.2 Runtime and observed state
- Runtime facts such as an attached view or a currently connected service should be derived from current process/platform observations.
- Do not restore runtime ACTIVE status just because a previous process saved it.
- If a previous run's last-known state is needed for diagnostics, label it explicitly as last_known_* with its observation timestamp.
- Use an UNKNOWN, CHECKING, or equivalent state when current truth cannot be established; choose exact enum values in STATE_AND_LIFECYCLE.md.
4.3 Diagnostics
[OWNER DECISION REQUIRED] Choose the smallest suitable bounded local store:
- Option A — bounded database: A small SQLite-backed store, optionally using Room if the team already chooses it. Useful for structured querying and retention.
- Option B — bounded structured file: A rotating JSON Lines or equivalent file with one structured event per line. Simpler, but partial writes, rotation, and corruption recovery need explicit handling.
Engineering recommendation: Use the simplest option the project can test well. Do not introduce Room or another persistence dependency only for theoretical future needs. Record the selection in ARCHITECTURE_DECISIONS.md.

Regardless of implementation:
- write events through one narrow diagnostics interface;
- apply a fixed maximum event count and/or total size;
- avoid synchronous disk I/O on the main thread;
- handle a corrupted/unreadable store without crashing the app;
- do not delete all prior evidence merely because one write fails;
- never store raw stack traces if they may contain payloads or sensitive values; sanitize and truncate exception details.
5. Diagnostic Event Model
Use a structured record rather than relying only on free-form log messages. Field names may change when implementation is selected, but the semantics should remain.
Field	Required	Meaning
event_id	Yes	Unique identifier for the local event
timestamp_utc	Yes	Wall-clock timestamp in UTC
event_type	Yes	Stable, low-cardinality event name, e.g. overlay_attach_failed
severity	Yes	DEBUG, INFO, WARN, or ERROR; exact mapping documented in code
component	Yes	Component responsible, e.g. overlay, permission, recovery, storage
result	Yes	SUCCESS, FAILED, BLOCKED, UNKNOWN, or an approved equivalent
reason_code	When applicable	Stable code, e.g. OVERLAY_PERMISSION_MISSING
desired_state	When applicable	User intent at event time
observed_state	When applicable	Current observed state, including UNKNOWN if needed
attempt_number	Recovery events only	Bounded retry attempt number
app_version	Yes	App release/version identifier
build_type	Yes	debug, internal, or release as applicable
session_id	Recommended	Random, app-local session identifier; not an account identifier
correlation_id	Recommended	Connect related operations without storing raw data
duration_ms	When applicable	Operation duration, nonnegative and bounded
error_class	When applicable	Exception class or categorized platform error, sanitized
details	Optional	Small allowlisted key/value map; never arbitrary payload dumps


Timestamp caveat: Wall-clock time may jump after time synchronization or manual changes. If event ordering and duration matter, use a monotonic clock during the process and optionally record elapsed duration. Do not treat monotonic timestamps from different process sessions as directly comparable.
5.1 Initial event vocabulary (proposal)
- app_started
- app_resumed
- settings_saved
- enable_requested
- disable_requested
- permission_check_completed
- permission_blocked
- overlay_attach_started
- overlay_attach_succeeded
- overlay_attach_failed
- overlay_remove_succeeded
- overlay_remove_failed
- runtime_start_failed
- accessibility_state_observed (only if applicable)
- action_attempted (only for an approved feature; record category, not target-app content)
- action_failed
- recovery_scheduled
- recovery_suppressed
- recovery_exhausted
- state_reconciled
- diagnostic_write_failed
- diagnostic_store_recovered
- diagnostic_retention_applied
The implemented vocabulary should be small and version-controlled. Do not create a new event type for every unique exception message.
6. Explicitly Prohibited Diagnostic Content
Do not collect or store:
- passwords, PINs, unlock patterns, tokens, private keys, or credentials;
- keystrokes, clipboard contents, messages, notifications from other apps, contacts, call logs, or screen text;
- Accessibility node trees, window contents, or raw event content from other apps;
- screenshots, screen recordings, microphone audio, or camera images;
- precise location, GPS coordinates, or location history;
- IMEI, IMSI, phone number, advertising ID, or a persistent cross-app/device identifier;
- complete raw Android intent bundles or unfiltered exception/object dumps;
- any remote endpoint URLs or payloads containing user data unless that feature is later explicitly approved and documented.
Do not collect sensor readings from other apps. For the current AssistX MVP, there is no need to collect unrelated phone usage data.
7. Retention and Size Limits
[OWNER DECISION REQUIRED] Exact retention numbers are not yet approved. Use bounded values from configuration/constants and cover them with tests.

[ENGINEERING PROPOSAL — FOR REVIEW]
- Retain the most recent 1,000 diagnostic events, subject to a maximum total size of 1 MiB.
- Remove oldest eligible events first when either limit is exceeded.
- Do not retain an unlimited event history.
- If one event would exceed the per-event size limit, truncate allowlisted details safely and record a minimal event; never truncate structural fields into invalid data.
- Avoid logging repetitive status polling. Log meaningful transitions and failures, not every UI render or every periodic health check.
- Do not add an arbitrary age-based retention window until it is useful and clearly specified; if age-based cleanup is added, make it bounded and testable.
The values above are proposals. Confirm, replace, or reject them before implementation uses them as acceptance criteria. Tests must use injected limits where appropriate so limit behavior is deterministic.
8. Access, Export, and Deletion
8.1 Access
- Diagnostics are available only to the app and approved user-visible troubleshooting surfaces.
- Do not expose diagnostics through an unprotected network server.
- Avoid printing user-sensitive diagnostic content to release-build logs.
- A debug build may emit additional sanitized logs; release behavior must remain privacy-preserving.
8.2 Export
- Not required for the MVP unless approved.
- If later added, export only after an explicit user action and show what will be included.
- Apply a final allowlist and sanitization pass before export.
- Do not export automatically on crashes or failures.
- Do not send logs to developers, third parties, or cloud services without a separately reviewed data-flow and consent design.
8.3 Deletion/reset
- Settings reset and diagnostics deletion are separate actions unless the owner explicitly decides otherwise.
- If a diagnostics-clear action is added, confirm user intent and report success/failure truthfully.
- Clearing diagnostics must not accidentally re-enable AssistX or reset permissions.
- App uninstall behavior is controlled by Android; do not promise retention after uninstall.
9. Storage Failure Behavior
Failure	Required behavior
Settings read fails	Do not assume the assistant should be enabled. Use a safe default and expose an error if the setting materially affects behavior.
Settings write fails	Do not claim the new preference was saved. Keep runtime behavior consistent with an explicitly defined policy and show/recover from the error.
Preferences are missing	Apply documented defaults; run normal current-state checks.
Preferences are malformed/out of range	Reject or safely normalize the affected value; record a sanitized diagnostic if possible.
Old settings schema is found	Run a deterministic migration or reset only the unsupported key with a documented reason.
Diagnostic write fails	Do not crash or recursively log the same failure. Preserve the primary operation outcome and report diagnostics as unavailable if shown to the user.
Diagnostic store is corrupt/unreadable	Recover using a bounded, documented process. Preserve recoverable data where practical; do not loop continuously.
Retention cleanup fails	Prevent unbounded growth by applying a safe fallback if possible; surface a nonfatal diagnostic/storage warning.
Disk space is low	Avoid large writes and uncontrolled retries. Keep core UI behavior safe and truthful.


When settings storage fails, the team must explicitly decide whether a user-initiated state change may take effect for the current process but fail to persist. Do not accidentally imply that temporary runtime success means the preference was saved.
10. Data Migration and Schema Evolution
- Include a schema/version marker where migrations are needed.
- Migrations must be deterministic, idempotent where possible, and covered by tests.
- Preserve only known supported keys; ignore unknown keys safely.
- Do not reinterpret old runtime ACTIVE fields as current truth.
- For a failed migration, use a safe, documented fallback and provide a diagnostic event if storage remains usable.
- Do not make migration depend on a network connection.
- Document backward-compatibility requirements for internal testers before releasing an upgrade.
11. Observability Without Over-Logging
Prefer:
- transition events instead of polling logs;
- stable reason codes instead of unique free-text messages;
- operation correlation IDs rather than copied payloads;
- summarized error classes rather than full object dumps;
- aggregate counters for repeated low-value events when they materially help troubleshooting.
Diagnostics must be useful enough to identify whether an operation was attempted, what blocked it, what result was observed, and whether recovery was scheduled—without collecting unrelated user content.
12. Verification Plan
The following tests should be added to the appropriate unit/integration suites. Record results after execution; this list is not a claim that they have passed.
- [ ] Defaults are applied when no preferences exist.
- [ ] Desired state is persisted only after a successful settings write.
- [ ] A stored ACTIVE-like legacy value does not override current runtime checks.
- [ ] Unsupported preference values are rejected or normalized according to policy.
- [ ] Migration succeeds from every supported prior schema.
- [ ] Migration failure does not crash startup or cause repeated migration loops.
- [ ] Each diagnostic event contains required fields and stable reason codes.
- [ ] A diagnostic-write exception does not mask the operation's real result.
- [ ] Diagnostics failure does not trigger recursive logging.
- [ ] Retention removes oldest events deterministically when count limit is reached.
- [ ] Retention removes/truncates safely when size limit is reached.
- [ ] Oversized or malformed events cannot corrupt later records.
- [ ] Corrupt store startup is bounded and has a safe outcome.
- [ ] Main-thread operations do not perform unbounded disk I/O.
- [ ] Release logging does not reveal prohibited content.
- [ ] Repeated UI rendering does not generate unbounded diagnostic events.
- [ ] No diagnostic upload/network request occurs in the MVP.
- [ ] A test verifies that user intent, permission state, and runtime health remain separate.
Suggested failure-injection references: FI-013 and FI-014 in FAILURE_INJECTION_PLAN.md.
13. Review Checklist
- [ ] Storage mechanism selected and documented in ARCHITECTURE_DECISIONS.md.
- [ ] Allowed settings keys approved.
- [ ] Default value for desired state approved.
- [ ] Diagnostics event schema approved.
- [ ] Maximum event count and size approved.
- [ ] Any age-based retention policy approved or explicitly deferred.
- [ ] Export and diagnostics-clear UI explicitly approved or out of scope.
- [ ] Privacy and release-log rules covered by code review.
- [ ] Storage-failure behavior covered by tests.
- [ ] Requirements traceability updated after decisions are finalized.
14. Owner Decisions
Decision ID	Question	Proposed default	Status
DATA-001	Settings storage	Preferences DataStore if compatible with the chosen dependencies	Proposed
DATA-002	Diagnostics storage	Small bounded SQLite store or structured rotating file; choose one	Open
DATA-003	Desired state default on first launch	Disabled until user explicitly enables	Proposed
DATA-004	Max diagnostic events	1,000	Proposed
DATA-005	Max diagnostics size	1 MiB	Proposed
DATA-006	Age-based retention	Defer until justified; event/size bound applies	Proposed
DATA-007	User-visible diagnostics viewer	Not required for MVP unless approved	Open
DATA-008	Manual diagnostics export	Out of MVP unless explicitly approved	Proposed
DATA-009	Clear diagnostics action	Separate from settings reset; implementation optional	Open
DATA-010	Persist button position	Only if draggable placement is approved	Conditional
DATA-011	Persistent device/station identifier	Do not collect one for MVP	Proposed


Owner response: Anuj Pawar (@anujp125)
Review date: 2026-10-29
Approved decisions: Room SQLite DB (max 500 events / 2 MB) for diagnostics; Jetpack DataStore Preferences for settings
Follow-up issues: None. Ready for implementation.