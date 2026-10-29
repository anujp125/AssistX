# Nudge — AI Agent Execution Playbook
## Mandatory rules for Antigravity and any coding agent working on this repository

**Version:** 1.0  
**Status:** Owner review required; intended to become a mandatory repository instruction  
**Last updated:** 2026-10-30  
**Applies to:** All AI-assisted implementation, refactoring, testing, UI changes, and feature additions for Nudge.

> **Mission:** Continue the existing, working Nudge Android app in small, verifiable sprints. Use the approved project documentation as the contract. Do not redesign the product, replace working behavior, or add features outside the approved sprint.

---

## 1. Mandatory First Step: Inspect Before Editing

Before changing any file, the agent MUST:

1. Inspect the current repository tree, Git status, active branch, recent commits, and existing uncommitted changes.
2. Identify the actual Android application module, Gradle wrapper, Kotlin version, Android Gradle Plugin, `minSdk`, `targetSdk`, `compileSdk`, application ID, UI toolkit, and current test tasks.
3. Read the current implementation of the floating overlay, gesture detection, Accessibility service, screen-off action, Home action, Studio/settings screen, state/persistence, and diagnostics.
4. Run the existing build and relevant tests where possible. Record exact commands and actual outcomes.
5. Locate and read the approved guiding documents listed in Section 2. If a document is missing, search the repository and supplied project docs before creating a replacement.
6. Produce a short **pre-change report** containing:
   - current behavior that already works;
   - architecture and key files/classes;
   - build/test baseline;
   - relevant open decisions or conflicts;
   - proposed smallest implementation plan;
   - files likely to change and tests to add.
7. Wait for explicit user approval if the change affects permissions, a core gesture/action, user data, background execution, release configuration, or a decision marked OPEN/PROPOSED. If the user has already explicitly approved the exact sprint in the current task, proceed within that scope.

**Never assume the repository matches a proposed package tree or a generated document. The actual code and approved decisions must be reconciled first.**

---

## 2. Guiding Documents: Read These First

Use these as the core project contract. The agent does not need to ingest every planning file for every small task, but it MUST read the relevant subset before implementation.

### Always read for every task

1. `AGENTS.md` — repository-wide agent rules, if present.
2. `README.md` — actual project entry point and setup instructions.
3. `MVP_SCOPE.md` — included/excluded functionality.
4. `SRS.md` — requirements and acceptance expectations.
5. `SPRINT_PLAN.md` — task sequence, dependencies, and sprint boundaries.
6. `PRODUCT_DECISIONS_CHECKLIST.md` and `DECISION_LOG.md` — verify that required decisions are approved.
7. `CODING_STANDARDS.md` — implementation conventions.
8. `DEFINITION_OF_DONE.md` — completion requirements.
9. `REQUIREMENTS_TRACEABILITY.md` — relevant requirement/task/test links.

### Read when relevant

| Work area | Required documents |
|---|---|
| Overlay, gestures, service lifecycle, runtime state | `SYSTEM_ARCHITECTURE.md`, `MODULE_AND_PACKAGE_STRUCTURE.md`, `STATE_AND_LIFECYCLE.md` |
| Accessibility, screen off, Home, Back, Recents, system actions | `PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md`, `SECURITY_AND_PRIVACY_REVIEW.md`, `SRS.md` |
| Settings, preferences, diagnostics, logs | `DATA_AND_DIAGNOSTICS.md`, `STATE_AND_LIFECYCLE.md`, `SECURITY_AND_PRIVACY_REVIEW.md` |
| Failure handling/recovery | `FAILURE_AND_RECOVERY_MATRIX.md`, `FAILURE_INJECTION_PLAN.md` |
| Tests and device validation | `TEST_STRATEGY.md`, `ACCEPTANCE_TESTS.md`, `REAL_DEVICE_TEST_MATRIX.md` |
| Build setup/CI | `SETUP_AND_DEVELOPER_GUIDE.md`, `CI_AND_AUTOMATED_QUALITY_GATES.md`, `CONTRIBUTING.md` |
| Feature scope or UX | `PRODUCT_BRIEF.md`, `MVP_SCOPE.md`, `UI_UX_SPECIFICATION.md`, `PRODUCT_DECISIONS_CHECKLIST.md` |
| Release/signing/distribution | `RELEASE_AND_VERSIONING_PLAN.md`, `SECURITY_AND_PRIVACY_REVIEW.md` |

