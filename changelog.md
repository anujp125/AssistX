# Changelog

All notable changes to the AssistX project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - 2026-10-29

### Added
- Complete architecture and product specification suite for AssistX v0.1.
- Initial decision log entries approving native Android Kotlin implementation, single-app architecture, and local-first diagnostics.
- Definition of functional requirements (FR-001 through FR-020) and non-functional requirements in SRS.
- Definition of Done, coding standards, change control, and agent workflow specifications.
- Failure injection plan and test strategy for target device (Realme Narzo 30 4G / Android 12).
- Approved core actions: Double-tap to lock screen (`GLOBAL_ACTION_LOCK_SCREEN`) and Long-press to go Home/Back (`GLOBAL_ACTION_HOME` / `GLOBAL_ACTION_BACK`).
- Approved UI stack (Jetpack Compose), persistence (Jetpack DataStore), diagnostics (Room SQLite DB), and minSdk (API 28).
- GPLv3 licensing for open-source distribution.

## [0.1.0-dev] - 2026-10-29

### Planned / In Progress
- Sprint S1: Project bootstrap (Gradle wrapper, version catalog, Jetpack Compose setup, initial test suite).
- Sprint S2: State model implementation (DesiredState, ObservedState, coordinator).
- Sprint S3: Floating overlay vertical slice with WindowManager, snap-to-edge dragging, and position persistence.
- Sprint S4: Runtime coordination, Foreground Service with low-priority notification, and bounded recovery engine.
- Sprint S5: Accessibility Service integration for double-tap lock screen and long-press navigation.
- Sprint S6: Studio Activity dashboard in Jetpack Compose and Room-based diagnostics viewer.
- Sprint S7: Real-device soak testing and failure injection on Realme Narzo 30 4G.
- Sprint S8: Release candidate packaging for personal APK distribution.

