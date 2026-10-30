AssistX — MVP Scope and Boundaries
Document ID: ASSISTX-SCOPE-001
Version: 0.1
Status: Approved
Last updated: 2026-10-29
Related documents: PRODUCT_BRIEF.md, SRS.md
Instruction for AI agents: This file defines the approved scope boundary for AssistX v0.1. Implement only work inside the MVP scope. A feature being easy, common in assistive-touch apps, or suggested by an AI agent does not make it in scope. Items marked [USER INPUT REQUIRED], TBD, or Candidate are unresolved and must not be silently treated as approved.

1. Purpose
This document establishes what AssistX v0.1 will and will not deliver. It prevents scope creep, helps divide work into small coding sprints, and gives reviewers a consistent basis for deciding whether a proposed change belongs in the MVP.

AssistX is one installable Android app. The floating assistant and Studio/settings interface are internal components of the same application.
2. MVP Mission
Demonstrate a minimal, usable floating assistant that reports its state honestly, distinguishes overlay/runtime health from Accessibility status, provides actionable recovery guidance, and records local diagnostic evidence for failures that can be observed.

The MVP prioritizes reliability, diagnosability, and predictable lifecycle behavior over feature count or visual customization.
3. Target Environment
Initial validation environment, as reported by the product owner:
- Device: Realme Narzo 30 4G
- Model: RMX2156
- Android: 12
- UI: realme UI 3.0
- Build: RMX2156_11_F.19
This is the initial test device, not a general compatibility claim.

Still unresolved:
- Minimum supported Android version: API 28 (Android 9.0 Pie)
- Target hardware: Single-device baseline on Realme Narzo 30 4G (RMX2156, realme UI 3.0, Android 12)
- Distribution method: Personal APK direct install / GitHub Releases
4. In-Scope Capabilities
The following capabilities define the intended MVP. Their detailed requirements are in SRS.md.
SCOPE-01 — Single installable application
- Ship the floating assistant and Studio/settings interface in one Android application package.
- Do not create a second installable Studio application.
- Keep component boundaries logical and as simple as the requirements permit.
Completion evidence: One installation provides both the floating assistant and Studio.
SCOPE-02 — Minimal floating button
- Display one floating button when enabled and the required overlay permission is available.
- Support clean attachment and removal.
- Prevent duplicate overlays during repeated start requests or lifecycle events.
- Provide only the approved button action and gesture.
Not yet finalized: Primary action, gesture, button appearance, drag behavior, and position persistence. See Section 10.
SCOPE-03 — Studio/settings interface
Provide a small interface to:
- enable or disable AssistX;
- show the desired enabled state separately from observed health;
- show relevant overlay/runtime and Accessibility status;
- explain missing permissions or unavailable services;
- guide the user toward relevant Android settings when user intervention is required;
- show recent diagnostic events if feasible within the MVP.
Studio is a control and status interface. It must not have to remain open for a healthy floating assistant to operate.
SCOPE-04 — Overlay permission and lifecycle management
- Check the permission required to draw over other apps before relying on an overlay.
- Report missing permission accurately.
- Provide a route to system settings where supported.
- Handle overlay attachment/removal failures.
- Avoid duplicate overlays and conflicting runtime instances.
SCOPE-05 — Accessibility status and integration
Accessibility integration is in scope only to the extent required by the owner-approved MVP action.

If the approved action depends on Accessibility:
- distinguish enabled-in-settings from connected-at-runtime where detectable;
- guide the user when the service is disabled;
- report a disconnection separately where detectable;
- do not imply that AssistX can silently re-enable a setting that Android requires the user to enable;
- do not let Accessibility failure falsely imply that the overlay itself has failed.
If the approved action does not require Accessibility, the service must not be added merely because assistive-touch products commonly use it.
SCOPE-06 — Explicit state model
Represent the desired enabled state and runtime health separately. The design must account for applicable states such as:
- Disabled
- Starting
- Active
- Overlay Permission Required
- Accessibility Disabled
- Accessibility Disconnected
- Interrupted
- Recovering
- Recovery Required
The authoritative state names, transitions, and invariants will be defined in STATE_AND_LIFECYCLE.md. An individual component may have a more specific internal state where justified.
SCOPE-07 — Bounded and platform-compliant recovery
- Recover automatically only when a supported, safe recovery path exists.
- Bound retries and make recovery idempotent where practical.
- Respect the user's explicit disabled state.
- Avoid restart loops, duplicate services, and repeated overlay creation.
- Ask the user to perform actions that Android reserves for the user.
- Document behavior that cannot be guaranteed due to Android or device-maker restrictions.
SCOPE-08 — Local preferences
Persist the preferences needed for the approved MVP, including the desired enabled state where appropriate.

