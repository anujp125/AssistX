AssistX — State and Lifecycle Specification
Document ID: ASSISTX-STATE-001
Version: 0.1
Status: Draft — owner and technical review required
Last updated: 2026-10-29
Related documents: SRS.md, MVP_SCOPE.md, SYSTEM_ARCHITECTURE.md, MODULE_AND_PACKAGE_STRUCTURE.md, FAILURE_AND_RECOVERY_MATRIX.md
Instruction for AI agents: This document defines state semantics and lifecycle invariants. Do not replace distinct states with a single isRunning boolean. Do not infer current health from a saved preference or an old log event. Preserve uncertainty when the platform cannot reliably expose a condition. Verify platform-specific lifecycle rules before implementation. Any change to state meaning or transitions must update this document and affected tests.

1. Purpose
AssistX must communicate what it knows about its own operation without claiming more certainty than Android exposes. This document defines:
- separate state dimensions;
- state meanings and ownership;
- state transitions and triggering events;
- lifecycle scenarios;
- recovery rules;
- invariants that must always hold;
- diagnostic evidence and testing expectations.
The most important design rule is:

The user's desired enabled state, the app runtime, the floating overlay, overlay permission, and Accessibility availability are related but distinct facts.

For example, the user may want AssistX enabled while overlay permission is missing. The overlay may be visible while Accessibility is disconnected. The Accessibility setting may be enabled while the Accessibility Service is not currently connected. These situations must not be collapsed into the same status.
2. State Model Overview
Use a small set of independent state dimensions, then derive the user-facing summary from them. Avoid maintaining multiple independent copies of the same state.
User intent
    DesiredState: ENABLED | DISABLED
                |
                v
     Runtime observation
    RuntimeState: STOPPED | STARTING | ACTIVE | STOPPING
                  | INTERRUPTED | RECOVERING | FAILED | UNKNOWN
                |
       +--------+---------+
       |                  |
       v                  v
Overlay dimension      Accessibility dimension
PermissionState        AccessibilityState
OverlayState           (only relevant if required)
       |                  |
       +--------+---------+
                |
                v
        Derived UI Status
      (accurate summary for user)
                |
                v
       Local diagnostic events
The diagram is conceptual. It does not prescribe one class per state dimension or require separate Gradle modules.
3. State Domains
3.1 DesiredState — user intent
Value	Meaning
ENABLED	The user has requested that AssistX be enabled. This is intent, not proof that all components are healthy.
DISABLED	The user has explicitly requested that AssistX be disabled. Recovery logic must respect this.
UNSET (optional persistence representation)	No explicit preference has been stored yet. This may be represented separately from the runtime enum if the implementation needs it.


Proposed safe first-run default: remain inactive until the user explicitly enables AssistX. This is a proposal pending owner approval; do not silently change the first-run behavior.

Ownership: preferences store records persistent intent; runtime/state logic reads it and applies it. A transient UI widget must not be the sole owner of desired state.

Rules:
- A stored ENABLED value does not establish that the runtime is active.
- A DISABLED value suppresses automatic recovery and startup of optional runtime behavior.
- A permission change must not silently change user intent.
- User intent may be updated only by an explicit user action or an approved, documented product operation.
3.2 OverlayPermissionState
Value	Meaning
GRANTED	Current platform check indicates that the permission/condition required for the overlay is available.
REQUIRED	Current platform check indicates that the permission/condition is missing.
UNKNOWN	The condition has not yet been checked, could not be checked reliably, or the observation is stale.


Do not replace UNKNOWN with GRANTED to simplify UI logic. Re-check at appropriate points, especially after returning from system settings or before an operation that depends on the permission.
3.3 OverlayState
Value	Meaning
DETACHED	AssistX's overlay is not attached.
ATTACHING	An attachment operation is in progress.
ATTACHED	The overlay manager has evidence that the single intended overlay is attached.
DETACHING	A removal operation is in progress.
ERROR	The latest known operation failed or an inconsistent condition requires investigation.
UNKNOWN	Current attachment state is not reliably known.


