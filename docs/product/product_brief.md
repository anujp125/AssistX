AssistX --- Product Brief
Document status: Approved
Version: 0.1
Last updated: 2026-10-29
Product type: Native Android application
Official product name: AssistX
AI-agent rule: This document is a product-level source of truth,
but it does not override the approved SRS, explicit user decisions, or
platform constraints. Items marked [USER INPUT REQUIRED] are
unresolved and must not be silently invented by an AI agent.

1. Product Summary
AssistX is a lightweight, reliability-focused Android application that
provides an assistive floating button and an in-app settings and
diagnostics interface.

AssistX will be distributed as one installable Android app. The
floating assistant and the Studio/settings interface are logical
components within the same application, not separate applications.

The primary product objective is to make the floating assistant's
runtime state understandable and as reliable as Android permits. The app
should distinguish overlay problems from Accessibility Service problems,
communicate required user action clearly, and attempt recovery only
where the operating system permits it.
2. Problem Statement
The existing assistive-touch application used by the product owner
sometimes loses its floating button. Opening the app can make the button
reappear. The owner has also observed occasions when Android's
Accessibility setting appears to become disabled or when an action
reports that Accessibility must be enabled again.

The cause has not yet been established. These observations may represent
different failure modes, including overlay lifecycle interruption,
Accessibility Service disconnection, a changed system setting, or
device-specific background restrictions.

AssistX must not assume a cause without evidence. It should expose
accurate status information and retain enough local diagnostic
information to help distinguish failure types.
3. Product Vision
Provide a minimal assistive floating interface that: - has a clear and
predictable enabled/disabled state; - separates floating-overlay health
from Accessibility functionality; - detects and communicates permission
or service problems accurately; - attempts safe recovery where Android
allows it; - does not create duplicate overlays or uncontrolled restart
loops; - requires no backend, account, or cloud service for the MVP.
4. Target User and Device
Primary user
The initial product owner and tester is the person developing AssistX for
personal use.
Initial target device
- Device: Realme Narzo 30 4G
- Model: RMX2156
- Android: Android 12
- UI: realme UI 3.0
- Build reported by owner: RMX2156_11_F.19
- Hardware reported by owner: MediaTek Helio G95; 4 GB physical
  RAM plus 3 GB extended RAM
These details are the initial test environment, not a guarantee of
compatibility across all devices.
Approved target baseline
- Minimum supported Android version: API 28 (Android 9.0 Pie)
- Initial target scope: Realme Narzo 30 4G (RMX2156, realme UI 3.0, Android 12)
- Intended distribution: Personal APK direct install / GitHub Releases
- Preferred application language: English (en-US)
Agents must not turn these open questions into permanent product
requirements without approval.
5. Product Structure
AssistX is one Android application with the following logical components:
1. Floating Assistant
   - Displays and manages the floating button.
   - Handles only gestures and actions approved for the MVP.
   - Must not require the Studio screen to remain open.
2. Studio / Settings Interface
   - Shows application and component status.
   - Provides enable/disable controls.
   - Explains required permissions and recovery steps.
   - Displays recent diagnostic events where implemented.
3. Runtime and State Management
   - Coordinates lifecycle and intended enabled state.
   - Prevents duplicate runtime instances and overlays.
   - Represents service and permission health accurately.
4. Accessibility Integration
   - Supports only functionality that genuinely requires
     Accessibility.
   - Tracks whether the service is enabled and connected, where
     detectable.
   - Must remain logically separate from the health state of the
     floating overlay.
5. Preferences and Diagnostics
   - Stores required local preferences.
   - Records relevant lifecycle, permission, disconnection, and
     recovery events.
   - Avoids unnecessary personal or sensitive data.
This is a responsibility-level description, not a mandate to create a
separate Gradle module for every component. Keep the implementation
simple unless a concrete requirement justifies additional structure.
6. MVP Objectives
The MVP should demonstrate that AssistX can:
1. Show one floating button when the user enables it and grants the
   required permission.
2. Avoid duplicate overlays and conflicting service instances.
3. Keep the floating assistant independent of the Studio Activity
   lifecycle.
4. Distinguish overlay permission problems from Accessibility being
   disabled or disconnected.
5. Display understandable states and direct the user to the relevant
   system settings when intervention is required.
6. Preserve user preferences through ordinary process recreation where
   feasible.
7. Handle process interruption and reboot behavior within Android's
   rules, without promising unrestricted background persistence.
8. Record useful local diagnostic events for investigation.
9. Run on the initial target device and pass documented acceptance
   tests.
7. MVP Scope
Included
- Minimal floating button.
- Basic enable/disable workflow.
- Overlay permission handling.
- Runtime/service lifecycle handling appropriate to the selected
  Android implementation.
- Accessibility enabled/connected status handling.
- Clear status and recovery UI.
- Local preferences.
- Local diagnostics.
- Automated tests for testable logic.
- Real-device validation on the target device.
- Build and installation documentation.
Excluded from the initial MVP
- Volume control features.
- Wi-Fi and Bluetooth toggles.
- Cloud synchronization or remote storage.
- User accounts or authentication.
- Backend services or APIs.
- Advanced gesture customization.
- Multiple themes and extensive personalization.
- Separate Studio application.
- Claims of guaranteed always-on behavior.
- Attempts to bypass Android permission or background-execution
  protections.
Any addition to this list requires explicit scope approval and an update
to the relevant requirements and sprint plan.
8. Reliability Principles
These principles are mandatory design constraints:
1. Truthful state: Do not report a component as active unless the
   available evidence supports that status.
2. Separate health dimensions: Overlay visibility,
   foreground/runtime state, and Accessibility state are distinct.
3. Safe recovery: Recovery must be bounded, observable, and
   idempotent where possible.
