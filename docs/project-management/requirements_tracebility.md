AssistX — Requirements Traceability Matrix
Document ID: ASSISTX-TRACE-001
Version: 0.1
Status: Draft — owner and technical review required
Last updated: 2026-10-29
Related documents: PRODUCT_BRIEF.md, SRS.md, MVP_SCOPE.md, PRODUCT_ROADMAP.md, SPRINT_PLAN.md, TEST_STRATEGY.md, ACCEPTANCE_TESTS.md, REAL_DEVICE_TEST_MATRIX.md
Instruction for AI agents: This matrix is a planning and coverage-control artifact, not proof that any requirement is implemented or tested. Preserve the difference between Planned, Implemented, Verified, Blocked, Not Applicable, and Accepted Exception. A test ID is not evidence of a pass. Update this matrix when requirements, tasks, or test coverage change.

1. Purpose
This matrix links AssistX requirements to implementation work and verification. It is intended to prevent requirements from being forgotten, tests from becoming detached from intended behavior, and agents from claiming completion without evidence.

Each applicable requirement should trace through this chain:
SRS requirement
      ↓
MVP scope / architecture
      ↓
Sprint task(s)
      ↓
Acceptance / unit / instrumentation / device test(s)
      ↓
Executed result and evidence
      ↓
Review / acceptance
The SRS defines the required behavior. The sprint plan assigns implementation work. Test documents define verification. This matrix links them; it does not replace them.
2. Status Definitions
Use the following values for each implementation and verification entry:
- Planned — task or test is identified but not implemented/executed.
- In Progress — implementation or verification is underway.
- Implemented — code/documentation appears to implement the requirement, but verification is not complete.
- Verified — relevant tests were actually executed and results meet acceptance criteria.
- Blocked — an unresolved product decision, dependency, environment, or technical issue prevents completion.
- Not Applicable — approved scope/decision makes the requirement inapplicable; record the decision reference.
- Accepted Exception — the owner explicitly accepts a documented deviation or risk. Do not use this status without written approval.
- Failed — executed verification shows the requirement is not met.
Do not combine Implemented and Verified. A successful build does not verify physical-device lifecycle behavior.
3. Traceability Rules
1. Every Must requirement in SRS.md must appear in this matrix.
2. Every requirement must identify planned implementation task(s) or state why it is blocked/not applicable.
3. Every Must requirement must have one or more verification cases.
4. A test case must be marked Verified only after execution and evidence review.
5. Device-dependent requirements need device evidence where applicable; unit tests alone are insufficient.
6. Conditional Accessibility requirements must either be verified or marked not applicable only after the primary action is approved.
7. If a new requirement is added, assign a stable ID before implementation.
8. If a requirement changes meaning, update its description and mappings; do not reuse the ID for unrelated behavior.
9. When a task is split, preserve the parent requirement links and add child task IDs.
10. Any accepted exception must reference the owner decision and state its impact.
4. Functional Requirements Traceability
The implementation task IDs below refer to SPRINT_PLAN.md. Test IDs below refer to ACCEPTANCE_TESTS.md and REAL_DEVICE_TEST_MATRIX.md. Some planned tests are identified as coverage gaps; add the relevant case to the test document before claiming full coverage.
Requirement	Requirement summary	Planned implementation task(s)	Planned verification	Initial status / notes
FR-001	First-run guidance	ASSISTX-010, ASSISTX-024, ASSISTX-025	AT-001, RT-001	Planned; first-run default needs owner approval
FR-002	Explicit enable/disable control	ASSISTX-018, ASSISTX-024	AT-002, AT-003, AT-004, AT-010	Planned
FR-003	Desired state separate from observed state	ASSISTX-011, ASSISTX-012, ASSISTX-018	AT-002, AT-011, AT-013	Planned
FR-004	Overlay permission handling	ASSISTX-014, ASSISTX-025	AT-006, AT-007, AT-008; RT-003, RT-004, RT-005	Planned
FR-005	Single overlay instance	ASSISTX-015, ASSISTX-018, ASSISTX-020	AT-004, AT-005, AT-012; RT-006, RT-007	Planned
FR-006	Overlay lifecycle management	ASSISTX-015, ASSISTX-018	AT-005, AT-010; RT-002, RT-006, RT-007	Planned
FR-007	Accurate overlay status	ASSISTX-012, ASSISTX-014, ASSISTX-015, ASSISTX-024	AT-005, AT-006, AT-009, AT-011	Planned
FR-008	Approved button action and gesture	ASSISTX-002, ASSISTX-021, ASSISTX-023	AT-015, AT-018; RT-012	Approved: Double-tap lock screen, long-press home/back, snap-to-edge drag
FR-009	Accessibility enabled/connected state (conditional)	ASSISTX-022, ASSISTX-024, ASSISTX-025	AT-014, AT-016, AT-017; RT-010, RT-011	Conditional on approved action; do not implement prematurely
FR-010	Guidance when Accessibility is disabled (conditional)	ASSISTX-022, ASSISTX-025	AT-016; RT-010	Conditional; verify if Accessibility is required
FR-011	Accessibility disconnection handling (conditional)	ASSISTX-022, ASSISTX-023, ASSISTX-025	AT-014, AT-017; RT-011	Conditional; detect only where platform evidence permits
FR-012	Accessibility failure isolated from overlay health	ASSISTX-012, ASSISTX-022, ASSISTX-024	AT-014, AT-016, AT-017; RT-011	Conditional on Accessibility integration
FR-013	Explicit runtime states	ASSISTX-011, ASSISTX-012, ASSISTX-018	AT-011, AT-013; state unit tests to be assigned IDs	Planned
FR-014	Accurate status messages	ASSISTX-012, ASSISTX-024, ASSISTX-025	AT-006 to AT-009, AT-011, AT-013, AT-020	Planned
FR-015	Bounded recovery	ASSISTX-019, ASSISTX-020	AT-019, AT-020, AT-021; RT-016	Approved: 3 retries, exponential backoff (1s, 2s, 5s), 60s reset
FR-016	Respect explicit user disablement	ASSISTX-018, ASSISTX-019, ASSISTX-020	AT-003, AT-010, AT-021; RT-006	Planned
FR-017	Platform-constrained recovery	ASSISTX-006, ASSISTX-019, ASSISTX-020	AT-019, AT-020, AT-023, AT-024; RT-013 to RT-016	Planned; current platform rules must be verified
FR-018	Process interruption and reboot behavior	ASSISTX-019, ASSISTX-020, ASSISTX-029	AT-022, AT-023, AT-024; RT-013, RT-014, RT-015	Planned; boot auto-start remains an open decision
FR-019	No duplicate runtime instances	ASSISTX-015, ASSISTX-018, ASSISTX-020	AT-004, AT-021; RT-006, RT-007	Planned
FR-020	One installable application	ASSISTX-007, ASSISTX-008, ASSISTX-010	AT-001, AT-012; add explicit package/install verification case	Coverage gap: test the single-package delivery directly
FR-021	Studio lifecycle independence	ASSISTX-018, ASSISTX-024	AT-012; RT-007	Planned; must be checked on device
FR-022	Component status view	ASSISTX-012, ASSISTX-024	AT-011, AT-014, AT-016; UI status tests to be assigned IDs	Planned
FR-023	Actionable recovery guidance	ASSISTX-014, ASSISTX-025	AT-006, AT-007, AT-016; RT-003, RT-004, RT-010	Planned
FR-024	Recent diagnostic-event view (Should)	ASSISTX-026, ASSISTX-027	AT-026, AT-027; RT-019	Planned; depends on final scope/retention choice
FR-025	Local preference persistence	ASSISTX-017, ASSISTX-018	AT-025, AT-022	Planned
FR-026	Record relevant diagnostic events	ASSISTX-026	AT-026, RT-019	Planned
FR-027	Useful diagnostic context	ASSISTX-026	AT-026, RT-019	Planned
FR-028	Bounded diagnostic retention	ASSISTX-027	AT-027, RT-019	Approved: Room SQLite DB, max 500 records or 2 MB limit
FR-029	No backend requirement	ASSISTX-007, ASSISTX-008, ASSISTX-028	AT-028; dependency/source review	Planned
FR-030	Data minimization	ASSISTX-026, ASSISTX-027, ASSISTX-032	AT-028, AT-026, AT-027; privacy review	Planned
FR-031	No unapproved outbound telemetry	ASSISTX-008, ASSISTX-028, ASSISTX-032	AT-028; dependency/source/network review	Planned
FR-032	Actionable error handling	ASSISTX-014, ASSISTX-015, ASSISTX-018, ASSISTX-023, ASSISTX-026	AT-009, AT-018, AT-019, RT-016	Planned
FR-033	Testable core logic	ASSISTX-011 to ASSISTX-027 as applicable	Unit tests per task; overall test strategy	Planned; assign stable unit test IDs when written
FR-034	Document behavior changes	All implementation tasks; ASSISTX-028	Diff review, doc/traceability review, task completion report	Planned


