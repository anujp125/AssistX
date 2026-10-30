AssistX — System Architecture
Document ID: ASSISTX-ARCH-001
Version: 0.1
Status: Draft — owner review required
Last updated: 2026-10-29
Product: AssistX — one installable native Android application
Related documents: PRODUCT_BRIEF.md, SRS.md, MVP_SCOPE.md, PRODUCT_ROADMAP.md
Instruction for AI agents: This document describes the intended architecture and component contracts. It is not permission to add frameworks, modules, services, permissions, or features without justification. Preserve unresolved decisions. Inspect the existing repository before implementing this design, and do not overwrite unrelated work. Verify Android-specific claims against current official documentation before relying on them.

1. Purpose
This document defines the system architecture for AssistX v0.1. It identifies the major components, their responsibilities, allowed dependencies, internal data flow, lifecycle boundaries, and design constraints.

The purpose is to give the product owner and AI coding agents a shared answer to:
- where a responsibility belongs;
- which component owns a state or operation;
- how components coordinate;
- what must remain independent;
- which decisions are still open;
- how to avoid unnecessary complexity.
This is a single-app architecture, not a two-app or client-server architecture.
2. Architectural Goals
1. Reliability-first behavior with observable failure states.
2. One installable Android app containing the floating assistant and Studio.
3. Clear separation of user intent from actual runtime health.
4. Clear separation of overlay health from Accessibility state.
5. Lifecycle behavior that is predictable, bounded, and testable.
6. Local-first operation with no required backend or cloud service.
7. Minimal dependencies and a small implementation surface.
8. Testable core logic without requiring a physical device for every test.
9. Accurate handling of Android permission and background-execution constraints.
10. A structure that AI agents can extend without broad, unrelated rewrites.
3. Non-Goals
The architecture does not require:
- separate application packages for Assistant and Studio;
- a backend API, database server, account system, or cloud synchronization;
- one Gradle module per logical component;
- a complex dependency-injection framework;
- a repository or use-case layer for every trivial operation;
- advanced gesture customization;
- a general-purpose plugin system;
- a promise that runtime components can remain alive under all OS conditions.
Add a separate Gradle module, framework, or abstraction only when a concrete requirement or testability need justifies its cost.
4. System Context
AssistX runs on the Android device. Its external dependencies are Android system capabilities and user actions in system settings.
+-----------------------------------------------------------+
|                       ASSISTX APP                           |
|                                                           |
|  +------------------+       +---------------------------+ |
|  | Studio UI        |       | Floating Assistant        | |
|  | - status         |       | - floating button         | |
|  | - controls       |       | - approved interaction    | |
|  | - recovery help  |       | - overlay presentation    | |
|  +--------+---------+       +-------------+-------------+ |
|           |                               |               |
|           +---------------+---------------+               |
|                           |                               |
|               +-----------v-----------+                   |
|               | Application State     |                   |
|               | and Runtime Control   |                   |
|               +-----------+-----------+                   |
|                           |                               |
|          +----------------+----------------+              |
|          |                |                |              |
|  +-------v------+ +-------v-------+ +------v-----------+  |
|  | Preferences   | | Diagnostics   | | Accessibility    |  |
|  | Store         | | Store         | | Integration      |  |
|  +--------------+ +---------------+ +------------------+  |
+-----------------------------------------------------------+
                            |
                            v
             +-----------------------------+
             | Android Operating System    |
             | - overlay permission        |
             | - Accessibility settings    |
             | - process/service lifecycle |
             | - boot/background policies  |
             +-----------------------------+
