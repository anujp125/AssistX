AssistX — Definition of Done
Document ID: ASSISTX-DOD-001
Version: 0.1
Status: Draft — owner review required
Last updated: 2026-10-29
Related documents: AGENTS.md, AI_AGENT_WORKFLOW.md, SPRINT_TASK_TEMPLATE.md, SPRINT_PLAN.md, TEST_STRATEGY.md, ACCEPTANCE_TESTS.md, REAL_DEVICE_TEST_MATRIX.md
Mandatory instruction for AI agents: “Done” means the defined completion conditions have been demonstrated with evidence. It does not mean the code was merely written or appears correct. Never mark an unexecuted test as passed, never hide blockers, and never expand scope to make a task look more complete.

1. Purpose
This document defines when a AssistX development task, sprint, milestone, and release can be considered complete.

It creates a consistent quality gate for a small project developed with AI coding agents. The purpose is not to add bureaucracy: it is to prevent incomplete features, misleading test reports, unapproved scope changes, and unreliable lifecycle behavior from being treated as finished.
2. Core Rules
1. Requirements first: The change must satisfy the assigned, approved requirement and its acceptance criteria.
2. Scope remains bounded: No unapproved features, permissions, dependencies, services, architecture changes, or unrelated refactors.
3. Tests are part of the task: Relevant tests are added or updated with behavior changes.
4. Evidence must be real: Only executed checks can be reported as passed.
5. Android behavior needs appropriate verification: Unit tests do not prove physical-device lifecycle, permission, or manufacturer behavior.
6. State must be truthful: Desired enabled state is not proof of runtime health.
7. Recovery must be bounded: No unbounded retries, restart loops, or attempts to bypass user/system controls.
8. Documentation follows approved behavior: Do not change requirements merely to make an accidental implementation appear compliant.
9. Unknown means unknown: Do not invent a root cause or imply a test result that the available evidence cannot establish.
10. Completion and acceptance are distinct: An agent can finish implementation and request review; a task is not accepted until its required review and verification gates are met.
3. Task Definition of Done
A task can move to Review Required only when every applicable item below is satisfied or explicitly identified as blocked/not applicable with a reason.
3.1 Scope and requirements
- [ ] The task objective and acceptance criteria are explicit.
- [ ] The implementation addresses the approved requirement IDs.
- [ ] All applicable acceptance criteria have a result or an explicit exception.
- [ ] The implementation stays within the task's in-scope work.
- [ ] Out-of-scope behavior has not been added as a bonus.
- [ ] Unresolved owner decisions have not been silently guessed.
- [ ] Requirement/test traceability is updated when required.
3.2 Code and architecture
- [ ] The agent inspected the existing repository and current working changes before editing.
- [ ] The change is located in the appropriate component/package.
- [ ] The implementation is no more complex than needed for the task.
- [ ] There are no unrelated refactors, mass formatting changes, or file moves.
- [ ] Existing user changes and unrelated files remain intact.
- [ ] Error paths are handled intentionally; important failures are not silently swallowed.
- [ ] New abstractions, modules, or dependencies are justified by a concrete need.
- [ ] The diff has been reviewed in full.
3.3 AssistX reliability invariants
For tasks that touch the corresponding area, verify:
- [ ] Desired enabled state is distinct from observed runtime health.
- [ ] Overlay permission is distinct from actual overlay attachment.
- [ ] Overlay state is distinct from Accessibility state.
- [ ] Accessibility enabled and connected are distinct where relevant.
- [ ] UNKNOWN or an equivalent uncertain state remains available when evidence is insufficient.
- [ ] An operation is not reported successful before suitable confirmation.
- [ ] Explicit user disablement suppresses automatic recovery.
- [ ] Start/stop and recovery operations are safe to repeat where practical.
- [ ] A single overlay instance is maintained.
- [ ] Retry behavior is bounded and does not create an uncontrolled loop.
- [ ] Stale persisted runtime state is not used as proof of current health after process recreation.
- [ ] Android permissions and user/system stopping controls are respected.
Items that do not apply to the task should be marked N/A with a brief reason in the task report, not silently skipped.
3.4 Permissions, platform behavior, security, and privacy
- [ ] Every manifest/permission/service/receiver change maps to an approved requirement.
- [ ] No sensitive capability is added without approval.
- [ ] Target-SDK- or OS-specific rules were verified where relevant.
- [ ] No unsupported background-start, reboot, force-stop, or keep-alive behavior is claimed.
- [ ] No unapproved networking, backend, cloud storage, analytics, or telemetry is added.
- [ ] No secrets, credentials, unnecessary personal data, local databases, or private logs are committed.
- [ ] Diagnostic data is minimized and subject to the documented retention policy where implemented.
- [ ] Any privacy/security implications are documented for review.
3.5 Testing
- [ ] Relevant unit tests are added or updated.
- [ ] Relevant instrumentation/device tests are identified.
- [ ] Relevant build, lint, or static checks are run when available.
- [ ] The exact commands or manual steps are recorded.
- [ ] Test results are reported accurately.
- [ ] Failures are fixed or recorded with a tracking issue and impact.
- [ ] Flaky tests are not silently removed or weakened.
- [ ] Tests that were not executed are marked NOT RUN or BLOCKED with a reason.
- [ ] Physical-device claims are supported by actual physical-device evidence.
A successful build is necessary when the task changes code, but it is not by itself sufficient for a behavior task.
3.6 Documentation and reporting
- [ ] Relevant source-of-truth documents are updated if approved behavior/design/test coverage changed.
- [ ] New or changed requirements are traceable to verification.
- [ ] Important decisions are recorded in DECISION_LOG.md and/or ARCHITECTURE_DECISIONS.md when applicable.
- [ ] The task completion report is filled in.
- [ ] Remaining limitations, blockers, and follow-up tasks are explicit.
Task status meanings
- In Progress: Work is underway.
- Blocked: A decision, environment, or dependency prevents safe progress.
- Review Required: Implementation and self-checks are complete; review is pending.
- Passed: Applicable acceptance and review criteria have been met.
- Deferred: The work was deliberately postponed with a reason.
- Rejected: The proposed work was not accepted.
A task must not move to Passed if a required acceptance test is still failing or unrun, unless the designated owner explicitly accepts a documented exception.
4. Sprint Definition of Done
A sprint can be marked Review Required only when:
- [ ] Every task planned for the sprint has a recorded status.
- [ ] Tasks reported as complete have met the task-level Definition of Done.
- [ ] Sprint acceptance criteria and exit gate from SPRINT_PLAN.md are checked.
- [ ] Relevant regression tests have been run.
- [ ] Dependencies for the next sprint are stable enough to proceed.
- [ ] Known failures and risks are documented.
- [ ] The backlog reflects the actual implementation state.
- [ ] No hidden scope expansion was introduced.
- [ ] Required docs and traceability are updated.
- [ ] The sprint summary states what passed, failed, was blocked, and was not run.
Sprint acceptance
A sprint is Passed when its exit criteria have evidence and its required reviewer/owner acceptance is recorded.