ATTACHED does not mean Accessibility is available or that every button action is operational. It only describes overlay attachment as defined by the implementation.

The app must use an appropriate runtime/platform observation to determine attachment; a start request alone is not evidence that attachment succeeded.
3.4 RuntimeState
Value	Meaning
STOPPED	The managed runtime is not running or has been deliberately stopped.
STARTING	Required startup operations are in progress.
ACTIVE	The runtime is operating according to the approved definition and current observations. This value alone must not imply that every dependent feature is ready.
STOPPING	A controlled shutdown is in progress.
INTERRUPTED	An interruption or unexpected loss of a required runtime component has been observed or inferred from available evidence.
RECOVERING	A permitted, bounded recovery attempt is in progress.
FAILED	A startup or recovery operation failed and no current successful path is established.
UNKNOWN	The current runtime condition cannot be established reliably.


ACTIVE must have a precise implementation definition in code and tests. It must not simply mean that a preference is enabled, that the app process exists, or that Studio is open.
3.5 AccessibilityState — conditional
This dimension is used only if the owner-approved MVP action depends on Accessibility.
Value	Meaning
NOT_REQUIRED	The approved v0.1 feature does not require Accessibility. No Accessibility service should be added just to populate this state.
DISABLED	Available platform observation indicates the service is not enabled in system settings.
ENABLED_CONNECTED	The service is enabled and the app has evidence that it is currently connected.
ENABLED_DISCONNECTED	Available evidence indicates the setting is enabled but the service is not currently connected.
UNKNOWN	The app cannot reliably determine the current condition.


Requirements:
- Enabled and connected are different conditions.
- Do not claim the user disabled Accessibility if only a connection loss was observed.
- If a service callback is unavailable or the process was stopped, use UNKNOWN or the state supported by current evidence; do not invent a live observation.
- AssistX must not silently re-enable the Accessibility setting where Android requires user action.
- If the primary action does not require Accessibility, the service and its associated permission/configuration should not be implemented.
3.6 ActionReadiness — optional derived dimension
The app may derive whether the approved primary action is currently usable.
Value	Meaning
READY	All known prerequisites for the approved action are satisfied.
BLOCKED	A known prerequisite is not satisfied.
UNKNOWN	Prerequisites have not been checked or current evidence is insufficient.
NOT_APPLICABLE	No separate readiness dimension is needed for the selected action.


This value should be derived from the real prerequisites, not manually set independently in several places. If it is only used to render a label, it may be calculated on demand rather than persisted.
4. Derived User-Facing Status
The UI may show a single primary summary, but it must retain access to the underlying dimensions. The summary is a presentation of the detailed state, not a replacement for it.

Suggested labels:
User-facing summary	Conditions / interpretation
Disabled	Desired state is disabled and no pending cleanup needs user attention.
Starting	Desired state is enabled and startup is in progress.
Active	The required runtime/overlay conditions for the approved core behavior are observed to be ready.
Overlay Permission Required	Overlay permission is known to be missing and the user needs to act.
Accessibility Disabled	Accessibility is required and the setting is known to be disabled.
Accessibility Disconnected	Accessibility is required, the setting is known enabled, and a disconnection is observed.
Interrupted	A relevant runtime interruption has been observed but recovery or further diagnosis is not yet complete.
Recovering	A permitted recovery operation is in progress.
Recovery Required	Recovery is exhausted, unavailable, or requires user intervention.
Checking Status	Current information is incomplete or being refreshed.
Stopping	The user disabled AssistX but teardown has not yet completed.


