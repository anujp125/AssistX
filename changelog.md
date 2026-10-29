# Changelog

All notable changes to the AssistX project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
