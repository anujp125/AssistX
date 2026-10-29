AssistX
AssistX is a reliability-first, native Android assistive app. The current plan is to deliver one installable application containing a floating assistant and an in-app Studio/settings interface.
Project status: Planning / documentation draft. This repository's Android project, package ID, minimum SDK, UI toolkit, and build commands have not yet been verified. Do not interpret the documentation as evidence that an app has already been implemented or tested.

1. Product Goal
AssistX aims to provide a minimal floating button with clear runtime status and practical recovery guidance. It is designed to distinguish:
- the user's desired enabled/disabled state;
- runtime health;
- overlay permission and actual overlay attachment;
- Accessibility enabled and connected state, if Accessibility is required by the approved primary action.
The existing app's intermittent floating-button and Accessibility problems are observations; their root cause has not been confirmed. AssistX must gather useful evidence rather than assume that every failure is caused by Android or Realme background management.
Initial target device
The product owner has reported using a Realme Narzo 30 4G (RMX2156) running Android 12 / realme UI 3.0, build RMX2156_11_F.19. Verify the device and build at test time. This is the initial test environment, not a claim of broad compatibility.
2. MVP Principles
- One installable app; Studio and floating assistant are logical components, not separate apps.
- Reliability, diagnosability, and predictable lifecycle behavior before extra features.
- No backend or cloud dependency for the MVP.
- Permissions and services only when justified by approved requirements.
- Recovery must be bounded and respect Android's controls.
- The app must not claim success before there is evidence.
- User disablement must suppress automatic recovery.
- Physical-device behavior must be tested before device-specific claims are made.
Explicitly out of scope for initial v0.1 unless approved
Volume controls, Wi-Fi/Bluetooth toggles, cloud sync, accounts, backend services, advanced gesture customization, a separate Studio app, and attempts to bypass Android permission or background-execution restrictions.

The primary floating-button action and gesture are still awaiting owner approval. “Double-tap to turn the screen off” is only a candidate, not a finalized requirement.
3. Start Here: Read the Source of Truth
Read the following before changing code. The files are being generated as a controlled documentation set; keep each document's Draft/Approved status accurate.
Order	Document	What it governs
1	AGENTS.md	Mandatory AI coding-agent guardrails and workflow
2	docs/01-product/PRODUCT_BRIEF.md	Product vision, goals, user/device context
3	docs/01-product/SRS.md	Testable functional and non-functional requirements
4	docs/01-product/MVP_SCOPE.md	Included and excluded v0.1 functionality
5	docs/01-product/PRODUCT_ROADMAP.md	Milestones, dependencies, and exit gates
6	docs/02-architecture/SYSTEM_ARCHITECTURE.md	Component responsibilities and data/control flow
7	docs/02-architecture/MODULE_AND_PACKAGE_STRUCTURE.md	Kotlin package and repository structure
8	docs/02-architecture/STATE_AND_LIFECYCLE.md	State domains, transitions, and lifecycle invariants
9	docs/02-architecture/PERMISSIONS_AND_PLATFORM_CONSTRAINTS.md	Android permissions and platform constraints
10	docs/02-architecture/FAILURE_AND_RECOVERY_MATRIX.md	Failure detection, handling, and permitted recovery
11	docs/03-ai-development/AI_AGENT_WORKFLOW.md	Standard agent execution process
12	docs/03-ai-development/SPRINT_TASK_TEMPLATE.md	Template for one bounded implementation task
13	docs/03-ai-development/SPRINT_PLAN.md	Ordered development backlog
14	docs/03-ai-development/DEFINITION_OF_DONE.md	Completion criteria for tasks, sprints, and release
15	docs/03-ai-development/DECISION_LOG.md	Approved decisions, open questions, and technical verification
16	docs/04-testing/TEST_STRATEGY.md	Testing levels and evidence policy
17	docs/04-testing/ACCEPTANCE_TESTS.md	Numbered acceptance cases and results
18	docs/04-testing/REAL_DEVICE_TEST_MATRIX.md	Target-device test plan and execution records


The final repository inventory may include further files, including requirements traceability, architecture decision records, coding standards, change control, diagnostics/data design, UI/UX specification, CI, issue templates, privacy/setup guidance, troubleshooting, and release documents.

