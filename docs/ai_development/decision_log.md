# AssistX — Decision Log
Document ID: ASSISTX-DECISIONS-001
Version: 0.1
Status: Approved register — decisions finalized
Last updated: 2026-10-29
Owner: Anuj Pawar (@anujp125)
Purpose: This is the central register for product-owner decisions and cross-cutting development choices. All foundational product and architectural decisions for v0.1 have been confirmed and approved.

1. How to Use This Log
Decision statuses
- Approved — the product owner explicitly accepted the choice, or the decision is directly confirmed in the conversation and recorded here.
- Superseded — a newer approved decision replaces this entry; retain the old entry for history.
- Rejected — an option was considered and explicitly declined.

Rules for agents
1. Read this log before implementing a decision-sensitive task.
2. Adhere to approved decisions; do not arbitrarily alter approved architecture or parameters.
3. If a new unforeseen technical dilemma arises, record it as a proposed decision before implementing.

2. Confirmed & Approved Product Decisions

| ID | Decision | Status | Chosen Option / Details | Affected Documents |
|---|---|---|---|---|
| **D-001** | Single-app delivery | Approved | AssistX is delivered as one installable Android app containing both the floating assistant and Studio settings interface. | PRODUCT_BRIEF.md, SRS.md, SYSTEM_ARCHITECTURE.md |
| **D-002** | Reliability-first MVP | Approved | Prioritize floating-button uptime, lifecycle predictability, and bounded diagnostics before feature expansion. | PRODUCT_BRIEF.md, SRS.md, MVP_SCOPE.md |
| **D-003** | Local-first architecture | Approved | No backend, remote servers, or cloud dependencies for MVP v0.1; purely local Android execution. | SRS.md, SYSTEM_ARCHITECTURE.md |
| **D-004** | Official Product Name | Approved | **AssistX** is the official application name; package ID is `com.nudge.assist`. | All documents, app resources, build config |
| **D-005** | Native Kotlin implementation | Approved | Implemented strictly in native Kotlin targeting modern Android SDKs. | SYSTEM_ARCHITECTURE.md, build files |
| **D-006** | Primary floating-button actions | Approved | **Double-tap**: Turn Screen Off / Lock Screen (`GLOBAL_ACTION_LOCK_SCREEN`). **Long-press**: Home / Back navigation (`GLOBAL_ACTION_HOME` / `BACK`). | SRS.md, UI_UX_SPECIFICATION.md, ACCEPTANCE_TESTS.md |
| **D-007** | Gesture timing and sensitivity | Approved | Double-tap window: **300 ms**; Long-press threshold: **500 ms**. | UI_UX_SPECIFICATION.md, STATE_AND_LIFECYCLE.md |
| **D-008** | Draggability in v0.1 | Approved | Floating button is draggable across the screen and smoothly snaps to the nearest vertical edge upon release. | UI_UX_SPECIFICATION.md, MVP_SCOPE.md |
| **D-009** | Button position persistence | Approved | Last snapped coordinates persist in Jetpack DataStore and restore automatically on launch. | MVP_SCOPE.md, STATE_AND_LIFECYCLE.md |
| **D-010** | Minimum supported Android SDK | Approved | **minSdk 28 (Android 9.0 Pie)** — provides native support for `GLOBAL_ACTION_LOCK_SCREEN` without DeviceAdmin. | build.gradle, SRS.md, PERMISSIONS.md |
| **D-011** | Target and Compile SDK | Approved | **targetSdk 34 / compileSdk 34 (Android 14)** — modern platform standard. | build.gradle, setup.md |
| **D-012** | UI Toolkit selection | Approved | **Jetpack Compose with Material 3** for all Studio interfaces, state binding, and dialogs. | SYSTEM_ARCHITECTURE.md, CODING_STANDARDS.md |
| **D-013** | Application ID & package root | Approved | **`com.nudge.assist`** | build.gradle, AndroidManifest.xml |
| **D-014** | Distribution method | Approved | **Personal APK direct install / GitHub Releases** (bypasses Google Play accessibility review overhead for v0.1). | README.md, SRS.md |
| **D-015** | Post-reboot auto-start | Approved | **Manual reopening required in v0.1** (no `RECEIVE_BOOT_COMPLETED` receiver in MVP to simplify lifecycle). | PERMISSIONS.md, MVP_SCOPE.md |
| **D-016** | Foreground service strategy | Approved | **Foreground Service with persistent minimal low-priority notification** ("AssistX is active") to prevent OEM process killing. | PERMISSIONS.md, SYSTEM_ARCHITECTURE.md |
| **D-017** | First-run default state | Approved | **Disabled by default** until the user completes onboarding and explicitly turns the toggle ON. | STATE_AND_LIFECYCLE.md, SRS.md |
| **D-018** | Recovery retry & backoff policy | Approved | **Max 3 retries with exponential backoff (1s, 2s, 5s)**. Retry counter resets after 60s of stable execution. | FAILURE_AND_RECOVERY_MATRIX.md |
| **D-019** | Diagnostics storage & bounds | Approved | **Room SQLite Database** storing structured event history, bounded to **max 500 records or 2 MB** with auto-pruning. | DATA_AND_DIAGNOSTICS.md |
| **D-020** | Soak test duration | Approved | **8-hour soak test** on target device to verify stability against realme UI 3.0 deep sleep. | TEST_STRATEGY.md, REAL_DEVICE_TEST.md |
| **D-021** | Measurable quality thresholds | Approved | Battery drain < 1% over 8h idle; Steady-state RAM < 40 MB RSS; Touch-to-action latency < 150 ms. | SRS.md, TEST_STRATEGY.md |
| **D-022** | Hardware validation scope | Approved | **Single-device baseline**: Realme Narzo 30 4G (RMX2156, realme UI 3.0, Android 12). Broader matrix in v1.0. | REAL_DEVICE_TEST.md |
| **D-023** | Open Source License | Approved | **GNU General Public License v3.0 (GPLv3)**. Full license text in `license.md`. | license.md, README.md |
| **D-024** | Git workflow and branching | Approved | Primary branch `main`. Development via atomic task branches with review before merge. | setup.md, AI_AGENT_WORKFLOW.md |

3. Technical Verification Records

| ID | Verified Fact | Evidence / Official Reference | Status |
|---|---|---|---|
| **T-001** | Screen lock without Device Admin requires API 28+ | Android AccessibilityService documentation confirms `GLOBAL_ACTION_LOCK_SCREEN` was added in API level 28 (Android 9.0). | Verified |
| **T-002** | Accessibility requirement for lock & navigation | Android security model prevents standard background apps from turning off screen or navigating back/home without Accessibility. | Verified |
| **T-003** | Foreground Service types on Android 14 (API 34) | On Android 14+, foreground services must declare a type (e.g. `specialUse` or `dataSync`). `specialUse` with clear explanation will be used. | Verified |
| **T-004** | DataStore vs SharedPreferences | Jetpack DataStore Preferences provides non-blocking coroutines and safe transactional disk I/O, preventing ANRs. | Verified |
| **T-005** | Realme UI background killer behavior | realme UI 3.0 terminates background apps without foreground service tokens during deep sleep. Persistent notification mitigates this. | Verified |

4. Approval
- Product owner: Anuj Pawar (@anujp125)
- Log status: Approved
- Review date: 2026-10-29
- Notes: All open decisions D-001 through D-024 and technical verifications T-001 through T-007 are finalized and approved. Ready for development.
