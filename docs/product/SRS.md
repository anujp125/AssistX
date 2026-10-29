AssistX — Software Requirements Specification (SRS)
Document ID: NUDGE-SRS-001
Version: 0.1
Status: Approved
Last updated: 2026-10-29
Product: AssistX, a single-installable native Android application
Related document: docs/01-product/PRODUCT_BRIEF.md
All requirements and platform decisions have been reviewed and approved by the product owner for AssistX v0.1.

1. Purpose
This document defines testable requirements for AssistX v0.1. It is intended to guide the product owner, developers, AI coding agents, reviewers, and testers.

The SRS describes what the product must do and the conditions under which behavior is accepted. It does not prescribe every implementation detail. Technical mechanisms belong in the architecture, Android platform constraints, and state/lifecycle documents.
2. Product Overview
AssistX is a lightweight assistive application intended to provide a floating on-screen button, an in-app Studio/settings interface, and clear diagnostics for runtime and permission problems.

AssistX is one Android application. The floating assistant and Studio interface are logical components within that application. They must not be implemented as two independently installed applications.

The primary motivation is reliability. The product owner has experienced an existing assistive-touch application whose floating button sometimes disappears and whose Accessibility setting or connection sometimes appears unavailable. The cause is not confirmed. AssistX must distinguish observable failure conditions rather than assume one cause.
3. Goals
AssistX v0.1 shall aim to:
- provide a predictable floating-button enable/disable workflow;
- maintain a single overlay instance when overlay use is enabled and permitted;
- keep the floating assistant independent of the Studio screen being open;
- distinguish overlay permission/availability from Accessibility enabled/connected status;
- report states accurately and guide the user through required system actions;
- attempt bounded recovery where supported by Android;
- persist relevant user preferences locally;
- create useful local diagnostic events;
- avoid unnecessary network dependencies, background work, and data collection;
- provide testable behavior on the initial target device.
These goals do not imply guaranteed uninterrupted execution. Android, device-manufacturer policies, user settings, and process lifecycle events may limit runtime persistence and automatic recovery.
4. Non-Goals and Exclusions for v0.1
Unless explicitly approved through the change process, v0.1 shall not include:
- volume controls;
- Wi-Fi or Bluetooth toggles;
- user accounts, sign-in, or a backend;
- cloud synchronization or remote diagnostics;
- analytics or telemetry sent to a third party;
- separate Studio and Assistant app packages;
- elaborate themes, animation systems, or extensive customization;
- unrestricted background execution or permission bypasses;
- automatic re-enabling of an Accessibility setting that Android requires the user to enable;
- claims of guaranteed survival through force-stop, operating-system termination, reboot, or manufacturer background restrictions.
The exact primary floating-button action and its gesture remain an owner decision (see Section 15). The currently known candidate is a double-tap-to-screen-off action, based on the owner's current use case. It is not considered finalized until approved.
5. Users and External Actors
Actor	Description
Primary user	The person operating AssistX and granting or revoking permissions.
Android operating system	Manages permissions, application processes, services, accessibility settings, overlays, and lifecycle events.
Device power/background manager	May impose device-specific restrictions on background behavior.
Developer or AI coding agent	Implements the approved requirements and reports test evidence.
Tester	Verifies requirements through automated tests and real-device scenarios.


