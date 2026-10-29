AssistX — UI/UX Specification
Document ID: NUDGE-UX-001
Version: 0.1
Status: Draft — product review required
Last updated: 2026-10-29
Related documents: PRODUCT_BRIEF.md, SRS.md, MVP_SCOPE.md, SYSTEM_ARCHITECTURE.md, STATE_AND_LIFECYCLE.md, PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md, FAILURE_AND_RECOVERY_MATRIX.md, DATA_AND_DIAGNOSTICS.md, ACCEPTANCE_TESTS.md, DECISION_LOG.md
Product constraint: AssistX is one installable Android app with two connected experiences: the floating assistant and the Studio/settings interface. This document describes user-visible behavior; it does not authorize extra features, permission use, or a specific primary button action. Items marked [OWNER DECISION REQUIRED] must remain unimplemented or use a clearly documented temporary prototype choice until approved.

1. Purpose
Define the user-facing screens, interaction rules, status language, permission guidance, error states, and basic accessibility requirements for AssistX's MVP.

The UI should be calm, compact, dependable, and transparent. It should make it easy to understand whether AssistX is enabled, whether the floating button is actually available, and what action—if any—the user needs to take.
2. UX Principles
1. Truthful status: Never show success merely because the user requested an operation.
2. One app, two experiences: The floating assistant and Studio share the same settings and state model.
3. Studio is not a runtime dependency: Closing Studio must not by itself be represented as disabling the assistant; the actual Android lifecycle and approved runtime architecture determine availability.
4. Explain blockers: When a permission or system condition blocks a feature, state what is missing and how the user can resolve it.
5. Separate state dimensions: Desired state, permission state, overlay attachment, runtime health, and Accessibility state (if applicable) are not synonyms.
6. No alarmist UI: Use neutral, actionable language rather than blaming the user, device, or operating system.
7. Accessible interaction: Support screen readers, large text, sufficient contrast, and clear touch targets.
8. Avoid permission pressure: Explain why a permission is required; never repeatedly prompt, mislead, or make unsupported claims.
9. Minimal MVP: Do not add customization, dashboards, accounts, remote services, or advanced gestures unless approved.
10. Respect user intent: Explicit Disable must stop automatic recovery from re-enabling the assistant.
3. Primary User Journeys
Journey A — First launch
1. User opens AssistX.
2. Studio explains the app's purpose and the approved primary action.
3. User reviews the explanation and chooses whether to enable the assistant.
4. If overlay permission is needed, AssistX explains why and offers a clear route to Android settings.
5. On return, AssistX checks the current permission instead of assuming it was granted.
6. The runtime attempts setup only after required conditions are met.
7. Studio reports the confirmed result or shows a blocker with next steps.
Do not: imply that visiting settings guarantees permission was granted, or that permission alone guarantees the floating button is attached.
Journey B — Normal use
1. The floating button is visible only while the relevant runtime and platform conditions allow it.
2. The user invokes the approved action using the approved gesture.
3. AssistX gives feedback appropriate to the outcome, without obscuring system UI.
4. Studio can be opened to inspect settings and the latest status.
[OWNER DECISION REQUIRED] The button action, gesture, action feedback, and whether a confirmation is required are not finalized. Do not use a potentially disruptive action as the assumed default.
Journey C — A permission is missing or revoked
1. AssistX observes or re-checks the required permission.
2. Studio shows a clear Permission required or Permission status unknown state.
3. User can open the relevant Android settings page through an explicit action.
4. On return, AssistX rechecks the current state.
5. If permission remains missing, the blocker remains; AssistX does not loop through settings or repeatedly attempt an operation that cannot succeed.
Journey D — Assistant appears unavailable
1. Studio shows the last confirmed state and the current blocker/uncertainty, where known.
2. The app offers a bounded, user-understandable recovery action only if one is approved.
3. Diagnostics record a sanitized event if storage is available.
4. The UI avoids claiming a specific cause—such as battery management or process termination—without evidence.
Journey E — User disables AssistX
1. User presses Disable in Studio.
2. AssistX records the new desired state if persistence succeeds.
3. Runtime cleanup is attempted.
4. Studio reports the actual result, including cleanup failure if applicable.
5. Automatic recovery does not restart the assistant while desired state is disabled.
4. Navigation and Information Architecture
MVP navigation should remain small. Use a single Studio Activity with clear sections rather than separate installable apps.
Suggested structure
- Home / Status
  - Current overall status
  - Enable/Disable control
  - Floating assistant state
  - Any required action
