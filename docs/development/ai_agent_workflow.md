AssistX — AI Agent Development Workflow
Document ID: ASSISTX-AI-WORKFLOW-001
Version: 0.1
Status: Draft — owner review required
Last updated: 2026-10-29
Related documents: AGENTS.md, PRODUCT_BRIEF.md, SRS.md, MVP_SCOPE.md, PRODUCT_ROADMAP.md, SYSTEM_ARCHITECTURE.md, STATE_AND_LIFECYCLE.md, FAILURE_AND_RECOVERY_MATRIX.md, TEST_STRATEGY.md, ACCEPTANCE_TESTS.md
Purpose for agents: Follow this workflow for every development task. It turns a sprint task into a small, reviewable code change with test evidence. It does not authorize work outside the assigned task or override AGENTS.md, the approved requirements, or Android platform/security constraints.

1. Core Workflow
Every coding task follows this sequence:
Read task and source-of-truth docs
             ↓
Inspect repository and current changes
             ↓
Check scope, prerequisites, and unresolved decisions
             ↓
Propose a small implementation plan
             ↓
Implement only the approved task
             ↓
Run relevant checks and tests
             ↓
Review diff, state invariants, and scope
             ↓
Update docs / traceability where needed
             ↓
Report evidence, limitations, and follow-ups
             ↓
Wait for review or the next assigned task
Do not skip inspection or testing because the task looks easy. For a pure documentation edit, run applicable link/consistency checks if available; do not claim code tests are relevant when no code changed.
2. Step 1 — Read and Understand the Task
Before touching files, identify:
- Task ID and sprint/milestone.
- Objective and the specific user problem.
- Requirement IDs being implemented.
- In-scope behavior and explicit non-goals.
- Acceptance criteria.
- Dependencies and prerequisites.
- Required test level(s).
- Whether an owner decision or platform verification is needed.
Read only the documents needed to understand the task, but always consult the relevant source of truth. For lifecycle, permission, or recovery tasks, read the related state/lifecycle and failure/recovery documents even if the immediate ticket is short.

If the task has no measurable completion criteria, propose a narrowed task with pass/fail outcomes before coding.
3. Step 2 — Inspect the Repository
First inspect the current state; do not assume the repository is empty or pristine.

Check:
- current working directory and repository root;
- git status and current branch, if Git is initialized;
- existing uncommitted user changes;
- root and nested AGENTS.md instructions;
- Gradle wrapper and actual build configuration;
- target/minimum SDK and application ID, if configured;
- relevant production/test files;
- existing implementation patterns, dependencies, and tests;
- current docs and their status (draft vs. approved).
Rules:
- Preserve unrelated uncommitted work.
- Never reset, clean, overwrite, or delete unrelated files to make a task easier.
- Do not replace an existing implementation with a generated template without inspecting it.
- Do not assume a path or task name based on this document; verify it in the actual repository.
- If the repository is not available, state that implementation cannot be safely applied there. You may prepare a proposed patch or code artifact separately only if the user requested that form of work.
4. Step 3 — Perform a Dependency and Decision Check
Before planning implementation, ask:
1. Are all prerequisite tasks complete?
2. Are the requirements sufficiently specific?
3. Does the solution depend on an unresolved owner decision?
4. Does it require a sensitive permission or system capability?
5. Does it change application lifecycle, persisted data, or component ownership?
6. Does it add a third-party dependency or new Gradle module?
7. Does it introduce networking, analytics, or external data handling?
8. Can the expected behavior be verified in the available environment?
If a high-impact decision is unresolved, do not silently guess. Provide the smallest set of options and a recommendation, or do safe independent work that does not commit to the unresolved choice.

