# AssistX Modern UI (Metro) Design Style Guide

## 1. Design Philosophy
AssistX follows the Windows Phone **"Metro" (Modern UI)** design language:
- **Content over Chrome**: Minimalist typography, monoline vector iconography, and flat solid colors without gradients, shadows, elevation, blurs, or skeuomorphism.
- **Strict Square Geometry**: Square corners everywhere (`0dp` radius). No rounded cards, pills, or circles except for circular action icons and touch targets.
- **Left-Aligned Grid**: 24dp start margins creating a strong, clean left-aligned visual spine.
- **Lowercase Typography**: Page titles and section pivot headers are strictly lowercase (e.g. `studio`, `diagnostics`, `overview`, `settings`).

---

## 2. Typography
- **Primary Typeface**: **Selawik** (Microsoft's open-source, Segoe UI-compatible font, MIT / SIL OFL licensed), bundled locally in `res/font/selawik_*.ttf`.
- **Offline Guarantee**: Fully offline and self-contained; zero cloud or downloadable font dependencies.

| Role | Font Weight | Size | Usage / Examples |
|---|---|---|---|
| **Page Title** | Light (`selawik_light.ttf`) | 42sp | `studio`, `diagnostics` |
| **Pivot Section Header** | SemiBold / Light | 24sp | `overview` (1.0 opacity), `settings` (0.4 opacity) |
| **Section Header** | SemiBold | 18–20sp | `appearance`, `behavior`, `subsystem telemetry` |
| **Tile Title / Metric** | Bold | 16–20sp | `assistant enabled`, `runtime health` |
| **Body Primary** | Regular | 15sp | Setting titles, diagnostic row titles |
| **Body Secondary** | Regular / Light | 12–13sp | Setting descriptions, sub-telemetry status |
| **Product Brand** | Bold / SemiBold | 11sp (2.5sp space) | `ASSISTX` (small uppercase) |

---

## 3. Color Palette & Theme Engine

### 3.1 Base Neutrals
- **Dark Mode (Default)**: True Black `#000000` base, `#161616` dark tile surfaces, `#2E2E2E` thin borders.
- **Light Mode**: Pure White `#FFFFFF` base, `#F0F0F0` light tile surfaces, `#D0D0D0` borders.

### 3.2 The 20 Windows Phone Metro Accent Colors
| Accent | Hex Code | Accent | Hex Code |
|---|---|---|---|
| **Cobalt** (Default) | `#0050EF` | **Indigo** | `#6A00FF` |
| **Lime** | `#A4C400` | **Violet** | `#AA00FF` |
| **Green** | `#60A917` | **Pink** | `#F472D0` |
| **Emerald** | `#008A00` | **Magenta** | `#D80073` |
| **Teal** | `#00ABA9` | **Crimson** | `#A20025` |
| **Cyan** | `#1BA1E2` | **Red** | `#E51400` |
| **Orange** | `#FA6800` | **Brown** | `#825A2C` |
| **Amber** | `#F0A30A` | **Olive** | `#6D8764` |
| **Yellow** | `#E3C800` | **Steel** | `#647687` |
| **Mauve** | `#76608A` | **Taupe** | `#87794E` |

### 3.3 Flat Status Cues
- **Healthy / Connected / Ready**: `#008A00` (Emerald Green)
- **Attention / Missing / Disconnected**: `#F0A30A` (Amber)
- **Error / Fault**: `#E51400` (Crimson Red)

---

## 4. Components & Layout Specifications

### 4.1 Metro Header (`MetroHeader.kt`)
- Handles edge-to-edge system bar insets (`WindowInsets.statusBars`).
- Features the geometric Metro tile logo mark (`ic_assistx_logo`) tinted with the active user accent.
- Product name `"ASSISTX"` in small uppercase with letter spacing.
- Large lowercase page title (42sp Selawik Light).

### 4.2 Metro Bottom App Bar (`MetroAppBar.kt`)
- Minimalist row of circular outlined icon buttons (36dp circle, 1.5dp stroke, 48dp touch target).
- Lowercase label underneath.
- Active tab highlighted in accent color.
- Edge-to-edge navigation bar padding.

### 4.3 Metro Live Tiles (`MetroTile.kt`)
- 2-column grid layout with 8dp gutters.
- Standard 1x1 Square Tile (`116dp` height) and Wide 2x1 Tile (`124dp` height).
- 0dp radius corners, 2dp solid border.
- Pressed state: fast, purposeful tilt/scale-down animation (0.965f) coupled with haptic tap.
- **Strict WCAG AA Contrast**: On `ACCENT` tiles (e.g. purple `#AA00FF`), foreground typography and icons are strictly pure white (`#FFFFFF`, >5.1:1 contrast ratio). Never render low-contrast green or amber text on accent surfaces.
- **Compact 1x1 Typography Spine**: For 1x1 subsystem tiles (`isCompact = true`), `label` and `statusText` stack vertically in a bottom column. This gives `statusText` the full container width (~150dp), completely preventing broken word wraps or clipping.
- **Zero Decorative Dots**: Repetitive AI-style colored circular dots before status texts are completely eliminated across tiles, telemetry, and badges.

### 4.4 Diagnostics & System Monitoring (`DiagnosticsScreen.kt`)
- **Grouped Subsystem Telemetry**: Subsystem facts (`system alert window`, `floating overlay view`, `accessibility service`, `runtime state engine`, `process memory`) are grouped inside a single flat, cohesive table container separated by subtle 1dp hairline dividers.
- **Right-Aligned Status Labels**: Status cues (`ready`, `attached`, `connected`, `healthy`, `nominal`) are cleanly positioned on the right as bold, readable labels colored by semantic state without circular dots or pills.
- **Professional Event Log**: Flat streaming table with monospace timestamps (`HH:mm:ss.SSS`), bold event type titles, and right-aligned severity labels (`info`, `warning`, `error`), separated by hairline horizontal dividers.

### 4.5 Studio Pivot Layout (`StudioScreen.kt`)
- Merges Dashboard and Settings into a unified Studio screen.
- Features horizontal swipe gesture transition between `overview` and `settings` powered by `HorizontalPager` and `rememberPagerState`.
- Bi-directional synchronization: swiping horizontally updates the pivot headers and triggers tactile `MetroHaptics.select()` feedback; tapping either pivot header smoothly scrolls the pager to the selected page over 200ms.
- Preserves nested vertical scrolling on both pages.

### 4.5 Metro Flat Toggle (`MetroToggle.kt`)
- 46dp x 24dp rectangular frame with 2dp solid outline.
- 14dp x 14dp square slider thumb.
- Solid accent fill when ON; transparent outline when OFF.
- Explicit lowercase `"on"` / `"off"` text label.

### 4.6 Metro Segmented Picker (`MetroSegmentedPicker.kt`)
- 40dp height flat options row with 0dp radius.
- Active segment filled with solid accent and white text.

### 4.7 Floating Assistant Restyle & Customizations (`FloatingButtonView.kt`)
- **Shapes**:
  - `CIRCLE`: Classic Metro circle with outer border, concentric inner ring, and clean solid center core dot.
  - `SQUARE`: Authentic flat Metro square (`0dp` sharp radius) with concentric square indicators and center core dot.
  - `ROUNDED`: Metro squircle with `12dp` corner radius, concentric ring, and center core dot.
- **Design Styles**:
  - `METRO_OUTLINE`: Black base with 2.5dp outer border, 1.5dp inner ring, and centered solid core dot.
  - `SOLID_ACCENT`: Solid accent fill with 2dp white outline, concentric ring, and centered core dot.
  - `MONOLINE_RING`: Pure transparent base with monoline accent ring and centered accent core dot.
- **Color Overrides**:
  - `ACCENT`: Synchronized with app-wide theme accent (Cobalt, Emerald, Crimson, etc.).
  - Standalone hex override options: `pure white` (`#FFFFFF`), `steel grey` (`#647687`), `cobalt` (`#0050EF`), `emerald` (`#107C10`), `crimson` (`#E51400`).
  - High-contrast inversion: when pure white solid fill is selected, border, ring, and center core dot invert to solid black (`#000000`) to maintain maximum readability and WCAG AA contrast.
- **Dual Status Indicator**: Shape outline + inner concentric ring + center core dot (never color alone; eliminates confusion with system power buttons).
- Full integration with `MetroHaptics`.

### 4.8 Popup Action Buttons Array & Background Capsule (`ActionPopoutView.kt`)
- **Action Icons (40–50% Button Footprint)**:
  - Phone Screenshot (`ic_action_screenshot`): Clean monoline smartphone silhouette with viewfinder selection corner.
  - Volume Adjustment (`ic_action_volume`): Monoline speaker cone with sound arcs.
  - Power / Lock Dialog (`ic_action_power`): Standby power ring with vertical stroke, accented in soft coral red (`#F87171`).
- **Array Configuration**:
  - **Button Size**: Configurable between `compact (46dp)`, `standard (54dp)` (default), and `large (62dp)`.
  - **Orientation**: `AUTO` (adaptive direction based on available screen space), `DOWN` (strictly downwards), `UP` (strictly upwards).
  - **Inter-Button Spacing**: Configurable between `6dp`, `10dp` (default), `14dp`, and `18dp`.
  - **Anchor Margin**: Strict spacing between floating button and popout array (`8dp`, `12dp` default, `16dp`, `20dp`).
  - **Action Order**: Fully reconfigurable:
    - `SCREENSHOT_VOLUME_POWER` (`shot · vol · power`)
    - `POWER_VOLUME_SCREENSHOT` (`power · vol · shot`)
    - `VOLUME_SCREENSHOT_POWER` (`vol · shot · power`)
- **Background Capsule Element**:
  - **Toggle**: On / Off container element wrapping the action buttons array.
  - **Capsule Styles**:
    - `OBSIDIAN`: Dark obsidian charcoal (`#121216`) with subtle white border.
    - `METRO_BLACK`: True black (`#000000`) with accent-colored border.
    - `SOLID_ACCENT`: Solid accent fill with pure white border.
    - `OUTLINE_ONLY`: Transparent background with crisp accent outline.
  - **Corner Radius**:
    - `PILL`: Fully rounded stadium pill.
    - `METRO_SQUARE`: Sharp `0dp` Metro rectangle.
    - `ROUNDED`: `14dp` rounded corners.
- **Modal Dismiss Scrim**: Full-screen transparent overlay intercepting outside touches, protecting underlying apps from accidental taps and gracefully auto-dismissing on timeout or tap-away.

### 4.9 Metro Flat Slider (`MetroSlider.kt`)
- **Zero Radius & Flat Architecture**: Fully custom slider replacing rounded Material 3 sliders with authentic Windows Phone Metro geometry.
- **Track**: 4dp flat horizontal track without rounded caps. Active portion is filled with the user's solid accent color; inactive portion is rendered in `MetroDarkBorder` (dark mode) or `#D0D0D0` (light mode).
- **Discrete Step Ticks**: 2dp x 8dp sharp vertical rectangular notches positioned across discrete steps (e.g. 20% intervals). Ticks inside the active portion invert to dark background color; ticks on the inactive track render with 35% contrast opacity.
- **Metro Rectangular Thumb**: 10dp x 24dp vertical rectangular block with a 2dp solid outline. Inverts to white fill and accent border during active dragging/pressing.
- **Tactile Scrubbing**: Emits subtle `MetroHaptics.light()` ticks on every discrete step change and supports instant tap-to-reposition across a 44dp accessible touch target.

### 4.11 Metro Grouped Collapsible Sections (`MetroCollapsibleSection.kt`)
- **Grouped Layout & Metro Hierarchy**: Organizes extensive Studio settings into 7 distinct collapsible groups (`theme & appearance`, `floating button`, `action popout & array`, `background capsule element`, `interaction & behavior`, `payment safety mode`, `system & reset`).
- **Auto-Collapse on Every Entry**: Whenever the user navigates into Settings (switching tabs from Overview, returning from Diagnostics, or opening the app fresh), all expandable sections are collapsed by default. Saved user preferences are 100% preserved.
- **Subtle Low-Contrast Styling**: Clean text-led headers avoiding heavy card containers or elevation boxes. Features a thin 1dp bottom border (`MetroDarkBorder` in dark mode).
- **Live Glanceable Subtitles**: Each section displays a 12sp dynamic status subtitle at a glance (e.g. `dark mode · cobalt`, `circle · 48dp · 50% opacity`, `46dp buttons · auto orientation`, `obsidian · pill`, `haptics on · snap on`, `safe payment protection`).
- **Monoline Sharp Chevron**: Canvas-rendered sharp chevron with square stroke caps, smoothly animating rotation $0^\circ \to 180^\circ$ on toggle.
- **Active State Indicator**: Highlights the title with a 3dp solid accent vertical bar on the left when expanded.
- **Snappy Motion**: Fast 200ms `expandVertically` / `shrinkVertically` + `fadeIn` / `fadeOut` animation.
- **Haptic Feedback**: Centralized via `MetroHaptics.tap()` on header click.

### 4.12 Payment Safety Mode & Confirmation Dialogs (`MetroConfirmationDialog.kt`)
- **Payment Safety Mode Branding**:
  - Feature / Section Name: `Payment Safety Mode`
  - Action Label: `Pause AssistX for payments`
  - Monoline Icon: Dedicated monoline pause icon (`ic_action_pause`) with two clean vertical bars.
  - Dashboard Description: *"Temporarily pause AssistX before opening a payment app."*
- **Full-Width Flat Dialog Panel**: Centered modal with authentic 0dp square corners, 2dp solid outline, and no drop shadows or rounded corners.
- **Double Approval for High-Impact Actions**: Protects critical actions (e.g. pausing AssistX which invokes `disableSelf()` to remove overlay and detach accessibility) against accidental mistouches.
- **Exact Structured Copy**:
  - Title: **Pause AssistX for payments?**
  - Message: *"AssistX will detach its floating button and attempt to stop its Accessibility service. Screen-off and Home gestures will be unavailable while AssistX is paused."*
  - Callout Box: *"Note: To restore these gestures, you may need to re-enable AssistX in Android Accessibility settings."*
  - Action Buttons: `[Cancel]` and `[Pause AssistX]`.
- **Dismissal Safety**: Tapping outside or pressing `Cancel` leaves the assistant, overlay, and accessibility connection completely untouched.

---

## 5. Haptic Feedback Hierarchy (`MetroHaptics.kt`)

Haptic feedback is centralized in `MetroHaptics` and respects the user's On/Off preference.

| Feedback Method | Target Components | API 31+ Effect | Fallback Constants |
|---|---|---|---|
| `tap(view)` | Live tile press, button tap, toggle switch, swatch selection | `VibrationEffect.EFFECT_CLICK` (12ms) | `HapticFeedbackConstants.KEYBOARD_TAP` (`FLAG_IGNORE_VIEW_SETTING`) |
| `select(view)` | Pivot tab change, bottom app bar switch, segmented picker | `VibrationEffect.EFFECT_TICK` (16ms) | `HapticFeedbackConstants.CLOCK_TICK` (`FLAG_IGNORE_VIEW_SETTING`) |
| `light(view)` | Drag threshold crossed, drag release | `VibrationEffect.EFFECT_TICK` (8ms subtle) | `CLOCK_TICK` (`FLAG_IGNORE_VIEW_SETTING`) |
| `success(view)` | Master activation enabled, permissions granted | Waveform `[0, 15ms, 60ms, 20ms]` | `HapticFeedbackConstants.CONFIRM` |
| `warning(view)` | Permissions missing, service disconnected | Waveform `[0, 30ms, 80ms, 30ms]` | `HapticFeedbackConstants.REJECT` |

---

## 6. Motion & Animation Rules
- **Fast, Purposeful Transitions**: Durations are kept between 100ms and 200ms.
- **Zero Heavy Bouncing**: No overshooting, spring bounces, or parallax bloat.
- **Accessibility Respect**: Respects system animation settings; instant snap when animations are disabled.

