# AssistX — Real Device Test Matrix (Realme Narzo 30 4G)
Document ID: ASSISTX-TEST-DEVICE-001  
Version: 0.1  
Status: Approved  
Last updated: 2026-10-29  
Owner: Anuj Pawar (@anujp125)  

---

## 1. Overview
This document specifies the real-device test execution matrix for physical hardware validation of AssistX v0.1. Hardware validation is required before any release candidate is considered ready for everyday personal use.

---

## 2. Target Hardware Baseline
- **Device Model**: Realme Narzo 30 4G (`RMX2156`)
- **Processor / Chipset**: MediaTek Helio G95 (Octa-core 2.05 GHz)
- **RAM / Storage**: 4 GB LPDDR4X (+ 3 GB DRE / Extended RAM) / 64 GB
- **Android Version**: Android 12 (API level 31)
- **OEM UI Version**: realme UI 3.0
- **Official Build Tag**: `RMX2156_11_F.19`
- **Display**: 6.5" IPS LCD, 90 Hz, 1080 x 2400 (FHD+)

---

## 3. Quantitative Test Pass Criteria
1. **8-Hour Soak Test**: AssistX overlay and foreground service must maintain continuous uptime (> 99.5%) over an 8-hour overnight standby and intermittent usage period without unhandled crash or silent detachment.
2. **Idle Battery Drain**: Battery consumption attributable to AssistX must remain **< 1.0%** over an 8-hour idle period (verified via Android Battery Settings / dumpsys batterystats).
3. **RAM Memory Budget**: Private Dirty RSS memory footprint must remain **< 40 MB** under steady-state operation.
4. **Touch-to-Lock Latency**: Time from double-tap completion to screen-off initiation must measure **< 150 ms**.

---

## 4. Real-Device Execution Matrix

| Test ID | Test Scenario | Subsystem | Target Criteria | Status | Verified Build | Notes / Evidence |
|---|---|---|---|---|---|---|
| **RT-001** | First-time installation & permission grant flow | Permissions | Clear guidance; direct links open Realme permission sub-settings | Ready for Run | v0.1.0-dev | Verify Overlay & Accessibility grant |
| **RT-002** | Master toggle ON / OFF overlay appearance | Overlay UI | Floating button attaches within 200ms; disappears cleanly on OFF | Ready for Run | v0.1.0-dev | WindowManager attachment |
| **RT-003** | Double-tap screen lock action | Accessibility | Double-tap within 300ms immediately locks display | Ready for Run | v0.1.0-dev | Tests `GLOBAL_ACTION_LOCK_SCREEN` |
| **RT-004** | Long-press Home / Back navigation | Accessibility | 500ms hold navigates to home/back; haptic feedback triggers | Ready for Run | v0.1.0-dev | Tests `GLOBAL_ACTION_HOME` / `BACK` |
| **RT-005** | Dragging & snap-to-edge animation | Interaction | Smooth 90Hz drag tracking; snaps to nearest left/right edge on touch release | Ready for Run | v0.1.0-dev | Tests physics/interpolation |
| **RT-006** | Screen coordinate persistence | Persistence | Coordinates persist after restarting Studio or killing activity | Ready for Run | v0.1.0-dev | DataStore read/write check |
| **RT-007** | Idle opacity fading | UI / Visual | Fades to 70%–80% after 3s; returns to 100% immediately on touch | Ready for Run | v0.1.0-dev | Alpha transition check |
| **RT-008** | Foreground service notification presence | Lifecycle | Low-priority silent notification appears; no noisy sound/vibration | Ready for Run | v0.1.0-dev | Tests NotificationManager |
| **RT-009** | 8-Hour Soak Test (Continuous Standby) | Reliability | Button remains visible and operational after 8 hours sleep | Ready for Run | v0.1.0-dev | Soak test requirement |
| **RT-010** | Camera / Heavy App Launch Preemption | Memory / LMK | Button recovers cleanly after playing high-RAM game / opening camera | Ready for Run | v0.1.0-dev | Tests Helio G95 memory pressure |
| **RT-011** | Revoke Overlay Permission from Settings | Error Handling | App detects revocation; removes button; Studio shows `BLOCKED` | Ready for Run | v0.1.0-dev | Tests permission watcher |
| **RT-012** | Revoke Accessibility from Settings | Error Handling | Button stays attached; double-tap prompts toast; Studio shows `DEGRADED` | Ready for Run | v0.1.0-dev | Tests service disconnect handling |
| **RT-013** | Orientation Change (Portrait <-> Landscape) | UI / Window | Button adjusts coordinates and clamps within new display bounds | Ready for Run | v0.1.0-dev | Rotation test |
| **RT-014** | Phone Reboot Verification | Lifecycle | App does NOT auto-start (v0.1 spec); opening Studio restores state cleanly | Ready for Run | v0.1.0-dev | Reboot test |
| **RT-015** | Studio Diagnostics History Inspection | Diagnostics | Room DB entries visible inside Studio list view; max 500 entries enforced | Ready for Run | v0.1.0-dev | Diagnostics viewer test |
| **RT-016** | Bounded Recovery backoff timing | Recovery | Unexpected detach initiates 3 retries (1s, 2s, 5s); stops at max retries | Ready for Run | v0.1.0-dev | Tests recovery backoff |
| **RT-017** | Idle Battery Drain Measurement | Performance | Dumpsys batterystats confirms <1.0% drain over 8h | Ready for Run | v0.1.0-dev | Power consumption check |
| **RT-018** | Steady-state RAM measurement | Performance | `dumpsys meminfo com.nudge.assist` reports Pss Total < 40 MB | Ready for Run | v0.1.0-dev | Memory leak check |
| **RT-019** | Rapid repeated toggling (Stress Test) | Concurrency | Rapid ON/OFF in Studio produces exactly 1 overlay; no duplicates | Ready for Run | v0.1.0-dev | Duplicate prevention check |
| **RT-020** | User Force-Stop Respect | OS Control | If user Force-Stops from Android Settings, app remains stopped | Ready for Run | v0.1.0-dev | Android guidelines compliance |

---

## 5. Execution Test Run Log Template
For recording official physical runs during Sprint S7:
```markdown
### Real-Device Run Log: RD-RUN-[001]
- **Date/Time**: YYYY-MM-DD HH:MM
- **Device**: Realme Narzo 30 4G (RMX2156, realme UI 3.0, F.19)
- **Git Commit**: [commit-hash]
- **Tester**: Anuj Pawar
- **Battery Start / End**: 100% -> 99.3% (Over 8 hours)
- **Observed RSS RAM**: 32.4 MB
- **Total Tests Executed**: 20
- **Results**: PASS: [ ] | FAIL: [ ] | BLOCKED: [ ]
- **Observations / Anomalies**:
  - [Note any unexpected OEM prompt, animation lag, or battery management interaction]
```