- Settings
  - Only approved settings
  - Relevant permission guidance
- Diagnostics — [OWNER DECISION REQUIRED]
  - May be omitted from MVP
  - If included, show a small, privacy-safe view of recent event categories and timestamps
  - Never display unrelated app content or raw sensitive exception data
- About
  - App version/build where useful
  - Short privacy statement
  - Support/troubleshooting guidance if approved
Do not add tabs or navigation destinations with no functioning content. Avoid a separate “AI dashboard” or cloud account surface in the MVP.
5. Screen Specifications
5.1 First-run / onboarding
Goal: Explain the value and required setup before requesting system permissions.

Suggested content:
- App name: AssistX
- One-sentence explanation: [APP PURPOSE COPY — OWNER APPROVAL REQUIRED]
- Approved button actions: Double-tap to lock screen (GLOBAL_ACTION_LOCK_SCREEN); Long-press for Home/Back (GLOBAL_ACTION_HOME/BACK)
- Why overlay permission is needed.
- Whether Accessibility permission is needed and why, but only if the final approved action requires it.
Primary action:
- Continue or Set up assistant, depending on the approved flow.
Secondary action:
- Not now or Keep disabled.
Rules:
- Do not request all permissions in a single unexplained sequence.
- Do not imply that Accessibility is required if the approved MVP action does not need it.
- If the user declines, preserve a valid disabled state and make setup available later.
- Avoid an onboarding flow that cannot be dismissed.
5.2 Home / status screen
Suggested content order:
1. Overall status with text and icon; color is supplementary.
2. Assistant state: enabled/disabled as desired by the user.
3. Floating button state: attached, unavailable, checking, or unknown as appropriate.
4. Blocker/action required: one concise explanation and a relevant action.
5. Enable/Disable control: clearly reflects the user's desired state.
6. Secondary navigation: Settings, About, and Diagnostics only if approved.
Example state content:
Condition	Suggested heading	Suggested detail	Primary next step
User has disabled AssistX	Assistant is off	The floating assistant will not be started while it is disabled.	Enable assistant
Enabled and confirmed available	Assistant is ready	The floating button is currently available.	Approved main control
Missing overlay permission	Permission required	Allow display over other apps to show the floating button.	Open settings
Checking current state	Checking status	AssistX is checking current permissions and runtime state.	No repeated action needed
Runtime startup failed	Assistant unavailable	AssistX could not start the floating assistant.	Show approved recovery/help action
Permission status unknown	Status unavailable	AssistX could not confirm the current permission state.	Check again if safe and supported
Overlay detached unexpectedly	Floating button unavailable	The button is not currently attached.	Show a bounded recovery or setup action
Required Accessibility service disconnected	Service disconnected	The required service is not currently connected.	Open system settings/help, if applicable
Settings could not be saved	Changes not saved	AssistX could not save this setting. Review the setting before leaving.	Try again if supported


These strings are proposed copy. Align them with the implemented state model before release. A detail line must not claim a root cause that the app cannot observe.
5.3 Enable / Disable control
- Clearly distinguish the user's desired state from current runtime availability.
- When the user requests Enable, show an in-progress state while required checks and setup occur.
- Do not convert to a success state until the relevant runtime outcome is confirmed.
- If Enable is blocked, preserve a truthful blocked state and show the next step.
- When the user requests Disable, update desired state promptly according to the approved persistence policy, then attempt cleanup.
- If cleanup fails, do not show an unqualified “fully stopped” success message.
- Prevent rapid repeated taps from starting duplicate operations. Disable or debounce the control while an operation is unresolved, but keep the UI responsive.
- Do not make the toggle visually jump back and forth due to asynchronous events.
- Use accessible labels that describe the resulting action, e.g. Enable assistant or Disable assistant, rather than an unlabeled icon-only switch.
[OWNER DECISION REQUIRED] Decide whether this should be a switch, a button, or another control after the state model and interaction are reviewed. If a switch is used, its checked meaning must consistently represent desired state—not a mixture of desired and runtime state.
5.4 Floating assistant button
Purpose: Provide one compact, persistent entry point for the approved action.

