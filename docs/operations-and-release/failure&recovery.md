AssistX — Failure and Recovery Matrix
Document ID: ASSISTX-RECOVERY-001
Version: 0.1
Status: Draft — owner and technical review required
Last updated: 2026-10-29
Related documents: SRS.md, MVP_SCOPE.md, SYSTEM_ARCHITECTURE.md, STATE_AND_LIFECYCLE.md, PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md, TEST_STRATEGY.md
Instruction for AI agents: Use this matrix to implement, test, and review failure handling. Do not infer a root cause from a symptom alone. Recovery must be bounded, respect user intent and Android controls, and be observable when the app is able to observe it. Values marked TBD require a documented proposal and approval before they become constants. Never claim a test was executed if it was not.

1. Purpose
This matrix describes how AssistX should handle failures and degraded conditions for the v0.1 MVP. For each scenario, it identifies:
- possible detection signal;
- confidence and important limitations;
- state to expose;
- permitted recovery;
- user guidance;
- diagnostic evidence;
- verification method.
The aim is to prevent misleading status, uncontrolled retries, duplicate overlays, and incorrect assumptions about Android or Realme behavior.
2. Rules for Interpreting Evidence
1. A symptom is not a root cause. A missing floating button does not prove the OS killed the process or that battery optimization is responsible.
2. Use the strongest available observation. A current permission check is stronger evidence than a previously saved value.
3. Keep unknown states. If a condition cannot be checked reliably, report it as unknown/checking rather than assuming success or failure.
4. Separate dimensions. Overlay permission, overlay attachment, runtime health, and Accessibility enabled/connected states must remain distinct.
5. Respect user intent. Automatic recovery is forbidden while the desired state is explicitly disabled.
6. Recover only where permitted. No permission bypass, forced Accessibility re-enablement, or workaround that evades force-stop/user controls.
7. Bound recovery. Retry counts, cooldowns, and reset rules must be explicit and testable.
8. Never promise uninterrupted operation. Android or device-specific policies can terminate or restrict components.
9. Diagnostics are best effort. A logging failure must not normally break the floating assistant.
10. Test on target hardware. Process lifecycle and manufacturer behavior require real-device evidence.
3. Severity Definitions
Severity	Meaning	Expected handling
S0 — Critical	Core functionality is unsafe, privacy/security boundary is violated, or uncontrolled behavior occurs	Stop affected behavior when safe; do not release; owner review required
S1 — High	Approved core workflow is unavailable or status is materially misleading	Actionable status, diagnostic evidence, prioritized fix
S2 — Moderate	A secondary function or recovery path is degraded, but main behavior remains usable	Clear status and recover or provide workaround
S3 — Low	Cosmetic or minor diagnostic issue that does not affect core behavior	Record and fix according to priority


