# AssistX — Architecture Decision Records (ADRs)
Document ID: ASSISTX-ADR-001
Version: 0.1
Status: Approved Architecture Baseline
Last updated: 2026-10-29
Related documents: SYSTEM_ARCHITECTURE.md, MODULE_AND_PACKAGE_STRUCTURE.md, STATE_AND_LIFECYCLE.md, PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md, DECISION_LOG.md
Owner: Anuj Pawar (@anujp125)

---

## 1. Decision Status Summary

| ADR | Title | Status | Summary |
|---|---|---|---|
| **ADR-001** | One installable application | Accepted | Floating assistant and Studio/settings UI are components of one app package `com.nudge.assist`. |
| **ADR-002** | Single Gradle app module for v0.1 | Accepted | Start with one app module (`:app`) to minimize build complexity while maintaining clean package separation. |
| **ADR-003** | Local-first architecture (no backend) | Accepted | Core v0.1 behavior operates completely locally without remote servers, accounts, or telemetry. |
| **ADR-004** | Separation of Desired State vs Observed Health | Accepted | State coordinator persists user intent (DataStore) while evaluating real-time subsystem readiness independently. |
| **ADR-005** | Independent Overlay and Accessibility Lifecycles | Accepted | Distinct component health and permission states are tracked independently; failure in one does not crash the other. |
| **ADR-006** | Native Android with Kotlin | Accepted | Native Kotlin using platform APIs for WindowManager overlays and AccessibilityService. |
| **ADR-007** | UI Toolkit: Jetpack Compose | Accepted | Jetpack Compose with Material 3 for all Studio activities, settings screens, and diagnostics lists. |
| **ADR-008** | Preference Storage: Jetpack DataStore | Accepted | Jetpack DataStore (Preferences) for non-blocking, coroutine-safe settings and coordinate persistence. |
| **ADR-009** | Diagnostics Storage: Bounded Room Database | Accepted | Room SQLite DB storing structured event history, bounded to max 500 events or 2 MB with FIFO pruning. |
| **ADR-010** | Foreground Service Protection Strategy | Accepted | Ongoing Foreground Service with silent low-priority notification to protect overlay from OEM termination. |
| **ADR-011** | Accessibility Integration Boundary | Accepted | Accessibility Service (`GLOBAL_ACTION_LOCK_SCREEN`, `HOME`, `BACK`) integrated conditionally for double-tap and long-press. |
| **ADR-012** | Bounded Recovery & Retry Configuration | Accepted | Max 3 recovery attempts with exponential backoff (1s, 2s, 5s), resetting after 60s of stable execution. |
| **ADR-013** | Testing Boundaries: Unit vs Instrumentation vs Device | Accepted | Pure logic gets JVM unit tests; platform interactions get connected instrumentation tests; hardware gets soak tests. |
| **ADR-014** | Manual Dependency Injection | Accepted | Manual constructor injection / service locator pattern to keep MVP lightweight and avoid Hilt/Dagger build overhead. |

---

## 2. Key Architecture Decision Records

### ADR-001: One Installable Application
- **Status**: Accepted
- **Context**: Deciding between two separate APKs (Studio vs Assistant) or a single unified APK.
- **Decision**: Deliver AssistX as a single Android application package containing both logical components.
- **Consequences**: Simplified installation, unified DataStore and Room database access, no inter-process AIDL complexity.

### ADR-006: Native Android with Kotlin
- **Status**: Accepted
- **Context**: Platform choice for assistive touch and low-level system interactions.
- **Decision**: Native Kotlin targeting Android SDKs directly.
- **Consequences**: Full access to WindowManager, AccessibilityService, and Android 12+ APIs without cross-platform abstraction overhead.

### ADR-007: UI Toolkit Selection (Jetpack Compose)
- **Status**: Accepted
- **Context**: Choosing between Jetpack Compose and traditional Android Views/XML.
- **Decision**: Use Jetpack Compose with Material 3 for all UI development.
- **Consequences**: Declarative reactive UI, seamless StateFlow binding, less boilerplate code, modern toolchain.

### ADR-008: Preference Persistence (Jetpack DataStore)
- **Status**: Accepted
- **Context**: Storing desired state, snapped button coordinates, and user preferences.
- **Decision**: Use Jetpack DataStore Preferences instead of SharedPreferences.
- **Consequences**: Async coroutine API, safe from UI-thread blocking / ANRs, atomic disk writes.

### ADR-009: Diagnostics Storage & Retention (Room Database)
- **Status**: Accepted
- **Context**: Logging lifecycle, error, and recovery events for local troubleshooting without USB ADB.
- **Decision**: Use Room Database (SQLite) bounded to 500 entries or 2 MB max size.
- **Consequences**: Structured SQL queries, indexed timestamp retrieval, automated FIFO eviction trigger on write.

### ADR-010: Foreground Service Strategy
- **Status**: Accepted
- **Context**: Background survival on aggressive OEM battery managers (realme UI 3.0 / ColorOS).
- **Decision**: Launch an ongoing Foreground Service with a silent, low-priority persistent notification ("AssistX is active").
- **Consequences**: Prevents Android LMK and Realme sleep optimizations from killing the overlay process; compliant with Android 14 foreground service type rules.

### ADR-011: Accessibility Integration Boundary
- **Status**: Accepted
- **Context**: Approved core actions are Double-tap to lock screen and Long-press for Home/Back.
- **Decision**: Implement `AssistAccessibilityService` using `performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)` and `GLOBAL_ACTION_HOME`/`BACK`.
- **Consequences**: Accessibility is required only when these actions are enabled; if user revokes Accessibility, overlay remains functional for dragging with clear guidance in Studio.

### ADR-012: Bounded Recovery Policy
- **Status**: Accepted
- **Context**: Responding to unexpected overlay detachments or process recreation.
- **Decision**: Cap automatic retries at 3 with exponential backoff (1s, 2s, 5s). Reset budget after 60s of continuous health.
- **Consequences**: Eliminates infinite restart loops and battery drain; transitions to truthful FAILED state when retries exhaust.

---

## 3. Architecture Sign-off
- **Architect / Owner**: Anuj Pawar (@anujp125)
- **Status**: Approved
- **Review Date**: 2026-10-29
- **Notes**: All 14 ADRs are formally accepted.
