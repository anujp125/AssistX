# Changelog

All notable changes to the AssistX project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0-rc2] - 2026-10-30

### Added
- **Action-Specific Accessibility Permission Handling**: Centralized `AssistAction` capability mapping and `ActionDispatcher` gating. Dispatches independent actions (`Volume Control`, `Drag & Drop`, `Popout Toggle`, `Edge Snapping`, `Studio Navigation`, `Payment Safety Mode`) immediately without prompts, while gating system-level actions (`Screen Lock`, `Home`, `Screenshot`, `Power Dialog`).
- **Metro Accessibility Required Dialog**: WindowManager `TYPE_APPLICATION_OVERLAY` modal with pure black `#121214` surface, 2dp amber outline, 0dp sharp geometry, and zero-replay invariant (does not auto-replay blocked actions after settings change).
- **Payment Safety Mode**: One-tap payment protection that invokes `disableSelf()` and detaches floating overlay, preventing accessibility blocking in financial applications (BHIM, Google Pay, PhonePe). Includes a double-approval confirmation modal to prevent accidental triggers.
- **Action Popout Menu**: Fully configurable floating action array supporting horizontal and vertical orientations, adaptive or directional expansion, customizable spacing, sizes (46dp/54dp/62dp), and optional background capsule styling.
- **Metro / Modern UI Design Language**: Comprehensive interface overhaul using Selawik typography, 0dp rectangular tiles, strict semantic coloring, and verified WCAG AA contrast (pure white text on accent, > 4.5:1 ratio).
- **Expanded Unit Test Suite**: Increased automated test coverage from 49 to 63 unit tests across 13 test suites covering state, recovery, action gating, and no-replay guarantees.

### Changed
- **Overview & Diagnostics Alignment**: Replaced decorative AI-style status dots with clean monoline icons and stacked vertical text hierarchy, completely eliminating awkward word wraps (e.g. `accessibilit\ny disabled`).
- **Settings Default Expansion**: Enforced fresh collapse on every navigation to the Settings tab across all accordion groups.
- **Display Lifecycle Invariants**: Floating overlay detaches on screen-off and reattaches on screen-wake; button coordinates clamp and mirror to nearest edge upon display rotation.
- **Floating Button Visual Glyph**: Replaced confusing power-style icon with a clean, unobtrusive concentric locator glyph.

### Fixed
- Fixed text wrapping and clipping in status tiles on varying display densities.
- Fixed overlay touch target conflicts between dragging, tapping, double-tapping, and popout expansion.
- Corrected release build signing template to prevent hardcoded credentials.

## [0.1.0-rc1] - 2026-10-29

### Added
- **Core Architecture & Foundation (S1)**: Single `:app` module Android project with Gradle 8.11.1, Kotlin 2.0.21, Jetpack Compose, Material 3, and OpenJDK 21 toolchain.
- **State Model & Coordinator (S2)**: Multidimensional immutable `AppStateSnapshot` separating user intent (`DesiredState`) from platform observations (`RuntimeState`, `OverlayPermissionState`, `OverlayState`, `AccessibilityState`). Deterministic `StatusEvaluator` providing truthful UI status.
- **Floating Overlay Slice (S3)**: Custom 56dp `FloatingButtonView` with decelerating snap-to-edge animation, double-tap, long-press, touch slop filtering, and 3-second idle opacity fade. Single-instance WindowManager management (`OverlayManager`) and persistent drag coordinates via Jetpack DataStore (`OverlayPreferences`).
- **Runtime Coordination & Bounded Recovery (S4)**: `RuntimeController` managing foreground service process survival and `RecoveryPolicy` enforcing bounded retries (max 3 attempts, 1s/2s/5s exponential backoff, 60s cooldown reset, strict user-disable suppression).
- **Primary Actions & Accessibility (S5)**: Non-invasive `AssistAccessibilityService` executing `GLOBAL_ACTION_LOCK_SCREEN` on double-tap and `GLOBAL_ACTION_HOME` on long-press, with real-time settings and binding state observation.
- **Studio Dashboard & Diagnostics (S6)**: Material 3 Compose dashboard (`StudioScreen`) with master activation switch, subsystem readiness cards (overlay permissions, accessibility settings), gestures guide, and local-only Room SQLite diagnostics recorder (`DiagnosticRecorder`) with bounded 500-event FIFO retention.
- **Automated Test Suite (S7)**: 49 automated unit tests across 11 test suites passing deterministically with 100% success rate.
- **Release Candidate (S8)**: Packaged `app-release-unsigned.apk` (42.5 MB) and `app-debug.apk` (58.1 MB) optimized for personal device deployment.
