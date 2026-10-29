AssistX — Change Control and Scope Governance
Document ID: NUDGE-CHANGE-001
Version: 0.1
Status: Draft — owner review required
Last updated: 2026-10-29
Related documents: AGENTS.md, AI_AGENT_WORKFLOW.md, DECISION_LOG.md, ARCHITECTURE_DECISIONS.md, SRS.md, MVP_SCOPE.md, SYSTEM_ARCHITECTURE.md, SPRINT_PLAN.md, REQUIREMENTS_TRACEABILITY.md, DEFINITION_OF_DONE.md
Mandatory instruction for AI agents: This document governs changes that may affect scope, permissions, architecture, data/privacy, reliability, dependencies, or release behavior. An agent's ability to implement a change is not permission to make it. When approval is required, stop before implementing the change and present a bounded proposal.

1. Purpose
AssistX is being developed in small, AI-assisted coding sprints. Without explicit change controls, agents can unintentionally expand the MVP, add sensitive permissions, restructure the project unnecessarily, or alter requirements to match their implementation.

This document defines:
- changes agents may make within an approved task;
- changes that require explicit owner approval;
- how to prepare and review a change proposal;
- how to update source-of-truth documents after approval;
- how to manage emergency fixes, discovered defects, and rejected proposals;
- rules for protecting existing work and test integrity.
The product remains one installable Android application with a floating assistant and an in-app Studio/settings interface.
2. Change Types
Type A — Routine in-scope implementation
Examples:
- implementing a clearly specified function;
- adding a unit test for approved behavior;
- fixing a directly related defect without changing the intended user-visible behavior;
- correcting a typo in documentation;
- improving error handling within the approved contract.
Approval: May proceed within the assigned task if requirements and acceptance criteria are clear. The agent must still inspect the repository, test the change, and report evidence.
Type B — Behavior clarification or low-risk internal refactor
Examples:
- renaming a private class for clarity;
- extracting a small function to improve testability;
- clarifying existing wording without changing the requirement;
- changing internal implementation while preserving approved observable behavior.
Approval: May proceed only if the change is proportionate, preserves behavior, does not widen the task, and does not affect permissions, data handling, lifecycle ownership, or public interfaces. Explain material deviations from the initial plan.
Type C — Product or scope change
Examples:
- adding a new floating-button action;
- changing the approved gesture;
- adding dragging, position persistence, profiles, themes, or customization beyond the approved scope;
- changing first-run behavior;
- removing or materially weakening an approved requirement;
- changing whether a feature is included in v0.1.
Approval: Explicit product-owner approval required before implementation.
Type D — Architecture or platform change
Examples:
- creating a second installable app;
- adding a Gradle module or major framework;
- changing ownership of runtime/service lifecycle;
- introducing or changing a foreground service;
- adding a boot receiver;
- changing Accessibility integration;
- changing state semantics or recovery policy;
- changing preference/diagnostic storage design materially.
Approval: Explicit owner approval after a written technical proposal. Platform-specific claims must also be verified against current official Android documentation.
Type E — Permission, security, privacy, or external-data change
Examples:
- adding or broadening a sensitive permission;
- adding Accessibility capabilities not required by the approved action;
- adding networking, analytics, telemetry, accounts, or cloud storage;
- changing diagnostic fields or retention in a material way;
- adding Device Administrator/device-owner behavior;
- collecting app content or unrelated device information.
Approval: Explicit owner approval and documented security/privacy/permissions review required before implementation.
Type F — Release, distribution, or destructive repository operation
Examples:
- changing package/application ID for a release;
- changing signing/release configuration;
- publishing or distributing the app;
- adding/changing the license;
- deleting or overwriting unrelated files;
- resetting/cleaning a repository, rewriting Git history, merging, pushing, tagging, or releasing.
Approval: Explicit owner authorization required for the specific action. Do not infer authorization from a general request to develop the app.
3. AssistX-Specific Approval Matrix
Change	Agent may proceed independently?	Required control
Implement approved acceptance criteria in the assigned task	Yes	Task plan, tests, diff review, completion report
Add or update tests for approved behavior	Yes	Execute tests and report actual results
Fix a directly related implementation bug	Usually	Confirm scope; add regression test; report any wider impact
Add a new button action or gesture	No	Owner approval; update SRS, scope, tests, roadmap
Change the primary action	No	Owner decision and revised technical/permission analysis
Add Accessibility Service	No, unless already approved in task/ADR	Confirm approved action requires it; review configuration/policy
Add foreground service or boot receiver	No	Technical analysis, official platform verification, owner approval
Add a sensitive permission	No	Requirement mapping, least-privilege analysis, user UX, tests, approval
Add backend/networking/analytics	No	Scope, privacy/security design, architecture approval
Add major dependency/framework	No	Alternatives and maintenance/security cost review, owner approval
Add a Gradle module	No by default	Demonstrate concrete need, compare simpler options, owner approval
Change persisted data schema/retention materially	No	Data impact, migration/retention plan, tests, approval
Modify architecture/state invariants	No	ADR, affected docs/tests, explicit review
Update documentation to match an approved decision	Yes	Keep all affected docs consistent
Mark a conditional feature not applicable	No	Reference the approved decision
Delete unrelated or apparently unused files	No	Separate proposal and explicit approval
Run build/unit tests locally	Yes	Report actual results
Push, merge, publish, or distribute a release	No by default	Explicit authorization for that action


