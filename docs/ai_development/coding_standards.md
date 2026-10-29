AssistX — Kotlin and Android Coding Standards
Document ID: NUDGE-CODE-001
Version: 0.1
Status: Draft — owner review required
Last updated: 2026-10-29
Related documents: AGENTS.md, AI_AGENT_WORKFLOW.md, SYSTEM_ARCHITECTURE.md, MODULE_AND_PACKAGE_STRUCTURE.md, STATE_AND_LIFECYCLE.md, TEST_STRATEGY.md
Instruction for AI agents: Follow the conventions already established by the repository unless an approved project standard supersedes them. This file defines the baseline for new code; it does not authorize broad reformatting of existing code. Keep changes focused, idiomatic, testable, and proportional to the task.

1. Principles
1. Clarity over cleverness. Code should be easy for the product owner and another agent to understand.
2. Smallest sufficient design. Avoid frameworks, abstractions, generic managers, and layers without a concrete need.
3. Single responsibility with pragmatic boundaries. Keep UI, state/policy, Android operations, preferences, and diagnostics separated according to the architecture, but don't create a class for every trivial expression.
4. Explicit state and outcomes. Do not collapse distinct runtime states into one boolean or assume an operation succeeded.
5. Safe lifecycle behavior. Repeated start/stop requests must be safe; recovery must be bounded.
6. Testability. Put deterministic logic where it can be tested without requiring a physical device.
7. Platform correctness. Verify Android-specific assumptions and API requirements before implementing them.
8. Privacy and minimal permissions. Collect only the data and permissions required by approved features.
9. No hidden scope changes. New features, permissions, frameworks, services, and dependencies require documented justification and approval where specified by AGENTS.md.
2. Kotlin Style and Formatting
Follow the official Kotlin coding conventions and the style settings of the actual project.
Naming
- Packages: lowercase, concise, and aligned with the approved package structure.
- Classes, interfaces, objects, enums: UpperCamelCase, e.g. OverlayManager.
- Functions and properties: lowerCamelCase, e.g. refreshPermissionState().
- Constants: follow the project's established Kotlin convention; use one consistent convention.
- Test names: describe behavior or condition, e.g. disableRequestPreventsAutomaticRecovery.
- File names should explain the main declaration or purpose. Avoid vague names such as Utils.kt, Common.kt, Helper2.kt, or Misc.kt.
Formatting
- Use Kotlin/Android Studio formatting consistent with the official Kotlin style.
- Use four spaces for indentation; do not use tabs in Kotlin source.
- Omit semicolons.
- Keep lines and functions readable; split long expressions when that improves understanding.
- Use braces for multiline control-flow bodies.
- Do not perform repository-wide formatting during a small task.
- If the project adopts a formatter or linter, use its committed configuration and documented command.
File organization
- One primary class/interface per file is the default; related small declarations may live together when that improves cohesion.
- Keep related methods together in a logical reading order.
- Place extension functions near the types and callers they primarily serve; avoid global extension-function clutter.
- Don't create files only to hold one trivial forwarding function.
- Don't create empty placeholders for future features.
Comments and documentation
- Comments explain why something non-obvious is necessary, not merely restate what the code says.
- Document important lifecycle assumptions, retry limits, platform workarounds, and non-obvious invariants.
- Public/reusable APIs should have concise documentation where the contract is not self-evident.
- Remove stale comments when behavior changes.
- Do not write comments claiming a root cause that has not been confirmed.
3. Types, Nullability, and Data Modeling
- Prefer val over var; use mutable state only where mutation is needed and ownership is clear.
- Prefer immutable data classes and read-only collection interfaces for values passed between layers.
- Model known alternatives explicitly with enums, sealed types, or small result models when they materially improve correctness.
- Represent uncertainty explicitly. Do not convert unknown/failed observations into success or a guessed default.
- Avoid Any-based models or unchecked casts where a typed representation is practical.
- Prefer Kotlin null-safety over Java-style sentinel values.
- Do not use !! as a shortcut. If a value is required, validate it or restructure the code to make the invariant explicit. A narrowly justified use must still be safe and documented.
- Avoid force-unwrapping state restored from storage or supplied by Android callbacks.
- Do not expose mutable collections or mutable state to callers unless mutation is part of the API contract.
- Validate persisted or externally supplied values before use; handle absent, malformed, or obsolete values safely.
AssistX state rules
- Desired enabled state is not runtime health.
- Overlay permission and overlay attachment are separate.
- Accessibility enabled and Accessibility connected are separate where applicable.
- A start request is not proof of startup success.
- An unknown observation must remain representable.
- A stored historical ACTIVE value must not be restored as current runtime truth.
These rules are mandatory even if a simpler boolean appears convenient.
4. Functions and Classes
- Prefer small functions with a clear purpose and testable outcome.
- A function should not silently perform unrelated side effects.
- Use descriptive names for actions and queries. A name should suggest whether the function changes state or returns information.
- Make visibility as narrow as practical (private or internal where appropriate).
- Prefer constructor parameters for required dependencies over global mutable singletons.
- Avoid large classes that own UI, permissions, lifecycle, persistence, and diagnostics all at once.
- Avoid excessive tiny classes that only forward calls and add no meaningful boundary.
- Avoid a generic Manager, Helper, or Utils abstraction unless its responsibility is precise.
- Do not introduce a repository/use-case/interface layer solely for architectural appearance. Add boundaries where they improve testability, ownership, or change isolation.
- Keep constructors and initialization free from surprising, expensive, or permission-sensitive side effects.
5. Errors and Results
- Handle expected failure paths explicitly.
- Do not swallow exceptions silently.
- Do not catch Throwable for routine error handling. Catch the narrowest expected exception types unless cleanup/rethrow behavior specifically requires broader handling.
- Do not use exceptions as ordinary control flow where a typed result is clearer.
- Keep user-facing error text understandable and free of stack traces, secrets, or excessive internal detail.
- Include useful safe diagnostic context: operation, component, result/error category, and relevant state.
- Avoid exposing raw exception messages directly to users unless reviewed for safety/usability.
- Distinguish success, failed, blocked, and unknown outcomes when the caller needs those distinctions.
- A failed diagnostics write should normally not cause the core floating assistant to fail.
- Cleanup code should handle partially initialized objects and repeated calls safely.
6. Coroutines and Asynchronous Work (If Used)
Do not introduce coroutines or a coroutine dependency solely because this document describes them. Use the project's established async approach where appropriate.