Examples of high-impact choices:
- primary floating-button action or gesture;
- whether Accessibility is required;
- minimum/target SDK or package ID;
- foreground service and reboot startup strategy;
- distribution method;
- adding sensitive permissions;
- changes to local data/privacy behavior.
Do not ask the owner to repeat a decision already recorded in an approved document.
5. Step 4 — Write a Small Plan
Before editing, prepare a concise plan containing:
Task ID:
Objective:
Requirement IDs:
Preconditions:
Files/components expected to change:
In scope:
Out of scope:
Implementation approach:
Tests to add/update:
Commands/manual checks to run:
Risks and invariants to preserve:
Documentation to update:
Unresolved questions:
The planned files are a hypothesis, not a license to make unrelated edits. If inspection reveals a different minimal path, explain the change before proceeding when it materially changes scope or design.
Task sizing
A task is probably too large when it:
- changes unrelated components;
- combines architecture redesign with multiple user-facing features;
- introduces a major dependency and implements a feature at the same time;
- cannot be tested with clear pass/fail criteria;
- has several independently reviewable outcomes.
Split such work into smaller tasks. Prefer a narrow vertical slice including implementation and tests.
6. Step 5 — Implement the Smallest Sufficient Change
Implementation rules:
- Solve the approved objective with the smallest maintainable change.
- Follow the actual repository's Kotlin, Gradle, resource, and test conventions.
- Do not change public behavior beyond the requirement.
- Do not expand MVP scope “for completeness.”
- Avoid speculative future-feature code and empty scaffolding.
- Do not create layers, interfaces, generic managers, or modules without a concrete benefit.
- Keep UI rendering separate from lifecycle side effects.
- Keep Android-specific operations separate enough to test core logic where practical.
- Handle failures explicitly; do not swallow important exceptions silently.
- Use safe defaults and preserve the UNKNOWN state where evidence is insufficient.
- Update tests as part of the same task when behavior changes.
Required AssistX invariants
Unless an approved architecture change explicitly refines them:
1. Desired enabled state is not proof of runtime health.
2. Overlay permission is distinct from overlay attachment.
3. Overlay health is distinct from Accessibility status.
4. Accessibility enabled is distinct from Accessibility connected.
5. User disablement prevents automatic recovery.
6. Recovery attempts are bounded.
7. Repeated start/stop requests must not create duplicate overlays or services.
8. No success state is shown before sufficient evidence of success.
9. Persisted state must not be treated as current runtime truth after process recreation.
10. Android permission and user-stop controls must not be bypassed.
When a task touches one of these invariants, explicitly mention it in the plan and review.
7. Step 6 — Test the Change
Choose verification based on the behavior changed.
For pure state or policy logic
- Add/update unit tests.
- Test positive, negative, unknown, and boundary cases.
- Test repeated and conflicting operations where relevant.
- Avoid real sleeps when deterministic events or an injected clock can be used.
For Android framework integration
- Run appropriate instrumentation tests if available.
- Test permission-check and failure paths, not just the success path.
- Verify manifest and target-SDK effects.
- Use the actual platform documentation for version-sensitive behavior.
For overlay, Accessibility, lifecycle, reboot, or manufacturer behavior
- Identify required physical-device tests.
- Use the target Realme device when available.
- Record the actual model/build and app commit.
- Keep device evidence distinct from emulator or unit-test results.
Commands
- Inspect actual available Gradle tasks and project setup first.
- Use the Gradle wrapper supplied by the repository.
- Do not assume the standard task exists in an unfinished project.
- Record exact commands and summaries.
- Do not hide test failures or change assertions merely to obtain a green run.
If a test cannot be run, report NOT RUN or BLOCKED and say why. Do not imply that a physical-device scenario was tested by a unit test.
8. Step 7 — Review the Diff
Before completing the task, inspect the entire diff and verify:
Scope
- Every changed file is related to the task or an explicitly approved supporting change.
- No excluded feature was added.
- No unrelated refactor was slipped in.
- Existing user changes remain intact.
Architecture
- Package ownership and dependency direction remain appropriate.
- No duplicate policy or state model was introduced.
- No cycle was created between UI, runtime, platform, and data code.
- The Activity or UI render path does not accidentally own the floating runtime.
Android and permissions
- Manifest changes are necessary and approved.
- Sensitive permissions/services/receivers are justified.
- Version- and target-SDK-dependent behavior was verified.
- No platform restriction is bypassed.
Reliability
- No unbounded retry, polling, or keep-alive loop was introduced.
- Duplicate overlays/services are prevented.
- Errors and unknown states are handled honestly.
- Disablement and partial cleanup are safe.
Data and privacy
- No secrets, tokens, production logs, or personal data were added.
- No unapproved networking/analytics is present.
- Diagnostic storage is bounded or its remaining work is tracked.
Quality
- Tests are meaningful and run where possible.
- New code follows the repository conventions.
- Documentation and acceptance criteria still match the behavior.
- Build/test output is not misrepresented.
9. Step 8 — Update Documentation and Traceability
Update documentation when the task changes:
- requirement behavior or scope → SRS.md, MVP_SCOPE.md;
- milestone ordering/dependencies → PRODUCT_ROADMAP.md, SPRINT_PLAN.md;
- component ownership/dependencies → SYSTEM_ARCHITECTURE.md, MODULE_AND_PACKAGE_STRUCTURE.md;
- state meanings/transitions → STATE_AND_LIFECYCLE.md;
- permissions or platform constraints → PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md;
- failure/recovery behavior → FAILURE_AND_RECOVERY_MATRIX.md;
- tests or coverage → ACCEPTANCE_TESTS.md, REQUIREMENTS_TRACEABILITY.md;
- major architecture choice → ARCHITECTURE_DECISIONS.md;
- owner-approved decisions → DECISION_LOG.md;
- user-facing setup/release behavior → relevant operations docs.
Do not update a requirement just to make an implementation that violates it appear compliant. If the requirement should change, propose the change and wait for approval.
10. Handling Blockers and Discoveries
If blocked by an unanswered owner decision
- stop the dependent implementation;
- explain the specific decision and why it matters;
- offer a concise recommendation and alternatives;
- continue only independent safe work if it remains within scope.
If documentation conflicts
- identify the exact conflicting requirements;
- do not silently choose;
- request a decision or follow the documented conflict-resolution order.
If platform behavior contradicts assumptions
- capture the official documentation or device evidence;
- explain the impact;
- propose a compliant alternative;
- update the platform/architecture documents after approval.
If a test uncovers a defect outside the current task
- do not automatically refactor it;
- record the finding with reproduction steps, impact, and logs;
- continue the current task only if it remains safe and its acceptance criteria can be assessed;
- create or propose a separate task.
If the implementation needs a larger change than expected
- stop before expanding scope;
- explain why the minimal approach is insufficient;
- list affected components and tests;
- propose a split or request approval.
11. Task Completion Report
Every task must conclude with a report using this structure:
Task ID:
Objective:
Requirement IDs:
Implementation summary:
Files changed:
Architecture/decision references:
Tests run:
  - Command or manual steps:
  - Result:
Tests not run or blocked:
  - Test:
  - Reason:
Device evidence (if applicable):
  - Device/OS/build:
  - App version/commit:
  - Observed result:
Permission/manifest/dependency changes:
Documentation updated:
Known risks or limitations:
Scope check:
  - No unapproved feature added: Yes/No
  - No unapproved permission/dependency added: Yes/No
  - No unrelated files overwritten: Yes/No
Follow-up tasks/unresolved questions:
A useful report is specific and concise. Include exact test counts/results when available. If the build fails, report the failure summary and whether its cause is known.
12. Review Handoff
After reporting:
- leave changes ready for review according to the repository workflow;
- do not merge, release, publish, or distribute unless explicitly authorized;
- do not start the next sprint task automatically unless the user or approved workflow directs it;
- surface any approval needed for a high-risk decision;
- preserve test output and references needed for review.
If the agent has access to Git operations, do not push, merge, tag, or rewrite history unless the task explicitly authorizes that action.
13. Agent Autonomy Boundaries
May proceed without asking (if within the assigned task)
- inspect code and documentation;
- implement a narrowly defined requirement;
- add relevant tests;
- fix a directly related issue discovered during implementation;
- update implementation documentation to match an already-approved decision;
- run available build/test/static checks;
- report a non-blocking limitation.
Must request approval before proceeding
- new product features or actions;
- change to the primary interaction;
- new sensitive permission or Accessibility capability;
- adding networking, a backend, telemetry, cloud data, or accounts;
- adding a foreground service, boot receiver, or Device Administrator capability;
- major dependencies/frameworks or extra Gradle modules;
- change to data retention or privacy model;
- public distribution, signing, publishing, licensing, or destructive Git operations;
- broad refactoring or deletion of unrelated code;
- ignoring or changing approved acceptance criteria.
14. Definition of “Done” for This Workflow
A task is ready for review when:
- the objective and acceptance criteria are addressed;
- implementation is limited to approved scope;
- relevant tests were added/updated and executed where possible;
- any test that could not be run is disclosed;
- the diff has been reviewed;
- state, permission, privacy, and recovery invariants are preserved;
- documents are updated where needed;
- the completion report contains evidence and limitations;
- unresolved issues are explicit.
A task is not necessarily “accepted” until the reviewer/product owner has reviewed the results and any required manual/device checks.
15. Workflow Review
- Product owner: Anuj Pawar (@anujp125)
- Status: Draft / Approved
- Preferred branch/PR workflow: main branch with task-specific branches (e.g. task/ASSISTX-007)
- Required review before merge: Yes, agent self-inspection and owner review
- Repository/CI details: Local gradlew gates; GitHub Actions on push to main
- Review date: 2026-10-29