This diagram describes logical responsibilities, not necessarily separate classes or modules. Final names and package boundaries are to be specified in MODULE_AND_PACKAGE_STRUCTURE.md.
5. Deployment Model
- One Android application ID and one installable package.
- One shared local application data area.
- Studio is an in-app UI and may open or close independently of runtime components that the platform allows to continue.
- Floating overlay/runtime components must not depend on a Studio Activity staying alive.
- Accessibility integration, if required by the approved MVP action, is an Android-managed service declared in the same app.
- No network service is required for the core MVP.
- The process may be terminated, and services may be interrupted or restricted by the OS or device policies. Architecture must handle observable interruption without claiming unrestricted persistence.
Application ID/package name: com.nudge.assist.
6. Logical Components and Ownership
6.1 Studio UI
Responsibilities
- Display desired enabled state and observed component status.
- Accept user enable/disable commands.
- Explain permission requirements.
- Present recovery guidance.
- Optionally display recent diagnostic events.
- Render state supplied by application logic.
Must not
- Own the lifecycle of the floating assistant merely because its Activity is open.
- Assume that a saved preference proves runtime health.
- manipulate system permissions silently.
- embed core lifecycle policy directly into UI event handlers.
- start additional services on every render or screen recreation.
6.2 Floating Assistant / Overlay Presentation
Responsibilities
- Present one floating button when the approved conditions are met.
- Handle only owner-approved interactions.
- Attach and remove the overlay through a dedicated, testable mechanism.
- Report attach/remove outcomes and relevant errors.
- Prevent duplicate overlay presentation.
Must not
- contain broad permission or recovery policy that is duplicated elsewhere;
- assume the overlay is valid because the desired state is enabled;
- require Studio to remain open;
- implement out-of-scope quick actions;
- use repeated attachment attempts as an unbounded keep-alive strategy.
6.3 Runtime Controller / State Coordinator
Responsibilities
- Coordinate the user's desired enabled/disabled state with observed component availability.
- Apply start/stop requests in an idempotent way.
- Maintain or derive the state required by the UI.
- Enforce recovery bounds and duplicate-prevention invariants.
- Coordinate overlay operations and other runtime capabilities without conflating their health.
- Record meaningful state transitions.
Must not
- treat a saved enabled preference as proof that every service is active;
- override explicit user disablement;
- silently bypass Android controls;
- become a general-purpose framework unrelated to the MVP.
The exact implementation may be a small class or a focused set of services. It should not be a large orchestration framework.
6.4 Overlay Permission/Status Adapter
Responsibilities
- Provide a single, testable place for checking overlay permission or the conditions needed for overlay use.
- Expose an observed permission state to the state coordinator and Studio.
- Recheck relevant conditions at appropriate points, including when the app resumes from system settings.
- Surface errors from platform operations.
Must not
- infer permission state solely from a previous request;
- claim the user granted permission until current state supports that conclusion;
- request permission in an uncontrolled loop.
6.5 Accessibility Integration (Conditional)
This component exists only if the approved v0.1 action requires Accessibility.

Responsibilities
- Implement the minimum approved Accessibility-dependent behavior.
- Expose enabled/connected observations where the platform provides a reliable way to observe them.
- report connection and disconnection events where detectable;
- follow the documented recovery policy;
- keep its health state separate from overlay state.
Must not
- be added just because other assistive apps use Accessibility;
- perform actions outside the approved scope;
- claim to re-enable Accessibility settings without user action;
- be treated as synonymous with overlay permission;
- hold unrelated UI or preference logic.
Architecture gate: Primary action must be approved and its permission/API requirements verified before this component is implemented.
6.6 Preferences Store
Responsibilities
- Persist the small set of preferences needed by v0.1, including the user's desired enabled state where applicable.
- Expose typed read/write operations.
- Provide predictable behavior when values are missing or invalid.
- Keep persistence details away from UI and lifecycle code where useful.
Must not
- store runtime truth as a stale boolean and use it as proof of current health;
- introduce account/cloud sync;
- persist unnecessary personal information.
Open decisions: persistence technology and schema — to be documented in DATA_AND_DIAGNOSTICS.md.
6.7 Diagnostics Store / Event Recorder
Responsibilities
- Record relevant local events, with timestamps, component, event type, and outcome when available.
- Capture useful error context without needlessly storing personal/sensitive data.
- Apply a bounded retention/size policy.
- Allow Studio or a diagnostic view to read recent events.
Must not
- send data externally in v0.1;
- record unlimited logs;
- treat an inferred cause as confirmed fact;
- expose stack traces or sensitive implementation details to ordinary users.
Storage mechanism: Jetpack DataStore Preferences for settings; Room SQLite DB for diagnostics (max 500 records or 2 MB).
6.8 State Model / Domain Logic
Responsibilities
- Define state values and valid transitions.
- Keep desired state distinct from observed status.
- Provide logic that can be unit tested where feasible.
- Prevent contradictory display states from being generated by unrelated UI code.
Must not
- duplicate state definitions across the UI, runtime service, and diagnostics;
- equate Accessibility enabled with Accessibility connected;
- equate foreground service running with overlay visible;
- equate process alive with all product features operational.
The authoritative state definitions and transitions belong in STATE_AND_LIFECYCLE.md.
7. Dependency Rules
Preferred logical dependency direction:
Studio UI --------------------+
                              |
