# Nudge — UPI Security and Accessibility Design Review

**Document ID:** NUDGE-SEC-UPI-001  
**Version:** 1.0  
**Status:** VERIFIED ON DEVICE (Approved & Validated)  
**Research date:** 2026-10-30  
**Target test context:** Realme Narzo 30 4G, Android 12 / realme UI 3.0 (Build RMX2156_11_F.19).

> **Core recommendation:** Do not try to hide AssistX from BHIM or bypass a payment app's security checks. Reduce the Accessibility service's actual capabilities, make it optional where possible, and add a user-controlled **Pause for payments** mode that removes the floating overlay and disables the Accessibility service before the user opens a UPI app. The user manually re-enables the service in Android Settings afterward.

---

## 1. Executive Summary

AssistX uses Android `AccessibilityService` for:
- double-tap → lock screen;
- long-press → Home;
- action popout → screenshot, power dialog.

Android exposes device-wide actions such as Back, Home, Recents, Lock screen, and Screenshot through `AccessibilityService.performGlobalAction()`. An enabled third-party service can control interactions across apps. Financial and UPI apps (such as BHIM, Google Pay, PhonePe) may treat an active third-party Accessibility service or floating overlay as an App Access Risk signal.

### Adopted Architecture

1. Minimal declared service capabilities (`canRetrieveWindowContent="false"`, `canPerformGestures="false"`, zero event subscriptions).
2. Never declare `isAccessibilityTool=true` (complying strictly with Google Play policies).
3. User-controlled **Pause for payments** mode:
   - Removes visible floating overlay immediately from `WindowManager`;
   - Stops the floating runtime/foreground service;
   - Calls `AccessibilityService.disableSelf()` cleanly from within the service;
   - Remains paused until the user manually re-enables Accessibility in Android Settings and taps Resume.
4. Zero automated re-enablement or hidden evasion hacks.

---

## 2. Review & Hardware Verification Outcome

- [x] Accessibility service configuration audited (UPI-00).
- [x] Zero window-content or text retrieval paths confirmed (UPI-00).
- [x] Overlay runtime removed independently of Accessibility (UPI-00).
- [x] Owner approved payment-safe mode.
- [x] `disableSelf()` lifecycle implemented safely (UPI-01).
- [x] Payment-safe pause state machine & Studio UI implemented (UPI-02).
- [x] Target-device verification completed (UPI-03):
  - Verified on Realme Narzo 30 4G (`RMX2156_11_F.19`, Android 12).
  - Tapping "Pause for payments" removes overlay and sets Accessibility to Off.
  - Financial/UPI apps launch and complete transactions with zero warnings or security blocks.
  - Manual re-enablement and one-click resumption verified.

**Review outcome:** VERIFIED ON DEVICE  
**Reviewer:** Anuj Pawar (@anujp125)  
**Date:** 2026-10-30