If a task's existing approved scope already authorizes a specific change, do not ask for approval a second time; reference the task or decision that provides the authorization.
4. Required Change Proposal
When a change requires approval, the agent must prepare a short, specific proposal before making the change.

Use this template:
# Change Proposal — CHG-XXX: [Title]

- Status: Proposed
- Requested by:
- Proposed by:
- Date:
- Related task/requirement:
- Change type: A / B / C / D / E / F

## Current approved behavior
[Describe the current source-of-truth behavior and reference relevant requirement IDs.]

## Proposed change
[State exactly what would change and what would remain unchanged.]

## Problem or evidence
[Explain the user problem, test failure, platform constraint, or observed evidence.]

## Options considered
1. Option A:
   - Benefits:
   - Costs/risks:
2. Option B:
   - Benefits:
   - Costs/risks:
3. Keep current behavior / defer:
   - Consequences:

## Recommendation
[Give a recommendation with rationale. This is not approval.]

## Impact assessment
- Product scope:
- Architecture/packages:
- Android permissions/platform behavior:
- Security/privacy/data:
- Battery/performance:
- Dependencies/build:
- Tests/regressions:
- Documentation:
- Schedule/dependencies (if relevant):

## Acceptance criteria
[Observable criteria that will determine whether the change is successful.]

## Approval
- Decision: Approved / Rejected / Deferred / Needs revision
- Approved option/limits:
- Approved by:
- Date:
- Decision log/ADR ID:
A proposal must state the least disruptive alternative. Do not offer a long list of speculative choices when two viable options and one recommendation are sufficient.
5. Approval Workflow
1. Identify: Agent detects the requested or necessary change.
2. Classify: Determine change type and approval requirement.
3. Gather evidence: Inspect requirements, code, tests, official platform documentation, or device observations as appropriate.
4. Propose: Write a change proposal with alternatives, trade-offs, and acceptance criteria.
5. Pause dependent work: Do not implement the approval-dependent part while waiting. Safe independent work may continue if it does not lock in the decision.
6. Decide: Product owner approves, rejects, defers, or asks for a revision.
7. Record: Update DECISION_LOG.md; update ARCHITECTURE_DECISIONS.md for architectural choices.
8. Revise source of truth: Update affected requirements, scope, architecture, roadmap, and tests after approval.
9. Implement: Add or revise sprint task(s) and execute the approved change.
10. Verify: Run tests, review the diff, and update traceability with actual evidence.
An informal agent recommendation, a generated draft, or the absence of an objection is not approval.
6. Document Update Rules
After a change is approved, update only the affected documents, but update all documents whose truth has changed.
Change affects	Documents to review/update
Product goal or user problem	PRODUCT_BRIEF.md, SRS.md
Included/excluded feature	MVP_SCOPE.md, SRS.md, PRODUCT_ROADMAP.md, SPRINT_PLAN.md
Requirement wording/behavior	SRS.md, REQUIREMENTS_TRACEABILITY.md, related acceptance tests
Component responsibilities/dependency direction	SYSTEM_ARCHITECTURE.md, MODULE_AND_PACKAGE_STRUCTURE.md, ARCHITECTURE_DECISIONS.md
State, transition, lifecycle, or recovery	STATE_AND_LIFECYCLE.md, FAILURE_AND_RECOVERY_MATRIX.md, SRS.md, tests
Permission or platform behavior	PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md, SYSTEM_ARCHITECTURE.md, privacy docs, manifest review checklist
Preference/diagnostic schema or retention	DATA_AND_DIAGNOSTICS.md, SRS.md, privacy docs, tests
Sprint order/dependencies	PRODUCT_ROADMAP.md, SPRINT_PLAN.md, task files
Test coverage/result	TEST_STRATEGY.md, ACCEPTANCE_TESTS.md, REAL_DEVICE_TEST_MATRIX.md, REQUIREMENTS_TRACEABILITY.md
Agent policy/workflow	AGENTS.md, AI_AGENT_WORKFLOW.md, SPRINT_TASK_TEMPLATE.md, DEFINITION_OF_DONE.md as applicable
Release/distribution behavior	README.md, setup/privacy/compatibility/release docs, changelog