Floating Assistant UI --------+--> State / Runtime APIs
                              |          |
                              |          +--> Platform adapters
                              |          |     - Overlay
                              |          |     - Accessibility (if needed)
                              |          |     - Lifecycle/service APIs
                              |          |
                              |          +--> Preferences store
                              |          +--> Diagnostics store
                              |
                              +--> Read-only state/events
Rules:
1. UI components request operations through clear APIs; they do not duplicate lifecycle policy.
2. Platform-dependent code is isolated enough that state logic can be tested without Android runtime where practical.
3. The state coordinator may depend on platform adapters and stores through narrow interfaces or concrete classes, depending on project size. Do not add interfaces solely for ceremony.
4. The Preferences Store must not depend on UI components.
5. The Diagnostics Store must not control product behavior. Logging failure must not normally prevent the core feature from operating.
6. Accessibility integration must not own the overlay lifecycle.
7. Overlay presentation must not own Accessibility connection policy.
8. UI rendering must not perform repeated service start/stop side effects.
9. Do not allow cyclic dependencies between logical components.
10. No component may introduce network access or an additional permission without explicit approval.
11. Do not create a separate Gradle module for each component unless build isolation or another concrete need justifies it.
12. Keep platform-specific workarounds localized and documented.
8. Main Data and Control Flows
8.1 User enables AssistX
1. User opens Studio and requests Enable.
2. The application records the desired state according to the approved persistence policy.
3. The runtime coordinator checks currently relevant prerequisites.
4. If overlay permission is missing, the app reports Overlay Permission Required and directs the user to system settings where supported.
5. If the required prerequisites are satisfied, the runtime controller requests the appropriate runtime component to start.
6. The overlay layer attempts to display one button.
7. Actual outcomes update the observed state; errors are recorded.
8. Studio displays the state based on observations, not merely on the button press.
The exact ordering and rollback behavior must be finalized in the lifecycle specification. In particular, a failed startup must not leave the app reporting an active state.
8.2 User disables AssistX
1. User requests Disable.
2. The application records the desired disabled state.
3. Recovery logic stops initiating automatic attempts to restore user-disabled behavior.
4. The runtime controller requests removal/stop of relevant components where permitted.
5. Successful and failed cleanup outcomes are reflected in observed state and diagnostics.
6. Repeated disable requests remain safe and do not create errors or duplicates.
8.3 User changes a system permission
1. AssistX sends the user to the relevant Android settings surface where supported.
2. The user makes the change in system settings.
3. On return or at an appropriate lifecycle/check point, AssistX rechecks the actual condition.
4. The state coordinator updates the observed state based on current evidence.
5. AssistX does not treat opening a settings screen or returning from it as proof that permission was granted.
8.4 Accessibility becomes unavailable
If Accessibility is part of the approved MVP:
1. the service reports a disconnect or the app otherwise observes a status change where possible;
2. the Accessibility state becomes unavailable or unknown according to the state model;
3. the overlay state remains independently represented;
4. recovery policy determines whether a permitted retry is appropriate or whether user guidance is required;
5. the event and outcome are recorded.
Do not infer that the user disabled Accessibility unless the available evidence supports that conclusion.
8.5 Runtime interruption
1. A lifecycle callback, service event, or later state check identifies an interruption where detectable.
2. The runtime controller compares observed status with desired user intent.
3. If the user explicitly disabled AssistX, no restart occurs.
4. If enabled, the controller follows the bounded policy appropriate to that type of interruption.
5. Each attempt records its outcome.
6. If recovery is unavailable or fails within the configured limits, AssistX reports Recovery Required or another accurately defined state.
Some termination modes prevent application code from running at the time of termination. The architecture must not imply that every interruption can be detected immediately or recovered automatically.
9. State Ownership and Source of Truth
Use separate concepts for:
- Desired state: what the user requested and the app persisted.
- Observed component state: what is currently known about each relevant component.
- Derived user-facing state: a UI-friendly summary computed from desired state and observations.
- Diagnostic history: records of events; not the current source of truth for component health.
The application should not use an event log as the sole authority for current runtime state. State should be refreshed from current platform observations wherever practical.