Severity is a project triage aid, not a declaration that Android itself categorizes a failure this way.
4. Failure Matrix
F-01 — Overlay permission is missing at startup
- Possible detection: A current overlay permission check indicates the permission is not granted.
- Confidence: High when confirmed by the relevant current platform API.
- Expected state: DesiredState may remain ENABLED if that is the user's saved intent; OverlayPermissionState = REQUIRED; derived UI status is Overlay Permission Required.
- Permitted recovery: Explain the permission and provide a settings shortcut. Re-check when the user returns.
- Forbidden behavior: Do not repeatedly open system settings, repeatedly attempt attachment, or display Active.
- User guidance: Tell the user that overlay access is required for the floating button and that they must enable it in Android settings.
- Diagnostics: OVERLAY_PERMISSION_CHECKED_REQUIRED; record a failed attach attempt only if one was actually attempted.
- Tests: Fresh install with permission not granted; attempt Enable; open settings; return without granting; verify status remains required.
F-02 — Overlay permission is revoked while AssistX is enabled
- Possible detection: A permission re-check after resume or an overlay operation failure indicates the permission is no longer available.
- Confidence: High only after a current check; an attach failure alone may have other causes.
- Expected state: OverlayPermissionState = REQUIRED; do not claim overlay readiness.
- Permitted recovery: Stop futile overlay retries and prompt the user to restore permission.
- Forbidden behavior: Do not infer the exact cause if only an operation failure is known.
- User guidance: Provide a direct route to overlay settings.
- Diagnostics: Record the permission observation and any actual overlay operation error.
- Tests: Revoke permission in settings while enabled, return to AssistX, verify re-check and truthful status.
F-03 — Overlay attachment fails despite permission being available
- Possible detection: The platform's overlay/window operation returns an error or throws an exception.
- Confidence: Failure is observed; root cause may remain unknown.
- Expected state: OverlayState = ERROR or UNKNOWN; runtime must not falsely be reported as fully active.
- Permitted recovery: Clean up partial overlay state safely; make at most the approved bounded recovery attempt if the error is eligible.
- Forbidden behavior: Do not label every attach failure as a battery restriction or immediately enter an endless restart loop.
- User guidance: Show a generic but actionable message, e.g. “The floating button could not be displayed. Check AssistX status and try again.”
- Diagnostics: Error category and safe technical detail, operation stage, timestamp, and recovery outcome.
- Tests: Use an injectable/fake overlay adapter for unit tests; run relevant Android/device tests for actual window behavior.
F-04 — Duplicate overlay is created
- Possible detection: Internal lifecycle bookkeeping or tests show more than one AssistX overlay/attachment operation is active.
- Confidence: High when detected by app-owned bookkeeping or direct test observation.
- Expected state: Treat as a high-priority invariant violation; reconcile to one overlay.
- Permitted recovery: Remove duplicate instances safely and retain at most one intended overlay.
- Forbidden behavior: Do not add more retries or create another instance to “fix” the duplication.
- User guidance: Normally no special prompt unless the condition persists; show an accurate status if the button is unavailable.
- Diagnostics: Record duplicate detection and cleanup result.
- Tests: Concurrent/repeated Enable requests; Activity recreation; repeated recovery; rapid Enable/Disable sequences.
F-05 — Overlay removal fails during Disable
- Possible detection: A removal operation fails or the manager cannot establish that the overlay is detached.
- Confidence: Depends on the platform error and available bookkeeping.
- Expected state: Desired state becomes DISABLED; cleanup state remains pending/failed instead of falsely claiming complete detachment.
- Permitted recovery: Perform safe, bounded cleanup; keep automatic feature startup disabled.
- Forbidden behavior: Do not re-enable AssistX to restore an overlay the user asked to remove.
- User guidance: If visible behavior remains inconsistent, provide a clear explanation and a safe app-restart/permission-settings recovery instruction as appropriate.
- Diagnostics: Record removal outcome and whether attachment status remains uncertain.
- Tests: Fake removal failure; repeated Disable; cleanup after partial initialization.
F-06 — Floating button disappears; process or cause is unknown
- Possible detection: User reports the symptom, a later app resume discovers overlay detached, or the runtime can observe an unexpected component/lifecycle change.
- Confidence: Often low until more evidence is collected.
- Expected state: INTERRUPTED, UNKNOWN, or a more specific state justified by evidence; do not assume process death.
- Permitted recovery: Re-check desired state, overlay permission, known runtime/component status, and then follow the bounded recovery policy.
- Forbidden behavior: Do not state “Realme killed the app” or “battery optimization caused it” without evidence.
- User guidance: Show current status and any actionable issue. If the cause is unknown, say so.
- Diagnostics: Record the observations and the time the app became aware of the problem; do not invent a termination event that was not observed.
- Tests: Controlled process interruption where possible; reopen Studio after a missing-button simulation; compare the state evidence available in each scenario.
F-07 — Accessibility is disabled in system settings (conditional)
- Possible detection: A reliable current check indicates the Accessibility service is not enabled.
- Confidence: High if obtained from the appropriate platform state.
- Expected state: AccessibilityState = DISABLED; overlay state remains independent.
- Permitted recovery: Guide the user to Accessibility settings and re-check after return.
- Forbidden behavior: Do not silently re-enable the service, repeatedly open settings, or describe this as merely a transient disconnect.
- User guidance: Explain that the approved action requires the user to enable AssistX's Accessibility Service.
- Diagnostics: Record the observed disabled state if useful and privacy-safe.
- Tests: Disable the service in settings; return to Studio; verify status and guidance. Re-enable it and verify re-check.
F-08 — Accessibility is enabled but the service is disconnected (conditional)
- Possible detection: System setting indicates enabled while service connection callbacks/current observations indicate disconnected, to the extent detectable.
- Confidence: Depends on available observation; avoid overclaiming if the setting and connection cannot both be checked reliably.
- Expected state: AccessibilityState = ENABLED_DISCONNECTED or UNKNOWN, not DISABLED.
- Permitted recovery: Apply a documented, bounded policy only when there is a supported recovery path. Otherwise guide the user to review/re-enable the setting or reopen AssistX as appropriate.
- Forbidden behavior: Do not change the system setting automatically or treat the disconnect as proof that the user disabled Accessibility.
- User guidance: Distinguish “enabled but not connected” from “disabled.”
- Diagnostics: Record actual connect/disconnect callbacks and current check results separately.
- Tests: Exercise disconnect/reconnect behavior where safely reproducible; distinguish from the setting being switched off.
F-09 — Accessibility action is unavailable or fails (conditional)
- Possible detection: The approved action is not available for the current service/device state, or the API reports the action failed.
- Confidence: The call result is evidence of the call outcome, not necessarily a complete explanation for the failure.
- Expected state: The dependent action is BLOCKED or UNKNOWN; do not equate this automatically with overlay failure.
- Permitted recovery: Verify prerequisites; provide a supported next step or an accurate failure message.
- Forbidden behavior: Do not silently add Device Administrator or another sensitive capability as a workaround.
- User guidance: Explain that the action could not be completed and identify only a cause supported by evidence.
- Diagnostics: Action attempted, relevant prerequisite status, safe result/error category, outcome.
- Tests: Test supported/unsupported API conditions and actual behavior on the target device.
F-10 — Runtime start request fails
- Possible detection: Service/component start or initialization reports failure, or expected startup evidence does not arrive within a defined observation policy.
- Confidence: Record precisely what failed; a timeout may indicate unknown outcome, not definitive failure.
- Expected state: RuntimeState = FAILED, INTERRUPTED, or UNKNOWN based on evidence; never set ACTIVE merely after requesting start.
- Permitted recovery: Clean up partial startup and evaluate the bounded recovery policy.
- Forbidden behavior: No immediate unbounded start loop, no unsupported background start, and no generic “battery optimization” diagnosis.
- User guidance: Provide next steps based on the failure category, or clearly say the cause is unknown.
- Diagnostics: Start request, outcome, component, error category, attempt number, and resulting state.
- Tests: Unit tests with fake start result; Android/device test of the actual service startup path.
F-11 — Runtime component stops or is interrupted unexpectedly
- Possible detection: A lifecycle callback, observable service status, failed operation, or later reconciliation provides evidence of interruption.
- Confidence: Some termination modes cannot be observed at the time they occur. Preserve this limitation.
- Expected state: INTERRUPTED or UNKNOWN, then bounded recovery or Recovery Required.
- Permitted recovery: Re-read desired state, check prerequisites, and recover only when platform rules and retry budget permit.
- Forbidden behavior: Do not claim all process kills can be detected. Do not use a keep-alive loop.
- User guidance: If AssistX cannot recover without user action, state that clearly.
- Diagnostics: Record observations available to the process, not a fabricated termination event.
- Tests: Simulate recoverable component loss where possible; use separate cases for process death, force-stop, and reboot.
F-12 — Automatic recovery attempts keep failing
- Possible detection: Configured retry count/cooldown reached or repeated eligible attempts fail.
- Confidence: High if the recovery controller maintains explicit bounded attempt state.
- Expected state: Stop retries and expose Recovery Required or an accurately defined failure state.
- Permitted recovery: Allow a deliberate user retry after prerequisites are checked or after the approved cooldown/reset condition.
- Forbidden behavior: Never reset the retry counter merely because UI recomposed or the Activity reopened; never loop indefinitely.
- User guidance: Explain the next supported action; avoid repetitive prompts.
- Diagnostics: Attempt number, reason, result, retry exhaustion, and any user-triggered reset.
- Tests: Deterministic unit tests for retry limit, cooldown, reset conditions, and repeated callbacks.
Open policy: Retry count, cooldown/backoff, and reset conditions remain [ENGINEERING PROPOSAL REQUIRED] until reviewed.
F-13 — User explicitly disables AssistX
- Possible detection: User selects Disable in Studio or another approved control.
- Confidence: High.
- Expected state: DesiredState = DISABLED; pending recoveries are suppressed/cancelled where possible.
- Permitted recovery: None automatically; wait for an explicit user enable request.
- Forbidden behavior: Re-enabling itself on process start, timer, boot, or a service callback.
- User guidance: Show Disabled and any remaining cleanup issue without misrepresenting the result.
- Diagnostics: Record the user action and cleanup outcome.
- Tests: Disable while active, starting, interrupted, and recovering. Verify no automatic re-enable.
F-14 — Studio Activity closes, is recreated, or changes configuration
- Possible detection: Android Activity lifecycle callbacks.
- Confidence: High for Activity lifecycle; it does not prove a service stopped.
- Expected state: No unexpected change in desired state. Runtime state is derived from actual component observations.
- Permitted recovery: Re-render UI and refresh state; coordinate service operations only when requested or required by the approved lifecycle policy.
- Forbidden behavior: Do not start another service or overlay each time Studio opens or renders.
- User guidance: Normally none.
- Diagnostics: Record lifecycle events only if useful; avoid noisy logs for every render.
- Tests: Open/close/recreate Studio repeatedly; ensure one overlay and no duplicate runtime.
F-15 — User returns from Android system settings
- Possible detection: Activity resume or the documented callback mechanism for the settings flow.
- Confidence: Resumption is confirmed; permission grant is not.
- Expected state: Re-check relevant permission/service state. Use the actual result.
- Permitted recovery: Continue startup if prerequisites are now satisfied and desired state is still enabled.
- Forbidden behavior: Do not assume success because the settings screen was launched or the app resumed.
- User guidance: If still blocked, repeat the clear instruction without repeatedly reopening settings.
- Diagnostics: Record the check result, not merely “settings opened.”
- Tests: Return without changing settings, return after granting, and return after revoking where applicable.
F-16 — App is force-stopped by the user
- Possible detection: The app may not receive a callback at the moment of force-stop; detection may only be possible on a later launch or observation.
- Confidence: Do not assume the app can know the exact time/cause.
- Expected state: On the next legitimate launch, reload desired state and re-evaluate current conditions. Do not claim automatic immediate restart.
- Permitted recovery: Resume through an ordinary, platform-permitted user launch.
- Forbidden behavior: No receiver/alarm/service workaround intended to evade force-stop.
- User guidance: Troubleshooting may explain that reopening the app is needed, if verified for the target environment.
- Diagnostics: Record a new launch and available current state; do not fabricate a force-stop event.
- Tests: Force-stop through Android's app settings or documented test procedure; observe subsequent launch behavior.
F-17 — Device reboot
- Possible detection: A boot-related component only if one is approved and supported; otherwise, behavior may first be observed when the user opens AssistX.
- Confidence: Depends on the approved implementation and platform restrictions.
- Expected state: Preference remains stored; current runtime status is not restored from stale saved ACTIVE state.
- Permitted recovery: Follow a reviewed, SDK-compliant startup policy. If no automatic startup path is approved, provide normal launch/setup behavior.
- Forbidden behavior: Do not promise immediate restart or add a boot receiver without approval.
- User guidance: Document any user action required on the actual target device.
- Diagnostics: Record only events the app can actually observe.
- Tests: Reboot the target phone, record actual startup behavior, permissions, overlay state, and required user actions.
F-18 — Foreground service cannot start or is stopped (only if such a service is approved)
- Possible detection: Start exception, lifecycle callback, service state check, or a later reconciliation.
- Confidence: Follow the exact API result; distinguish rejected start from later termination.
- Expected state: Runtime is not reported active without the required evidence.
- Permitted recovery: Retry only when current start restrictions, service type, user intent, prerequisites, and retry limits allow it.
- Forbidden behavior: Do not use a foreground service as a generic “always-on” promise or misdeclare its service type.
- User guidance: Explain the observed failure and next supported step.
- Diagnostics: Service type, start context, API/exception category, attempt, outcome.
- Tests: Target-SDK-specific start scenarios, notification behavior, and user-stop behavior.
F-19 — Device background-management behavior is suspected
- Possible detection: Repeated, reproducible symptoms on a specific device/build after other observable causes have been checked.
- Confidence: Low until supported by logs, reproduction, or documented platform/vendor behavior.
- Expected state: Keep actual detected states; the suspected root cause is separate diagnostic metadata, not a definitive product status.
- Permitted recovery: Provide optional, evidence-based guidance after verification. Test the effect of any user setting change.
- Forbidden behavior: Do not automatically manipulate undocumented vendor settings or claim battery optimization is responsible without evidence.
- User guidance: Explain what is known, what is suspected, and how to gather additional evidence.
- Diagnostics: Device model/build, reproduction steps, timestamps, observed component states, and test conditions.
- Tests: Controlled repeat tests on the target device; compare before/after observations and preserve uncertainty if inconclusive.
F-20 — Diagnostic storage fails or reaches its limit
- Possible detection: Storage exception, database/file failure, or retention cleanup failure.
- Confidence: High for an observed write/cleanup error.
- Expected state: Record/logging status may be degraded; primary runtime and overlay states remain independently evaluated.
- Permitted recovery: Apply bounded cleanup or a safe fallback if designed.
- Forbidden behavior: Do not recursively log logging failures or let repeated diagnostics retries drain battery.
- User guidance: Show a nonfatal warning only if useful.
- Diagnostics: A fallback signal if available; avoid endless attempts to write the same failure to the broken store.
- Tests: Fake storage failures, retention limits, and recovery from a write error.
F-21 — Conflicting or stale state observations
- Possible detection: A stale cached value conflicts with a fresh platform observation, or callbacks arrive out of order.
- Confidence: The conflict is observable; its exact cause may require investigation.
- Expected state: Prefer current authoritative observations and represent uncertainty where the conflict cannot be resolved.
- Permitted recovery: Reconcile state, serialize operations where practical, re-check current conditions.
- Forbidden behavior: Do not let a stale ACTIVE cache override a known missing permission or known disconnection.
- User guidance: Show checking/recovery status rather than a false success state.
- Diagnostics: Record the conflicting observations and reconciliation outcome.
- Tests: Out-of-order completion, rapid enable/disable, Activity recreation, and state refresh tests.
5. Recovery Policy Requirements
5.1 Recovery decision checklist
Before each automatic retry, verify all applicable conditions:
- Desired state is still ENABLED.
- The failure type is eligible for recovery.
- Current permissions and prerequisites are suitable.
- The component is not already starting, stopping, or recovering.
- The operation is allowed in the current Android lifecycle/background context.
- The retry budget and cooldown permit another attempt.
- The previous attempt's outcome has been reconciled.
- The proposed action does not bypass user or operating-system controls.
If any check fails, do not retry. Expose the corresponding blocked/unknown/recovery-required state.
5.2 Retry values must not be hidden constants
Before production recovery logic is implemented, define and review:
- maximum attempts per failure episode: [ENGINEERING PROPOSAL REQUIRED];
- cooldown/backoff: [ENGINEERING PROPOSAL REQUIRED];
- conditions for opening a new failure episode: [ENGINEERING PROPOSAL REQUIRED];
- manual-retry behavior: [ENGINEERING PROPOSAL REQUIRED];
- event aggregation/rate limit: [ENGINEERING PROPOSAL REQUIRED].
Use one documented policy rather than scattered, unexplained integer literals.
5.3 Recovery outcomes
Every recovery attempt should end in one of:
- Succeeded: the required outcome is confirmed by suitable evidence.
- Failed: the operation reports failure.
- Blocked: a required permission, user decision, or platform condition prevents the attempt.
- Unknown: available evidence cannot establish whether the intended outcome was achieved.
Do not turn Blocked or Unknown into Succeeded.
6. Status Mapping Requirements
Use the state definitions from STATE_AND_LIFECYCLE.md. At minimum:
- missing overlay permission -> Overlay Permission Required;
- Accessibility is required and confirmed disabled -> Accessibility Disabled;
- Accessibility is required, enabled, and confirmed disconnected -> Accessibility Disconnected;
- interruption under investigation -> Interrupted or Checking Status;
- bounded recovery in progress -> Recovering;
- retry exhausted or user action necessary -> Recovery Required;
- desired state is explicitly disabled -> Disabled, except where UI needs to indicate cleanup still pending;
- confirmed ready state -> Active.
A single summary label must not erase the secondary component statuses. Studio should make it possible to inspect the overlay and Accessibility conditions independently when relevant.
7. Test Requirements
Each matrix row should become one or more tests in ACCEPTANCE_TESTS.md and/or FAILURE_INJECTION_PLAN.md.