### Conflict rule

When a document conflicts with the actual implementation or another document:

1. Do not silently choose whichever is easiest.
2. Check the latest explicitly approved decision in `DECISION_LOG.md` and any relevant ADR.
3. Report the conflict and its impact.
4. Ask for a decision if it affects user-visible behavior, permission use, security/privacy, architecture, or release.
5. Update the relevant docs only after the decision is resolved.

A generated plan, sample, screenshot, mock, or agent suggestion is not approval.

---

## 3. Current Working Baseline — Preserve It

The user reports that Antigravity has built and the user is currently running a basic Android app with these working behaviors:

- Floating assistant/overlay button.
- Double-tap: turn the screen off using the current Accessibility-based implementation.
- Long-press: return to Home.
- Studio screen showing overlay permission, Accessibility service status, floating-window status, configured gestures, and local diagnostics.
- Dragging/repositioning and edge snapping are displayed as configured behavior in the current Studio UI.
- Idle fade is displayed as a configured behavior in the current Studio UI.

**Important:** Treat these as user-reported current behavior, not independently verified implementation details. Inspect the code and test the behavior before changing it. Do not assume every item displayed in Studio is fully implemented or reliably verified.

### Non-negotiable preservation rules

- Do not remove, rename, or rewrite the existing screen-off or Home actions while adding another feature.
- Do not replace the current overlay/service architecture without showing a concrete defect and obtaining approval for a major refactor.
- Do not introduce a second app or cloud/backend dependency.
- Do not remove Studio status indicators or local diagnostics without approval.
- Do not claim that a status is true merely because a preference says it is enabled.
- Do not add permissions just because a competitor or sample app uses them.
- Keep the existing app installable and preserve its application ID and signing/update compatibility.
- Do not reset the Git tree, discard user changes, or rewrite history to make the task easier.
- Do not commit generated secrets, signing keys, passwords, tokens, or private logs.

---

## 4. Sprint Rules

Every feature MUST be implemented as a small sprint with one primary outcome.

### Before implementation

For each sprint, record:
- Sprint ID and feature.
- User-visible objective.
- Requirements and decision IDs.
- Dependencies and permission impact.
- Files/classes likely to change.
- Acceptance criteria.
- Unit/integration/device tests.
- Rollback strategy.
- Explicit out-of-scope items.

If the requested feature has unresolved permission or platform implications, research and report options first. Do not silently implement the most powerful approach.

### During implementation

- Make the smallest cohesive change that satisfies the sprint.
- Preserve existing behavior and use the project's existing patterns where sound.
- Avoid broad refactors, unrelated formatting changes, dependency churn, or package moves.
- Keep platform-specific behavior behind a testable boundary where practical.
- Keep UI state truthful and observable.
- Make repeated actions safe; guard against duplicate overlays, duplicate listeners, duplicate service starts, and stale callbacks.
- Make Disable win over any pending start/recovery callback.
- Bound retries; do not create infinite restart loops.
- Use deterministic fakes for platform failure tests where possible.
- Add comments for non-obvious platform constraints, not for every line.
- Do not hide exceptions or mark an operation successful if the Android API did not confirm success.

### After implementation

1. Run the relevant unit tests.
2. Run the project build and any applicable lint/static checks.
3. Run instrumentation tests if the feature changes Android integration behavior and the environment supports them.
4. Test on the target phone when the acceptance criteria require real-device validation.
5. Compare old behaviors against the regression checklist.
6. Update requirements traceability, relevant docs, and the sprint record.
7. Report changed files, commands, actual outcomes, known limitations, and unrun tests.
8. Do not mark the sprint complete if a required test was not run or a required acceptance criterion failed.

