AssistX — Android Permissions and Platform Constraints
Document ID: NUDGE-PLATFORM-001
Version: 0.1
Status: Draft — technical and owner review required
Last reviewed against official documentation: 2026-10-29
Related documents: SRS.md, MVP_SCOPE.md, SYSTEM_ARCHITECTURE.md, STATE_AND_LIFECYCLE.md
Mandatory instruction for AI agents: Do not add permissions, receivers, services, privileged APIs, or background-start workarounds without demonstrating the approved requirement they satisfy. Verify API/target-SDK-specific rules against the official Android documentation linked below before implementation. Device-specific behavior must be tested on the target Realme phone. Never claim that Android permits a behavior simply because another app appears to do it.

1. Purpose
AssistX requires a small number of Android capabilities, but several are controlled by special system settings rather than ordinary runtime permission dialogs. Background execution and service-start rules also vary by OS version and target SDK.

This document establishes:
- what permissions/capabilities may be needed;
- how the app should request and verify them;
- which behaviors must remain under user or OS control;
- when a foreground service might be considered;
- constraints on Accessibility integration, reboot recovery, and overlays;
- what must be verified before implementation and release.
The app's minimum Android version, target SDK, primary action, and distribution method are not finalized. Therefore, this is a requirements and verification guide, not a final manifest. Do not copy a speculative permission list directly into AndroidManifest.xml.
2. Current Product Context
The first target device, as reported by the product owner, is a Realme Narzo 30 4G (RMX2156), running Android 12 / realme UI 3.0, build RMX2156_11_F.19.

The product is a single-installable app. The primary action is not yet approved; the current candidate is a double-tap-to-screen-off action.

That choice affects whether Accessibility is needed. Resolve the primary action and verify its implementation before committing to the Accessibility architecture or adding its manifest declaration.
3. Permission and Capability Inventory
Capability	Requirement status	Why it may be needed	User/system control
Draw over other apps (SYSTEM_ALERT_WINDOW)	Required for a floating overlay using Android application-overlay windows	Allows the floating button to appear above other apps	Special app access; user grants/revokes it in system settings
Accessibility Service	Conditional	Needed only if the approved action requires an Accessibility API, such as a supported global action	User explicitly enables/disables the service in Android Accessibility settings; lifecycle is managed by the system
Foreground service permission/type	Conditional, architecture-dependent	Only if the approved runtime design genuinely needs an eligible foreground service	Subject to OS, target-SDK, service-type, notification, and background-start rules
Notifications (POST_NOTIFICATIONS)	Conditional on target/device behavior and product UI	May be needed to display normal notifications on Android 13+; not inherently needed just to request an overlay	Runtime permission on Android 13+ for non-exempt notifications
Boot receiver (RECEIVE_BOOT_COMPLETED)	Not approved by default	Could support a reviewed post-reboot recovery strategy if supported and needed	Must respect boot and foreground-service restrictions; cannot guarantee immediate restart
Device Administrator / device-owner APIs	Out of scope by default	Sometimes proposed for screen lock or device management	Do not add; requires separate justification and substantial UX/policy implications
Exact alarms, accessibility shortcuts, battery-optimization exemption, all-files access, notification listener, usage access	Not required by current scope	No current MVP requirement justifies these capabilities	Do not request unless a future approved requirement demonstrates necessity


The table intentionally distinguishes a manifest permission from a special system capability and from a service that must be enabled by the user.
4. Overlay Permission
4.1 Purpose
AssistX's floating button needs permission to draw over other apps if implemented using Android application-overlay windows.

Android documentation describes Settings.canDrawOverlays(context) as the check for whether an app can draw over other apps. The Settings.ACTION_MANAGE_OVERLAY_PERMISSION intent opens the relevant permission-management surface where supported.