Each state field must have one clear owner or source. If multiple components can report a status, the state model must define how reports are reconciled and how Unknown/not-yet-observed cases are represented.
10. Lifecycle and Platform Constraints
The selected implementation must account for:
- Android Activity lifecycle;
- process death;
- service lifecycle and applicable foreground-service requirements;
- special overlay permission behavior;
- Accessibility settings and service connection behavior, if used;
- device reboot and available startup mechanisms;
- OS background-execution restrictions;
- target-SDK-specific rules;
- manufacturer-specific battery/background management.
These rules can change with Android versions. Before implementation, agents must verify relevant details using current official Android documentation and record consequential decisions in ARCHITECTURE_DECISIONS.md or PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md.

Do not assume:
- a service is immortal because it is foreground;
- an app can always start itself from the background;
- reboot guarantees immediate restart;
- Accessibility enabled means its process connection is currently healthy;
- returning from settings means permission was granted;
- process death and user force-stop are interchangeable;
- behavior observed on RMX2156 necessarily applies to every Android device.
11. Error Handling and Observability
Every meaningful operation that can fail should have:
- a defined success outcome;
- a defined failure outcome;
- appropriate user-facing guidance where the user can act;
- a diagnostic event where useful and safe;
- a predictable state transition;
- tests for logic that is reasonably testable.
Do not swallow errors silently. Avoid flooding diagnostics with repeated identical events; rate-limit or aggregate repetitive failures where appropriate.

