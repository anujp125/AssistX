AssistX — Test Strategy
Document ID: ASSISTX-TEST-001
Version: 0.1
Status: Draft — owner review required
Last updated: 2026-10-29
Related documents: SRS.md, MVP_SCOPE.md, PRODUCT_ROADMAP.md, SYSTEM_ARCHITECTURE.md, STATE_AND_LIFECYCLE.md, FAILURE_AND_RECOVERY_MATRIX.md, ACCEPTANCE_TESTS.md
Mandatory instruction for AI agents: Tests are part of implementation, not an optional final activity. Report only checks actually executed. Never mark a test as passed because a test file was written, a code path looks correct, or a build succeeded. Keep automated-test evidence separate from actual-device evidence. If a test cannot be run, report it as NOT RUN and explain why.

1. Purpose
This document defines how AssistX is tested throughout development and before the v0.1 release. It gives the product owner and AI coding agents a repeatable strategy for verifying functionality, lifecycle behavior, recovery, permissions, privacy, and compatibility.

AssistX is a reliability-first Android app. Many of its important behaviors depend on Android system settings, component lifecycle, and manufacturer-specific policies. Therefore, unit tests alone are not sufficient. The test strategy combines automated tests with Android instrumentation and deliberate testing on the target Realme device.
2. Testing Objectives
Testing must establish that:
1. AssistX obeys the approved MVP scope.
2. User intent is distinct from observed runtime health.
3. Overlay permission, overlay attachment, runtime lifecycle, and Accessibility state are not incorrectly conflated.
4. Repeated operations do not create duplicate overlays or uncontrolled recovery loops.
5. Missing permissions and unavailable services produce accurate status and useful guidance.
6. Automatic recovery respects user intent and Android restrictions.
7. Preferences and diagnostics behave according to the approved data rules.
8. Core functionality remains independent of the Studio Activity staying open.
9. Android-specific behavior is verified in an appropriate environment.
10. Agents provide honest, reproducible evidence for each sprint.
3. Testing Principles
3.1 Risk-based testing
Prioritize behavior that could break the core use case, mislead the user, violate permissions, cause battery drain, or trigger repeated service/overlay creation.

Highest-priority test areas:
- enable/disable and user intent;
- overlay permission and attach/remove;
- state transitions;
- lifecycle coordination;
- bounded recovery and duplicate prevention;
- Accessibility status/action, if needed by the approved feature;
- process interruption, force-stop, and reboot semantics;
- diagnostics retention and privacy.
3.2 Test the contract, not implementation trivia
Tests should validate requirements and observable behavior rather than mirror private implementation details. Prefer tests that remain useful if an internal class is refactored without changing behavior.
3.3 Keep tests deterministic
- Use controlled fake dependencies for pure logic.
- Avoid real delays and sleeps when an injected clock, scheduler, or deterministic event can be used.
- Avoid network dependencies; the MVP has no backend.
- Keep unit tests independent of the developer's personal device state.
- Separate tests requiring special Android settings from ordinary automated tests.
3.4 Keep tests scoped
Every sprint task must identify its relevant tests. Do not use a feature task as a reason to rewrite the entire test suite or refactor unrelated production code.
3.5 Preserve truthful evidence
A successful compile is not a functional test. A unit test does not prove Android system integration. A manual run on one device does not prove broad compatibility. Report the level of evidence accurately.
4. Test Levels
4.1 Static checks and code review
Purpose:
- identify compilation errors and obvious type/API misuse;
- review manifest permissions and exported components;
- identify unsafe permission handling, unbounded retries, or excessive logging;
- inspect dependency and privacy changes.
Expected checks as the project supports them:
- Kotlin compilation;
- Android/Gradle build;
- lint or equivalent Android static analysis;
- formatting/style check if the project adopts one;
- manifest review;
- dependency and secret review.
Static checks complement, but do not replace, behavior tests.
4.2 Unit tests
Use unit tests for logic that can reasonably be tested without a full Android device.

Priority candidates:
- desired-state and state-transition logic;
- derived UI status mapping;
- recovery eligibility and retry limits;
- idempotent start/stop decision logic;
- overlay duplicate-prevention decisions;
- safe default preferences;
- diagnostic event creation;
- diagnostic retention/size policy;
- error classification and blocked/unknown outcomes.
Unit-test guidelines:
- assert observable outputs and state transitions;
- test both success and failure conditions;
- cover boundary cases and invalid inputs;
- use fakes or mocks only where useful;
- do not mock the system so extensively that the test merely repeats the implementation;
- keep tests fast and deterministic.
4.3 Android instrumentation tests
Use instrumentation tests when code depends on the Android runtime or platform framework and the behavior can be exercised reliably in an emulator/device environment.