Visual guidance:
- Small, high-contrast, recognizable shape.
- Avoid a decorative glow or heavy animation that implies active monitoring when not confirmed.
- Keep the icon distinct at small sizes and with Android system themes.
- Avoid covering common system controls or critical app UI where practical.
- Ensure touch target and hit area meet the accessibility/platform requirements chosen by the implementation.
- Keep text labels out of the floating control unless they are needed for clarity.
Interaction:
- Gesture and action must be explicitly specified before implementation.
- The button must not execute a different action because another service or permission is unavailable.
- If the approved action fails, provide non-disruptive, truthful feedback.
- Do not add long-press, double-tap, drag, or gesture customization merely as assumptions.
- If moving/repositioning is approved, persist normalized coordinates rather than raw absolute pixels and recover safely after display-size/orientation changes.
- Any action requiring Accessibility must be guarded by a current availability check; do not assume enabled setting means the service is connected.
Visual states:
- Available: current runtime confirms the button can be used.
- Unavailable: current evidence confirms it is not available.
- Unknown: AssistX cannot determine the current state.
- Transitioning: setup or removal is in progress.
Do not rely on color alone to distinguish these states. The floating button itself may not be capable of presenting all states; Studio remains the authoritative user-facing explanation.

[OWNER DECISION REQUIRED]
- Primary action: Double-tap to lock screen (GLOBAL_ACTION_LOCK_SCREEN)
- Gesture: Double-tap within 300ms window; Long-press 500ms
- Drag/reposition support: [OWNER INPUT REQUIRED]
- Button opacity/size options: excluded unless approved
- Feedback method for action completion/failure: [OWNER INPUT REQUIRED]
5.5 Settings screen
Only expose settings that have been approved and implemented.

Potential MVP settings:
- Assistant enabled/disabled control if not exclusively on Home.
- Open overlay permission settings, when permission is required.
- Open Accessibility settings, only when the approved feature requires Accessibility.
- Basic appearance/feedback choices, only if included in MVP scope.
For every setting:
- Use a precise label.
- Explain material effects in short text.
- Persist changes through the approved settings repository.
- Show whether saving succeeded.
- Avoid silently rewriting values during Activity recreation.
- Do not include nonfunctional controls or future-feature toggles.
5.6 Permission guidance screen/section
Each permission explanation should state:
1. What capability needs the permission.
2. What AssistX will use it for.
3. What will not be collected or done.
4. How to grant it through Android settings.
5. What remains unavailable if the user declines.
Example overlay permission copy:
Allow floating display

AssistX needs permission to display its floating button over other apps. Android manages this permission in system settings. The button will remain unavailable until permission is granted and the runtime successfully attaches it.

Only use this copy if the feature indeed requires overlay permission.

Example Accessibility copy (conditional):
Accessibility service needed

[EXPLAIN THE EXACT APPROVED ACTION AND WHY THIS SERVICE IS REQUIRED.] AssistX must not claim this permission is required for unrelated features. You can keep the assistant disabled if you do not want to enable it.

Rules:
- Use Android's normal user-controlled settings pages.
- Re-check on return; never assume the user granted permission.
- Don't open settings repeatedly or unexpectedly.
- Don't claim to enable a permission automatically.
- If a permission cannot be verified, use an unknown/checking state rather than assuming success.
5.7 Diagnostics screen (optional)
[OWNER DECISION REQUIRED] Whether a user-visible diagnostics screen is in MVP.

If included, keep it minimal:
- Latest status observation time.
- App version/build.
- Recent operational event category and timestamp.
- A plain-language summary of the last failure, if known.
- Clear indication when diagnostics could not be recorded.
Do not display:
- raw stack traces by default;
- unrelated app content;
- hidden/system data that is not required for troubleshooting;
- a root-cause claim based on a guess.
Export is not required. If later approved, it must be explicit, sanitized, previewed, and user-initiated.
5.8 About / privacy
Include the app version and concise privacy statement appropriate to the actual implementation. Proposed statement for review:
AssistX's MVP stores its settings and diagnostic information locally on your device. It does not require a AssistX account or cloud service. The app should not collect unrelated app content. Review this statement against the final permissions, libraries, and data flows before release.