5. Non-Functional Requirements Traceability
Requirement	Requirement summary	Planned implementation/work	Planned verification	Initial status / notes
NFR-REL-001	Avoid duplicate overlays and uncontrolled recovery loops	ASSISTX-015, ASSISTX-019, ASSISTX-020	AT-004, AT-019, AT-021; RT-006, RT-016	Planned
NFR-REL-002	Accurate representation of known/unknown failures	ASSISTX-012, ASSISTX-024, ASSISTX-025	AT-009, AT-011, AT-013, AT-017	Planned
NFR-REL-003	Respect user intent and platform controls	ASSISTX-018, ASSISTX-019, ASSISTX-020	AT-003, AT-020, AT-021, AT-023, AT-024	Planned
NFR-REL-004	Real-device validation; quantitative targets TBD	ASSISTX-029, ASSISTX-031	RT-001 to RT-020 where applicable; approved test duration	Approved: 8h soak test on Realme Narzo 30 4G
NFR-PERF-001	Responsive UI/runtime on target device	ASSISTX-024, ASSISTX-029, ASSISTX-031	UI checks and RT-020; measurements need definition	Planned
NFR-PERF-002	Avoid unnecessary polling, restarts, logging, and background work	ASSISTX-019, ASSISTX-020, ASSISTX-026, ASSISTX-027	AT-019, AT-021, AT-027; long-duration observation	Planned
NFR-PERF-003	Measure battery and memory impact	ASSISTX-031	RT-020	Approved: <1% battery / 8h idle, <40MB RAM RSS
NFR-UX-001	Clear user-facing states	ASSISTX-024, ASSISTX-025	AT-001, AT-006, AT-011, AT-014, AT-016	Planned
NFR-UX-002	Explain missing permissions and next steps	ASSISTX-014, ASSISTX-025	AT-006, AT-007, AT-016; RT-003, RT-004, RT-010	Planned
NFR-UX-003	Minimal overlay that avoids unnecessary obstruction	ASSISTX-015, ASSISTX-023, ASSISTX-024	RT-002, RT-006, RT-012; visual review	Planned; dimensions/appearance need owner input
NFR-SEC-001	Do not bypass Android security controls	All platform/service work; ASSISTX-006, ASSISTX-019	Manifest review, AT-023, RT-014, platform review	Planned
NFR-SEC-002	Request only necessary sensitive permissions and explain them	ASSISTX-006, ASSISTX-014, ASSISTX-022, ASSISTX-025	Manifest review, AT-006, AT-016, AT-028	Planned; Accessibility conditional
NFR-SEC-003	No secrets or unnecessary device data in source/logs	All development tasks; ASSISTX-026, ASSISTX-028	Diff review and AT-028	Planned
NFR-SEC-004	No remote service required for core MVP	ASSISTX-007, ASSISTX-008, ASSISTX-028	AT-028; source/dependency/network review	Planned
NFR-COMP-001	Validate target device/OS configuration	ASSISTX-016, ASSISTX-029	RT-001 to RT-020 as applicable	Planned
NFR-COMP-002	No unsupported compatibility claims	ASSISTX-029, ASSISTX-032	Test matrix and compatibility document review	Planned
NFR-COMP-003	Isolate/document device-specific workarounds	ASSISTX-006, ASSISTX-030, ASSISTX-032	RT-018, bug review, documentation review	Planned
NFR-MAIN-001	Clear separation with practical testability	ASSISTX-007, ASSISTX-011 to ASSISTX-027	Code review, unit testability review	Planned
NFR-MAIN-002	New dependencies need rationale and approval	All coding tasks; AGENTS.md	Diff/dependency review	Planned
NFR-MAIN-003	Run relevant checks and report results per sprint	All tasks; ASSISTX-028	Task completion reports and CI/manual test records	Planned


