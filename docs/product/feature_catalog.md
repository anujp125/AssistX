# AssistX — Feature Catalog
Document ID: ASSISTX-FEAT-001  
Version: 0.1  
Status: Approved  
Last updated: 2026-10-29  
Owner: Anuj Pawar (@anujp125)  

---

## 1. Overview
This document catalogs all functional and technical features planned or considered for AssistX, linking each capability to its functional requirement (FR), architectural module, release milestone, and implementation status.

---

## 2. Milestone Summary
- **v0.1 MVP (Core Reliability & Diagnostics)**: Minimal assistive floating button, double-tap lock screen, long-press home/back, snap-to-edge drag, foreground service protection, Jetpack Compose Studio dashboard, bounded Room diagnostics.
- **v0.2 (Interaction & Customization Expansion)**: Single-tap configurable action menu, customizable gesture timing, custom icon selection, additional quick actions (screenshot, notification shade, quick settings).
- **v1.0 (Public Release & Device Broadening)**: Multi-device certification, automated post-reboot restore options, exportable diagnostic bundles, dark/light theme sync, localization.
- **Future Backlog (Deferred / Under Evaluation)**: Cloud backup, companion tile, advanced accessibility automation.

---

## 3. Feature Catalog Matrix

| Feature ID | Feature Name | Category | Milestone | SRS Mapping | Description | Status |
|---|---|---|---|---|---|---|
| **FEAT-001** | Floating Action Button (FAB) Overlay | Overlay UI | v0.1 | FR-004, FR-005, FR-006 | Translucent circular on-screen overlay window rendered via Android WindowManager (`TYPE_APPLICATION_OVERLAY`). | Approved (In Scope) |
| **FEAT-002** | Snap-to-Edge Dragging | Overlay Interaction | v0.1 | FR-008, FR-017 | Interactive touch dragging allowing user to move the button across the screen, automatically snapping smoothly to the nearest screen edge on release. | Approved (In Scope) |
| **FEAT-003** | Button Position Persistence | Persistence | v0.1 | FR-017 | Saves the last snapped screen coordinates in Jetpack DataStore and restores them across app restarts and process recreation. | Approved (In Scope) |
| **FEAT-004** | Double-Tap to Lock Screen | Core Action | v0.1 | FR-008 | User double-taps the button within 300ms to immediately lock the screen via Accessibility Service (`GLOBAL_ACTION_LOCK_SCREEN`). | Approved (In Scope) |
| **FEAT-005** | Long-Press Home / Back Action | Core Action | v0.1 | FR-008 | User holds the button for 500ms to trigger Home (`GLOBAL_ACTION_HOME`) or Back navigation. | Approved (In Scope) |
| **FEAT-006** | Visual Idle Fading | Overlay UI | v0.1 | FR-018 | Button remains at 70%–80% opacity during idle states and transitions smoothly to 100% opacity upon touch interaction. | Approved (In Scope) |
| **FEAT-007** | Studio Dashboard | UI / Management | v0.1 | FR-001, FR-002, FR-003 | Jetpack Compose settings and management screen displaying true observed runtime health, permission status, and master enable/disable switch. | Approved (In Scope) |
| **FEAT-008** | Runtime State Separation | Core Architecture | v0.1 | FR-003, FR-007 | Separates user intent (DesiredState) from observed reality (ObservedState), preventing false "Active" claims when permissions are revoked or services killed. | Approved (In Scope) |
| **FEAT-009** | Foreground Service Protection | Process Lifecycle | v0.1 | FR-006, NFR-REL-001 | Runs an ongoing foreground service with a low-priority notification to protect the overlay from aggressive OEM background termination (Realme UI / ColorOS). | Approved (In Scope) |
| **FEAT-010** | Permission Guidance Flow | Permissions | v0.1 | FR-004, FR-011 | Detects missing overlay or accessibility permissions and provides clear, direct intents into Android system settings. | Approved (In Scope) |
| **FEAT-011** | Bounded Recovery Engine | Reliability | v0.1 | FR-015, FR-016 | Automatic recovery with max 3 retries and exponential backoff (1s, 2s, 5s), suppressed if the user manually disabled the service. Resets after 60s stable run. | Approved (In Scope) |
| **FEAT-012** | Local Diagnostics Event Log | Diagnostics | v0.1 | FR-019, FR-020 | Bounded Room SQLite database recording lifecycle, permission, error, and recovery events (max 500 records or 2 MB) with auto-pruning. | Approved (In Scope) |
| **FEAT-013** | In-App Diagnostics Viewer | UI / Diagnostics | v0.1 | FR-020 | Studio interface to inspect recent diagnostic events and error codes without needing USB ADB. | Approved (In Scope) |
| **FEAT-014** | Quick Settings / Notification Action | Core Action | v0.2 | FR-EXT-001 | Trigger `GLOBAL_ACTION_QUICK_SETTINGS` or `GLOBAL_ACTION_NOTIFICATIONS` from a configurable gesture. | Planned (v0.2) |
| **FEAT-015** | Screenshot Capture Action | Core Action | v0.2 | FR-EXT-002 | Trigger `GLOBAL_ACTION_TAKE_SCREENSHOT` via accessibility. | Planned (v0.2) |
| **FEAT-016** | Radial / Pop-up Action Menu | UI / Interaction | v0.2 | FR-EXT-003 | Expanding radial menu on single-tap offering quick access to multiple actions. | Planned (v0.2) |
| **FEAT-017** | Custom Timing & Sensitivity | Settings | v0.2 | FR-EXT-004 | Settings sliders to adjust double-tap window (200ms–500ms) and long-press delay (300ms–800ms). | Planned (v0.2) |
| **FEAT-018** | Custom Button Icon & Theming | UI / Appearance | v0.2 | FR-EXT-005 | User choice of icon style, tint color, and custom idle opacity levels (30%–100%). | Planned (v0.2) |
| **FEAT-019** | Auto-Start on Boot | Lifecycle | v1.0 | FR-EXT-006 | Optional `RECEIVE_BOOT_COMPLETED` receiver to restore overlay upon phone reboot if enabled. | Planned (v1.0) |
| **FEAT-020** | Diagnostic Export & Log Share | Diagnostics | v1.0 | FR-EXT-007 | Export diagnostic events into encrypted JSON or plain text file for bug reporting. | Planned (v1.0) |
| **FEAT-021** | Multi-Device Certification | Quality / Compatibility | v1.0 | NFR-EXT-001 | Broad testing across Samsung OneUI, Xiaomi MIUI/HyperOS, and stock Pixel Android. | Planned (v1.0) |
| **FEAT-022** | Cloud Sync & Remote Backup | Network | Future | N/A | Cloud sync of preferences across devices. (Explicitly excluded from MVP). | Excluded |

---

## 4. Feature Dependencies and Boundaries
- **Accessibility Dependency**: Features `FEAT-004` (Lock Screen) and `FEAT-005` (Home/Back) depend on the Accessibility Service (`AssistAccessibilityService`). If accessibility is disabled, the overlay button remains visible but taps indicate an accessibility-blocked state.
- **Overlay Permission Dependency**: All overlay interaction features (`FEAT-001` through `FEAT-006`) require `SYSTEM_ALERT_WINDOW` permission.
- **Process Isolation**: Studio (`FEAT-007`) runs in the same process but manages state independently through shared DataStore and Room instances, allowing the overlay to persist when Studio Activity finishes.