Summary precedence
The exact display algorithm must be implemented centrally and tested. Use this recommended priority as a starting point:
1. Explicit user-disabled intent, except while visible cleanup work is still pending.
2. A known missing prerequisite that blocks the required core action.
3. A required service known disabled.
4. A required service known disconnected.
5. Startup, shutdown, interruption, or recovery in progress.
6. Confirmed ready/active state.
7. Unknown/checking/recovery-required state where no more specific known condition applies.
This order is a recommendation. The final mapping must be reviewed against the selected primary action and actual state model. Do not hide independent failures: Studio should be able to show, for example, overlay status and Accessibility status separately even when one primary summary is displayed.

If two failures are simultaneously relevant, the UI should show the primary recovery action and retain the other status details instead of discarding them.
5. State Ownership
Use one authoritative owner or source for each dimension.
Dimension	Primary source/owner	Persistence?
Desired state	Preferences store plus explicit user commands	Yes, according to approved policy
Overlay permission	Current Android permission check	No; re-check current state
Overlay attachment	Overlay manager/runtime observation	No; reconstruct from current runtime
Runtime state	Runtime coordinator plus current lifecycle observations	Usually transient; do not trust a stale persisted ACTIVE flag
Accessibility enabled/connected	Android settings observation and applicable service callbacks	Do not use stale persisted value as current truth
Action readiness	Derived from current prerequisites	No, unless a carefully justified cache is used
Diagnostic history	Diagnostics store	Yes, bounded by retention policy


Persisting an old ACTIVE state and restoring it as current truth is prohibited. After process recreation, the app must re-evaluate what is currently observable.
6. Events That May Trigger State Changes
Use typed events or similarly clear operations. Names below are illustrative; implementation names may differ if semantics remain equivalent.
User events
- USER_ENABLE_REQUESTED
- USER_DISABLE_REQUESTED
- USER_OPENED_STUDIO
- USER_REQUESTED_RECOVERY
- USER_OPENED_SYSTEM_SETTINGS
Permission and platform observations
- OVERLAY_PERMISSION_CHECKED_GRANTED
- OVERLAY_PERMISSION_CHECKED_REQUIRED
- OVERLAY_PERMISSION_CHECK_FAILED
- ACCESSIBILITY_OBSERVED_DISABLED (only if applicable)
- ACCESSIBILITY_OBSERVED_ENABLED
- ACCESSIBILITY_CONNECTED (only if applicable and actually observed)
- ACCESSIBILITY_DISCONNECTED (only if applicable and actually observed)
- APP_RESUMED
- RUNTIME_START_SUCCEEDED
- RUNTIME_START_FAILED
- RUNTIME_STOP_SUCCEEDED
- RUNTIME_STOP_FAILED
- OVERLAY_ATTACH_SUCCEEDED
- OVERLAY_ATTACH_FAILED
- OVERLAY_DETACH_SUCCEEDED
- OVERLAY_DETACH_FAILED
- PROCESS_RECREATED or equivalent lifecycle trigger
- BOOT_START_TRIGGERED only if a boot path is approved and legally/technically supported
- RECOVERY_ATTEMPT_STARTED
- RECOVERY_ATTEMPT_SUCCEEDED
- RECOVERY_ATTEMPT_FAILED
- RECOVERY_LIMIT_REACHED
Do not emit an event claiming something occurred unless the relevant operation or observation actually supports that claim. For example, a call to open settings is not an event confirming that permission was granted.
7. Core Transition Rules
7.1 Enable request
When USER_ENABLE_REQUESTED occurs:
1. Update desired state to ENABLED according to the approved persistence policy.
2. Re-check prerequisites that are required for the approved feature.
3. If overlay permission is required and known missing, expose Overlay Permission Required; do not pretend startup succeeded.
4. If Accessibility is required and known disabled, expose Accessibility Disabled; provide user guidance.
5. If required conditions are satisfied, request startup through the runtime controller.
6. Set runtime state to STARTING while startup is in progress.
7. Change runtime/overlay state only on supported outcomes or observations.
8. Emit diagnostic events for significant transitions and failures.
A start request must not directly set runtime state to ACTIVE.
7.2 Disable request
When USER_DISABLE_REQUESTED occurs:
1. Persist desired state as DISABLED.
2. Cancel or suppress pending recovery tasks that can still be cancelled.
3. Request appropriate shutdown and overlay removal.
4. Use STOPPING while teardown is in progress if needed.
5. Reflect successful cleanup or errors based on actual results.
6. Do not schedule automatic recovery while desired state remains disabled.
A repeated disable request must be safe. If the overlay is already detached and the runtime stopped, the operation should not create a new error merely because there was nothing to remove.
7.3 Overlay permission is missing
When current observation indicates the required overlay permission is missing:
1. Set OverlayPermissionState = REQUIRED.
2. Do not claim that overlay attachment is ready.
3. If the user wants AssistX enabled, expose the permission-required state.
4. Provide a clear explanation and a settings shortcut where supported.
5. Do not repeatedly relaunch system settings or request permission in a loop.
6. When the user returns to AssistX, re-check the actual permission state.
Do not infer a grant because a settings screen was opened or because the application was resumed.
7.4 Accessibility is disabled or disconnected
If Accessibility is required:
- A confirmed disabled setting maps to DISABLED.
- A confirmed connection maps to ENABLED_CONNECTED.
- A confirmed disconnect while the setting remains enabled maps to ENABLED_DISCONNECTED.
- An uncertain observation maps to UNKNOWN.
On a disabled setting:
- Explain that the user must enable the service in Android settings.
- Do not claim it can be re-enabled silently.
- Do not use an Accessibility-dependent action until its prerequisites are satisfied.
On a disconnect:
- Preserve the distinction from a disabled setting.
- Follow a bounded, documented recovery policy only if one is supported.
- If recovery requires user action or cannot be performed safely, show an actionable status.
- Keep overlay health independent.
If Accessibility is not required by the approved primary action, use NOT_REQUIRED conceptually and omit unnecessary Accessibility implementation.
7.5 Startup succeeds or fails
Only treat startup as successful after the necessary component operations produce evidence of success.