Diagnostics are best-effort. Failure to write a log must not normally cause the floating button or approved action to fail.
12. Concurrency and Idempotency
The design must avoid races between:
- repeated enable/disable actions;
- Activity recreation;
- service startup callbacks;
- permission checks;
- overlay attachment/removal;
- Accessibility connection/disconnection;
- recovery attempts.
Requirements:
- Start/stop operations must be idempotent where practical.
- Only one overlay instance may exist.
- State transitions must not regress to an older observation without a defined reason.
- Recovery attempts must not overlap uncontrollably.
- Cleanup should be safe when partially initialized components are encountered.
- If an operation is asynchronous, completion/failure must be reflected rather than assuming success immediately.
The detailed locking/serialization approach is an implementation decision and should be as simple as possible.
13. Security and Privacy Boundaries
- Keep data local for the MVP.
- Do not introduce Internet permission or network dependencies without approval.
- Request only permissions required by approved functionality.
- Explain special permissions in user-facing language.
- Do not collect unrelated device identifiers, usage details, or personal data.
- Never include secrets in source control or diagnostic logs.
- Do not bypass Android security controls.
- Keep release-signing material out of the repository.
- Review all dependencies for necessity before adding them.
Details of stored fields and retention belong in DATA_AND_DIAGNOSTICS.md and PRIVACY_AND_PERMISSIONS.md.
14. Testability and Verification
The architecture should support:
Unit tests
- state transition rules;
- desired-state handling;
- retry limits and recovery decision logic;
- duplicate-overlay prevention logic;
- preference interpretation and defaults;
- diagnostic event construction and retention policies.
Android/instrumentation tests, where practical
- overlay permission handling;
- overlay attach/remove behavior;
- lifecycle integration;
- service integration;
- permission and Accessibility status handling.
Real-device tests
- opening/closing Studio;
- switching apps and locking/unlocking the screen;
- permission changes;
- Accessibility enabled/disabled/disconnected scenarios, if relevant;
- process interruption and reboot;
- long-duration operation and resource observations.
Tests that require Android or a physical device must not be reported as passed merely because unit tests passed. Test scope and limitations must be explicit.
15. Proposed Project Layout
The following is a logical starting point, not a mandate for every directory to exist immediately. The actual package and file names are finalized in MODULE_AND_PACKAGE_STRUCTURE.md.
app/
  src/
    main/
      java/com/assistx/
        ui/
          studio/
          floating/
        runtime/
        state/
        overlay/
        accessibility/       # only if required by approved MVP action
        data/
          preferences/
          diagnostics/
        platform/
      res/
      AndroidManifest.xml
    test/
    androidTest/
docs/
  01-product/
  02-architecture/
  03-ai-development/
  04-testing/
  05-operations/
Do not create empty folders just to match this diagram. Add files as implementation tasks require them.
16. Architecture Decisions Still Open
ID	Decision	Current status	Guidance
ADR-OPEN-01	UI toolkit: Compose or Views	Approved	Jetpack Compose with Material 3
ADR-OPEN-02	Minimum/target SDK and compatibility policy	Approved	minSdk 28, targetSdk 34, compileSdk 34; Realme Narzo 30 4G baseline
ADR-OPEN-03	Application ID/package name	Approved	com.nudge.assist
ADR-OPEN-04	Primary button action and required capability	Approved	Double-tap lock screen, long-press home/back via Accessibility
ADR-OPEN-05	Runtime/foreground-service strategy	Approved	Foreground Service with persistent low-priority silent notification
ADR-OPEN-06	Preference storage implementation	Approved	Jetpack DataStore Preferences
ADR-OPEN-07	Diagnostics storage implementation and retention	Approved	Room SQLite DB bounded to max 500 events or 2 MB
ADR-OPEN-08	Dependency injection approach	Approved	Manual constructor injection (no Hilt/Dagger initially)
ADR-OPEN-09	App distribution and release signing	Approved	Personal APK direct install / GitHub Releases


The architecture is not approved until decisions that materially affect runtime behavior and permissions are resolved or an explicit, limited spike is approved.
17. Architecture Change Rules
A coding agent must request approval before:
- creating a separate installable app;
- adding a backend, network calls, or cloud storage;
- adding a sensitive permission or Accessibility capability outside the approved action;
- materially changing runtime/service ownership;
- adding a framework or dependency with material maintenance or security impact;
- introducing a complex module structure;
- changing persistence or diagnostic data handling in a way that affects compatibility or privacy;
- adding new floating-button actions or customization.
For each proposed change, provide the motivation, alternatives, affected documents/files, security and battery implications, test plan, and migration considerations where relevant.

Small internal refactors that preserve behavior may proceed within an assigned task if tests and scope remain appropriate. Do not use a feature task as a pretext for a broad architecture rewrite.
18. Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Architecture status: Draft / Approved
- Review date: 2026-10-29
- Approved UI toolkit: Jetpack Compose with Material 3
- Approved primary action: Double-tap lock screen, long-press home/back
- Accepted architecture deviations: None
- Notes: System architecture formally approved.
This document is a controlled draft until reviewed. It should be updated alongside the SRS, scope, decision log, and tests whenever an approved change alters the architecture.