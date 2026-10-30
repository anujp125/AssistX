# AssistX — Product Decisions Checklist

**Document ID:** ASSISTX-DECISION-CHECKLIST-001  
**Last Updated:** 2026-10-30  
**Status:** Approved by Product Owner (@anujp125)  
**Governing Standard:** [`ASSISTX_AGENT_EXECUTION_PLAYBOOK.md`](../development/ASSISTX_AGENT_EXECUTION_PLAYBOOK.md)

---

## 1. Core Architectural & Product Decisions (Approved)

| ID | Topic | Decision | Status | Rationale |
|:---|:---|:---|:---:|:---|
| **D-001** | App Identity | Name: **AssistX** . Package: `com.nudge.assist` (debug suffix `.debug`) | **Approved** | Clean naming, unique package identity. |
| **D-002** | Target Architecture | **Single-module native Android** (`:app`), native Kotlin, Jetpack Compose | **Approved** | Zero unnecessary multi-module overhead for MVP. |
| **D-003** | Core Action 1 | **Double-tap to lock screen** via `GLOBAL_ACTION_LOCK_SCREEN` | **Approved** | Instant hardware power button replacement (<100ms). |
| **D-004** | Core Action 2 | **Long-press (500ms) to return Home** via `GLOBAL_ACTION_HOME` | **Approved** | Standard accessibility assistive touch gesture. |
| **D-005** | Drag & Interaction | **Draggable with decelerating snap-to-edge** animation and DataStore persistence | **Approved** | Keeps overlay out of the user's primary workspace. |
| **D-006** | Idle Appearance | **Idle opacity fade to 40%** after 3 seconds of inactivity | **Approved** | Prevents visual obstruction of content underneath. |
| **D-007** | Target Hardware | **Realme Narzo 30 4G** (`RMX2156`, realme UI 3.0 / Android 12, Build `F.19`) | **Approved** | Primary daily driver test device. |
| **D-008** | SDK Versioning | `minSdk = 28` (Android 9 Pie), `targetSdk = 34` (Android 14), `compileSdk = 35` | **Approved** | Compatible with modern APIs while supporting Android 9+. |
| **D-009** | Build Toolchain | **Gradle 8.11.1** with Android Studio JBR 21 OpenJDK | **Approved** | Stable, reproducible build environment. |
| **D-010** | UI Stack | **Jetpack Compose with Material 3** | **Approved** | Declarative, reactive UI for Studio dashboard. |
| **D-011** | Overlay Capability | WindowManager `TYPE_APPLICATION_OVERLAY` | **Approved** | Official Android standard for assistive overlay views. |
| **D-012** | Accessibility Scope | Least-privileged `AssistAccessibilityService` (no window content capture, zero event interception) | **Approved** | Privacy-first; zero user content snooping. |
| **D-013** | Process Survival | **Foreground Service** with low-priority silent notification | **Approved** | Protects process from OEM low-memory killers. |
| **D-014** | Preferences Store | **Jetpack DataStore (Preferences)** | **Approved** | Thread-safe, coroutine-based persistence for button coordinates. |
| **D-015** | Diagnostics Store | **Local Room SQLite Database** (`assistx_diagnostics.db`) | **Approved** | Structured querying, zero network transmission. |
| **D-016** | Diagnostic Limits | **Strict 500-event FIFO retention pruning** (or < 2 MB) | **Approved** | Bounded local storage guarantee. |
| **D-017** | State Model | **Multidimensional immutable snapshot** (`AppStateSnapshot`) | **Approved** | Completely separates user intent from platform reality. |
| **D-018** | Recovery Bounds | **Max 3 retries**, exponential backoff (1s, 2s, 5s), 60s cooldown reset | **Approved** | Prevents battery-draining infinite recovery loops. |
| **D-019** | User Disablement | Disabling via Studio **immediately suppresses and cancels all recovery** | **Approved** | Strict user intent precedence. |
| **D-020** | Licensing | **GNU General Public License v3.0 (GPLv3)** | **Approved** | Open-source copyleft protection. |
| **D-021** | Payment Safety Mode | **One-tap pause invoking `disableSelf()` and detaching overlay** with double-approval modal | **Approved** | Protects UPI applications (BHIM, GPay, PhonePe) from accessibility blocks. |
| **D-022** | Action-Specific Gating | Centralized `AssistAction` capability mapping & `ActionDispatcher` with **zero-replay guarantee** | **Approved** | Non-accessibility actions run unblocked; gated actions trigger Metro dialog. |
| **D-023** | Metro UI Language | **0dp sharp geometry, 2dp borders, Selawik typography, WCAG AA compliance** | **Approved** | Pure Windows Phone Metro visual style, eliminated decorative AI dots/badges. |
| **D-024** | Action Popout Array | **Configurable orientation (H/V), adaptive expansion, 46/54/62dp sizes, optional capsule** | **Approved** | Full customization in Settings accordion. |
| **D-025** | Screen-Off Lifecycle | **Detach overlay on screen-off, reattach on screen-wake** | **Approved** | Conserves battery and avoids stale window rendering while locked. |
| **D-026** | Display Rotation | **Coordinate clamping and mirror-to-nearest-edge on rotation** | **Approved** | Prevents off-screen floating button bugs across portrait and landscape. |

---

## 2. Strictly Scoped Core Feature Set

Per owner decisions on 2026-10-30, the core scope is strictly verified across:
1. **Screen Off** (`GLOBAL_ACTION_LOCK_SCREEN` — Implemented & Verified)
2. **Home** (`GLOBAL_ACTION_HOME` — Implemented & Verified)
3. **Screenshot** (`GLOBAL_ACTION_TAKE_SCREENSHOT` — Implemented & Verified)
4. **Volume** (`AudioManager.adjustStreamVolume` with `FLAG_SHOW_UI` — Implemented & Verified)
5. **Payment Safety Mode** (`AccessibilityService.disableSelf()` — Implemented & Verified)

| Feature Sprint | Capability | Approved Platform Design | Notes / Constraints |
|:---|:---|:---|:---|
| **F-01: Baseline Audit** | Baseline validation & regression suite | Complete baseline verification | **Passed** (49 unit tests, hardware verified). |
| **F-02: Screenshot Action** | Capture screen | `AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT` | Native system screenshot (API 28+). No media projection dialogs or storage risks. **Passed**. |
| **F-03: Volume Control** | Adjust volume | `AudioManager.adjustStreamVolume(..., FLAG_SHOW_UI)` | Opens native system volume slider without custom floating bars. **Passed**. |
| **F-04: Hardware Regression** | Full verification on target phone | Core feature test run on Realme Narzo 30 4G | Real-device validation of the final release candidate build. **Passed**. |
| **F-05: UPI Safety & Metro UI** | Payment Safety & Metro UI Polish | `disableSelf()` modal & 0dp Metro design system | Verified on real hardware; 63 unit tests passing. **Passed**. |