Official reference:
- [Android Settings API reference — canDrawOverlays and `ACTION_MANAGE_OVERLAY_PERMISSION](https://developer.android.com/reference/android/provider/Settings)
4.2 Required implementation behavior
Agents implementing the overlay must:
1. Declare SYSTEM_ALERT_WINDOW only if the chosen overlay approach requires it.
2. Check current permission using the supported platform API before attempting a dependent operation.
3. If not granted, explain why AssistX needs the capability and provide the appropriate system-settings shortcut.
4. Re-check permission after the user returns from settings. Opening settings is not evidence that permission was granted.
5. Handle a permission that becomes unavailable while AssistX is enabled.
6. Reflect the result in the state model; do not display Active solely because DesiredState is ENABLED.
7. Handle exceptions/failures from adding or removing an overlay.
8. Ensure repeated starts do not create duplicate windows.
9. Keep the overlay minimal, touchable, and limited to the approved interaction.
10. Avoid full-screen or unnecessarily large transparent overlays.
4.3 Permission UX
The UI must not imply that AssistX can grant this permission itself. It can guide the user to Android settings and then re-check the current condition.

Where possible, handle the case where the settings activity cannot be resolved. The Android reference notes that not every settings action is guaranteed to have a matching activity on every environment.
4.4 Android 12 overlay touch constraint
Android 12 introduced restrictions on touch events passing through certain untrusted windows, including some overlay cases using FLAG_NOT_TOUCHABLE. AssistX must not build its behavior around invisible/full-screen overlays that depend on touches passing through them. The floating button should receive the intended user's touch directly, and any special touch behavior must be verified against the current platform documentation and target device.

Reference:
- Behavior changes: all apps on Android 12 — untrusted touch events
5. Accessibility Service — Conditional and User-Controlled
5.1 Gate before implementation
Do not implement an Accessibility Service until all of the following are true:
- the primary v0.1 action is approved;
- the action's capability requirements have been verified;
- the Accessibility approach is necessary and narrower than a viable alternative;
- the owner has accepted the required permission UX and behavior;
- distribution-policy implications have been considered.
An Accessibility Service is not a generic mechanism for keeping an app process alive. It must exist to perform the approved assistive function.
5.2 System-managed lifecycle
Android documents that the Accessibility Service lifecycle is managed by the system and that starting the service is triggered by the user explicitly turning it on in device settings. After the system binds the service, onServiceConnected() is called. The service can stop when the user turns it off in settings or the service disables itself.

Reference:
- Create an accessibility service — Android Developers
- AccessibilityService API reference
5.3 Required implementation principles
If the approved feature requires Accessibility:
- Declare the service with the required Accessibility Service intent and BIND_ACCESSIBILITY_SERVICE protection as documented by Android.
- Use a minimal configuration. Do not request access to window content or broad event types unless the feature truly needs them.
- Do not inspect unrelated app content.
- Do not use Accessibility to perform actions outside the user-approved scope.
- Do not try to silently enable or prevent the user from disabling the service.
- Distinguish the setting being enabled from the service currently being connected.
- Treat onServiceConnected() and applicable disconnect/lifecycle signals as observations, not permanent guarantees.
- Do not assume an enabled setting means the service connection will always remain available.
- Keep overlay state independent from Accessibility state.
- Provide clear guidance when the user must enable the service in settings.
5.4 Screen-lock action candidate
The current candidate is a double-tap-to-screen-off action. Android's AccessibilityService API defines GLOBAL_ACTION_LOCK_SCREEN, added in API level 28, and provides performGlobalAction() to request system global actions. The service API also exposes system actions that may be currently available.

Reference:
- AccessibilityService API reference — global actions
This is a technical candidate, not an approved product decision. If selected:
- verify that the target API and device support the required action;
- check action availability where the API provides that information;
- check and handle the operation's reported result;
- test on the Realme device;
- do not silently substitute Device Administrator or another sensitive mechanism if the action fails;
- document that a successful API return does not prove every device's user-visible lock behavior without actual testing.
5.5 If distributed through Google Play
Google Play has additional Accessibility API policy requirements. The use of the API must be documented in the Play listing; additional declarations or prominent disclosures may apply depending on the app's status and use. Accessibility must not be used to change user settings without permission, bypass platform controls, or deceive users.

Official policy references:
- Google Play Developer Program Policy — Accessibility API
- Use of the AccessibilityService API — Play Console Help
The distribution method is unresolved. If Play Store distribution is chosen, review the current policy before implementing and again before submission; do not assume a personal-use APK decision satisfies Play policy.
6. Foreground Services — Do Not Use as a Generic Keep-Alive Mechanism
6.1 Decision rule
A foreground service may be considered only after the architecture explains which ongoing, user-visible work requires it and why a less complex solution is inadequate.

Do not add a foreground service just to claim that AssistX will never be stopped. Foreground services remain subject to Android lifecycle, policy, start restrictions, service-type rules, and user stopping behavior.

References:
- Launch a foreground service
- Restrictions on starting a foreground service from the background
- Changes to foreground services
6.2 Background-start restrictions
Relevant version-dependent rules include:
- Apps targeting Android 12 (API 31) or higher generally cannot start a foreground service while running in the background except for defined exceptions.
- Apps targeting Android 14 (API 34) or higher must follow foreground-service type and associated permission requirements that apply to their declared type.
- Android 15 (API 35) adds further restrictions involving BOOT_COMPLETED-started foreground services and narrows the overlay-permission exemption for starting a foreground service from the background: when relying on that exemption, a visible overlay window is required.
- Current Android documentation also describes further foreground-service requirements for Android 16 (API 36), including service-type declarations and type-specific permissions.
These are not exhaustive rules. The exact requirements depend on device OS version, targetSdkVersion, service type, and the intended operation. Agents must re-check current documentation when deciding the SDK and service strategy.
6.3 Foreground service decision checklist
Before adding a foreground service, the agent must document:
- What user-visible ongoing work is being performed?
- Why must it continue while Studio is not visible?
- Which documented foreground service type is correct for that work?
- Is that service type permitted by the target SDK and OS version?
- When and from where is it started?
- Can it be started from the current app state under background-start rules?
- What persistent notification and user controls are required?
- What happens if the user stops the service from system UI?
- What is the shutdown path when the user disables AssistX?
- How will the service be tested under target-device conditions?
- What is the alternative if the OS refuses the start?
If the intended service does not fit an allowed, documented service type, do not invent a type or misrepresent the work to keep it running. Reconsider the architecture and raise a decision request.
6.4 Notifications
For Android 13 (API 33) and higher, POST_NOTIFICATIONS is a runtime permission for non-exempt notifications. Android's documentation explains that this permission is not required merely to launch a foreground service, but foreground services still must provide a notification; denied notification permission affects how the user sees it.

References:
- Notification runtime permission
- Behavior changes: all apps on Android 13
If AssistX uses a foreground service, notification design, permission handling, user controls, and status messaging must be specified and tested. Do not add POST_NOTIFICATIONS before the chosen notification behavior is known.
7. Reboot and BOOT_COMPLETED
7.1 Default position
Automatic startup after reboot is not promised by this document. A boot receiver is not approved by default.

Before considering one:
- decide whether automatic post-reboot behavior is necessary for the user-approved MVP;
- verify current Android limits for the selected target SDK;
- assess whether starting the required component from the receiver is permitted;
- determine whether the user must open the app or grant/restore a permission;
- test the behavior on the target Realme device;
- document failure behavior and user recovery.
7.2 Android 15 and newer restrictions
Official Android documentation specifies that apps targeting Android 15 or higher cannot launch certain foreground service types from a BOOT_COMPLETED receiver. This includes specific types listed in the official behavior-change documentation. The restrictions do not mean every foreground-service type is prohibited at boot, so agents must verify the exact type and target configuration instead of generalizing.

References:
- Behavior changes: apps targeting Android 15 or higher
- Changes to foreground services
7.3 Required behavior
- Never claim reboot restoration is guaranteed.
- Do not add a boot receiver solely to create a keep-alive loop.
- Do not treat reboot and ordinary process recreation as identical.
- Do not infer that Android will launch AssistX immediately after reboot.
- Re-check user intent and relevant permissions when the app is able to run.
- Provide settings/setup guidance if the user must manually restore functionality.
8. Force-Stop, Task Manager, and User-Requested Stopping
AssistX must respect explicit user and system stopping actions.
- Do not promise automatic restart after a user force-stop.
- Do not use another component or repeated alarms/receivers to evade user force-stop semantics.
- On Android 13 and higher, users can stop apps with running foreground services through the system Task Manager workflow. If AssistX uses a foreground service, test and handle this user-initiated stop behavior.
- Record a stop event only if the app has an opportunity to observe it. Do not invent a diagnostic event for termination that prevented the app from running.
- Upon the next legitimate launch or callback, re-check actual state rather than restoring a stale ACTIVE value.
Reference:
- Behavior changes: all apps on Android 13 — Task Manager
9. Background Activity Launches and Settings Navigation
Android restricts apps from bringing Activities to the foreground while the app is in the background. AssistX must not force-open Studio or other screens unexpectedly as a recovery technique.

For user-directed recovery:
- launch the settings screen from a clear user interaction where possible;
- handle the case where the intended settings Activity is unavailable;
- re-check permission/service state after the user returns;
- do not create a loop that repeatedly opens settings;
- prefer a concise in-app status and an explicit user action to continue.
Reference:
- Activity security and background activity launch restrictions
10. Device-Specific Battery and Background Management
The initial device uses Realme UI 3.0. Manufacturer power management may affect background execution, but AssistX must not assume it is the cause of every observed failure.

Rules:
- First collect evidence about overlay attachment, service connection, permission state, and runtime failures.
- Test the exact target device and build.
- If a device-specific setting must be changed by the user, present clear, optional instructions only after the need is supported by test evidence.
- Do not direct the user to grant broad, unrelated permissions.
- Do not claim that ignoring battery optimization or enabling an autostart setting guarantees persistence.
- Do not try to manipulate undocumented vendor settings automatically.
- Record the exact device model, OS/build, reproduction steps, and observed result for relevant bugs.
Open item: A device-specific troubleshooting guide should be written only after behavior is reproduced or officially documented, not from assumption.
11. Screen-Off and Device Administrator Alternative
The current candidate action is screen-off following a gesture. The Accessibility global lock-screen action is one possible route on supported API levels; device-admin privileges are a materially different mechanism and are not approved.

Rules:
- Do not add Device Administrator, device-owner, or enterprise-management features to the MVP without explicit approval.
- Do not use admin enrollment simply as a workaround for a failed Accessibility implementation.
- Verify API availability, operation result, and behavior on the real device.
- If the chosen action cannot be implemented with an acceptable permission flow, present the alternatives to the owner rather than silently changing the requirement.
12. Permission-Minimization Rules
For every proposed permission or privileged component, the task proposal must state:
1. the approved requirement it satisfies;
2. why the capability is needed;
3. the least-privileged viable alternative considered;
4. how the user grants/revokes it;
5. expected failures when absent;
6. platform/target-SDK restrictions;
7. tests and documentation updates;
8. security, privacy, and battery implications.
Do not add any of the following without a new approved requirement:
- INTERNET;
- location, microphone, camera, contacts, SMS, call-log, or phone-state permissions;
- notification listener access;
- usage access;
- exact alarm access;
- all-files access;
- Device Administrator/device-owner capabilities;
- battery-optimization exemption requests;
- boot receivers;
- Accessibility Service declarations unrelated to the chosen action;
- broad event/window content access in Accessibility configuration.
The list is a guardrail, not a statement that every listed item is inherently impermissible. It means none is justified by the current MVP scope.
13. Manifest Review Checklist
Before merging any manifest change, the agent must check:
- [ ] Every new permission maps to an approved requirement.
- [ ] Every component is necessary and has the correct exported setting.
- [ ] Accessibility declarations, if any, are user-controlled and narrowly configured.
- [ ] Foreground-service declarations/types/permissions, if any, match a valid use case and the selected SDK rules.
- [ ] Boot receiver, notification, or background-start behavior has a documented need and platform review.
- [ ] No unused sensitive permission remains.
- [ ] User-facing disclosures and settings links exist for special capabilities.
- [ ] Current OS and targetSdkVersion implications have been checked.
- [ ] Relevant automated or manual tests are identified.
- [ ] PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md, SYSTEM_ARCHITECTURE.md, and PRIVACY_AND_PERMISSIONS.md are updated where needed.
14. Verification Matrix
Before release, validate at minimum:
Scenario	Required evidence
Overlay permission granted	Button can appear; actual state reports correctly
Overlay permission denied/revoked	No false active state; guidance is actionable
Return from overlay settings	Permission is re-checked, not assumed
Accessibility disabled (if used)	App indicates disabled and guides user
Accessibility enabled but disconnected (if detectable)	Distinct from disabled; overlay health unaffected
Foreground service start from visible UI (if used)	Works with correct service type and notification behavior
Attempted background service start	Follows current platform restrictions; errors handled
User stops FGS from system UI (if used)	No restart loop or misleading status
Device reboot	Actual result documented, including restrictions and required user steps
Force-stop	No unsupported restart promise or bypass
Android version/target SDK boundary	Platform-dependent behavior verified or declared out of scope
Realme background management	Behavior verified with device/build details; no unsupported root-cause claim


15. Required Pre-Implementation Decisions
ID	Decision	Status
PC-01	Minimum Android version	Approved: API 28 (Android 9.0 Pie)
PC-02	Target SDK/release target policy	Technical recommendation and owner approval required
PC-03	Primary button action and whether Accessibility is needed	Approved: Double-tap lock screen, long-press home/back (Requires Accessibility)
PC-04	UI toolkit	Approved: Jetpack Compose with Material 3
PC-05	App distribution method	Approved: Personal APK direct install / GitHub Releases
PC-06	Whether a foreground service is justified	Must be decided from approved runtime design and current rules
PC-07	Whether post-reboot activation is required for v0.1	Approved: Manual reopening required in v0.1 (no boot receiver)
PC-08	Whether notification-based runtime controls are needed	Depends on service architecture
PC-09	Device coverage beyond RMX2156	Approved: Single-device baseline on Realme Narzo 30 4G (RMX2156)


16. Official References
These official pages are the baseline references consulted while drafting this document. Android documentation and distribution policies may change; re-check them before implementation and release.
1. Android Settings API — overlay permission check and management intent:
   https://developer.android.com/reference/android/provider/Settings
2. Create an Accessibility Service:
   https://developer.android.com/guide/topics/ui/accessibility/service
3. AccessibilityService API, including lifecycle and global actions:
   https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
4. Launch a foreground service:
   https://developer.android.com/develop/background-work/services/fgs/launch
5. Restrictions on starting foreground services from the background:
   https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
6. Changes to foreground service requirements:
   https://developer.android.com/develop/background-work/services/fgs/changes
7. Android 12 behavior changes affecting touch events through overlays:
   https://developer.android.com/about/versions/12/behavior-changes-all
8. Android 13 behavior changes affecting Task Manager and foreground-service stopping:
   https://developer.android.com/about/versions/13/behavior-changes-all
9. Android 13 notification runtime permission:
   https://developer.android.com/develop/ui/compose/notifications/notification-permission
10. Android 15 behavior changes, including boot and overlay-related foreground-service restrictions:
    https://developer.android.com/about/versions/15/behavior-changes-15
11. Activity security and background activity launch restrictions:
    https://developer.android.com/guide/components/activities/secure-bal
12. Google Play Developer Program Policy — Accessibility API:
    https://support.google.com/googleplay/android-developer/answer/18258653
13. Use of the AccessibilityService API — Play Console Help:
    https://support.google.com/googleplay/android-developer/answer/10964491
17. Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Status: Draft / Approved
- Approved minimum Android version: API 28 (Android 9.0 Pie)
- Approved target SDK policy: targetSdk 34 / compileSdk 34 (Android 14)
- Approved primary action and capability: Double-tap lock screen (GLOBAL_ACTION_LOCK_SCREEN); Long-press home/back (GLOBAL_ACTION_HOME/BACK) via Accessibility
- Foreground service decision: Approved (Ongoing Foreground Service with low-priority notification)
- Reboot behavior requirement: Manual reopening required in v0.1
- Distribution method: Personal APK direct install / GitHub Releases
- Review date: 2026-10-29
- Notes: Platform constraints and permissions approved.
This document is a controlled draft. A permission or platform behavior may be implemented only when its requirement is approved, its current rules are verified, and the corresponding error/recovery and testing behavior is documented.