### Sprint size

Prefer a single feature or tightly coupled behavior per sprint. Do not bundle Back, Recents, screenshots, volume, UI redesign, storage changes, and service refactoring into one large implementation.

---

## 5. Mandatory Feature Safety and Platform Review

Before adding any system action, the agent must document:

1. What exact user action is requested?
2. Which Android API or capability implements it?
3. Does it require Accessibility, overlay permission, a media/session API, projection/consent, or another special permission?
4. Is the capability officially available to an ordinary third-party app on the supported Android versions?
5. What happens if the permission is denied, revoked, or the service disconnects?
6. What user-visible status and feedback are shown?
7. What is the fallback if the action is unavailable?
8. How is the action tested without pretending a simulation is a device-level result?
9. Does the feature collect or expose screen/app content, audio, or other sensitive information?
10. Does it violate current scope or require owner approval?

Use official Android documentation for platform claims. Record the source and date reviewed in the relevant technical note. Do not rely on an old Stack Overflow answer or another app's behavior as proof of API availability.

### Permission principles

- Ask only for permissions justified by an approved feature.
- Explain the purpose before directing users to system settings.
- Use the standard Android settings flow; do not bypass consent or security controls.
- Detect and report missing permissions truthfully.
- If the service is unavailable, do not report that the corresponding action is ready.
- Do not attempt to defeat Android force-stop behavior or promise guaranteed background survival.

---

## 6. Proposed Feature Sprint Roadmap

This is a **proposed sequence**, not automatic approval to implement every feature. Inspect the current `SPRINT_PLAN.md` and update it after the owner approves this roadmap. Do not reuse task IDs if they already exist; allocate unique IDs in the project plan.

### Sprint F-01 — Baseline audit and regression tests

**Objective:** Protect the current working behavior before adding more actions.

Tasks:
- Inventory current overlay, gesture, service, Studio, settings, and diagnostics implementation.
- Verify double-tap screen-off and long-press Home on the target phone.
- Verify drag/edge snap/idle fade only if those behaviors are intended and actually implemented.
- Add regression tests for existing gesture recognition, lifecycle cleanup, and truthful status.
- Record current permissions, device build, app version, and source commit.

Acceptance:
- Baseline build/test commands are recorded.
- Existing features have explicit pass/fail/not-run evidence.
- No behavior is silently changed.

**Priority:** Mandatory first sprint.

### Sprint F-02 — Back action

**Objective:** Add a dedicated Back action to the floating button's interaction model.

Before coding:
- Decide how Back is invoked (for example, a new gesture, a secondary menu, or an action panel). Do not overload the existing double-tap or long-press without approval.
- Review the supported Android versions and Accessibility behavior.
- Define what happens when Back cannot be dispatched or the service is disconnected.

Acceptance:
- Existing double-tap screen-off and long-press Home still work.
- Back runs only through an approved, supported path.
- Failure is surfaced truthfully.
- No permission bypass or misleading success state.
- Tests cover recognition, dispatch result, missing service, and conflicts with other gestures.

**Owner decision needed:** interaction mapping and supported API behavior.

### Sprint F-03 — Recent apps / running-apps switcher

**Objective:** Add a user-invoked path to Android's Recents UI if the approved implementation can support it.

Important distinctions:
- “Open Recents” is not the same as enumerating installed/running apps.
- Do not implement a hidden task/app inventory merely because the request says “background/running apps.”
- Background process visibility and task enumeration are restricted and version-dependent.

Before coding:
- Ask the owner to confirm whether they mean **open the Android Recents screen** or **display an app/task list inside Nudge**.
- Research official Android support and Accessibility implications for the target API levels.
- Prefer opening the system Recents interface over inventing a custom task manager, unless the owner explicitly approves a separate, feasible design.

Acceptance:
- The meaning of “running apps” is explicitly resolved.
- The implementation uses a supported user-initiated action.
- No unsupported enumeration or misleading list of “running” apps.
- Existing actions remain unchanged.
- Device-specific behavior is recorded.

