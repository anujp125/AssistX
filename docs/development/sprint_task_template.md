AssistX — Sprint Task Template
Template version: 0.1
Status: Reusable template — copy for each task
Last updated: 2026-10-29
Owner: Anuj Pawar (@anujp125)
How to use this template: Copy this file into a task-specific Markdown file, for example docs/03-ai-development/tasks/ASSISTX-023-overlay-permission-check.md. Fill in every section relevant to the task. Do not leave critical acceptance criteria vague. If a placeholder blocks a safe implementation, keep the task blocked and request the missing decision instead of guessing.

1. Task Metadata
- Task ID: ASSISTX-XXX
- Title: [Use an action + outcome, e.g. "Represent missing overlay permission accurately"]
- Milestone: M__
- Sprint: S__
- Priority: P0 / P1 / P2
- Status: Not Started / Ready / In Progress / Blocked / Review Required / Passed / Deferred / Rejected
- Task owner/agent: [FILL IN]
- Reviewer: [FILL IN]
- Created: [YYYY-MM-DD]
- Related issue/PR: [LINK OR N/A]
2. Objective
In one or two sentences, state the single outcome this task must deliver.

[FILL IN]
Why this task is needed
Describe the user problem or approved requirement this task addresses.

[FILL IN]
Observable end state
At completion, a reviewer should be able to observe or verify:

[FILL IN]

Avoid objectives such as “make it reliable,” “clean up architecture,” or “implement the whole runtime” unless they are split into bounded, testable tasks.
3. Source of Truth and Requirement Links
- SRS requirement IDs: [FR-___, NFR-___, or N/A with reason]
- Acceptance test IDs: [AT-___, RT-___, UT/IT/FI IDs, or TBD]
- Failure/recovery IDs: [F-___, if applicable]
- Architecture section(s): [FILL IN]
- State/lifecycle section(s): [FILL IN]
- Related decision log entries: [FILL IN]
- Other documents to read: [FILL IN]
Applicable instructions
- Root AGENTS.md: required.
- AI_AGENT_WORKFLOW.md: required when available.
- DEFINITION_OF_DONE.md: required when available.
- Other relevant instructions: [FILL IN]
If the requirement or source document is missing or conflicts with an approved decision, explain the issue before implementation. Do not invent requirement IDs.
4. Prerequisites and Dependencies
Preconditions
- [ ] Required product decisions are approved.
- [ ] Required prior sprint tasks are complete.
- [ ] Relevant architecture/state rules are understood.
- [ ] Required SDK, Gradle, emulator, or device environment is available.
- [ ] Test approach and acceptance criteria are clear.
Dependency tasks
Task ID	Dependency	Required status	Notes
ASSISTX-___	[FILL IN]	Passed / Approved	[FILL IN]


Unresolved decisions or blockers
[Write "None" only after checking. Otherwise list the specific choice/evidence needed and why the task depends on it.]

If the task depends on an owner decision, mark status Blocked until the decision is recorded. Safe independent work may proceed only if it does not commit to the unresolved choice.
5. Scope
In scope
- [ ] [Specific behavior or change 1]
- [ ] [Specific behavior or change 2]
- [ ] [Specific test or documentation update]
Explicitly out of scope
- [Feature/refactor/permission not included]
- [Other adjacent behavior not included]
- [Future work to track separately]
Scope rule: The agent must not add “bonus” functionality, unrelated refactoring, new permissions, networking, telemetry, frameworks, or dependencies unless explicitly approved.
6. Proposed Implementation Boundary
Describe the smallest implementation approach that can satisfy the objective. Do not prescribe unnecessary line-by-line code if the agent should inspect and follow existing project patterns.
Expected files/components
Path or component	Expected change	Why it belongs in this task
[FILL IN or "Determine after repository inspection"]	[Add / modify / test / docs]	[FILL IN]


These are expected files, not permission to edit unrelated code. If repository inspection shows the change needs different files, the agent must explain material deviations in its report.
Component ownership
- Responsible component/package: [FILL IN]
- Interfaces/state affected: [FILL IN]
- Dependencies used: [Existing dependencies only unless approved]
- Manifest/permission changes: None / Describe and link approval
- Persistence/schema changes: None / Describe
- Public/user-visible behavior changes: None / Describe
Design constraints
List constraints specific to this task.

[FILL IN]

At minimum, preserve all applicable AssistX invariants:
- Desired enabled state is not proof of runtime health.
- Overlay permission and actual overlay attachment are distinct.
- Overlay state and Accessibility state are distinct.
- Accessibility enabled and connected are distinct where relevant.
- Explicit user disablement suppresses automatic recovery.
- Recovery must be bounded and platform-compliant.
- Start/stop operations must not create duplicate overlays or services.
- Do not report success before there is appropriate confirming evidence.
- Do not restore stale runtime health as current truth after process recreation.
- Do not bypass Android permissions or user/system stopping controls.
7. Acceptance Criteria
Write criteria that are testable and unambiguous. Each criterion should have a clear pass/fail result.
AC ID	Given / condition	When / action	Then / expected result	Verification
AC-1	[PRECONDITION]	[ACTION]	[OBSERVABLE OUTCOME]	[TEST ID or review evidence]
AC-2	[PRECONDITION]	[ACTION]	[OBSERVABLE OUTCOME]	[TEST ID or review evidence]
AC-3	[FAILURE/BOUNDARY CONDITION]	[ACTION]	[EXPECTED SAFE BEHAVIOR]	[TEST ID or review evidence]