No remote server or third-party service is required for the MVP.
6. Operating Environment
6.1 Initial target environment
The product owner's reported test device is:
- Realme Narzo 30 4G;
- model RMX2156;
- Android 12;
- realme UI 3.0;
- build RMX2156_11_F.19.
These are the initial test conditions and are not a promise of compatibility with other devices.
6.2 Unresolved platform decisions
- Minimum supported Android API level: API 28 (Android 9.0 Pie)
- Target SDK policy for the initial build: targetSdk 34 / compileSdk 34 (Android 14)
- UI toolkit: Jetpack Compose with Material 3
- Initial distribution method: Personal APK direct install / GitHub Releases
- Application ID/package name: com.nudge.assist
- Hardware validation target: Realme Narzo 30 4G (RMX2156, realme UI 3.0, Android 12)
Agents must not finalize these values without an approved decision or an explicitly accepted architecture proposal.
7. Definitions
Term	Meaning
Floating overlay	The on-screen floating interface shown above other apps, subject to Android permission and behavior.
Studio	AssistX's in-app settings, status, and diagnostics interface.
Desired enabled state	The user's saved intention for whether AssistX should be active. It is not proof that every runtime component is currently healthy.
Runtime state	The current observed state of the app's relevant runtime components.
Accessibility enabled	Android system settings indicate that AssistX's Accessibility Service is enabled.
Accessibility connected	The Accessibility Service has an active connection, when that status can be determined.
Recovery	A bounded attempt to restore functionality without bypassing user or operating-system controls.
Diagnostic event	A local record of a relevant lifecycle, permission, state, error, or recovery event.
Owner decision	A product choice explicitly approved by the product owner.


8. Functional Requirements
Requirements are assigned stable IDs so they can be linked to implementation tasks and tests. Priorities: Must = required for v0.1; Should = expected unless a documented technical blocker is accepted; TBD = pending owner input or technical validation.
8.1 Setup and user intent
FR-001 — First-run guidance (Must)
The application shall provide a first-run interface that explains AssistX's purpose and identifies the system permissions required for the approved MVP actions.

Acceptance evidence: A new installation presents the setup guidance without implying that permissions have already been granted.

FR-002 — Explicit enable/disable control (Must)
The user shall be able to request that AssistX be enabled or disabled from Studio.

Acceptance evidence: The saved desired state reflects the user's selection. Errors that prevent activation are surfaced rather than hidden.

FR-003 — Separate desired and observed state (Must)
AssistX shall represent the user's desired enabled state separately from the observed health of the overlay/runtime and Accessibility Service.

Acceptance evidence: A saved preference alone cannot cause the UI to display a false Active status.
8.2 Floating overlay
FR-004 — Overlay permission handling (Must)
Before attempting an overlay operation that requires special permission, AssistX shall check the relevant permission state and respond appropriately when permission is missing.

Acceptance evidence: When permission is missing, the app explains the requirement and offers a route to the relevant system settings where supported.

FR-005 — Single overlay instance (Must)
Repeated enable requests, lifecycle callbacks, or recovery attempts shall not create duplicate floating buttons.

Acceptance evidence: Repeating the start workflow does not produce more than one AssistX overlay.

FR-006 — Overlay lifecycle management (Must)
AssistX shall manage attachment and removal of its floating overlay in response to approved user intent, permission availability, and runtime lifecycle events.

Acceptance evidence: Disabling AssistX removes its overlay when the operating system and runtime allow the action; failures are handled and reported rather than silently ignored.

FR-007 — Overlay status accuracy (Must)
AssistX shall not label the overlay as active merely because the user's desired enabled state is true.

Acceptance evidence: The status UI differentiates at least the desired state from any known permission or runtime failure.

FR-008 — Approved button action (Must)
The floating button shall execute the approved gestures and actions for v0.1:
1. Double-Tap Action: Turn Screen Off / Lock Screen via Accessibility Service (`GLOBAL_ACTION_LOCK_SCREEN`). Gesture window: 300 ms.
2. Long-Press Action: Home / Back navigation (`GLOBAL_ACTION_HOME` / `GLOBAL_ACTION_BACK`). Long-press hold threshold: 500 ms.
3. Drag & Snap: Draggable across the display; animates and snaps to the nearest vertical screen edge upon release. Coordinates persist in DataStore.
- Behavior if required capability is unavailable: Show truthful status (DEGRADED / BLOCKED) in Studio and guide user to system settings.
8.3 Accessibility integration
FR-009 — Accessibility state monitoring (Must if the approved MVP action depends on Accessibility)
AssistX shall distinguish, to the extent Android APIs permit, between Accessibility being enabled in system settings and the service being connected at runtime.