If Kotlin coroutines are used:
- Follow Android coroutine best practices.
- Avoid GlobalScope; use a scope whose lifetime matches the work, or an explicitly owned scope for work that must outlive a UI component.
- Do not tie floating-runtime work to an Activity or ViewModel scope if it must continue after Studio closes.
- Cancel work when its owning component stops or the operation is no longer needed.
- Make suspend functions main-safe; blocking I/O or expensive CPU work must not block the main thread.
- Prefer injected dispatchers for nontrivial code that needs deterministic tests, rather than hardcoding dispatchers throughout the implementation.
- Use cancellation cooperatively. Do not swallow CancellationException; rethrow it or allow it to propagate.
- Do not launch a new coroutine for every UI recomposition or repeated callback.
- Prevent concurrent recovery attempts where the policy requires single-flight operation.
- Use a test dispatcher and coroutine test utilities only if the project has adopted coroutines and the relevant dependency is approved.
If the project does not use coroutines, do not add a coroutine dependency without a concrete, reviewed requirement.
7. Android Lifecycle and Threading
- Do not block the main thread with disk access, long computations, waits, or retry loops.
- Do not use arbitrary Thread.sleep() for app behavior or tests when a state/event-based approach is appropriate.
- Do not assume an Activity remains alive while the floating assistant is active.
- Do not start/stop runtime components from UI rendering or recomposition.
- Respect the actual lifecycle of Activities, services, and Accessibility Service callbacks.
- Treat callbacks as observations; do not infer a permanent state from a single old callback.
- Re-check current permission state at appropriate points rather than trusting a cached value indefinitely.
- Make start/stop operations idempotent where practical.
- Handle callbacks and asynchronous results that arrive after the user has disabled AssistX.
- Keep UI work on the required UI thread and move blocking work appropriately.
- Do not assume Android will let the app start itself from the background, after reboot, or after force-stop.
- Check the relevant current Android documentation when target SDK or OS version affects behavior.
Official starting points:
- Android app architecture
- Android lifecycle guidance
- Foreground-service launch restrictions
8. UI Standards (Toolkit-Independent)
The UI toolkit remains an owner decision. Apply these principles to Compose, Views, or any approved combination.
- UI presents state and emits user intent; it does not own the whole product runtime.
- Keep state derivation in one authoritative place.
- Do not show Active immediately when Enable is tapped; wait for sufficient evidence.
- Explain special permissions before sending the user to settings.
- Re-check permissions after the user returns.
- Distinguish a disabled Accessibility setting from a disconnected service where detectable.
- Do not repeatedly open settings or show repeated permission prompts in a loop.
- Keep the floating button minimal and avoid unnecessary screen obstruction.
- Keep controls and error states understandable.
- Use Android string resources for user-visible text when appropriate; avoid scattering hardcoded messages across services and UI.
- Do not start services, attach overlays, write preferences, or perform other lifecycle side effects solely because a UI screen rendered.
- Ensure Studio can close or recreate without creating duplicate overlays/services.
If Jetpack Compose is selected
- Follow official Compose state and unidirectional data-flow guidance.
- Prefer state flowing down and user events flowing up.
- Keep composables focused on rendering and callbacks.
- Do not start long-lived work directly during recomposition.
- Use lifecycle-aware collection/observation according to the selected architecture.
References:
- State and Jetpack Compose
- Android app architecture
If Android Views are selected
- Keep Activity/Fragment code focused on view wiring and lifecycle.
- Observe state in a lifecycle-aware way.
- Avoid retaining Activity/view references in long-lived service/runtime objects.
- Ensure listeners are registered/unregistered in the correct lifecycle.
- Avoid duplicate observer registration after recreation.
9. Overlay and Service Code
- Isolate overlay permission checks and window add/remove operations in the overlay boundary defined by the architecture.
- Check permission before operations that require it.
- Maintain one overlay at most.
- Do not use invisible full-screen overlays or touch interception unless explicitly required, approved, and verified against platform rules.
- Handle addView/remove operations and relevant window errors; do not assume they always succeed.
- Do not store an Activity reference in a long-lived overlay/service component.
- Use application context where appropriate, but do not treat that as permission to ignore lifecycle cleanup.
- Keep the Accessibility Service narrow and independent from overlay lifecycle.
- A foreground service, if approved, must have a permitted purpose/type and appropriate user-visible notification behavior.
- Do not add a boot receiver, alarms, or repeated polling as a substitute for a sound lifecycle model.
- Ensure user Disable prevents further automatic recovery attempts.
10. Persistence and Diagnostics Code
- Keep persistence local for the MVP.
- Persist user preferences, not stale runtime health as the current truth.
- Centralize defaults and validation.
- Keep the storage mechanism proportionate to the amount and structure of data.
- Diagnostics must be bounded by a documented retention/size policy.
- Avoid logging personal data, unrelated app content, secrets, tokens, credentials, or full payloads that are unnecessary for diagnosis.
- Prefer stable event types and structured fields over arbitrary strings where useful.
- Include timestamp/component/event/outcome when available.
- Use a safe fallback or nonfatal handling when diagnostics storage fails.
- Do not recursively log failures from the logging mechanism.
- Do not add network uploads or analytics to the MVP without explicit approval.
11. Manifest, Permissions, and Dependencies
- Add permissions only when they map to an approved requirement.
- Do not add INTERNET, Accessibility, foreground-service, boot receiver, Device Administrator, notification listener, location, or other sensitive capability without a documented need and approval where required.
- Ensure exported components are configured intentionally.
- Keep Accessibility configuration minimal and only if the approved action requires it.
- Verify current target-SDK and version-specific requirements before modifying service declarations or permissions.
- Do not misdeclare a foreground-service type to avoid platform restrictions.
- Before adding a third-party dependency, explain why existing platform/project tools are insufficient, list reasonable alternatives, and assess maintenance/security implications.
- Do not upgrade unrelated dependencies during a feature task.
- Keep dependency versions aligned with the project's actual Gradle/version-catalog convention.
- Never commit signing keys or credentials.
12. Testing Standards
- Add tests for behavior changes in the same task wherever practical.
- Test success, failure, unknown, boundary, repeated, and conflicting operations when relevant.
- Test recovery retry bounds and reset conditions.
- Prefer deterministic fakes for platform operations when testing policy logic.
- Use Android instrumentation/device tests for behavior that depends on Android APIs or device manufacturer behavior.
- Do not claim a real-device scenario was verified by unit tests.
- Do not hardcode real delays in unit tests if a deterministic clock/scheduler can be used.
- Do not silently skip or weaken a failing test.
- Record tests not run and why.
- Keep test names behavior-oriented.
- Tests should verify outcomes and invariants, not mirror every private implementation detail.
13. Performance and Resource Use
- Avoid polling when event/callback-based observation is suitable.
- Never implement unbounded retry loops.
- Avoid expensive work on the main thread.
- Avoid repeated allocations, logging, or service creation caused by UI rendering or ordinary Activity recreation.
- Measure battery/memory behavior on the target device before making performance claims.
- Do not add a timer, wake lock, alarm, or background service without a documented requirement and approved design.
- Do not claim “always-on,” “never killed,” or equivalent guarantees.
14. Git and Change Hygiene
- Inspect git status before editing.
- Keep the diff focused on the assigned task.
- Do not delete/move unrelated files or mass-format the repository in a feature task.
- Do not add generated build outputs, runtime databases, local diagnostic logs, or personal data.
- Do not hide changes by amending/rebasing/pushing/merging unless explicitly authorized.
- Run the relevant tests and inspect the final diff.
- Update related docs and traceability if behavior, architecture, permissions, or tests change.
15. Pull Request / Task Review Checklist
Before requesting review:
- [ ] Task objective and requirement IDs are clear.
- [ ] The solution is minimal and follows package ownership.
- [ ] Naming and formatting follow Kotlin conventions.
- [ ] State/lifecycle invariants remain satisfied.
- [ ] Errors are handled explicitly.
- [ ] No unnecessary dependencies, permissions, or services were added.
- [ ] Tests were added/updated and actually run where possible.
- [ ] Platform-specific claims are verified or marked uncertain.
- [ ] No unrelated user changes were overwritten.
- [ ] Documentation and traceability are updated.
- [ ] The completion report includes tests run and not run.
- [ ] Known limitations and follow-up work are recorded.
16. Official References
Use current official documentation as the primary source for evolving Android/Kotlin conventions. These references are starting points; verify the relevant content for the selected SDK and target device.
- Kotlin Coding Conventions: https://kotlinlang.org/docs/coding-conventions.html
- Android App Architecture: https://developer.android.com/topic/architecture
- Kotlin Coroutines Best Practices for Android: https://developer.android.com/kotlin/coroutines/coroutines-best-practices
- State and Jetpack Compose: https://developer.android.com/develop/ui/compose/state
- Android Lifecycle: https://developer.android.com/topic/libraries/architecture/lifecycle
- Foreground Service Background-Start Restrictions: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
- Android 12 Untrusted Touch Restrictions: https://developer.android.com/about/versions/12/behavior-changes-all
17. Owner Review
- Product owner: Anuj Pawar (@anujp125)
- Status: Draft / Approved
- UI toolkit selected: Jetpack Compose with Material 3
- Lint/formatter policy: [DECIDE AFTER PROJECT SETUP]
- Review date: 2026-10-29
- Accepted deviations: None
- Notes: Approved coding standards.