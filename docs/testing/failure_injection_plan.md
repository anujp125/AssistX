AssistX — Failure Injection Plan
Document ID: ASSISTX-FI-001
Version: 0.1
Status: Draft — owner and technical review required
Last updated: 2026-10-29
Related documents: FAILURE_AND_RECOVERY_MATRIX.md, STATE_AND_LIFECYCLE.md, TEST_STRATEGY.md, ACCEPTANCE_TESTS.md, REAL_DEVICE_TEST_MATRIX.md, SPRINT_PLAN.md
Instruction for agents/testers: This document defines tests to deliberately exercise failure handling. It is not evidence that failures have already been injected or handled successfully. Prefer deterministic test doubles for pure logic. Use real-device failure injection only when safe, reversible, and necessary. Do not bypass Android user controls, manipulate undocumented system settings automatically, or claim a failure was reproduced if it was only simulated.

1. Purpose
AssistX's reliability requirements cannot be demonstrated through happy-path tests alone. Failure injection checks whether the app responds safely when an operation fails, a permission is missing, state becomes uncertain, or concurrent requests conflict.

This plan defines:
- what failure to inject;
- whether to use a fake, instrumentation test, or physical device;
- how to trigger it safely;
- what behavior must be observed;
- how to clean up;
- what evidence to record.
The main goals are to verify truthful status, bounded recovery, idempotent operations, no duplicate overlays, and respect for user intent.
2. Safety and Test Boundaries
Allowed techniques
- Unit-test fakes/stubs that return controlled outcomes.
- Dependency injection at the narrow boundary needed for the test, without adding a framework unnecessarily.
- Instrumentation tests using a test build and controlled test data.
- Manually changing a permission through Android's normal settings UI on the test device.
- Ordinary app lifecycle tests, with unsaved work secured.
- Observing and recording actual device behavior.
Not allowed
- Attempts to bypass Android permission requirements.
- Attempts to evade force-stop or user-requested stopping.
- Automated manipulation of undocumented Realme system settings.
- Infinite restart loops or aggressive keep-alive behavior to “stress test” persistence.
- Destructive testing on a device containing important unsaved data.
- Marking a simulated fake failure as proof that the corresponding physical-device condition was reproduced.
- Deleting diagnostics before they have been captured for a failure under investigation.
For potentially disruptive cases, record the starting state and have a recovery path before beginning. Use a dedicated test build and test environment when possible.
3. Injection Levels
Level	Method	What it proves	What it does not prove
L1 — Pure logic	Fake repository/adapter or deterministic state event	State mapping, decision policy, retry bounds, error handling	Real Android API behavior
L2 — Android integration	Instrumentation or test build exercising Android components	Integration with framework APIs in the tested environment	Manufacturer behavior in all conditions
L3 — Physical device	Manual or automated execution on the recorded phone/build	Observed behavior on that device under recorded conditions	General compatibility across all devices
L4 — Long-duration observation	Defined use period with repeated state checks	Intermittent failure/resource observations over that period	Guaranteed future reliability