**Owner decision needed:** Recents shortcut versus in-app app/task list.

### Sprint F-04 — Screenshot / screen capture

**Objective:** Decide and, if approved, implement a safe screenshot workflow.

Before coding:
- Determine whether the requirement is a system screenshot action or capturing/saving/sharing screen content from Nudge.
- Review the exact Android version and official API availability.
- Identify consent, projection, Accessibility, or other restrictions that apply.
- Define whether screenshots are saved, where they are stored, how users are notified, and whether any content leaves the device.
- Avoid capturing, storing, or uploading screen contents without explicit user intent and compliant platform APIs.

Acceptance:
- The chosen screenshot mechanism is supported on the target configuration.
- Consent and permission requirements are honored.
- The user receives truthful feedback on success/failure.
- Storage behavior and deletion expectations are documented.
- No automatic upload or hidden capture.
- Tests distinguish simulated capture results from actual device capture.

**Owner decision needed:** screenshot mechanism, storage, and sharing policy.

### Sprint F-05 — Volume controls

**Objective:** Add a user-initiated volume action only after deciding which volume stream and control interaction are intended.

Before coding:
- Clarify whether the button should open the system volume UI, adjust media volume, adjust ring volume, or expose a volume panel.
- Prefer the official system volume UI or supported audio APIs appropriate to the use case.
- Do not silently change unrelated streams or override the user's volume settings.
- Define steps, range handling, feedback, and behavior when an adjustment is unavailable.

Acceptance:
- Only the approved volume stream/control is affected.
- Volume limits are respected.
- Current value and operation outcome are represented accurately where displayed.
- Tests cover minimum/maximum bounds and failure paths.
- Existing gestures still work.

**Owner decision needed:** open system volume panel versus direct volume adjustment and target stream.

### Sprint F-06 — Floating-button action menu / interaction redesign

**Objective:** Make multiple actions discoverable without accidental triggers.

This sprint depends on the number of approved actions and should not begin before the action map is agreed.

Possible approaches to evaluate:
- tap opens a small action panel;
- a distinct gesture selects secondary actions;
- an on-screen menu appears adjacent to the button.

Do not select an interaction solely for implementation convenience. Consider accessibility, touch exploration, accidental activation, screen-edge placement, and usability.

Acceptance:
- Every action has an unambiguous activation path.
- Existing screen-off/Home behavior is preserved unless the owner explicitly approves a migration.
- The panel does not block essential screen controls unnecessarily.
- Dismissal, outside taps, rotation/window changes, and service failure are handled.
- UI tests cover action selection and cancellation.

**Owner decision needed:** final interaction model.

### Sprint F-07 — Reliability and recovery hardening

**Objective:** Harden the expanded action set after individual actions work.

Potential tasks:
- reconcile permission/service status on resume;
- handle Accessibility disconnect/reconnect without loops;
- handle overlay attachment/removal failures;
- test repeated enable/disable;
- make late callbacks respect current desired state;
- add bounded retry/cooldown after the owner approves exact policy;
- ensure Studio Activity recreation does not duplicate runtime components;
- verify diagnostic storage remains bounded.

Acceptance:
- No false ACTIVE/ready state.
- No duplicate overlays/services.
- No infinite retries.
- Disable suppresses automatic recovery.
- Failure scenarios have recorded tests and evidence.
- Real-device behavior is reported separately from faked failures.

### Sprint F-08 — Accessibility, usability, and device regression

**Objective:** Validate the completed interaction set on the supported test matrix.

Test:
- target Realme device and exact OS/build;
- gesture conflict and accidental activation;
- screen lock/unlock;
- app switching and Studio navigation;
- permission revoke/grant through system settings;
- process recreation separately from force-stop and reboot;
- text scaling, TalkBack, contrast, and touch targets;
- repeated use and idle fade;
- failure feedback and recovery.

Acceptance:
- Results recorded per scenario and device.
- No unverified universal compatibility claims.
- Remaining issues are prioritized and documented.

---