Acceptance evidence: The status UI does not treat these conditions as interchangeable.

FR-010 — Accessibility disabled guidance (Must if Accessibility is required)
When an approved action requires Accessibility and the service is disabled, AssistX shall explain the condition and guide the user to the relevant system settings.

Acceptance evidence: AssistX does not claim it can silently re-enable Accessibility when user action is required by Android.

FR-011 — Accessibility disconnection handling (Must if Accessibility is required)
When AssistX can detect that its Accessibility Service is not connected, it shall represent that condition separately from the setting being disabled and apply the documented recovery policy.

Acceptance evidence: The app reports the observed condition and does not mislabel a disconnection as a confirmed settings change.

FR-012 — Isolation from basic overlay health (Must)
Failure or unavailability of Accessibility functionality shall not automatically be represented as proof that the floating overlay itself is unavailable.

Acceptance evidence: Overlay and Accessibility state can be shown independently.
8.4 Runtime state and recovery
FR-013 — Explicit runtime states (Must)
AssistX shall use a documented state model for the desired application state and the observable health of its relevant components.

The detailed allowed states, transitions, and invariants shall be defined in STATE_AND_LIFECYCLE.md.

FR-014 — Accurate status messages (Must)
When a known failure prevents a feature from operating, AssistX shall present a status or instruction that reflects the observed condition.

Acceptance evidence: The UI does not report a generic Active state when a required permission is known to be missing.

FR-015 — Bounded recovery attempts (Must)
Automatic recovery logic shall be bounded, idempotent where possible, and designed to avoid duplicate instances and endless restart loops.

Acceptance evidence: Repeated lifecycle or recovery events do not produce unbounded restart attempts or duplicate overlays.

FR-016 — Respect user disablement (Must)
Automatic recovery shall not override the user's explicit disabled state.

Acceptance evidence: A user-disabled AssistX session is not automatically re-enabled by internal recovery logic.

FR-017 — Platform-constrained recovery (Must)
AssistX shall attempt only recovery actions supported by the operating system and current permissions. When recovery requires user action or is blocked by the operating system, AssistX shall report that limitation.

Acceptance evidence: The app does not attempt to bypass system permission controls and does not promise that every interruption can be recovered automatically.

FR-018 — Process interruption and reboot behavior (Must, within platform constraints)
AssistX shall preserve the user's desired enabled preference locally and apply the approved startup/recovery policy after process recreation or device reboot where platform rules permit.

Acceptance evidence: Tests document what is restored automatically, what requires user interaction, and what Android prevents.

FR-019 — No duplicate runtime instances (Must)
The implementation shall prevent repeated start requests and ordinary lifecycle changes from creating conflicting runtime instances or duplicate overlays.

Acceptance evidence: Repeated starts, stops, and screen transitions produce consistent state and no duplicates.
8.5 Studio interface
FR-020 — Single application interface (Must)
Studio and the floating assistant shall be delivered in the same installable application package.

Acceptance evidence: A single installation provides both the floating assistant and Studio.

FR-021 — Studio lifecycle independence (Must)
Closing or backgrounding Studio shall not, by itself, disable a correctly running floating assistant when the approved runtime architecture supports continued operation.

Acceptance evidence: Open/close Studio tests do not cause unwanted overlay removal or duplication.

FR-022 — Component status view (Must)
Studio shall display the desired enabled state and the known status of relevant components, including overlay permission/availability and Accessibility status when applicable.

Acceptance evidence: Each displayed status maps to a documented state or known condition.

FR-023 — Recovery guidance (Must)
When user intervention is required, Studio shall state the likely condition and the next user action. It shall provide a system-settings shortcut where supported.

Acceptance evidence: Instructions are specific enough to distinguish at least overlay permission and Accessibility setting problems.