If startup fails:
- record the affected component and available error detail;
- keep the state from falsely becoming ACTIVE;
- classify the failure when evidence permits;
- attempt recovery only if the failure is eligible and the retry policy permits it;
- otherwise expose a recovery-required or failed state and user guidance.
Unknown failures remain unknown. Do not relabel every startup failure as “battery optimization.”
7.6 Runtime interruption
When interruption is detected or reasonably inferred:
1. Record the evidence and source of the observation.
2. Re-read desired state.
3. If desired state is DISABLED, do not restart.
4. If required permission is missing, request user intervention rather than retrying startup indefinitely.
5. If a supported transient recovery path exists, transition to RECOVERING and attempt it within defined limits.
6. If recovery succeeds, update state based on current observations.
7. If recovery fails or cannot run, stop automatic attempts according to the policy and expose Recovery Required or another accurate state.
Some system events may terminate a process without giving it a chance to log the termination. The design must not promise that every failure can be detected immediately.
8. Lifecycle Scenario Requirements
LS-01 — Fresh installation
- Load preferences using defined safe defaults.
- Do not claim runtime readiness before checking required conditions.
- Show setup guidance.
- Do not create an overlay before the approved activation workflow and permissions allow it.
- First-run default enabled/disabled behavior requires owner approval.
LS-02 — Studio opens
- Read current desired state and refresh relevant observable conditions.
- Do not start duplicate services merely because the Activity is created.
- Do not create a second overlay.
- UI rendering/recomposition must not repeatedly trigger service operations.
LS-03 — Studio closes or is recreated
- The floating assistant must not depend on the Studio Activity staying alive.
- Activity destruction/recreation must not itself start duplicate runtime components.
- Persistent runtime responsibilities must live in the relevant Android component and coordinator, not in the Activity.
LS-04 — App switches to background
- The UI may stop rendering while supported runtime components continue according to the chosen architecture and platform rules.
- The app must not use unsupported background execution techniques.
- Actual behavior must be verified for the selected Android SDK and target device.
LS-05 — Screen lock and unlock
- Re-check any relevant observable status when appropriate.
- Do not assume screen lock means all components stopped.
- Do not assume unlock means all components are healthy.
- Record unexpected lifecycle behavior when detectable.
LS-06 — Process recreation or ordinary process death
- Reload desired state.
- Re-check current permissions and current component availability.
- Do not restore stale ACTIVE, ATTACHED, or Accessibility-connected values as current truth.
- Follow the approved platform-compliant startup policy.
- Record only events the process is able to observe; do not fabricate an event for a termination it could not detect.
LS-07 — User force-stop
- Respect Android's force-stop semantics and restrictions.
- Do not promise that AssistX can automatically restart after a force-stop.
- Test and document the observable behavior for the supported Android version.
- Do not use another component to circumvent the user's explicit force-stop action.
LS-08 — Device reboot
- Preserve required preferences locally.
- Use a boot/startup mechanism only if approved, justified, and permitted for the selected Android version and distribution model.
- Do not claim immediate restart is guaranteed.
- Verify actual behavior on the target device and document where user interaction is required.
LS-09 — Permission revoked while enabled
- Re-check the condition when the app can observe it.
- Represent the relevant permission as missing.
- Avoid falsely presenting the dependent feature as ready.
- Provide recovery guidance and stop futile retries that cannot resolve a user-controlled permission.
LS-10 — Rapid repeated enable/disable actions
- Serialize or otherwise safely coordinate conflicting operations.
- The last confirmed user intent should determine the intended final state.
- Prevent overlapping attach/remove operations from creating duplicates or leaving stale state.
- Tests must cover repeated requests and out-of-order completion where feasible.
LS-11 — Accessibility disconnects while overlay remains visible
- Keep overlay and Accessibility dimensions separate.
- Disable or mark only the dependent action unavailable where appropriate.
- Do not remove the overlay merely because Accessibility disconnected unless an explicit approved product rule requires that behavior.
- Show a specific status and record the event if observable.
LS-12 — Diagnostics cannot be written
- The primary feature should normally continue if diagnostics storage fails.
- Handle the error safely; avoid a recursive logging loop.
- Use a minimal fallback or surface a nonfatal diagnostic warning if justified.
- Do not let repeated logging failures create battery-draining retries.
9. Recovery Policy
Recovery is not “restart everything until it works.” Every recovery path must identify the failure type, eligibility conditions, retry bounds, exit conditions, and user-facing result.
9.1 Recovery eligibility
A recovery attempt is eligible only when all applicable conditions are true:
- Desired state is ENABLED.
- The failure mode is known or sufficiently characterized.
- The proposed operation is supported by the platform.
- The required permissions and prerequisites are currently satisfied, or the attempted action is specifically intended to re-check them.
- The recovery attempt will not duplicate an existing operation.
- The attempt count and time/cooldown policy permit another attempt.
- The action does not bypass a user decision or Android security control.
9.2 Do not automatically retry when
- desired state is DISABLED;
- required special permission is known missing and user action is necessary;
- Accessibility is known disabled and requires user action;
- Android/user force-stop semantics prohibit the intended recovery;
- the same operation is already in progress;
- the configured retry limit/cooldown has been reached;
- the failure is not understood well enough to retry safely;
- recovery would create repeated user-facing prompts or a restart loop.
9.3 Retry limits
The exact retry counts, cooldowns, and reset conditions are not yet approved.
- Maximum retries per failure episode: [USER INPUT REQUIRED / engineering proposal required]
- Cooldown/backoff policy: [USER INPUT REQUIRED / engineering proposal required]
- Criteria for starting a new failure episode: Any unexpected service unbind, WindowManager detach exception, or process restart
- When a manual user action resets the retry budget: User toggles master switch in Studio or 60s of stable execution elapses
Before implementation, the agent must propose concrete values with rationale and add them to the recovery matrix/configuration design for review. Do not scatter retry numbers as unexplained magic constants.
9.4 Recovery idempotency
- Calling a start operation twice must not create two overlays.
- Calling stop when already stopped must be safe.
- Retrying after partial startup must clean up or reconcile partial state safely.
- An operation should not report success until it has the required evidence.
- Recovery should emit a bounded event trail, not an unlimited loop of duplicate log entries.
10. State Invariants
The following invariants are mandatory and should be unit-tested where practical.
1. DesiredState.DISABLED prevents automatic recovery of user-disabled behavior.
2. DesiredState.ENABLED does not imply RuntimeState.ACTIVE.
3. Runtime active does not by itself prove the overlay is attached.
4. Overlay attached does not imply Accessibility is connected.
5. Accessibility enabled does not imply Accessibility connected.
6. Opening Studio does not imply that the runtime should start again.
7. Returning from system settings does not prove a permission was granted.
8. A state is updated to success only after a successful operation or reliable observation.
9. Unknown observations remain unknown until refreshed with evidence.
10. At most one AssistX overlay instance may be attached.
11. Recovery attempts are bounded and cannot overlap uncontrollably.
12. Permission/setting changes must not silently change user intent.
13. Diagnostics failure must not normally cascade into core feature failure.
14. A status message must not claim a confirmed root cause without supporting evidence.
15. Persisted data cannot be used as proof of current runtime/connection health after process recreation.
16. No state transition may silently grant a protected permission or bypass a system setting.
11. Suggested State Transition Table
This table defines expected high-level behavior. The state reducer/coordinator design may refine the transitions, but must preserve the invariants.
Trigger	Precondition / observation	Required result
User enables	Overlay permission missing	Desired state becomes enabled; status indicates overlay permission required; no false active state
User enables	Required prerequisites satisfied	Runtime transitions to starting; startup is requested once
Startup success	Required runtime/overlay outcome observed	Update runtime and overlay state from actual result
Startup failure	Failure observed	Record failure; keep state non-active; evaluate bounded recovery eligibility
User disables	Any enabled/interrupted state	Persist disabled intent; stop/suppress recovery; request cleanup
Overlay permission check says required	Any	Permission state becomes required; dependent capability is blocked
Overlay permission check fails/uncertain	Any	Permission state remains unknown or error is represented explicitly
Accessibility disabled	Accessibility required	Accessibility state disabled; action marked blocked; user guidance shown
Accessibility disconnected	Accessibility required, setting known enabled	Accessibility state disconnected; overlay state remains independent
Runtime interruption	Desired state disabled	No automatic restart
Runtime interruption	Desired state enabled and recoverable	Bounded recovery may begin
Retry limit reached	Recovery failed repeatedly	Stop automatic retries; status requires intervention or review
Return from settings	App resumes	Re-check actual permission/service state; do not assume success
Diagnostics write failure	Any	Handle nonfatally where possible; avoid recursive retries
Duplicate start request	Startup/overlay already active or starting	Reconcile with current operation; do not create a second instance