Required test evidence includes:
- unit tests for state transitions, retry bounds, and duplicate prevention;
- Android/instrumentation tests for behavior dependent on platform APIs, where practical;
- actual-device tests for overlay, Accessibility, process lifecycle, reboot, and Realme-specific behavior;
- explicit documentation of scenarios that could not be reproduced or tested.
A test record should include:
- failure ID;
- device/API/OS build where relevant;
- setup and permissions;
- exact steps;
- expected state/result;
- actual result;
- diagnostic evidence;
- pass/fail/not tested;
- follow-up bug/task ID.
8. Implementation Acceptance Checklist for Agents
Before marking failure handling complete, verify:
- [ ] Symptom is not confused with an unconfirmed root cause.
- [ ] Current permissions and prerequisites are re-checked where appropriate.
- [ ] Desired state is respected.
- [ ] Overlay and Accessibility state remain independent.
- [ ] Automatic retries are bounded and idempotent where practical.
- [ ] Missing permissions lead to user guidance, not futile retry.
- [ ] User force-stop is not bypassed.
- [ ] Recovery-required states have an understandable next step.
- [ ] Startup and cleanup failures do not falsely report success.
- [ ] Diagnostic events represent observed facts and are bounded.
- [ ] Tests cover both success and failure paths.
- [ ] Actual device results are separated from unit-test results.
- [ ] Related requirements and documentation are updated.
- [ ] No unapproved features, permissions, or dependencies were added.
9. Open Decisions and Engineering Proposals
ID	Item	Current status
FM-01	Primary button action and required capabilities	Approved: Double-tap lock screen, long-press home/back
FM-02	Which failures receive automatic recovery vs. user action only	Review against final service architecture
FM-03	Retry count and cooldown/backoff	[ENGINEERING PROPOSAL REQUIRED]
FM-04	Diagnostic event size/retention limits	[USER INPUT REQUIRED / PROPOSAL REQUIRED]
FM-05	Exact supported Android/API range	Approved: minSdk 28 (Android 9.0 Pie) to Android 14+ (API 34)
FM-06	Foreground service necessity/type	Only after architecture and official-rule review
FM-07	Reboot auto-start requirement	Approved: Manual reopening required in v0.1 (no boot receiver)
FM-08	Severity/triage thresholds for internal issue tracking	Owner may revise
FM-09	Whether a recent-events UI ships in v0.1	Currently intended, subject to MVP review


10. Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Status: Draft / Approved
- Approved primary action: Double-tap lock screen (GLOBAL_ACTION_LOCK_SCREEN), Long-press home/back (GLOBAL_ACTION_HOME/BACK)
- Approved retry policy: [ENGINEERING PROPOSAL REQUIRED]
- Review date: 2026-10-29
- Accepted deviations: None
- Notes: Recovery policy approved: max 3 retries, exponential backoff (1s, 2s, 5s), 60s cooldown reset.
This matrix is a controlled draft. If an actual failure differs from these assumptions, create a bug report with evidence and update this document only after the finding is reproduced or the revised behavior is explicitly approved.