If one task is deferred, the sprint may only pass if:
- the task is not a required dependency or exit criterion;
- the impact is understood;
- the owner or designated reviewer accepts the deferral;
- the task remains in the backlog with a reason.
Do not mark the entire sprint passed by averaging test results or counting completed tasks.
5. Milestone Definition of Done
A milestone can be considered complete only when:
- [ ] Every milestone exit criterion in PRODUCT_ROADMAP.md and SPRINT_PLAN.md has evidence.
- [ ] Required tasks are Passed or have an explicitly accepted exception.
- [ ] The milestone's test scope has been executed.
- [ ] Its deliverables exist and describe actual behavior.
- [ ] Open issues that affect later milestones have been evaluated.
- [ ] Architectural or permission-sensitive decisions are recorded.
- [ ] The next milestone's prerequisites are satisfied.
- [ ] The owner/reviewer has accepted the milestone where required.
A milestone is not complete merely because all planned files exist.
6. Physical-Device and Reliability Definition of Done
For milestones involving the overlay, permissions, Accessibility, lifecycle, recovery, or background behavior:
- [ ] Relevant tests were run on the recorded target device/build.
- [ ] The app version/commit and initial permission states are recorded.
- [ ] The test method distinguishes process recreation, force-stop, reboot, and permission revocation.
- [ ] Expected and actual results are recorded for each applicable case.
- [ ] Intermittent failures include timestamps, reproduction steps, and available diagnostics.
- [ ] Suspected causes are separated from confirmed causes.
- [ ] Recovery behavior respects desired state and platform restrictions.
- [ ] The test report documents scenarios that could not be reliably induced.
- [ ] No unsupported guarantee of always-on operation is made.
- [ ] Battery, memory, log growth, and long-duration measurements are recorded when required by the approved plan.
If a target-device test cannot be performed because the device is unavailable, the related work is not device-verified. It must remain BLOCKED/`NOT RUN` or be explicitly excepted by the owner; an emulator result is not a substitute for evidence about Realme-specific behavior.
7. Release Candidate Definition of Done
A v0.1 release candidate can be submitted for owner acceptance only when all applicable items below are satisfied.
Product and scope
- [ ] Product scope and primary action are approved.
- [ ] The product remains one installable application.
- [ ] No excluded feature or unapproved permission is present.
- [ ] Known limitations are visible.
Build and distribution
- [ ] Application ID and distribution method are confirmed.
- [ ] Release build can be produced with documented instructions.
- [ ] App can be installed and launched using the documented procedure.
- [ ] Signing material and secrets are stored safely and excluded from source control.
- [ ] Required package/version metadata is correct.
- [ ] Public-distribution policy and licensing requirements have been reviewed if applicable.
Tests and defects
- [ ] Core acceptance tests have been executed.
- [ ] Required automated checks pass.
- [ ] Required physical-device tests are completed or the owner has accepted explicit exceptions.
- [ ] No unresolved critical defect blocks the approved core workflow.
- [ ] High-severity defects have been fixed or explicitly accepted with documented risk.
- [ ] Flaky and unrun tests are listed honestly.
Operations, privacy, and documentation
- [ ] Installation/setup instructions match the actual build.
- [ ] Recovery and troubleshooting instructions are based on verified behavior.
- [ ] Permissions are explained and minimized.
- [ ] Local data handling and diagnostic retention are documented.
- [ ] Compatibility statements are limited to tested or justified configurations.
- [ ] Changelog and version are updated.
- [ ] Release checklist is complete.
Owner sign-off
- [ ] The owner reviewed the build and remaining risks.
- [ ] Distribution is explicitly approved.
- [ ] The approved artifact/version/commit is recorded.
8. Required Evidence for a “Done” Claim
Every code task completion report must include:
Task ID:
Objective:
Requirement IDs:
Files changed:
Acceptance criteria met:
Tests run (exact commands/manual steps):
Results:
Tests not run or blocked:
Build/lint/static-check results:
Device/OS/build and app commit (if applicable):
Permission/manifest/dependency changes:
Documentation updated:
Known limitations:
Open risks/bugs:
Scope confirmation:
Review status:
Evidence must be proportional to the risk:
- A pure text/documentation change may need review and documentation checks.
- A pure state-mapping change needs unit-test output.
- An overlay implementation needs Android/platform checks and physical-device validation.
- A release candidate needs the complete applicable release checklist and owner approval.
9. Exceptions
An exception does not silently change the definition of done. It is a recorded risk acceptance.