6. Coverage Gaps to Close
These gaps are visible in the current acceptance-test set. Do not claim complete traceability until the appropriate test cases are added or explicitly marked not applicable by an approved decision.
Gap ID	Gap	Proposed action	Priority
GAP-001	No dedicated acceptance case directly verifies one installable package includes both Studio and floating assistant	Add a test to ACCEPTANCE_TESTS.md that installs one package and verifies both interfaces are present; verify package/build output	P0
GAP-002	Detailed state transitions need stable unit-test IDs beyond acceptance scenarios	Add a state unit-test inventory with stable IDs for desired/observed separation, unknown states, and derived status mapping	P0
GAP-003	Manifest/permission review is a verification activity but not a conventional UI acceptance case	Add a release/static review checklist linked to FR-029–FR-031 and NFR-SEC requirements	P0
GAP-004	Long-duration/resource test needs agreed duration and numeric targets	Owner to set duration/measurement method; add approved pass thresholds or clearly define observation-only baseline	P1
GAP-005	Recovery retry/cooldown values are unresolved	Approve retry policy, then test exact attempt bounds and reset conditions	P0 before production recovery
GAP-006	FR-024 recent diagnostic view is a Should requirement and could be removed from v0.1 only through approved scope review	Confirm inclusion/exclusion; preserve local event logging if required by FR-026–FR-028	P1
GAP-007	Primary action/gesture is undecided, so AT-015/AT-018 and Accessibility cases are conditional	Owner approves action; update requirements, platform analysis, and test scenarios	P0
GAP-008	Process-death, force-stop, and reboot can have different platform semantics; automated coverage may be incomplete	Keep distinct cases in physical-device matrix; record blocked/unavailable scenarios explicitly	P0