Acceptance criteria must cover failure paths when the task touches permissions, lifecycle, persistence, concurrency, or recovery.
Negative acceptance criteria
List what must not happen.
- [ ] No unapproved feature is added.
- [ ] No unapproved permission, service, receiver, framework, or dependency is added.
- [ ] No unrelated files are overwritten or deleted.
- [ ] No unbounded retry, polling, or keep-alive loop is introduced.
- [ ] No state is reported as successful without evidence.
- [ ] [Task-specific prohibited outcome]
8. Testing Plan
Automated tests
Test ID / command	Type	Expected outcome	Required environment
[FILL IN]	Unit / Instrumentation / Static / Build	[FILL IN]	[FILL IN]


Tests required by this task:
- [ ] Unit tests for applicable business/state logic.
- [ ] Instrumentation tests if the change depends on Android framework behavior.
- [ ] Regression tests for affected features.
- [ ] Static/build checks where available.
- [ ] No new tests needed — explain why: [FILL IN]
Manual/device tests
Test ID	Steps	Expected result	Device/environment
[RT/AT/FI ID]	[FILL IN]	[FILL IN]	[FILL IN or N/A]


Test commands
Record exact commands after inspecting the actual project.
[FILL IN COMMANDS]
Test reporting rules
- Report only commands/steps that were actually executed.
- Include actual pass/fail summaries.
- Mark non-executed tests NOT RUN or BLOCKED, with reason.
- Do not represent unit-test results as proof of physical-device behavior.
- Do not remove or weaken a failing test solely to obtain a passing run.
9. Android, Permission, Privacy, and Reliability Review
Complete this section if the task touches platform APIs, manifest, services, UI over other apps, data, or diagnostics.
- Android APIs/behaviors to verify: [FILL IN / N/A]
- Official documentation reviewed: [URL(s) and date / N/A]
- Permission impact: None / Explain and link owner approval
- Accessibility impact: None / Explain
- Foreground-service/boot impact: None / Explain
- Privacy/data impact: None / Explain
- Battery/background impact: None / Explain
- Recovery/idempotency impact: None / Explain
- Target-device check required: Yes / No; explain
- Unknowns that remain: [FILL IN]
A missing answer must not be treated as approval. If the task changes sensitive permissions or platform behavior in a material way without an approved design, stop and request review.
10. AI Agent Execution Plan
The agent must complete the following steps:
1. Inspect repository status, current instructions, relevant code, and tests.
2. Confirm the task is not blocked by scope or unresolved decisions.
3. State a concise implementation plan.
4. Make only the scoped change.
5. Add/update tests.
6. Run the relevant checks.
7. Inspect the complete diff for unrelated changes and permission/dependency additions.
8. Update documentation/traceability where required.
9. Produce the completion report in Section 12.
Stop conditions
The agent must stop before dependent implementation if:
- acceptance criteria are materially ambiguous;
- required product approval is missing;
- the solution requires an unapproved permission/dependency/service/module;
- the repository has unrelated changes at risk of being overwritten;
- current official platform rules contradict the proposed approach;
- implementation scope is materially larger than the task;
- no safe, testable approach is available.
When blocked, report the specific decision/evidence needed and any safe partial work. Do not ask the user to repeat information that is already in an approved source-of-truth document.
11. Definition of Done for This Task
- [ ] Objective and all applicable acceptance criteria are satisfied.
- [ ] Change remains inside approved scope.
- [ ] Relevant tests were added or updated.
- [ ] Relevant automated checks were executed.
- [ ] Required manual/device checks were performed or clearly marked not run.
- [ ] Test output/results are recorded accurately.
- [ ] Diff contains no unrelated changes.
- [ ] No unapproved permissions, dependencies, features, or architecture changes were introduced.
- [ ] AssistX state/lifecycle/permission invariants remain valid.
- [ ] Applicable documentation and traceability are updated.
- [ ] Known limitations and unresolved questions are reported.
- [ ] Completion report is filled in.
- [ ] Review is requested if the repository workflow requires it.
Task status should be Review Required once implementation and self-checks are complete, and Passed only after the applicable reviewer/acceptance process has approved it.
12. Completion Report (Filled by Agent)
Task ID:
Objective:
Requirement IDs:
Summary of implementation:
Files changed and reasons:
Decisions made and references:
Tests run:
  - Command/manual steps:
  - Result:
Tests not run or blocked:
  - Test:
  - Reason:
Target-device evidence (if applicable):
  - Device/OS/build:
  - App version/commit:
  - Observed result:
Manifest/permission/dependency changes:
Documentation/traceability updated:
Known risks/limitations:
Unresolved questions:
Scope and guardrail check:
  - No unapproved features added: Yes/No
  - No unapproved permission/dependency added: Yes/No
  - No unrelated work overwritten: Yes/No
Recommended follow-up:
13. Reviewer Checklist
The reviewer should check:
- [ ] The task solves the stated objective without scope creep.
- [ ] Requirement and acceptance criteria links are correct.
- [ ] Implementation uses the intended component/package ownership.
- [ ] State invariants and failure paths are handled.
- [ ] Tests are meaningful and results are supported by actual evidence.
- [ ] Unrun/flaky tests and device limitations are visible.
- [ ] Platform and manifest changes are justified.
- [ ] Privacy, data retention, and dependency impacts are acceptable.
- [ ] Documentation reflects actual behavior.
- [ ] No unrelated generated files, secrets, or user edits were modified.
- [ ] Any unresolved high-impact decisions are appropriately blocked or explicitly accepted.
Reviewer: [FILL IN]
Review result: Approved / Changes Requested / Rejected
Date: [FILL IN]
Comments: [FILL IN]
14. Task Template Approval
- Product owner: Anuj Pawar (@anujp125)
- Template status: Draft / Approved
- Preferred task directory: docs/development/tasks/
- Review date: 2026-10-29