Do not change every document mechanically when one internal implementation detail changes. Update the source documents that would otherwise become misleading or inconsistent.
7. Change Impact Classification
Before approval, assess at least these dimensions:
- Scope: Does the change add/remove user-visible behavior?
- Permissions: Does it introduce a new or broader system capability?
- Architecture: Does it alter component ownership, lifecycle, or dependency direction?
- Reliability: Does it change state meaning, retry behavior, or recovery guarantees?
- Privacy/security: Does it change collected data, storage, network, or external access?
- Compatibility: Does it affect minimum SDK, target SDK, API behavior, or device support?
- Testing: What automated, instrumentation, and physical-device verification is required?
- Maintenance: Does it add a dependency, build step, framework, or ongoing operational obligation?
A change may be high impact even if it takes only a few lines of code. For example, a new permission or a different service type can materially alter policy, behavior, and distribution.
8. Defect Fixes vs. Scope Changes
A defect fix restores already-approved behavior to its intended state. It does not automatically authorize unrelated improvements.

For a bug fix:
1. capture reproduction steps or explain why the failure is intermittent;
2. link the affected requirement and failure ID;
3. make the smallest safe fix;
4. add a regression test where feasible;
5. run applicable checks;
6. document any remaining uncertainty.
If the apparent fix requires a new permission, changes the primary action, adds a service, or alters approved product behavior, treat that portion as a change proposal.

Do not change an acceptance criterion to make a failing implementation pass. If the criterion itself is wrong, propose a requirements change for review.
9. Emergency and Security-Related Changes
“Emergency” does not mean unrestricted scope.

If a defect risks data loss, privacy violation, permission misuse, repeated restart loops, or severe battery drain:
- stop or disable the unsafe behavior when feasible;
- preserve evidence needed to diagnose the problem;
- make a narrowly scoped mitigation if authorized by the task and safe to do so;
- notify the owner with severity and impact;
- do not introduce a new sensitive permission or broad architecture change as an unreviewed shortcut;
- document any temporary mitigation, its limits, and the required follow-up;
- test the mitigation and a regression path.
If immediate action is not clearly authorized or may cause destructive effects, stop and request owner direction rather than making irreversible changes.
10. Rejected or Deferred Changes
For a rejected or deferred proposal:
- record the decision and rationale;
- do not implement the rejected option;
- leave the current approved behavior in place unless a separate defect requires a change;
- add a backlog item only if the owner wants the idea retained;
- link the proposal to the decision log and mark its status accurately.
Do not repeatedly re-propose the same rejected change unless material new evidence or requirements have changed.
11. Protecting the Repository
Before any code change:
- inspect git status;
- identify existing user modifications;
- avoid resetting, cleaning, mass-reformatting, or overwriting unrelated files;
- keep changes focused on the assigned task.
Explicit approval is required for destructive or externally visible operations such as deleting unrelated files, rewriting Git history, force-pushing, merging, tagging a release, publishing artifacts, or distributing a build.

A request to “develop the app” does not itself authorize release or publication.
12. Review Checklist
Before accepting a change proposal, verify:
- [ ] The current approved behavior is identified and linked to requirements.
- [ ] The problem or evidence is specific.
- [ ] The smallest reasonable alternatives are considered.
- [ ] Product, architecture, permission, privacy, battery, compatibility, and test impacts are addressed.
- [ ] The recommendation is clearly distinguished from owner approval.
- [ ] Acceptance criteria are measurable.
- [ ] Required documents and decisions are identified.
- [ ] Required approvals are recorded before implementation.
- [ ] The sprint backlog is updated after approval.
- [ ] Tests and traceability are updated after implementation.
13. Current High-Impact Open Changes
These are not approved changes. They identify decisions that must be resolved before dependent implementation:
ID	Open decision/change area	Current status	Blocks
CHG-OPEN-01	Exact primary floating action and gesture	Open	Action implementation and permission choice
CHG-OPEN-02	Minimum/target Android SDK strategy	Open / technical verification	Final build and platform-specific behavior
CHG-OPEN-03	UI toolkit	Open	Final UI implementation
CHG-OPEN-04	Whether a foreground service is genuinely necessary	Needs technical analysis	Runtime persistence architecture
CHG-OPEN-05	Whether reboot auto-start is required	Open	Boot receiver/startup implementation
CHG-OPEN-06	Retry/cooldown policy	Needs engineering proposal	Production recovery code
CHG-OPEN-07	Diagnostics retention/storage	Open	Final diagnostics storage and release readiness
CHG-OPEN-08	Distribution method and app identity	Open	Release/signing and any store-specific compliance


14. Approval
- Product owner: Anuj Pawar (@anujp125)
- Status: Draft / Approved
- Approval for change classes requiring explicit consent: Explicit owner consent required for scope, permissions, or architecture changes
- Review date: 2026-10-29
- Notes: Approved change control process.
This policy applies to all agents and contributors. If another instruction conflicts with it, follow the higher-priority approved requirements and security/platform constraints, and raise the conflict rather than silently bypassing approval.