Candidate areas:
- Activity lifecycle and state refresh;
- overlay permission-check behavior;
- manifest configuration;
- preference persistence using the actual selected implementation;
- service binding/lifecycle where deterministic;
- UI rendering and status presentation;
- Accessibility integration if applicable.
Instrumentation coverage depends on the selected SDK, emulator/device setup, and APIs. If a scenario relies on settings controlled by the user or device manufacturer and cannot be exercised reliably in CI, document a separate manual device test.
4.4 Real-device tests
Real-device tests are mandatory for critical lifecycle and permission behavior, especially on the initial target device.

Initial test device, as reported by the owner:
- Realme Narzo 30 4G;
- model RMX2156;
- Android 12;
- realme UI 3.0;
- build RMX2156_11_F.19.
Device tests should cover:
- floating button appearance and interaction;
- overlay permission grants/revocation;
- Studio opening/closing and app switching;
- screen lock/unlock;
- Accessibility enabled/disabled/disconnected conditions if applicable;
- process interruption and recovery behavior;
- user force-stop behavior;
- reboot behavior;
- long-duration observation;
- battery/memory/logging observations.
Do not claim other Android versions or devices are supported based solely on the target phone passing.
5. Requirement Traceability
Every Must requirement in SRS.md must map to one or more verification cases. Use stable IDs.

Suggested ID formats:
- Unit: UT-###
- Android/instrumentation: IT-###
- Acceptance: AT-###
- Real-device: RT-###
- Failure injection: FI-###
Examples:
- FR-005 (single overlay instance) → UT-..., IT-... and/or RT-...
- FR-010 (Accessibility disabled guidance, when applicable) → AT-... and RT-...
- FR-015 (bounded recovery) → unit tests for retry policy plus applicable failure scenario tests
- FR-021 (Studio lifecycle independence) → RT-...
The exact mapping belongs in REQUIREMENTS_TRACEABILITY.md. Do not invent test IDs in code comments without updating the test inventory.
6. Test Environment and Setup
6.1 Environments
Maintain separate records for:
- local development environment;
- CI environment, if configured;
- Android emulator/device used for instrumentation tests;
- target Realme physical device;
- any additional device or OS version explicitly included in compatibility claims.
Record as relevant:
- operating-system version and build;
- Android API level;
- device/model;
- app version/build variant;
- target SDK and minimum SDK;
- permission settings;
- whether a debugger or test instrumentation was attached;
- network state where relevant;
- battery/background settings when they form part of the scenario.
Do not include personal identifiers or unrelated device data in test reports.
6.2 Target phone baseline
Before a test cycle, record the actual device and build observed on the phone rather than relying only on the historical project notes.
- Device/model: TBD — verify at test execution
- Android version/API: TBD — verify at test execution
- UI/build: TBD — verify at test execution
- AssistX version/commit: TBD
- Test date/time: TBD
- Relevant permission states: TBD
6.3 Reproducible build and test commands
The project has not yet finalized Gradle configuration, test libraries, package ID, or build variants. Therefore, the commands below are templates to be confirmed after M2 (project foundation).

Typical Gradle command candidates, if the project uses the standard wrapper:
# Windows PowerShell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug

# macOS/Linux
./gradlew testDebugUnitTest
./gradlew assembleDebug
Instrumentation command candidate:
# Only after a test device/emulator and instrumentation setup are configured
./gradlew connectedDebugAndroidTest
Agent rules:
- inspect the actual Gradle tasks before using or documenting commands;
- use gradlew.bat on Windows if the wrapper is present;
- do not report a command as successful unless it was executed and returned success;
- record the exact command, exit result, and relevant summary;
- if dependencies, SDK, emulator, or device access are missing, state the blocker and mark the test NOT RUN.
The command list must be updated to match the actual project once the foundation sprint is complete.
7. Required Test Areas
7.1 Desired state and runtime state
Verify:
- enabling updates desired state but does not instantly imply runtime readiness;
- disabled intent prevents automatic recovery;
- runtime state reflects operation outcomes;
- stale persisted runtime values are not restored as current truth;
- UNKNOWN remains available for conditions the app cannot determine;
- UI status is derived consistently from state dimensions.
7.2 Overlay behavior
Verify:
- permission granted → overlay can be requested;
- permission missing → actionable permission state and no false active state;
- permission revoked → status refresh and no futile retry loop;
- repeated enable/start → one overlay maximum;
- repeated disable/stop → safe cleanup;
- attachment failure → observable error state;
- removal failure → do not claim confirmed detach without evidence;
- Activity recreation and Studio open/close do not create duplicates;
- overlay state remains independent from Accessibility status.
7.3 Accessibility behavior (conditional)
Only test this area if the approved primary action requires Accessibility.