An exception must include:
- requirement/test/milestone affected;
- why the criterion cannot currently be met;
- likely user and technical impact;
- available workaround;
- remediation/follow-up task and priority;
- who accepted the risk and on what date;
- whether the exception affects distribution.
AI agents may recommend an exception but may not approve product-level risk on the owner's behalf.
10. Anti-Patterns That Are Never “Done”
- Writing code without tests for changed behavior when tests are feasible.
- Reporting a build as proof of correct runtime behavior.
- Marking physical-device tests passed without using the device.
- Silently skipping flaky tests.
- Adding unapproved permissions or features.
- Persisting and restoring a stale ACTIVE flag.
- Treating overlay availability and Accessibility connection as the same state.
- Adding unbounded retries or keep-alive loops.
- Hiding unknown causes behind a confident explanation.
- Leaving known failures out of the report.
- Updating requirements to disguise a behavior mismatch.
- Declaring a sprint or milestone complete without its exit criteria.
- Publishing or distributing a release without owner approval.
11. Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Status: Draft / Approved
- Required reviewer(s): Anuj Pawar (@anujp125)
- Approved exception policy: Explicitly documented exception note linked to decision record
- Review date: 2026-10-29
- Notes: Approved Definition of Done.
This document should be referenced by AGENTS.md, AI_AGENT_WORKFLOW.md, SPRINT_TASK_TEMPLATE.md, and SPRINT_PLAN.md. Keep those documents consistent if this Definition of Done is changed.