Do not publish this statement unchanged until it has been validated against the release build, dependencies, and platform permissions.
6. Visual System
The exact brand system has not been approved. Use a small, consistent design system rather than ad hoc styling.
6.1 Layout and hierarchy
- One clear page title per Studio screen.
- Primary status appears near the top.
- One dominant next action per blocker state.
- Secondary controls appear below the main status/action.
- Use consistent spacing and alignment.
- Support portrait layouts and narrow screens; do not depend on a fixed width.
- Scroll when necessary rather than truncating important permission or error text.
- Avoid graphs, cards, and metrics that do not support the MVP's core task.
6.2 Color and status
[OWNER INPUT REQUIRED] Approve brand colors and light/dark theme policy.

Recommended semantics:
- Neutral: inactive, informational, or not yet known.
- Green or equivalent positive cue: confirmed available/success.
- Amber or equivalent warning cue: user action required, uncertainty, or recoverable issue.
- Red or equivalent error cue: confirmed failure that needs attention.
Never communicate status through color alone. Pair color with a label, icon, or concise explanation. Do not use green merely because the desired state is enabled.
6.3 Typography and touch targets
- Use Android's supported scalable text sizes.
- Avoid fixed-height containers that clip large text.
- Maintain adequate contrast in light and dark modes.
- Provide sensible minimum interactive target sizes using current Android accessibility guidance.
- Avoid small unlabeled icon-only controls in Studio.
- Respect system font scaling and display size.
- Test with TalkBack and large font settings.
6.4 Motion and feedback
- Prefer short, subtle transitions.
- Avoid continuous pulsing to imply the app is monitoring or healthy.
- Never use animation as the sole confirmation of an operation.
- Do not delay critical feedback behind a long animation.
- Haptics are not assumed; add only if approved and user-controllable where appropriate.
7. Accessibility Requirements
- Every actionable UI element has a meaningful accessible name.
- Status changes are exposed to screen readers without repeatedly announcing unchanged status.
- Button meaning remains understandable without color or animation.
- Touch targets are appropriately sized and spaced.
- Text supports system font scaling and does not clip at large sizes.
- Contrast follows current Android accessibility guidance.
- Screen navigation order is logical.
- Modal permission explanations can be dismissed or navigated predictably.
- Controls expose desired state consistently to accessibility services.
- Do not add an Accessibility permission requirement just to implement basic screen-reader support.
Verification: Include manual TalkBack and large-font checks in REAL_DEVICE_TEST_MATRIX.md or the appropriate acceptance suite.
8. UI State Model and Copy Rules
The UI should render from a state model supplied by the app architecture, not independently infer operational truth from a persisted boolean.