Verify:
- service declaration/configuration matches the approved function;
- setting disabled is distinguished from service disconnected when detectable;
- the dependent action is unavailable while required prerequisites are missing;
- the service does not perform unapproved actions or inspect unrelated app content;
- AssistX does not claim it can silently re-enable the setting;
- API availability and action outcomes are handled;
- real-device behavior matches the documented claim.
7.4 Runtime and recovery
Verify:
- startup success and failure paths;
- partial initialization cleanup;
- disabled intent prevents recovery;
- permission blockers stop futile attempts;
- retries are bounded and use the approved policy;
- duplicate or concurrent start/stop requests are reconciled safely;
- recovery outcomes distinguish success, failed, blocked, and unknown;
- retry exhaustion presents a recovery-required state;
- process death, force-stop, and reboot are tested as different scenarios;
- no unsupported keep-alive or background-start workaround is used.
7.5 Preferences and diagnostics
Verify:
- defaults are safe and documented;
- desired state persists according to the approved policy;
- stored values are validated and recover safely if missing or malformed;
- important state transitions produce diagnostic events where feasible;
- event records include useful context but avoid unnecessary personal/sensitive data;
- retention limits are enforced;
- diagnostic storage failures are handled without recursive logging or core-feature failure;
- there is no unapproved outbound telemetry or network dependency.
7.6 User interface and recovery guidance
Verify:
- labels correspond to documented states;
- the app does not show success before the necessary operation succeeds;
- permission instructions identify the relevant setting;
- secondary component status remains inspectable;
- opening settings is not treated as proof of a change;
- errors are understandable and do not expose stack traces or sensitive details;
- accessibility/usability checks are performed for the chosen UI implementation.
8. Regression Testing Rules
Run tests affected by the change plus relevant core regressions.

Always consider regression tests when changing:
- state definitions or state mapping;
- enable/disable logic;
- overlay attach/remove behavior;
- service startup/shutdown;
- permission checks or settings navigation;
- Accessibility integration;
- recovery policy;
- persistence schemas;
- diagnostic retention;
- manifest permissions, components, or service types;
- Gradle/SDK/target-SDK settings.
For changes touching lifecycle or permissions, the minimal regression set should include:
1. build and relevant unit tests;
2. state/recovery tests;
3. overlay lifecycle tests where available;
4. a target-device scenario if the behavior depends on Android or manufacturer implementation.
A change must not be merged solely because the modified file compiles if a relevant failure mode has not been checked.
9. Negative, Boundary, and Failure Testing
Every substantial feature should test at least:
- normal success;
- prerequisite missing;
- operation failure;
- repeated or duplicate request;
- state change while an operation is in progress;
- process/activity lifecycle change where relevant;
- recovery blocked by user intent or platform rule.
For lifecycle-sensitive components, consider:
- rapid Enable → Disable → Enable;
- Disable during startup;
- permission revoked during active use;
- a start operation returning late after Disable;
- an overlay attach callback arriving after a removal request;
- repeated callbacks;
- diagnostic storage failing while the runtime continues.
These cases may expose race conditions that simple happy-path testing misses.
10. Flaky Test Policy
A flaky test is a test that passes and fails under apparently equivalent conditions without a code change or a known controlled cause.

