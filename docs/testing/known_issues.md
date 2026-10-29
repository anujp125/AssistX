# AssistX — Known Issues & Platform Limitations Register
Document ID: ASSISTX-KNOWN-001  
Version: 0.1  
Status: Approved  
Last updated: 2026-10-29  
Owner: Anuj Pawar (@anujp125)  

---

## 1. Overview
This register tracks known defects, platform-specific anomalies, OEM background-execution behaviors, and architectural limitations observed during AssistX testing. Each issue contains its symptom, affected environments, confirmed or suspected root causes, status, and recommended user/developer workarounds.

---

## 2. Issue Register Matrix

| Issue ID | Subsystem | Severity | Target Environment | Status | Description Summary | Workaround / Mitigation |
|---|---|---|---|---|---|---|
| **ISSUE-001** | Process Lifecycle | High | Realme Narzo 30 4G (realme UI 3.0 / Android 12) | Under Investigation | **Aggressive Background Termination without Foreground Service**: Prior apps lost overlay button when device entered deep sleep or during intense memory load (Helio G95, 4GB RAM). | Mitigated in AssistX by running an ongoing Foreground Service with persistent low-priority notification (`FEAT-009`). |
| **ISSUE-002** | Accessibility | Medium | Android 12+ (API 31+) | Known Platform Behavior | **Accessibility Service "Enabled but Disconnected"**: Under certain OS power-save events, Android reports the Accessibility setting as ON, but the system unbinds the service without calling `onServiceConnected()`. | Handled by state coordinator: distinguishes `ENABLED` from `CONNECTED`; prompts user in Studio to toggle Accessibility if disconnected. |
| **ISSUE-003** | Overlay Window | Medium | Fullscreen Games / Secure Windows | Expected Platform Behavior | **Overlay Occlusion on Secure / Flag-Secure Surfaces**: When banking apps or screens with `FLAG_SECURE` are in the foreground, Android overlay buttons may be hidden or untappable. | Expected Android security constraint (`TYPE_APPLICATION_OVERLAY` restriction). Truthfully reported as `OCCLUDED` in diagnostics; no futile retry. |
| **ISSUE-004** | Lifecycle | Low | All Android 9+ devices | By Design (v0.1) | **No Post-Reboot Auto-Start in v0.1**: When device reboots, AssistX button does not appear automatically until user opens Studio once. | Documented MVP scope decision (`D-015`). Planned for v1.0 via `RECEIVE_BOOT_COMPLETED`. |
| **ISSUE-005** | UI / Display | Low | Landscape orientation transitions | Open | **Button Snap Coordinate Jitter across Orientation Changes**: Changing between portrait and landscape can cause the button to be placed slightly outside screen bounds before snap recalculation. | Snapping logic enforces boundary clamp to current display metrics on configuration change. |

---

## 3. Detailed Issue Reports

### ISSUE-001: Realme UI 3.0 Background Freezing
- **Symptom**: Floating button disappears after 15–30 minutes of screen-off time.
- **Affected Devices**: Realme Narzo 30 4G (RMX2156, Build RMX2156_11_F.19).
- **Technical Analysis**: Realme UI employs a proprietary battery engine ("Quick Freeze" / "Sleep Standby Optimization") that terminates background processes lacking active foreground service tokens.
- **Resolution in AssistX**:
  1. Enforce `FOREGROUND_SERVICE` with notification channel configured for `IMPORTANCE_LOW` (silent, non-intrusive).
  2. Studio onboarding instructs users on Realme-specific settings: disable "Auto-freeze" for AssistX in Battery Settings.

### ISSUE-002: Accessibility Unbind on Idle
- **Symptom**: Double-tap on the button produces no action; Studio shows Accessibility setting ON, but the action does not execute.
- **Affected Devices**: Android 11 & Android 12 builds across multiple OEMs.
- **Technical Analysis**: Android OS occasionally unbinds accessibility services during extreme low-memory spikes. Calling `performGlobalAction()` fails silently because the service instance is null.
- **Resolution in AssistX**:
  1. `AssistAccessibilityService` maintains an observable singleton StateFlow indicating true active binding (`isBound`).
  2. If user taps while `isBound == false`, a quick toast is displayed and Studio guides the user to toggle the service off/on in system settings.