12. Diagnostics for State Transitions
For every significant transition, record a local event where feasible:
- timestamp;
- event/state identifier;
- component involved;
- previous and resulting state, if known;
- trigger/source (user action, platform callback, check, recovery attempt);
- operation outcome;
- error category or safe technical details when available;
- recovery attempt number or episode identifier, if applicable.
Avoid recording:
- unnecessary personal information;
- sensitive content unrelated to the failure;
- secrets, tokens, or credentials;
- stack traces in user-facing status UI;
- repeated identical events without bounds or aggregation.
If a timestamp or previous state is unknown, leave it unknown rather than inventing one. Event schema and retention limits are defined in DATA_AND_DIAGNOSTICS.md.
13. Testing Requirements
The state and lifecycle design must be verified through both automated and device tests.
Unit-test candidates
- enabling with missing overlay permission;
- enable request does not immediately become active;
- disabling prevents recovery;
- runtime state and overlay state remain independent;
- Accessibility enabled and connected remain distinct;
- unknown permission/connection state is handled conservatively;
- repeated start requests do not create duplicate attachment operations;
- retry budget is enforced;
- retry budget cannot be bypassed by repeated UI renders;
- failure to record a diagnostic does not create a recovery loop;
- stale persisted runtime state is not restored as current truth.
Integration/device scenarios
- first run;
- enabling and disabling repeatedly;
- opening/closing Studio;
- granting/revoking overlay permission;
- enabling/disabling Accessibility if applicable;
- returning from settings;
- app switching and screen lock/unlock;
- process death versus user force-stop;
- device reboot;
- long-duration observation;
- recovery from a supported transient failure;
- a failure that requires user intervention.
The detailed cases and expected results belong in ACCEPTANCE_TESTS.md, REAL_DEVICE_TEST_MATRIX.md, and FAILURE_INJECTION_PLAN.md.
14. Unresolved Decisions Requiring Owner or Technical Review
ID	Decision	Status
ST-01	First-run desired state default	Proposed disabled until explicit enable; owner approval required
ST-02	Exact primary action and whether Accessibility is required	Approved: Double-tap lock screen, long-press home/back (Accessibility required)
ST-03	Exact user-facing status priority when multiple blockers coexist	Review against final action and UI design
ST-04	Retry count, cooldown/backoff, and episode reset policy	[ENGINEERING PROPOSAL REQUIRED]
ST-05	Persistence timing for enable/disable requests	Technical design; must avoid stale intent or unsafe recovery
ST-06	Supported startup behavior after reboot	Verify against Android requirements and owner distribution choice
ST-07	Runtime definition of ACTIVE for the approved action	Must be made explicit before implementation
ST-08	Exact observation mechanism for overlay and Accessibility state	Verify against chosen Android APIs and target SDK
ST-09	Whether recovery-required state needs manual user acknowledgement	[USER INPUT REQUIRED if behavior affects product UX]
ST-10	Diagnostic event schema and retention	See DATA_AND_DIAGNOSTICS.md