FR-024 — Diagnostic event view (Should)
Studio should provide a basic view of recent relevant diagnostic events.

Acceptance evidence: A tester can inspect recent events without a network connection.
8.6 Persistence and diagnostics
FR-025 — Local preference persistence (Must)
AssistX shall store the preferences required to restore the user's configuration and desired enabled state.

Acceptance evidence: Ordinary process recreation does not erase stored preferences. Any behavior constrained by the operating system is documented separately.

FR-026 — Diagnostic event recording (Must)
AssistX shall record relevant events, where observable, including:
- application/runtime start and stop;
- overlay attach, detach, or failure;
- relevant permission-state changes;
- Accessibility enabled/connected state changes, where detectable;
- recovery attempt and outcome;
- errors that prevent approved functionality.
Acceptance evidence: Tests or a manual diagnostic procedure can verify that the expected event types are recorded.

FR-027 — Diagnostic usefulness (Must)
Diagnostic records shall include enough context to distinguish event type, time, relevant component, and outcome where available.

Acceptance evidence: Events are not limited to an unexplained generic error message.

FR-028 — Diagnostic retention and storage bounds (Must)
The implementation shall have a documented policy to prevent diagnostic data from growing without limit.
- Maximum log size or retention period: Max 500 events OR 2 MB limit with automated FIFO pruning
- Detailed implementation: DATA_AND_DIAGNOSTICS.md
Agents must propose a safe default in the data design but must not present an unapproved value as an owner decision.
8.7 Privacy and networking
FR-029 — No backend requirement (Must)
The MVP's core behavior shall not depend on a backend, account, or cloud service.

Acceptance evidence: The core workflow can be used without signing in or connecting to an application server.

FR-030 — Data minimization (Must)
AssistX shall collect only local data needed for preferences, operation, and diagnostics. Diagnostic records shall avoid unnecessary personal or sensitive information.

Acceptance evidence: A review of stored fields and logs confirms their purpose and minimization.

FR-031 — No unapproved outbound telemetry (Must)
The app shall not send diagnostics, usage analytics, or personal data to external services in v0.1 unless the product owner explicitly approves a change.

Acceptance evidence: Dependencies and network behavior are reviewed before release.
8.8 Errors and maintainability
FR-032 — Actionable error handling (Must)
Errors that prevent a user action or component startup shall be surfaced or logged with enough information to investigate them. The user-facing message shall be understandable and shall not expose stack traces or sensitive implementation details.

FR-033 — Testable core logic (Must)
Business/state-transition logic that can reasonably be tested without a physical device shall be structured so it can be covered by automated tests.

FR-034 — Documented behavior changes (Must)
A change to approved user-visible behavior, permissions, runtime lifecycle, or data handling shall update the applicable requirements, design documents, and tests.
9. Non-Functional Requirements
9.1 Reliability
NFR-REL-001: The app shall avoid duplicate overlays and uncontrolled recovery loops.

NFR-REL-002: Known failure conditions shall be represented accurately; unknown causes shall not be stated as facts.

NFR-REL-003: Recovery behavior shall respect user intent and platform controls.

NFR-REL-004: The release shall be tested against the approved real-device matrix.
- Required continuous test duration: 8-hour soak test on target device
- Acceptable unexpected interruption rate: 0 unhandled terminations during 8-hour soak test
- Acceptable recovery latency: Recovery initiated within 1000 ms of detected failure
These quantitative targets must be defined after a measurement approach is agreed. Agents must not fabricate target values.
9.2 Performance and resource usage
NFR-PERF-001: The floating interface and Studio shall remain responsive during ordinary use on the target device.

NFR-PERF-002: AssistX shall avoid unnecessary polling, repeated service restarts, excessive logging, and avoidable background work.