Do not treat a stored preference as proof that the relevant runtime component has successfully started.

Preference technology and migration strategy are architecture decisions, not pre-approved dependencies.
SCOPE-09 — Local diagnostics
Record useful local events, where observable, such as:
- startup and shutdown;
- overlay attachment, removal, and failures;
- relevant permission-state changes;
- Accessibility enabled/connected changes where detectable;
- recovery attempts and outcomes;
- errors that prevent approved functionality.
Diagnostics must be bounded, useful, and privacy-conscious. Retention limits and implementation choices must be documented in DATA_AND_DIAGNOSTICS.md.
SCOPE-10 — Testability and documentation
- Write automated tests for logic that can reasonably be tested without the target phone.
- Document acceptance criteria and run relevant checks.
- Perform actual-device validation on the target device.
- Document observed behavior, failed cases, workarounds, and limitations.
- Provide instructions to build, install, grant permissions, and inspect diagnostics.
5. Explicitly Out of Scope for v0.1
The following are excluded unless the product owner formally approves a scope change.
Feature exclusions
- Volume up/down/mute actions.
- Wi-Fi and Bluetooth toggles.
- Mobile-data, hotspot, GPS/location, NFC, flashlight, or other quick settings.
- Screenshot, screen recording, power menu, recent-apps, or navigation shortcuts not explicitly approved.
- Custom gesture builder.
- Arbitrary multi-action radial menu.
- User-created button layouts.
- Themes, extensive animation, decorative effects, and extensive personalization.
- Multiple floating buttons or multiple simultaneous overlays.
- Profiles, schedules, macros, and automation rules.
- Cloud backup, cloud synchronization, accounts, sign-in, and social features.
- Remote monitoring or notifications delivered through a server.
- Advertising, analytics SDKs, or third-party telemetry.
- Support commitments for a device/Android version that has not been tested.
Architecture and platform exclusions
- A separately installed Studio app.
- A backend or database server.
- A requirement that the app remain alive under every OS condition.
- Attempts to bypass Android permission, Accessibility, or background execution controls.
- Claims that the app can silently restore a disabled Accessibility setting.
- Unbounded service restarts, aggressive polling, or battery-draining keep-alive tricks.
- Unnecessary third-party dependencies or large frameworks without a documented need.
- Broad refactoring that is unrelated to an approved sprint.
Exclusions are intentional
Agents must not implement an excluded feature as an “improvement,” “bonus,” “future-proofing,” or hidden extra. Additions require a recorded owner decision and updated scope, requirements, tests, and sprint tasks.
6. Definition of a Successful MVP
The MVP is considered complete only when all applicable conditions below have evidence:
1. The app installs as one application and includes both Floating Assistant and Studio.
2. The user can request enable and disable actions from Studio.
3. The floating button appears when the necessary conditions are satisfied.
4. Repeated startup or recovery attempts do not create duplicate overlays.
5. Opening and closing Studio does not itself cause unwanted loss or duplication of the floating assistant.
6. Missing overlay permission is represented accurately and has actionable guidance.
7. If Accessibility is needed for the approved action, its enabled and connected conditions are distinguished as far as the platform makes them observable.
8. A disabled user preference is respected by recovery logic.
9. Recovery is bounded, does not bypass system controls, and records relevant outcomes.
10. Process-death and reboot behavior has been tested and documented without claiming unsupported guarantees.
11. Relevant preferences and diagnostics are persisted locally with documented bounds.
12. Core automated tests pass and their actual results are reported.
13. Real-device tests on the initial target device are recorded.
14. Installation, permission setup, and troubleshooting instructions are available.
15. No unresolved critical defect blocks the owner-approved core workflow.
A test is not complete merely because an agent generated a test file. It must be executed where the required environment is available, and the result must be reported honestly.
7. Milestone Boundaries
The detailed sprint sequence belongs in SPRINT_PLAN.md. These milestone boundaries are the high-level scope gates.
Milestone	Included work	Exit condition
M0 — Requirements baseline	Review brief/SRS/scope; resolve architecture-critical questions	Owner approves the requirements needed to begin implementation
M1 — Project foundation	Android project, manifest baseline, package structure, build/test setup	Clean project builds and baseline checks pass
M2 — Floating overlay	Permission handling, one overlay, attach/remove, duplicate prevention	Core overlay acceptance tests pass
M3 — State and runtime	Desired vs observed state, lifecycle management, bounded recovery foundation	State invariants and relevant tests pass
M4 — Accessibility integration	Only the owner-approved action and its required Accessibility behavior	Enabled/connected/disabled scenarios are handled and tested where applicable
M5 — Studio and diagnostics	Status UI, recovery guidance, local event history	Statuses match observable conditions; logs are useful and bounded
M6 — Reliability validation	Failure tests, device tests, bug fixes, resource checks	Required test matrix completed or exceptions explicitly accepted
M7 — v0.1 release candidate	Documentation, release build, known limitations, final checks	Release checklist completed and owner approves release