A case may require more than one level. Record each result separately.
4. Preconditions Before Every Injection
- [ ] Test case ID is selected.
- [ ] App build/version and Git commit are recorded.
- [ ] Device/OS/build is recorded for device tests.
- [ ] Initial desired state and relevant permissions are recorded.
- [ ] The failure trigger and expected result are clear.
- [ ] Diagnostics or logs can be captured after the injection.
- [ ] Recovery/cleanup steps are known.
- [ ] Existing user work and personal data are safe.
- [ ] Relevant test environment is isolated from release/prod data.
- [ ] The test will not use an unsupported permission or force-stop bypass.
If a precondition is not met, mark the test BLOCKED rather than improvising an unsafe trigger.
5. General Evidence Record
For each injection, capture:
- Failure ID: [FILL IN]
- Run ID: [FILL IN]
- Injection level: L1 / L2 / L3 / L4
- Date/time: [FILL IN]
- App version/commit: [FILL IN]
- Device/OS/API/build (if applicable): [FILL IN]
- Initial desired state: [FILL IN]
- Initial overlay permission/attachment: [FILL IN]
- Initial Accessibility state (if applicable): [FILL IN]
- Exact trigger/method: [FILL IN]
- Expected state/result: [FILL IN]
- Actual state/result: [FILL IN]
- Diagnostic evidence: [FILL IN]
- Recovery/cleanup performed: [FILL IN]
- Result: PASS / FAIL / BLOCKED / NOT RUN / NOT APPLICABLE / FLAKY
- Bug/task ID: [FILL IN]
- Root cause: Confirmed / Suspected / Unknown
- Rationale/evidence for root-cause label: [FILL IN]
6. Failure Injection Matrix
FI-001 — Overlay permission absent
Level: L2/L3
Related failure: F-01
Method:
1. Use a test install or Android settings UI to ensure AssistX does not have overlay permission.
2. Launch AssistX and request Enable.
3. Observe the status and operation attempts.
4. Return from settings without granting permission, then repeat after granting it.
Expected:
- Current permission is checked.
- Missing permission is represented as a blocker.
- The app does not claim the overlay is active.
- No unbounded attach retries or repeated settings launches occur.
- After a confirmed grant, startup follows the approved policy.
Cleanup: Leave permission in the agreed test state or restore its original value.
Evidence: State, displayed message, attach attempts, and diagnostics.
FI-002 — Permission check returns unknown/error
Level: L1
Related failures: F-01, F-02, F-21
Method:
1. Make the permission adapter return an unknown or controlled error result.
2. Request Enable or refresh status.
3. Observe derived UI status and recovery decisions.
Expected:
- Unknown/error does not map to permission granted.
- Status remains checking/unknown or displays an appropriate error.
- The app avoids false success and does not enter a retry loop.
Cleanup: Restore the normal fake result.
Evidence: Unit-test output and resulting state.
FI-003 — Overlay attachment throws/returns failure
Level: L1; L2 if a platform failure can be safely reproduced
Related failure: F-03
Method:
1. Configure a fake overlay manager to fail the attachment operation.
2. Request Enable.
3. Verify returned outcome, runtime/overlay states, and diagnostic event.
4. Repeat with a partial-initialization outcome if supported by the abstraction.
Expected:
- No false ATTACHED or fully ACTIVE state.
- Failure category/context is recorded.
- Cleanup of partial state is safe.
- Recovery is attempted only if policy permits it.
Cleanup: Reset fake; verify no test-owned object remains registered.
FI-004 — Overlay removal fails
Level: L1
Related failure: F-05
Method:
1. Start with a simulated attached overlay.
2. Configure remove to fail.
3. Request Disable.
4. Request Disable again.
Expected:
- Desired state stays DISABLED.
- Cleanup failure is visible or recorded.
- Repeated cleanup is safe.
- No automatic re-enable or overlay recreation occurs.
Cleanup: Reset fake state; ensure no real overlay was left on the device.
FI-005 — Duplicate or overlapping start requests
Level: L1/L2
Related failures: F-04, F-21
Method:
1. Hold the first start/attach operation in a pending state.
2. Issue a second Enable/start request.
3. Complete operations in both normal and out-of-order sequences.
4. Inspect operation count and final overlay state.
Expected:
- At most one overlay attachment is active.
- Duplicate calls are deduplicated or reconciled.
- State reflects confirmed outcomes and latest user intent.
Cleanup: Complete/cancel pending fakes and reset test state.
FI-006 — Disable arrives while startup is pending
Level: L1
Related failures: F-05, F-11, F-13, F-21
Method:
1. Start with desired state disabled.
2. Request Enable and pause startup completion.
3. Request Disable before startup completes.
4. Complete the earlier startup callback late.
5. Inspect final state and whether a late callback reattaches the overlay.
Expected:
- Latest user intent remains disabled.
- Late start completion cannot override the disable request.
- No overlay is left attached or re-created without an explicit new Enable request.
- Cleanup and diagnostics are handled safely.
Cleanup: Reset all pending operations and state.
FI-007 — Recovery policy reaches retry limit
Level: L1
Related failures: F-12, F-19
Method:
1. Use the approved retry policy values.
2. Inject a repeatable eligible failure for each retry.
3. Continue through the configured limit.
4. Request another automatic reconciliation without a new eligible event/user action.
Expected:
- Retry count follows the policy exactly.
- Automatic attempts stop at the limit.
- Status becomes recovery-required/failed according to the approved model.
- UI re-rendering or Activity recreation does not reset the budget.
Blocker: Do not execute this as a final acceptance test until retry values and reset conditions are reviewed.
Cleanup: Reset failure episode using the documented test fixture; do not change production policy solely for a test.
FI-008 — User disables during recovery
Level: L1/L2
Related failures: F-13, F-21
Method:
1. Trigger an eligible recovery attempt.
2. While it is pending, send Disable.
3. Complete the recovery attempt.
4. Observe whether any additional retry occurs.
Expected:
- Desired state becomes disabled.
- Pending recovery is cancelled or its result is reconciled safely.
- No subsequent automatic restart occurs while disabled.
- Any partial overlay is cleaned up where permitted.
FI-009 — Permission blocker prevents retry
Level: L1/L3
Related failures: F-01, F-02, F-12
Method:
1. Set desired state enabled.
2. Make a required permission check report missing.
3. Repeatedly trigger reconciliation using a controlled test loop or repeated explicit checks.
4. Inspect platform operation counts and UI status.
Expected:
- The app does not repeatedly attempt an operation that cannot succeed without user action.
- The permission-required status remains.
- No settings screens are opened automatically.
- Recovery becomes eligible again only after a new current check and approved conditions.
Cleanup: Restore original permission state.
FI-010 — Accessibility enabled but disconnected (conditional)
Level: L1/L2/L3
Related failures: F-08, F-09
Applicability: Only if the approved feature requires Accessibility.
Method:
1. Unit level: configure the state source to report enabled setting plus disconnected service.
2. Integration/device level: attempt a safe, documented disconnect/reconnect scenario if reproducible.
3. Compare with the disabled-setting case.
4. Observe overlay status independently.
Expected:
- State is disconnected or unknown based on evidence, not falsely disabled.
- Overlay state is not automatically changed to an error solely because Accessibility disconnected.
- Recovery remains bounded.
Cleanup: Restore service state using normal Android settings.
Note: If a real disconnect cannot be safely or reliably induced, record L1 coverage and mark L3 as BLOCKED or NOT RUN; do not claim physical reproduction.
FI-011 — Accessibility setting disabled (conditional)
Level: L1/L3
Related failure: F-07
Applicability: Only if Accessibility is required.
Method:
1. Verify service is enabled.
2. Disable it using Android Accessibility settings.
3. Return to AssistX and inspect current state.
4. Re-enable it and re-check.
Expected:
- Disabled setting is detected where reliable.
- User guidance is given.
- AssistX does not attempt to silently enable it.
- Overlay status remains a separate dimension.
Cleanup: Restore the original setting if appropriate and desired by the tester.
FI-012 — State observation becomes stale or out of order
Level: L1
Related failure: F-21
Method:
1. Create two controlled observations with known order.
2. Deliver an older success result after a newer permission failure or Disable request.
3. Reconcile state.
4. Repeat with asynchronous startup/remove completion.
Expected:
- Stale success does not override newer authoritative user intent or current permission observation.
- Unknown states are retained when ordering cannot be resolved.
- Any reconciliation policy is deterministic and testable.
FI-013 — Diagnostic store throws on write
Level: L1
Related failure: F-20
Method:
1. Configure the diagnostics store to throw on the next write.
2. Perform a normal or failing overlay operation.
3. Inspect the primary operation outcome.
4. Check that logging failure does not recursively trigger logging or repeated recovery.
Expected:
- Primary feature behavior is not normally blocked by a diagnostics write failure.
- Failure is handled nonfatally.
- No unbounded recursive logging occurs.
Cleanup: Restore the fake store; verify a subsequent event can be written.
FI-014 — Diagnostics retention bound is exceeded
Level: L1
Related failure: F-20
Method:
1. Populate test storage above the approved limit.
2. Run retention cleanup.
3. Write a new event and inspect the resulting records.
Expected:
- Storage is bounded according to the approved policy.
- Cleanup is deterministic.
- Current important events can still be recorded where storage is available.
Blocker: The retention limit must be approved before acceptance.
FI-015 — Runtime startup returns failure or unknown outcome
Level: L1/L2
Related failure: F-10
Method:
1. Inject startup failure.
2. Repeat with an unknown/timeout outcome if the API abstraction allows it.
3. Inspect runtime state and recovery eligibility.
Expected:
- Failure or uncertainty is explicit.
- The app does not report ACTIVE without confirmation.
- Recovery is attempted only when appropriate.
FI-016 — App resumes from system settings without a change
Level: L3
Related failure: F-15
Method:
1. Start with overlay permission missing.
2. Open system settings.
3. Return without changing anything.
4. Inspect state and any automatic operations.
Expected:
- Current permission is re-checked.
- Missing permission remains a blocker.
- No false grant or repeated settings loop occurs.
FI-017 — Process recreation without force-stop
Level: L2/L3
Related failure: F-11
Method:
1. Use a documented method to cause process recreation/termination without intentionally force-stopping the app, if feasible.
2. Reopen or observe the supported startup path.
3. Inspect preferences and current state.
4. Compare results with the exact test method.
Expected:
- Desired state follows the approved persistence rule.
- Runtime/overlay/Accessibility health is re-evaluated.
- No stale active state is treated as current truth.
- The report distinguishes the injected method from actual operating-system termination causes.
Note: Do not use am force-stop or an equivalent force-stop action and call it ordinary process death.
FI-018 — Force-stop and reboot handled as separate scenarios
Level: L3
Related failures: F-16, F-17
Method:
1. Run a force-stop test and record its outcome.
2. Restore the app through a normal user launch.
3. In a separate run, reboot the device and record behavior.
4. Do not infer that a result in one scenario applies to the other.
Expected:
- AssistX does not bypass force-stop.
- Reboot behavior is recorded based on actual observation and verified platform constraints.
- No immediate auto-start guarantee is assumed.
- Any required user action is documented.
FI-019 — Suspected Realme background-management failure
Level: L3/L4
Related failure: F-19
Method:
1. Record device/build and all observable AssistX states.
2. Reproduce the symptom under current normal settings.
3. Capture evidence before changing system settings or reopening Studio, if safe.
4. Change only one documented/relevant setting at a time, if the investigation justifies it.
5. Repeat the same procedure and compare outcomes.
Expected:
- The report distinguishes symptom, hypothesis, and confirmed cause.
- It does not state “battery optimization killed AssistX” unless supported by reproducible evidence.
- Any workaround is optional, user-controlled, and documented.
FI-020 — Repeated callback or UI render does not trigger repeated side effects
Level: L1/L2
Related failures: F-04, F-12, F-14, F-21
Method:
1. Invoke status rendering/recomposition or repeated callbacks under the test harness.
2. Keep desired state unchanged.
3. Count service starts, overlay attachment calls, and recovery attempts.
Expected:
- Rendering alone does not repeatedly start services or attach overlays.
- Repeated callbacks are idempotent or safely reconciled.
- Counters/logs do not grow without bounds.
7. Injection Implementation Guidance
7.1 Prefer narrow test seams
Use small interfaces/fakes at boundaries such as:
- overlay add/remove operation;
- permission-state query;
- startup/stop operation;
- Accessibility availability/action outcome where applicable;
- preferences read/write;
- diagnostics append/read.
Do not refactor the whole architecture solely to accommodate failure injection. Introduce a test seam when it clearly improves verification without disproportionate complexity.
7.2 Keep test controls out of production behavior
- Do not expose a hidden production screen that can disable arbitrary system functionality just to test failures.
- Do not leave hardcoded “always fail” switches in release code.
- Prefer unit-test fakes, debug-only instrumentation, or manual system settings.
- If a debug-only test control is needed, guard it clearly and verify it cannot be enabled in the release build.
7.3 Preserve retry policy
Do not modify retry counts or cooldowns during tests unless using an injectable test policy. Tests should verify the same policy as production or clearly identify when a controlled test policy is being used.
8. Cleanup Checklist
After an injection:
- [ ] Restore fake adapters and controlled test state.
- [ ] Stop test-owned background work.
- [ ] Verify no test overlay/service remains.
- [ ] Restore permission settings changed solely for the test, if appropriate.
- [ ] Capture diagnostics before clearing test data.
- [ ] Record any configuration changes made.
- [ ] Confirm the next test starts from its documented preconditions.
- [ ] Do not delete evidence needed for a bug report.
9. Failure Injection Result Table
Update this table after execution; blank values mean untested, not passed.
Failure ID	Level planned	Run ID	Result	Evidence reference	Bug/task	Root cause status
FI-001	L2/L3	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-002	L1	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-003	L1/L2	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-004	L1	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-005	L1/L2	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-006	L1	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-007	L1	Unit test	Ready (Approved: 3 retries, 1s/2s/5s)	Deterministic backoff	TBD	Bounded recovery
FI-008	L1/L2	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-009	L1/L3	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-010	L1/L2/L3	[FILL IN]	Conditional	[FILL IN]	[FILL IN]	Unknown
FI-011	L1/L3	[FILL IN]	Conditional	[FILL IN]	[FILL IN]	Unknown
FI-012	L1	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-013	L1	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-014	L1	Unit test	Ready (Approved: Room DB, max 500 events / 2 MB)	FIFO auto-prune	TBD	Bounded storage
FI-015	L1/L2	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-016	L3	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-017	L2/L3	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-018	L3	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-019	L3/L4	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown
FI-020	L1/L2	[FILL IN]	NOT RUN	[FILL IN]	[FILL IN]	Unknown


10. Exit Criteria
Failure injection is sufficiently complete for a milestone only when:
- all injection cases applicable to the implemented feature have a recorded result;
- blocked/not-run cases have a reason and an assigned follow-up if material;
- no critical failure is ignored;
- retry bounds, duplicate prevention, and disablement invariants are tested;
- platform/device results are distinguished from simulations;
- relevant defects are fixed or accepted with recorded risk;
- diagnostics and cleanup behavior are verified;
- the owner/reviewer accepts any exceptions.
Do not require every conditional case to pass before its feature is approved; instead, resolve applicability explicitly and retain traceability.
11. Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Plan status: Draft / Approved
- Approved injection environment: Realme Narzo 30 4G (RMX2156, realme UI 3.0, Android 12) + API 31/34 Emulators
- Approved retry policy: [ENGINEERING PROPOSAL REQUIRED]
- Review date: 2026-10-29
- Accepted limitations: None