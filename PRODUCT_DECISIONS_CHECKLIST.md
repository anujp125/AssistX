# Nudge / AssistX — Product Decisions Checklist

**Document ID:** NUDGE-DECISION-CHECKLIST-001  
**Last Updated:** 2026-10-30  
**Status:** Approved by Product Owner (@anujp125)  
**Governing Standard:** `NUDGE_AGENT_EXECUTION_PLAYBOOK.md`

---

## 1. Core Architectural & Product Decisions (Approved)

| ID | Topic | Decision | Status | Rationale |
|:---|:---|:---|:---:|:---|
| **D-001** | App Identity | Name: **AssistX** (code-name: Nudge). Package: `com.nudge.assist` (debug suffix `.debug`) | **Approved** | Clean naming, unique package identity. |
| **D-002** | Target Architecture | **Single-module native Android** (`:app`), native Kotlin, Jetpack Compose | **Approved** | Zero unnecessary multi-module overhead for MVP. |
| **D-003** | Primary Action 1 | **Double-tap to lock screen** via `GLOBAL_ACTION_LOCK_SCREEN` | **Approved** | Instant hardware power button replacement (<100ms). |
| **D-004** | Primary Action 2 | **Long-press (500ms) to return Home** via `GLOBAL_ACTION_HOME` | **Approved** | Standard accessibility assistive touch gesture. |
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

---

## 2. Feature Roadmap Extension Decisions (Sprints F-02 to F-06)

| Feature Sprint | Capability | Approved Platform Design | Notes / Constraints |
|:---|:---|:---|:---|
| **F-02: Back Action** | Dispatch Back key | `AccessibilityService.GLOBAL_ACTION_BACK` | Native API 28+ call; requires interaction mapping approval (e.g. single tap or menu). |
| **F-03: Recent Apps** | Open Recents UI | `AccessibilityService.GLOBAL_ACTION_RECENTS` | Opens native system overview. No hidden background process inspection. |
| **F-04: Screenshot** | Capture screen | `AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT` | Native system screenshot (API 28+). No media projection dialogs or storage risks. |
| **F-05: Volume Control** | Adjust volume | `AudioManager.adjustStreamVolume(..., FLAG_SHOW_UI)` | Opens native system volume slider without custom floating bars. |
| **F-06: Action Menu** | Multi-action access | Expandable circular / grid Action Panel on tap | Preserves double-tap/long-press shortcuts while exposing secondary actions. |