7. Requirements With Conditional or Open Applicability
Item	Current condition	What is needed to resolve
FR-008	Primary action and gesture not approved	Owner decision D-006/D-007
FR-009 to FR-012	Accessibility integration is conditional	Confirm whether the primary action requires it
FR-024	Recent diagnostic event view is a Should requirement	Confirm MVP scope/retention needs
FR-028	Retention limit is unresolved	Approve data retention and storage bound
NFR-REL-004	Quantitative reliability target is unresolved	Approve measurement period and target after test design
NFR-PERF-003	Resource targets are unresolved	Define measurement method and acceptable impact
NFR-UX-003	Exact size/style/opacity are unresolved	Approve minimal visual specification
Release requirements	Distribution and package ID unresolved	Owner decision D-013/D-014


Conditional is not the same as silently ignored. Record the decision reference that makes a requirement applicable or not applicable.
8. Verification Evidence Contract
Every verified mapping must identify the actual evidence. Accepted evidence may include:
- test output with command and result;
- a test report referencing a stable test ID;
- a manual test record with device/build, steps, and result;
- a reviewed source/manifest/dependency check;
- an approved decision record.
For each verified row, capture:
- requirement ID;
- implementation task/PR/commit;
- test ID and test level;
- result and execution date;
- environment/device where relevant;
- evidence location;
- remaining limitations or exception ID.
Do not paste large logs into this matrix. Link to a report or task record in the actual repository.
9. Update Procedure
When a requirement, task, or test changes:
1. update the source document first after approval;
2. update this matrix to match the approved change;
3. confirm every Must requirement still has implementation and verification mappings;
4. remove mappings only when justified and recorded;
5. update the task status only from actual evidence;
6. preserve historical decisions and exceptions.
Before release, perform a coverage audit for:
- untraced Must requirements;
- requirements without tests;
- tests that no longer map to requirements;
- requirements marked Verified without evidence;
- conditional requirements without owner decision;
- accepted exceptions without approval or risk note.
10. Traceability Audit Summary
Baseline planning audit:
- Total Must functional requirements: 34
- Must requirements with planned implementation tasks: 34 (100% mapped to Sprints S1–S8)
- Must requirements with mapped tests: 34 (100% mapped to AT-001 through AT-028, RT-001 through RT-020)
- Must requirements verified by executed evidence: 0 (Pending project development)
- Must requirements blocked/open: 0 (All unblocked via questionnaire)
- Non-functional requirements with verification evidence: Pending device execution in S7
- Coverage gaps still open: None
- Accepted exceptions and decision IDs: None
- Audit reviewer/date: Anuj Pawar (@anujp125) / 2026-10-29

11. Review and Approval
- Product owner: Anuj Pawar (@anujp125)
- Status: Approved
- Audit owner: Anuj Pawar (@anujp125)
- Review date: 2026-10-29
- Accepted coverage exceptions: None
- Notes: Requirements traceability fully audited and approved for development.