Minimum distinct concepts:
- Desired state: enabled or disabled.
- Permission state: granted, missing, checking, or unknown as applicable.
- Overlay runtime state: attached, detached, transitioning, or unknown.
- Overall readiness: ready, blocked, in progress, unavailable, or unknown.
- Accessibility state, only when applicable: setting disabled, service disconnected, connected, or unknown.
- Settings persistence state: saved, saving, or failed where relevant.
Not every screen needs to show every dimension. Home should show the most useful summary plus any actionable blocker.
Copy rules
- Say “currently available” only with current confirming evidence.
- Say “permission required” only when the relevant permission is confirmed missing.
- Say “could not verify” when the state source failed or is unknown.
- Say “not saved” when persistence fails.
- Say “attempting to recover” only while a bounded recovery is actually in progress.
- Say “stopped” only when the required stop/cleanup outcome is confirmed; otherwise qualify it.
- Do not say “Android killed the app,” “Realme battery manager caused this,” or similar without supporting evidence.
- Avoid blame: prefer “AssistX couldn't verify...” over “You didn't allow...”.
9. Loading, Error, Empty, and Recovery Patterns
Loading
- Use a brief status such as Checking status….
- Avoid endless spinners. If the operation exceeds a defined timeout, show a recoverable state.
- Do not block unrelated Studio navigation while a status check is pending.
Error
Each user-facing error should include:
- what failed, at a useful level;
- whether the assistant is currently available, unavailable, or unknown;
- one next step when an actionable step exists.
Do not surface raw exception messages directly.
Empty
- Empty diagnostics is a valid state, not an error.
- Missing optional settings should use defaults and not render broken placeholders.
- A disabled assistant should have an intentional layout, not look like a failed load.
Recovery
- Recovery must obey the retry/cooldown policy in the approved engineering design.
- Show a retry action only if it is safe and supported.
- Avoid repeated automatic permission prompts.
- Do not restart after explicit Disable.
- Keep the user informed if recovery is exhausted or manual action is required.
10. UI Event Handling and Lifecycle
- Activity recreation must not attach a second floating button.
- Opening or closing Studio must not itself toggle assistant desired state.
- Rendering/recomposition must be side-effect-free with respect to start/stop operations.
- A late asynchronous result must not override a newer Disable request.
- On resume from Android settings, re-check the relevant permission and runtime state.
- If the process is recreated, rebuild current status from current observations and approved persisted preferences.
- Do not restore a stale saved ACTIVE value as proof of availability.
- If the diagnostics repository fails, show the operational result independently from the diagnostics result.
- Controls should not fire duplicate actions from accidental repeated taps or re-rendering.
11. UI Acceptance Checklist
The following are proposed checks; they are not evidence of execution.
- [ ] First launch does not imply that the assistant is enabled.
- [ ] Permission explanations precede any permission request.
- [ ] Declining permission leaves a usable Studio with a clear blocker.
- [ ] Returning from settings re-checks the permission.
- [ ] A missing permission cannot show a ready state.
- [ ] Enable shows a transitional state until an operation result is confirmed.
- [ ] Enable failure is clearly distinguished from permission missing and unknown state.
- [ ] Disable suppresses further automatic recovery.
- [ ] Failed cleanup does not show an unqualified successful stop.
- [ ] Desired state and actual overlay/runtime state are displayed distinctly where needed.
- [ ] Activity recreation does not duplicate the overlay.
- [ ] Opening/closing Studio does not toggle desired state.
- [ ] Errors have actionable, nontechnical copy.
- [ ] No unapproved gesture or primary action is implemented.
- [ ] No nonfunctional setting appears in the UI.
- [ ] Status is readable without color alone.
- [ ] Text scales without clipping at large font settings.
- [ ] TalkBack can identify and operate all essential controls.
- [ ] Diagnostics failure does not mask primary operation feedback.
- [ ] Release UI and privacy copy match the actual permissions/data flows.
12. Owner Decisions Required
Decision ID	Question	Current status
UX-001	What exact primary action does the floating button perform?	Open
UX-002	What gesture invokes it?	Open
UX-003	Does the action require Accessibility, overlay permission, or both?	Depends on UX-001
UX-004	Should the button be draggable or have persistent placement?	Open
UX-005	Is there a user-visible Diagnostics screen in MVP?	Open
UX-006	Is manual diagnostics export in scope?	Proposed out of scope
UX-007	Which visual theme/brand palette is approved?	Open
UX-008	Is haptic feedback needed?	Open
UX-009	What is the first-run default desired state?	Proposed disabled
UX-010	What minimum supported Android version and text/accessibility targets must be tested?	Open
UX-011	Should the enable control be a switch or action button?	Open
UX-012	Which diagnostic details may be shown to the end user?	Open


Owner response: Anuj Pawar (@anujp125)
Reviewed by: Anuj Pawar (@anujp125)
Review date: 2026-10-29
Approved copy/actions: 56dp circle, 70-80% idle opacity, 100% touch opacity, snap-to-edge drag with DataStore persistence
13. Change Rules
Any change to the primary action, required permissions, first-run flow, persisted settings, or recovery interaction must be reviewed against MVP_SCOPE.md, SRS.md, STATE_AND_LIFECYCLE.md, PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md, and CHANGE_CONTROL.md.

A UI mockup or generated implementation is not approval to add a feature. Update the decision log and acceptance criteria before the changed behavior is treated as part of the MVP.