NFR-PERF-003: Battery and memory impact shall be measured during the MVP validation process.
- Acceptable battery impact: < 1.0% battery drain over an 8-hour idle period
- Target memory budget: < 40 MB Private Dirty RSS memory usage
- Startup/interaction latency targets: Touch-to-lock action latency < 150 ms
No numerical target is approved yet.
9.3 Usability and accessibility
NFR-UX-001: Important states shall use clear wording rather than unexplained internal terms.

NFR-UX-002: Permission failures shall explain what is missing and what the user can do next.

NFR-UX-003: The floating interface shall remain minimal and avoid unnecessary screen obstruction.
- Button dimensions, opacity, color, placement, and visual style: 56dp diameter, 70-80% idle opacity / 100% active opacity, neutral dark translucent circle with subtle white icon
- Gesture timing and drag behavior: Double-tap 300ms window, long-press 500ms, snap-to-edge drag with coordinate persistence
9.4 Security and privacy
NFR-SEC-001: AssistX shall not bypass Android security boundaries or system-controlled permissions.

NFR-SEC-002: Sensitive permissions shall be requested only when needed for approved functionality and explained to the user.

NFR-SEC-003: No secrets, personal data, or unnecessary device details shall be included in source control or logs.

NFR-SEC-004: The MVP shall not require remote access to operate.
9.5 Compatibility and portability
NFR-COMP-001: The initial implementation shall be validated on the target device and OS configuration listed in Section 6.

NFR-COMP-002: Compatibility with other Android versions and manufacturers shall not be claimed unless tested or appropriately justified.

NFR-COMP-003: Device-specific workarounds shall be documented and isolated where practical.
9.6 Maintainability and testability
NFR-MAIN-001: Responsibilities shall be separated enough to allow the overlay, state logic, Accessibility integration, preferences, and diagnostics to be tested and maintained without unnecessary complexity.

NFR-MAIN-002: New dependencies require a clear rationale and approval under the change-control policy.

NFR-MAIN-003: Each sprint shall run the relevant automated checks and record their actual results.
10. Global Business Rules and Invariants
These rules apply across the implementation:
1. Desired enabled state is not equivalent to runtime health.
2. Overlay state and Accessibility state are separate.
3. Accessibility being enabled is not proof that the service is currently connected.
4. Missing permission must not be represented as an unexplained runtime crash.
5. A saved preference must not be presented as proof of successful startup.
6. User disablement takes precedence over automatic recovery.
7. Recovery cannot silently grant permissions or change protected system settings.
8. One logical overlay instance is allowed at a time.
9. Repeated start/stop operations must be safe and predictable.
10. No core MVP workflow may depend on a network service.
11. Unknown failure causes must remain unknown until diagnostic evidence supports a conclusion.
12. Unapproved features are out of scope, even if an agent can implement them easily.
11. External Interface Requirements
11.1 User interface
The application shall expose:
- an initial setup/permission explanation;
- enable/disable controls;
- a floating button when the required conditions are satisfied;
- status information for relevant components;
- permission and Accessibility recovery guidance where applicable;
- recent diagnostic events if the diagnostic-event view is implemented for the build.
Detailed layouts and visual behavior are specified in UI_UX_SPECIFICATION.md.
11.2 Android system interface
AssistX may interact with Android's overlay permission, Accessibility, service lifecycle, and startup mechanisms only as justified by approved features and applicable platform rules.