## 7. Shared Regression Checklist for Every Feature Sprint

Before merging a feature, check all applicable items:

- [ ] App builds with the existing verified Gradle wrapper.
- [ ] Existing double-tap screen-off behavior still works.
- [ ] Existing long-press Home behavior still works.
- [ ] No gesture conflict causes two actions to fire for one interaction.
- [ ] Overlay permission status is current.
- [ ] Accessibility status is current when relevant to the approved action.
- [ ] Floating-window status is not inferred from permission alone.
- [ ] Studio open/close does not create duplicate runtime components.
- [ ] Activity recreation does not create duplicate overlays/services.
- [ ] Disable prevents pending or future recovery from re-enabling the feature.
- [ ] Failure does not become a false success state.
- [ ] Diagnostics remain local and bounded according to approved policy.
- [ ] No unrelated data, screenshot content, or secrets are added to logs.
- [ ] No unapproved permission, network call, dependency, or exported component was added.
- [ ] UI remains usable with text scaling and accessibility services.
- [ ] Actual test outcomes and unrun checks are reported honestly.
- [ ] Related docs and traceability are updated.

If a regression appears, fix it or stop and report it; do not hide it by weakening an acceptance test without owner approval.

---

## 8. Task Template for Antigravity

Copy this template into the agent task for each sprint.

### Task identity
- **Sprint ID:** `[FILL IN]`
- **Task ID:** `[FILL IN]`
- **Feature:** `[FILL IN]`
- **Approved decision IDs:** `[FILL IN]`

### Goal
`[One observable user outcome]`

### Current baseline
`[Describe the existing implementation after inspecting code; include files/classes and current behavior]`

### Scope
**Must do**
- `[FILL IN]`

**Must not do**
- Do not change unrelated features.
- Do not refactor unrelated architecture.
- Do not add unapproved permissions/dependencies/network calls.
- Do not alter application ID or signing configuration.
- Do not claim unrun tests passed.

### Plan before coding
1. Files/classes to change: `[FILL IN]`
2. Platform/API constraints: `[FILL IN]`
3. Test plan: `[FILL IN]`
4. Regression risks: `[FILL IN]`
5. Rollback plan: `[FILL IN]`

### Acceptance criteria
- [ ] `[Observable criterion 1]`
- [ ] `[Observable criterion 2]`
- [ ] Existing behavior preserved.
- [ ] Failure behavior defined and tested.
- [ ] Documentation and traceability updated.

### Required report
- Summary of implementation.
- Files changed and why.
- Build/test commands actually run.
- Results: PASS / FAIL / BLOCKED / NOT RUN.
- Device/model/Android build if device-tested.
- Known limitations and unresolved decisions.
- Commit/hash if committed.
- Any deviations from this plan, with explanation.

---

## 9. Definition of Done for a Feature Sprint

A feature is not “done” merely because a button or menu appears.

The sprint is complete only when:
1. The user-visible behavior matches an approved requirement.
2. Permission and platform constraints are satisfied.
3. Existing actions pass regression checks.
4. Success and failure states are truthful.
5. Repeated operations and lifecycle events do not create duplicates or unsafe loops.
6. Required automated tests actually ran and passed.
7. Required device tests actually ran, or the sprint is explicitly reported as blocked/partially complete.
8. The implementation has been reviewed for accidental data collection and unnecessary permissions.
9. Relevant documentation, traceability, and the decision log are updated.
10. The final agent report clearly distinguishes completed, failed, blocked, and untested items.

---

## 10. Final Agent Instruction

Work incrementally and conservatively. Nudge already has a user-reported working baseline. Protect that baseline first, then add one approved capability at a time.

**Do not implement all proposed features in one pass. Do not assume “Back,” “running apps,” “screenshot,” and “volume” have identical permission or platform requirements. Do not make a feature appear implemented by updating only the UI.**

The preferred loop is:

**Inspect → read guiding docs → confirm decisions → plan one sprint → implement smallest change → test → regression-check → update docs → report evidence → wait for next sprint.**