When a test is flaky:
1. capture the exact command, environment, and output;
2. attempt a bounded reproduction;
3. identify whether the flakiness comes from timing, platform state, test setup, or product behavior;
4. do not silently delete, permanently skip, or weaken the assertion;
5. quarantine or mark the test only through a documented review decision;
6. create a tracking item with impact and proposed repair;
7. keep the relevant risk visible until resolved.
Tests involving Android settings, process termination, and reboot may need manual steps. A documented manual test is preferable to a misleading or nondeterministic CI test.
11. Test Result Statuses
Use these statuses in sprint reports and test matrices:
- PASS — the defined steps ran and expected results were observed.
- FAIL — the test ran and expected results were not observed.
- BLOCKED — setup/environment/permission dependency prevented execution.
- NOT RUN — the test was not executed.
- NOT APPLICABLE — the test is irrelevant to the approved feature scope, with a reason.
- FLAKY — repeated results differ without a confirmed explanation; include evidence and a tracking item.
Do not use PASS for unexecuted tests. A compile or lint result must be reported separately from functional tests.
12. Evidence Required From Every AI Coding Agent
Every task completion report must include:
1. Summary: what requirement/task was implemented.
2. Files changed: exact paths and purpose.
3. Tests run: exact command or manual steps.
4. Results: pass/fail counts or concise raw summary.
5. Not run: relevant tests that could not be executed and why.
6. Device evidence: device, OS/build, app commit/version, and observed result for any claimed device behavior.
7. Risks/limitations: known caveats, uncertainty, or remaining failures.
8. Documentation: documents updated or reason none were necessary.
9. Scope confirmation: confirmation that no unapproved feature, permission, dependency, or unrelated refactor was added.
Required reporting template will be maintained in SPRINT_TASK_TEMPLATE.md and AI_AGENT_WORKFLOW.md.
13. Test Data and Privacy
- Use synthetic data and fake adapters for unit tests where possible.
- Do not place real personal data, production secrets, tokens, or unrelated device identifiers in test fixtures.
- Avoid collecting screenshot contents or other app content unless explicitly needed and approved for the test.
- Diagnostic tests must confirm data retention limits and minimization.
- Test logs attached to bug reports should be reviewed for personal or sensitive content before sharing.
- Do not upload device logs to external services automatically.
14. Exit Criteria by Development Stage
Project foundation complete
- App builds with the documented wrapper/toolchain.
- Baseline unit tests run.
- Test dependencies and commands are documented.
- No unexpected secrets/generated files are committed.
Floating-overlay stage complete
- Overlay permission handling is tested.
- Overlay attach/remove and duplicate prevention are tested.
- Core state and permission failure cases pass.
- Target-device manual tests are recorded.
Runtime/recovery stage complete
- State invariants and retry limits have tests.
- Startup, shutdown, blocked recovery, and retry exhaustion are tested.
- User disablement is respected.
- Lifecycle behavior has appropriate instrumentation/device coverage.
Accessibility stage complete (if applicable)
- Only approved capabilities are implemented.
- Enabled/connected distinction is tested as far as detectable.
- Disabled and disconnected scenarios are recorded.
- Actual action behavior has been verified on the target device.
Release candidate complete
- All applicable acceptance tests are executed.
- Required automated tests pass.
- No unresolved critical bug blocks the approved core workflow.
- Not-run and flaky tests are explicitly listed.
- Target-device test matrix is completed or exceptions are owner-accepted.
- Resource observations, known issues, and limitations are documented.
- Release checks are run on the actual candidate build.
15. Open Testing Decisions
ID	Decision	Status
TS-01	Minimum SDK and supported device matrix	Approved: minSdk 28, targetSdk 34; Realme Narzo 30 4G (RMX2156, realme UI 3.0, Android 12)
TS-02	Chosen UI toolkit and test dependencies	Approved: Jetpack Compose with compose-ui-test-junit4
TS-03	CI platform and available Android runner	Approved: Local gradlew testDebugUnitTest + GitHub Actions
TS-04	Required long-duration test period	Approved: 8-hour continuous soak test on target device
TS-05	Quantitative battery/memory/recovery thresholds	Approved: <1% battery / 8h idle; <40MB RAM RSS; <150ms latency
TS-06	Which lifecycle scenarios can be automated reliably	Determine after test harness exists
TS-07	Test report storage location and naming convention	Approved: build/reports/tests/ and docs/testing/reports/
TS-08	Owner acceptance of any flaky/unrun release tests	Required before release exception


16. Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Status: Draft / Approved
- Initial test environment: verify on execution
- Approved device test duration: 8-hour soak test
- Approved quantitative quality thresholds: <1% battery / 8h idle; <40MB RAM RSS; <150ms touch latency
- Review date: 2026-10-29
- Notes: Test strategy formally approved.
This strategy is a controlled draft. Update it when the repository's actual build setup, approved primary action, or verified platform behavior changes the available testing approach.