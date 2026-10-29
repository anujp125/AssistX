# AssistX — User Stories
Document ID: ASSISTX-US-001  
Version: 0.1  
Status: Approved  
Last updated: 2026-10-29  
Owner: Anuj Pawar (@anujp125)  

---

## 1. Overview
This document defines the core user stories and acceptance scenarios for AssistX v0.1, structured around user personas, goals, functional requirement linkages, and observable acceptance criteria.

---

## 2. Personas
- **Primary Persona: The Accessibility & Power User (Alex)**
  - Operates a modern Android phone (Realme Narzo 30 4G with realme UI 3.0 / Android 12).
  - Relies on an assistive floating button to lock the screen and navigate home/back without wearing out physical power/volume buttons or reaching across a large screen.
  - Frustrated by past assistive touch apps whose floating button silently disappears or whose accessibility services disconnect without explanation.
  - Needs reliable, continuous operation, clear status feedback, and zero battery drain when idle.

---

## 3. User Story Matrix

### Epic 1: Onboarding, Setup & State Control
#### US-001: Initial Setup Guidance
- **As a** first-time user opening AssistX,
- **I want to** see an onboarding screen explaining what permissions AssistX needs and why,
- **So that** I understand the app's requirements before granting special system privileges.
- **Maps to**: FR-001
- **Acceptance Criteria**:
  - Studio displays an onboarding overview explaining Overlay and Accessibility needs.
  - No permission is claimed as already granted until verified by the system.
  - Clear buttons lead directly to the corresponding Android Settings sub-pages.

#### US-002: Master Enable/Disable Control
- **As a** user in the Studio app,
- **I want to** explicitly toggle the assistant on or off,
- **So that** I have full control over when the floating button appears on my screen.
- **Maps to**: FR-002, FR-003
- **Acceptance Criteria**:
  - Toggling ON initiates overlay attachment and starts the foreground service if permissions exist.
  - Toggling OFF immediately removes the overlay and stops the foreground service.
  - The saved desired state persists across app closures.

#### US-003: Truthful Status Indication
- **As a** user inspecting Studio,
- **I want to** see the actual operational health of the overlay and accessibility service,
- **So that** I am not misled into believing the app is active when a permission was revoked in the background.
- **Maps to**: FR-003, FR-007, FR-011
- **Acceptance Criteria**:
  - If desired state is ON but Overlay permission is missing, Studio displays `BLOCKED: Missing Overlay Permission`.
  - If overlay is attached but Accessibility is disabled, Studio displays `DEGRADED: Accessibility Inactive`.
  - Only when all required subsystems are running is the status shown as `ACTIVE`.

---

### Epic 2: Floating Button Interaction
#### US-004: Screen Lock via Double-Tap
- **As a** user interacting with the floating button,
- **I want to** double-tap the button in rapid succession,
- **So that** my phone screen locks immediately without pressing the physical power button.
- **Maps to**: FR-008
- **Acceptance Criteria**:
  - Double-tap within 300ms triggers `GLOBAL_ACTION_LOCK_SCREEN`.
  - The phone screen locks without perceptible delay (<150ms).
  - Single taps do not inadvertently lock the screen.

#### US-005: Home / Back Navigation via Long-Press
- **As a** user operating the phone one-handed,
- **I want to** long-press the floating button for 500ms,
- **So that** I can navigate back or go to the home screen effortlessly.
- **Maps to**: FR-008
- **Acceptance Criteria**:
  - Holding the button for 500ms triggers the navigation action (`GLOBAL_ACTION_HOME` / `GLOBAL_ACTION_BACK`).
  - Haptic feedback or a subtle scale animation confirms the long-press threshold was reached.

#### US-006: Smooth Snap-to-Edge Dragging
- **As a** user whose floating button is blocking content on screen,
- **I want to** drag the button to a new location,
- **So that** it gets out of my way while remaining accessible.
- **Maps to**: FR-008, FR-017
- **Acceptance Criteria**:
  - Dragging follows the finger smoothly across screen bounds.
  - Upon release, the button animates and snaps to the nearest vertical screen edge (left or right).
  - Dragging does not trigger the double-tap or long-press action.

#### US-007: Button Position Persistence
- **As a** user who customized the button position,
- **I want to** have the button remain at that exact edge height after closing other apps or restarting AssistX,
- **So that** I don't have to reposition it every time.
- **Maps to**: FR-017
- **Acceptance Criteria**:
  - Snapped coordinates are written to DataStore upon drag release.
  - On overlay recreation, the button attaches at the persisted coordinates.

#### US-008: Idle Translucency Fading
- **As a** user viewing media or reading text,
- **I want the** floating button to fade to a subtle transparency when idle,
- **So that** it does not distract from my screen content.
- **Maps to**: FR-018
- **Acceptance Criteria**:
  - Button fades to 70%–80% opacity after 3 seconds of inactivity.
  - Touching the button immediately restores 100% opacity.

---

### Epic 3: Process Reliability & Failure Handling
#### US-009: Background Survival on Aggressive OEMs
- **As a** user with an aggressive background battery manager (Realme UI),
- **I want** AssistX to maintain a persistent foreground service with a low-priority notification,
- **So that** the operating system does not kill my floating button when memory gets constrained.
- **Maps to**: FR-006, NFR-REL-001
- **Acceptance Criteria**:
  - AssistX runs a foreground service while enabled.
  - A compact, silent notification ("AssistX is active") is present in the notification shade.
  - The notification includes a quick-action toggle to open Studio or disable the overlay.

#### US-010: Bounded Automatic Recovery
- **As a** user whose overlay was temporarily disrupted by a full-screen app or OS transition,
- **I want** AssistX to automatically attempt to re-attach the overlay up to 3 times with backoff,
- **So that** minor transient glitches fix themselves without manual intervention.
- **Maps to**: FR-015, FR-016
- **Acceptance Criteria**:
  - If overlay is detached unexpectedly, recovery tries reattaching at 1s, 2s, and 5s intervals.
  - If all 3 retries fail, recovery halts, records a failure event, and transitions to a truthful `FAILED` state.
  - If the user explicitly disabled AssistX, no automatic recovery attempts are made.

---

### Epic 4: Diagnostics & Troubleshooting
#### US-011: Inspecting Local Diagnostics
- **As a** user troubleshooting why an action failed,
- **I want to** view a recent log of events inside the Studio interface,
- **So that** I can see exact error codes and event timestamps.
- **Maps to**: FR-019, FR-020
- **Acceptance Criteria**:
  - Studio provides a "Diagnostics History" screen listing recent events (Room DB).
  - Each entry displays timestamp, subsystem (OVERLAY, SERVICE, ACCESSIBILITY), outcome, and reason code.
  - The store automatically limits itself to 500 records or 2 MB to protect disk space.
  - A "Clear Logs" button allows purging local event history.