4. No restart loops: Do not repeatedly restart components without a
   defined limit and reason.
5. No duplicate overlays: Repeated start requests must not create
   multiple floating buttons.
6. Respect user intent: A user-disabled app must not automatically
   re-enable itself.
7. Respect Android: Do not claim the app can silently re-enable a
   permission or Accessibility setting that Android requires the user
   to change.
8. Observable failures: Record useful diagnostic events without
   collecting unnecessary personal data.
9. Device evidence: Device-specific behavior must be verified on
   hardware before being described as confirmed.
10. Small changes: Agents must implement approved tasks
    incrementally and avoid unrelated refactoring.
9. Success Criteria
The MVP is successful when: - the floating button can be enabled and
disabled predictably; - only one overlay is present; - Studio can open
and close without being required to keep the overlay running; - the app
distinguishes the principal overlay and Accessibility failure states it
can detect; - recovery guidance matches the detected condition; -
relevant events are recorded locally; - automated tests pass; - the
defined real-device test matrix is completed and results are
documented; - no unresolved critical defect prevents the core workflow.

Exact numerical thresholds for uptime, battery use, memory use, and
recovery time are not yet defined.
- Reliability measurement period: 8-hour continuous soak test on target device
- Acceptable battery impact: < 1.0% battery drain over 8 hours idle
- Acceptable recovery latency: < 150 ms touch-to-action; recovery triggered within 1000 ms
Agents must not invent performance targets. Propose measurable targets
for owner approval when the test strategy is drafted.
10. Technical Direction
Current direction: - Native Android. - Kotlin. - One installable
application. - No backend or cloud dependency for the MVP. - Android
overlay APIs for the floating interface. - Accessibility Service only
for functionality that requires it. - Local persistence for preferences
and diagnostics. - Automated tests plus actual-device testing.

The precise UI toolkit, minimum SDK, persistence library, service type,
and any additional dependencies must be decided in the architecture and
technical-design documents.
- UI toolkit: Jetpack Compose with Material 3
- Minimum SDK: API 28 (Android 9.0 Pie); targetSdk 34 (Android 14)
- Application/package ID: com.nudge.assist
- Repository: AssistX
Agents may recommend options, but must not treat these placeholders as
approved decisions.
11. Constraints and Known Risks
- Android may restrict background execution and startup behavior.
- Device manufacturers may apply additional battery or
  background-management policies.
- Overlay permission and Accessibility enablement are controlled by
  Android system settings.
- An enabled Accessibility Service may still be disconnected; enabled
  and connected are not equivalent.
- Reboot behavior and service restoration must be tested rather than
  assumed.
- The original application's failure cause is not yet known.
- Long-duration reliability and battery behavior need empirical
  testing.
- A foreground service can improve runtime visibility/persistence in
  suitable cases but cannot guarantee uninterrupted execution.
12. Development Approach
Development will proceed in small, dependency-ordered sprints.

Every sprint task must specify: - objective and rationale; - scope and
explicit non-goals; - dependencies and prerequisites; - expected files
or components to change, where known; - acceptance criteria; - tests to
run; - permissions, lifecycle, and regression risks; - documentation
updates; - completion evidence and unresolved issues.

An agent must inspect the current repository and relevant documentation
before editing. It must not assume the repository is empty or overwrite
existing work without inspection.
13. AI-Agent Guardrails
AI agents must:
- treat approved requirements and user decisions as authoritative;
- stop and ask when a decision materially affects product behavior,
  permissions, privacy, architecture, or scope;
- preserve [USER INPUT REQUIRED] placeholders until answered;
- distinguish facts, assumptions, recommendations, and unresolved
  questions;
- avoid unsupported claims about Android or Realme behavior;
- prefer official Android documentation for platform-sensitive claims;
- avoid adding dependencies or features without approval;
- avoid broad refactors unrelated to the assigned task;
- write or update tests for behavior changes;
- run relevant checks and report exact results;
- never claim tests or device validation were performed if they were
  not;
- document any task that cannot be completed and explain the blocker;
- never attempt to bypass system permissions or security restrictions.
14. Open Decisions Register
The following decisions need owner input or a documented recommendation
before they become fixed requirements.
  ID    Decision                  Status    Approved Value
  U-01  Minimum Android version   Approved  API 28 (Android 9.0 Pie)
  U-02  Initial distribution      Approved  Personal APK direct install / GitHub Releases
  U-03  UI toolkit                Approved  Jetpack Compose with Material 3
  U-04  Package/application ID    Approved  com.nudge.assist
  U-05  Exact MVP gestures        Approved  Double-tap: Screen Lock; Long-press: Home/Back; Drag: Snap to edge
  U-06  Button appearance         Approved  56dp diameter, 70-80% idle opacity, neutral dark style
  U-07  Performance targets       Approved  8h soak test; <1% battery / 8h idle; <40MB RAM; <150ms latency
  U-08  License                   Approved  GNU General Public License v3.0 (GPLv3)
  U-09  Device compatibility      Approved  Baseline focus on Realme Narzo 30 4G (RMX2156)
15. Document Authority and Change Policy
This brief defines product intent. The SRS will define testable
requirements. Architecture documents will define technical structure.
The sprint plan will define implementation order.

When documents conflict: 1. Explicit, recent owner decisions take
precedence. 2. Approved requirements and scope decisions take precedence
over agent assumptions. 3. Platform and security constraints must be
respected. 4. The agent must identify conflicts instead of silently
choosing one. 5. Any approved change must be reflected in the affected
documents and decision log.
16. Approval
- Product owner: Anuj Pawar (@anujp125)
- Review status: Approved
- Approval date: 2026-10-29
- Notes: Product brief approved via project questionnaire.