The order may be adjusted if the architecture demonstrates a better dependency order, but changes must be recorded. A milestone cannot be marked complete while its exit conditions are unmet.
8. Scope Rules for Coding Agents
1. One task, one objective: implement the requested task rather than an entire feature family.
2. No opportunistic features: do not add excluded functionality.
3. No silent product decisions: use [USER INPUT REQUIRED] or propose options when user guidance is necessary.
4. No speculative platform claims: verify current Android behavior from reliable documentation and test device-specific behavior.
5. No unrelated refactors: changes must be proportionate to the task.
6. No dependency inflation: propose and justify a dependency before adding it.
7. No fake verification: report only tests actually run and their results.
8. No permission bypass: respect Android user-consent and system-security requirements.
9. No uncontrolled recovery: retries must be bounded and observable.
10. Update the source of truth: when an approved change affects scope, requirements, architecture, or tests, update the impacted documents.
11. Inspect before editing: check the current repository, branch, and relevant files before changes; never assume the repository is empty.
12. Preserve unrelated work: do not overwrite or delete existing files without an explicit task requirement and review.
9. Scope Change Process
A proposed scope change must include:
- requested change and user problem it solves;
- whether it is required for the MVP or can be deferred;
- architecture and permission implications;
- privacy and battery implications;
- implementation effort and affected files;
- test plan and regression risks;
- changes needed to the SRS, roadmap, and related documents.
The agent must wait for owner approval before implementing any change that adds a feature, changes a user-visible action, adds a sensitive permission, changes data handling, adds networking, or materially changes architecture.

A bug fix that restores already-approved behavior is not a scope expansion, but it must still be tested and documented.
10. Owner Decisions Required Before Scope Is Frozen
These items must stay visibly unresolved until the owner responds or accepts a written recommendation.
ID	Decision	Approved State
S-01	Exact primary floating-button action	Approved: Double-tap to lock screen (GLOBAL_ACTION_LOCK_SCREEN); Long-press for Home/Back (GLOBAL_ACTION_HOME/BACK)
S-02	Exact triggering gesture, including double-tap timing if applicable	Approved: Double-tap within 300ms window; Long-press 500ms
S-03	Is dragging required in v0.1?	Approved: Yes, draggable with snap-to-edge animation
S-04	Should floating-button position persist after restart?	Approved: Yes, persist snapped coordinates in DataStore
S-05	Which UI toolkit should be used?	Approved: Jetpack Compose with Material 3
S-06	Minimum supported Android version	Approved: API 28 (Android 9.0 Pie); targetSdk 34
S-07	Personal APK, private distribution, or Play Store	Approved: Personal APK direct install / GitHub Releases
S-08	Target only RMX2156 initially or multiple devices?	Approved: Focused validation on Realme Narzo 30 4G (RMX2156)
S-09	Desired test duration and reliability targets	Approved: 8-hour soak test; <1% battery / 8h idle; <40MB RAM; <150ms latency
S-10	Button appearance and dimensions	Approved: 56dp circle, 70-80% idle opacity, 100% active opacity, neutral dark style


A task that materially depends on one of these unresolved choices must not be implemented by guessing. The agent should identify the dependency and present the smallest set of options needed for a decision.
11. Deferred Post-MVP Ideas
These are possibilities, not commitments:
- additional device actions;
- configurable gestures;
- button position and appearance customization;
- expanded diagnostics/export;
- broader device compatibility;
- extra user-interface polish.
No post-MVP feature should be added to v0.1 unless explicitly approved.
12. Scope Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Scope status: Approved
- Approved v0.1 core action: Double-tap to lock screen, Long-press for Home/Back, snap-to-edge drag, foreground service protection
- Approval date: 2026-10-29
- Approved exceptions: None (all exclusions maintained)
- Review notes: Approved via project questionnaire.
Until approved, this document is a controlled draft. Requirements that are clearly defined may be developed incrementally, but unresolved high-impact choices must not be silently decided by an AI agent.