Exact APIs, manifest declarations, service types, and target-SDK implications belong in PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md and SYSTEM_ARCHITECTURE.md. These decisions must be verified against current official Android documentation before implementation or release.
11.3 Network interface
No network API is required for the MVP. Any future network capability is a scope change and must include security, privacy, data-handling, and testing requirements.
12. Acceptance Summary
The MVP cannot be considered complete unless the evidence demonstrates all of the following:
- one app installation supplies both Studio and the floating assistant;
- the basic enable/disable workflow works on the target device;
- only one overlay instance is created;
- Studio can open and close without being required to remain active for the floating assistant;
- missing overlay permission is detected and reported appropriately;
- Accessibility state is reported accurately if required by the approved feature set;
- user-disabled state is respected;
- recovery behavior is bounded and does not create duplicate services or restart loops;
- process interruption and reboot behavior have been tested and documented within Android's constraints;
- preferences and diagnostic events are handled according to approved local-data rules;
- core automated tests pass;
- actual-device results and any outstanding limitations are documented;
- no unresolved critical defect prevents the approved MVP workflow.
Detailed test cases and test identifiers will be defined in ACCEPTANCE_TESTS.md and REAL_DEVICE_TEST_MATRIX.md.
13. Traceability Requirements
Every Must requirement must map to:
- one or more implementation tasks in SPRINT_PLAN.md or a sprint task;
- one or more verification cases in the test documents;
- implementation evidence or a documented reason for non-implementation.
Requirement IDs shall remain stable. If a requirement changes meaning, update its text and the traceability mapping rather than reusing the ID for an unrelated requirement.
14. Assumptions, Constraints, and Risks
ID	Item	Status	Handling
A-01	AssistX is one installable app.	Approved direction	Use one application package.
A-02	MVP has no backend/cloud dependency.	Approved direction	Keep core behavior local.
A-03	Kotlin native Android is the technical direction.	Current direction	Confirm project setup in technical design.
C-01	Android controls special permissions and background execution.	Platform constraint	Verify current platform behavior before implementation.
R-01	The source of the existing app's failures is unknown.	Unresolved	Use diagnostics and controlled tests; do not assume root cause.
R-02	Realme background management may influence behavior.	Needs verification	Test on target device and document results.
R-03	Some recovery scenarios may require user action.	Expected constraint	Provide truthful status and guidance.


15. Owner Decisions Required
The following must remain unresolved until the owner responds or explicitly accepts a documented proposal.
ID	Question	Approved Value
O-01	What is the exact primary button action in v0.1?	Double-tap: Lock screen (GLOBAL_ACTION_LOCK_SCREEN); Long-press: Home/Back (GLOBAL_ACTION_HOME/BACK)
O-02	What gesture triggers the action?	Double-tap within 300ms; Long-press with 500ms hold
O-03	Should the button support dragging and position persistence in v0.1?	Yes: Draggable with snap-to-edge; coordinates persist in DataStore
O-04	Minimum supported Android version?	API 28 (Android 9.0 Pie); Target SDK 34
O-05	UI toolkit: Compose or Views?	Jetpack Compose with Material 3
O-06	Distribution method?	Personal APK direct install / GitHub Releases
O-07	Application ID/package name?	com.nudge.assist
O-08	Visual dimensions, style, and opacity?	56dp circle, 70-80% idle opacity, 100% touch opacity, neutral dark style
O-09	Diagnostic retention/size limit?	Bounded Room SQLite DB: Max 500 records or 2 MB limit
O-10	Reliability test duration and measurable targets?	8-hour soak test; <1% battery / 8h idle; <40MB RAM; <150ms latency
O-11	Is v0.1 intended only for the target Realme device or multiple devices?	Single-device focus: Realme Narzo 30 4G (RMX2156, realme UI 3.0, Android 12)
O-12	Which license should be used for public distribution, if any?	GNU General Public License v3.0 (GPLv3)


An agent may provide a short recommendation with trade-offs, but must not silently mark an item as approved.
16. Definition of SRS Approval
This SRS is approved only when the product owner has reviewed:
- MVP scope and exclusions;
- the primary button action and gesture;
- support and distribution decisions needed for the first implementation;
- the open decisions that materially affect architecture or permissions;
- the acceptance summary.
Until then, this document is a controlled draft. Implementation may proceed only on requirements that are sufficiently defined and do not depend on unresolved high-impact decisions.
- Product owner: Anuj Pawar (@anujp125)
- Status: Approved
- Approval date: 2026-10-29
- Review notes: Formally reviewed and approved via the project pre-development questionnaire.