15. Implementation Rules for AI Agents
Before changing state or lifecycle code, an agent must:
1. read this document and the failure/recovery matrix;
2. identify which state dimensions and invariants the task affects;
3. describe the state transitions the implementation will add or modify;
4. add or update tests for the relevant transitions;
5. verify platform requirements for Android-specific behavior;
6. update diagnostic event handling if meaningful transitions/errors changed;
7. report tests actually run and results;
8. note any state that remains unknown or any behavior not verified on the device.
Do not:
- introduce a single isRunning flag as a substitute for this model;
- store stale runtime health as authoritative truth;
- restart components from UI rendering;
- add a boot receiver or foreground-service strategy without platform review;
- silently treat disconnection as a disabled setting;
- implement unbounded retries;
- claim automatic recovery after a force-stop without evidence that the platform permits it;
- modify multiple unrelated state domains in one task without justification.
16. Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Status: Draft / Approved
- Approved first-run behavior: Disabled until explicitly enabled by user
- Approved primary action: Double-tap lock screen, long-press home/back, snap-to-edge drag
- Recovery retry policy: [ENGINEERING PROPOSAL REQUIRED]
- Review date: 2026-10-29
- Accepted deviations: None
- Notes: State coordinator and lifecycle model approved.
This document is a controlled draft until reviewed. Any approved change to state semantics, lifecycle behavior, recovery policy, or invariants must update the SRS, architecture, failure/recovery matrix, relevant sprint tasks, and tests.