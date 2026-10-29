# AssistX — Bug Report Template

Use this template to log defects, unexpected runtime behavior, service termination, or performance regressions encountered during automated, emulator, or real-device testing.

---

## 1. Issue Summary
- **Title**: `[SUBSYSTEM] Short descriptive summary of the failure` (e.g., `[OVERLAY] Button fails to re-attach after screen unlock on realme UI 3.0`)
- **Reported by**: [Your Name / Agent ID]
- **Date / Time**: YYYY-MM-DD HH:MM (Timezone)
- **Severity**: Critical (P0) / Major (P1) / Normal (P2) / Minor (P3)
- **Subsystem**: Overlay / Accessibility / Lifecycle / Studio UI / Diagnostics / Recovery / Persistence

---

## 2. Test Environment
- **Device & Model**: (e.g., Realme Narzo 30 4G / RMX2156)
- **Android OS & API Level**: (e.g., Android 12, API 31)
- **OEM Firmware / Build ID**: (e.g., realme UI 3.0, RMX2156_11_F.19)
- **AssistX Version / Git Commit**: (e.g., v0.1.0-dev, commit abc1234)
- **Build Variant**: Debug / Release
- **Target SDK**: 34
- **Battery Optimization Status**: Default / Unrestricted / Optimized

---

## 3. Subsystem States Prior to Reproduction
- **Desired Enabled State**: Enabled / Disabled
- **Overlay Permission (`SYSTEM_ALERT_WINDOW`)**: Granted / Revoked
- **Overlay Window State**: Attached / Detached / Unknown
- **Accessibility Service State**: Enabled & Connected / Enabled & Disconnected / Disabled
- **Foreground Service Running**: Yes (Notification visible) / No
- **Recent Recovery Attempts**: 0 / 1 / 2 / 3 (Exhausted)

---

## 4. Steps to Reproduce
1. Launch AssistX Studio.
2. Enable AssistX from the main toggle switch.
3. Observe floating button appearing at the right screen edge.
4. [Describe trigger, e.g., turn off screen, wait 30 minutes, launch camera, reboot, etc.]
5. [Describe observed trigger action, e.g., double-tap button]

---

## 5. Expected vs. Actual Behavior
- **Expected Result**: 
  [Describe what should have occurred according to SRS requirements]
- **Actual Result**: 
  [Describe what actually occurred, e.g., button disappeared, double-tap was ignored, crash occurred]

---

## 6. Failure Category & Root Cause Assessment
- [ ] **Confirmed Root Cause**: [Provide concrete technical rationale and evidence]
- [ ] **Suspected / Unverified**: [Initial hypothesis, e.g., OS Low-Memory Killer (LMK), OEM battery manager restriction, race condition]
- [ ] **Platform / OEM Constraint**: [Android OS restriction that cannot be circumvented]

---

## 7. Diagnostics & Evidence
### A. In-App Diagnostic Events (from Studio Room DB)
```text
[Timestamp] [Event Type] [Subsystem] [Result] [Reason Code / Details]
2026-10-29 14:02:11 OVERLAY_DETACHED OVERLAY FAILED reason=window_token_invalid
2026-10-29 14:02:12 RECOVERY_TRIGGERED RECOVERY IN_PROGRESS attempt=1 backoff_ms=1000
```

### B. Android Logcat Extract
```text
[Paste relevant logcat lines filtered by app package com.nudge.assist or tag AssistX]
```

### C. Screenshots / Screen Recordings
- [Attach screenshot showing Studio status or missing overlay]

---

## 8. Workaround & Next Actions
- **Temporary Workaround**: (e.g., toggle master switch OFF and ON in Studio)
- **Proposed Fix / Investigation Task**: (e.g., SPRINT task reference `NUDGE-025` or `CHG-002`)