If a referenced file has not been created yet, do not invent its content. Use the available approved source of truth and report the missing dependency when it blocks the task.
4. Documentation Status and Owner Decisions
Several choices are deliberately unresolved:
- exact primary action and gesture;
- minimum supported Android version and target-SDK approach;
- UI toolkit (Jetpack Compose or Android Views);
- application ID/package name;
- whether the button is draggable and its position persists;
- foreground-service strategy, if any;
- post-reboot behavior;
- recovery retry/cooldown policy;
- diagnostic retention bounds;
- distribution method;
- measurable reliability and resource-use targets.
Check DECISION_LOG.md before making any decision-sensitive change. Anuj Pawar (@anujp125), Open, Proposed, Blocked, or Needs Verification do not mean approved.
5. Development Workflow
Use one small task at a time:
1. Read AGENTS.md and relevant product, architecture, and test docs.
2. Inspect the actual repository, current branch/status, and existing user changes.
3. Confirm prerequisites and check the decision log.
4. Create a task-specific brief from SPRINT_TASK_TEMPLATE.md.
5. State the objective, changed files, non-goals, acceptance criteria, and tests.
6. Implement only the assigned task.
7. Run relevant tests/build/static checks and inspect the diff.
8. Update docs and requirement-to-test mapping where needed.
9. Report exact results, tests not run, device evidence, risks, and follow-up tasks.
10. Request review before progressing through dependent work.
Do not ask an agent to build the entire MVP from a single large prompt. Use SPRINT_PLAN.md to select one task with clear prerequisites and an observable outcome.
6. Repository Layout
The intended layout is:
AssistX/
├── app/                              # One Android application module
├── docs/
│   ├── 01-product/
│   ├── 02-architecture/
│   ├── 03-ai-development/
│   ├── 04-testing/
│   └── 05-operations/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   └── workflows/
├── AGENTS.md
├── README.md
├── CONTRIBUTING.md
├── CHANGELOG.md
├── .gitignore
└── LICENSE                            # after licensing decision
This is the planned layout, not confirmation that these paths already exist. Start with one Android app Gradle module unless a concrete requirement justifies otherwise. Do not create empty packages or future-feature scaffolding merely to match the diagram.
7. Build and Test
Current status
The Android project setup and its exact Gradle tasks have not yet been confirmed. Do not report a build or tests as passing until commands have actually been run in the real repository.

After the Android project is initialized, confirm the wrapper and available tasks. Standard command candidates, if the generated project supports them, are:

Windows PowerShell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
macOS/Linux
./gradlew assembleDebug
./gradlew testDebugUnitTest
Instrumentation test candidate, after a compatible emulator/device and test setup are available:
./gradlew connectedDebugAndroidTest
These are provisional command examples, not yet verified project commands. Check the actual Gradle configuration and task list before using or documenting them. On Windows use the wrapper script if it exists.
Test evidence rules
- A successful build is not proof of correct runtime behavior.
- Unit-test results do not prove real-device overlay or lifecycle behavior.
- Tests not executed must be reported as NOT RUN or BLOCKED, with a reason.
- Actual-device claims must include device/OS/build, app version or commit, steps, and observed results.
- Do not remove or weaken a test only to obtain a green run.
Refer to docs/04-testing/TEST_STRATEGY.md and ACCEPTANCE_TESTS.md for the testing contract.
8. Android and Privacy Guardrails
- Do not add a permission unless it maps to an approved requirement.
- Do not add Accessibility simply as a keep-alive mechanism.
- Do not add a foreground service or boot receiver merely to promise always-on operation.
- Do not bypass force-stop, user-disabled state, or system permission controls.
- Do not claim Android permits a behavior without verifying the relevant API/target-SDK rules.
- Do not add networking, analytics, telemetry, accounts, or cloud storage to v0.1 without approval.
- Keep local diagnostics useful, bounded, and free of unnecessary personal data.
- Never commit signing keys, tokens, credentials, private logs, local databases, or generated build output.
9. Issue and Task Reporting
Use a separate task brief for implementation and a bug report for defects. A useful bug report includes:
- exact app build/commit;
- device and OS/build, if applicable;
- prerequisites and permission states;
- numbered reproduction steps;
- expected and actual results;
- frequency/repeatability;
- available diagnostics;
- severity and impact;
- whether the root cause is confirmed or only suspected.
Do not write “Realme killed the app” unless the evidence supports that conclusion. Record what happened and what was observable.
10. Product and Build Metadata
Fill these values when decided; do not guess them:
- Product owner: Anuj Pawar (@anujp125)
- Application ID: com.nudge.assist
- Kotlin package root: com.nudge.assist
- Minimum SDK: 28 (Android 9.0)
- Target SDK policy: 34 (Android 14) with Compile SDK 34
- UI toolkit: Jetpack Compose
- Current version: 0.1.0-dev (provisional label only; align with actual build configuration)
- Distribution method: Personal APK / GitHub Releases
- Repository URL: Local development (AssistX)
- Build/test commands: ./gradlew assembleDebug, ./gradlew testDebugUnitTest
11. What “Ready” Means
The project is ready to begin implementation only when:
- the requirements needed for the first task are clear;
- permission-sensitive decisions are resolved or a limited technical spike is approved;
- the repository is inspected and the task is bounded;
- acceptance criteria and relevant tests are identified.
A release is ready only after the Definition of Done and release checklist are satisfied, the applicable acceptance/device tests are executed, known limitations are recorded, and the product owner approves distribution.
12. License
Licensed under the GNU General Public License v3.0 (GPLv3). See license.md.
13. Maintainer Approval
- Maintainer/product owner: Anuj Pawar (@anujp125)
- README status: Approved
- Last reviewed: 2026-10-29
- Notes: Approved via project questionnaire. Core actions: double-tap lock screen, long-press home/back.