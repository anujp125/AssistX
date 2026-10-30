AssistX — Setup and Developer Guide
Document ID: ASSISTX-DEV-001
Version: 0.1
Status: Approved — Project Setup Guide
Last updated: 2026-10-29
Related documents: README.md, AGENTS.md, AI_AGENT_WORKFLOW.md, CONTRIBUTING.md, CODING_STANDARDS.md, SPRINT_PLAN.md, TEST_STRATEGY.md, ACCEPTANCE_TESTS.md, REAL_DEVICE_TEST_MATRIX.md, CI_AND_AUTOMATED_QUALITY_GATES.md, SECURITY_AND_PRIVACY_REVIEW.md
Important: This guide was prepared before the actual Android repository structure and build configuration were inspected. Commands below are common starting points, not confirmation that the project already has a particular module, Gradle task, Kotlin version, Android SDK level, or UI toolkit. Confirm the repository's actual files and task names first. Do not install arbitrary versions or change SDK/toolchain versions merely to make a guessed command work.

1. Purpose
This guide helps a developer or AI coding agent set up AssistX, inspect the project, build and run it, execute tests, and report evidence accurately.

AssistX is intended to be one installable native Android app with:
- a floating assistant component;
- a Studio/settings interface;
- shared local settings and diagnostic behavior;
- no required backend or cloud account for the MVP.
The final primary button action, minimum supported Android version, SDK strategy, UI toolkit, application ID, and several runtime decisions are still subject to approval. Setup work must not silently settle those product decisions.
2. Before Installing Tools
Review the current project files and decisions first:
- README.md
- AGENTS.md
- MVP_SCOPE.md
- SPRINT_PLAN.md
- DECISION_LOG.md
- ARCHITECTURE_DECISIONS.md
- SYSTEM_ARCHITECTURE.md
- STATE_AND_LIFECYCLE.md
If the repository is not initialized yet, use the agreed project bootstrap task rather than inventing a full architecture independently.
Repository facts to identify
Record these from the project, not from this guide:
Item	Source to inspect	Value
Repository URL/path	Git remote or local folder	Local workspace (AssistX)
Default branch	Repository settings / Git	main
Gradle wrapper	gradlew, gradlew.bat, gradle/wrapper/	Present / Missing
Gradle distribution	gradle-wrapper.properties	[FILL IN]
JDK requirement	Gradle and Android Gradle Plugin compatibility	[FILL IN]
Android Gradle Plugin version	Build configuration	[FILL IN]
Kotlin plugin version	Build configuration	[FILL IN]
Android application module(s)	settings.gradle(.kts) / project tasks	[FILL IN]
minSdk, targetSdk, compileSdk	Module build configuration	minSdk 28, targetSdk 34, compileSdk 34
UI toolkit	Existing dependencies/source code	Jetpack Compose
Application ID	Module build configuration	com.nudge.assist
Test tasks	Gradle task listing	testDebugUnitTest, connectedDebugAndroidTest


Do not assume the module is named app; inspect settings.gradle.kts / settings.gradle and Gradle's project list.
3. Development Environment
3.1 Required software
The usual native Android development setup includes:
- Android Studio, using a version compatible with the project's Android Gradle Plugin;
- a compatible JDK;
- Android SDK platform/build tools required by the project;
- Git;
- an Android emulator or a physical Android test device when required by the acceptance tests.
Use Android Studio's bundled JDK if the project/toolchain recommends it and it is compatible. Do not install a newer JDK simply because it is available; a Gradle or Android Gradle Plugin version may not support it.

[OWNER/ENGINEERING DECISION REQUIRED] The exact Android Studio release, JDK version, Gradle version, Android Gradle Plugin version, Kotlin plugin version, and SDK packages must be selected after the repository bootstrap and compatibility checks.
3.2 Install Android Studio
1. Obtain Android Studio through the official Android developer site.
2. Follow its setup wizard.
3. Install the SDK platform and build tools required by the repository.
4. Install an emulator system image if emulator testing is needed.
5. Open the project directory that contains the Gradle settings file, rather than opening an arbitrary subfolder.
6. Allow Gradle sync to finish and review errors before changing build files.
Official starting points (verify current versions when setting up):
- Android Studio: https://developer.android.com/studio
- Android Studio installation: https://developer.android.com/studio/install
- Android SDK setup: https://developer.android.com/tools
- Android build configuration: https://developer.android.com/build
These are references, not a commitment to a specific SDK or tool version.
3.3 Git
Install Git using the distribution appropriate for the operating system. Confirm it is available in the terminal:
git --version
Set the repository-approved author identity and signing configuration if the project requires them. Do not put access tokens or passwords in Git configuration values that may be committed or shown in diagnostic logs.
4. Get the Repository
Existing remote repository
Replace the placeholder URL with the real repository URL supplied by the owner:
git clone https://github.com/anujp125/AssistX.git
cd AssistX
git status
git remote -v
Do not use a guessed URL. For a private repository, follow the owner's approved authentication method.
Already cloned repository
cd AssistX
git status
git branch --show-current
git log -5 --oneline
Before changing files:
- confirm the current branch;
- preserve any local modifications;
- identify work created by another person/agent;
- do not reset, clean, or overwrite user changes without explicit authorization.
If git status shows uncommitted work that is not yours, inspect it and preserve it.
5. Inspect the Project Before Building
From the project root, locate the Gradle settings file and wrapper. Common names are:
- settings.gradle.kts or settings.gradle
- build.gradle.kts or build.gradle
- gradle/libs.versions.toml (if a version catalog is used)
- gradle/wrapper/gradle-wrapper.properties
- gradlew / gradlew.bat
Read the wrapper distribution URL and plugin versions before invoking the build. If the wrapper is missing, do not silently substitute a local Gradle installation and claim reproducible setup. Ask the owner to bootstrap or repair the wrapper.
Print the basic environment
git status
git rev-parse --short HEAD
java -version
These commands provide local context; Gradle may use a different JDK configured through Android Studio or JAVA_HOME. Compare command-line behavior with Android Studio's configured Gradle JDK if they disagree.
List Gradle projects and tasks
On macOS/Linux or a shell with Unix-style wrapper invocation:
./gradlew projects
./gradlew tasks
On Windows PowerShell or Command Prompt:
.\gradlew.bat projects
.\gradlew.bat tasks
These are task-discovery commands. They do not guarantee that every optional Android plugin or SDK component is installed or that the build is fully healthy.
6. First Build
1. Open the project in Android Studio.
2. Wait for Gradle sync to complete.
3. Install the SDK packages that the actual project requires through the supported SDK Manager.
4. Run the documented project build command.
5. Save the first meaningful error and its surrounding context.
6. Fix the underlying configuration or environment problem before changing unrelated architecture.
A common Android app task may look like:
./gradlew assembleDebug
On Windows:
.\gradlew.bat assembleDebug
This task name is illustrative. Confirm it exists with the project's task listing and module structure. A multi-module project may require a specific task such as :moduleName:assembleDebug; use the actual task for this repository.

Do not commit:
- local SDK locations;
- machine-specific local.properties unless repository policy explicitly requires a safe template;
- private signing keys or passwords;
- environment-specific credentials;
- generated outputs that should be ignored.
7. Run AssistX
7.1 Using Android Studio
1. Start an emulator or connect an approved physical test device.
2. Verify Android Studio recognizes the target.
3. Select the correct app run configuration and build variant.
4. Run the app.
5. Record the build version/commit, device details, and actual behavior.
If the app run configuration is missing, inspect the project import and app module configuration. Do not create an arbitrary second module to make a Run button appear.
7.2 Using a physical device
The project currently has a reported target test device in the planning context, but compatibility is not assumed across builds or other devices. Confirm the device is actually available before recording a device test.

Before testing:
- use a dedicated test device where feasible;
- record model, Android version, manufacturer UI/build, app version/commit, and relevant permissions;
- avoid testing with important unsaved data or changing unrelated system settings;
- use Android's ordinary settings UI for permission tests;
- capture only AssistX UI/logs relevant to the issue and redact sensitive information.
Physical-device tests are essential when acceptance depends on actual overlay attachment, permission behavior, system lifecycle, or manufacturer-specific behavior. An emulator or unit test is not equivalent to a physical-device result.
7.3 Permissions
Use only permissions required by the approved product design. The primary action is still an owner decision; therefore, do not add Accessibility, boot startup, foreground-service behavior, or other permissions solely because similar apps use them.

For permission testing:
- observe the initial state;
- request or navigate through the documented user flow;
- grant/deny through Android's normal settings;
- return to AssistX and confirm it rechecks actual state;
- record what the app displayed and did;
- restore test settings where appropriate.
Do not attempt to bypass Android permission gates or force-stop behavior.
8. Run Automated Tests
Inspect actual test source folders and Gradle tasks before running tests.

A common unit-test task may look like:
./gradlew test
A specific Android variant may instead use a task shaped like:
./gradlew :app:testDebugUnitTest
For a connected Android instrumentation test, a task may look like:
./gradlew :app:connectedDebugAndroidTest
These are patterns only. Substitute the module and task names confirmed from the project. Do not use angle-bracket placeholders literally.
Test reporting
For each run, capture:
- exact command;
- branch and commit;
- relevant environment/device;
- pass/fail/skip summary;
- report path or CI artifact link;
- any warning or blocker that materially affects confidence.
Use the status values defined in TEST_STRATEGY.md:
- PASS
- FAIL
- BLOCKED
- NOT RUN
- NOT APPLICABLE
- FLAKY
Do not mark a test as passed because its source code exists or because an AI agent generated it. It must be executed and its result observed.
9. Suggested Development Loop
For each task:
1. Read: Review the assigned task, acceptance criteria, related requirements, decisions, and relevant source files.
2. Inspect: Check Git status, current patterns, dependencies, and test coverage.
3. Plan: Identify the smallest change, relevant failure paths, and test strategy.
4. Implement: Change only the files needed for the task; preserve other work.
5. Test: Run focused tests first, then broader required checks.
6. Review: Inspect the full diff, errors, logs, and dependency changes.
7. Document: Update requirements, state diagrams, privacy rules, tests, or setup docs when behavior/contracts change.
8. Report: List files changed, commands actually run, results, unrun checks, and remaining risks.
9. Submit: Follow CONTRIBUTING.md and the adopted GitHub templates.
Do not combine a feature, broad refactor, dependency upgrade, and unrelated formatting change in one task without an explicit reason.
10. Working with AI Coding Agents
Give an agent one bounded task from SPRINT_PLAN.md or a similarly specific issue. Provide the relevant docs and ask it to follow AGENTS.md, AI_AGENT_WORKFLOW.md, SPRINT_TASK_TEMPLATE.md, and CODING_STANDARDS.md.
Before the agent edits
Require it to:
- inspect the repository tree and current Git status;
- read relevant implementation and tests;
- summarize the intended behavior and acceptance criteria;
- flag unknown product decisions or toolchain assumptions;
- propose a concise file-level plan.
During implementation
Require it to:
- avoid overwriting unrelated work;
- avoid inventing APIs or commands;
- avoid changing toolchain/SDK versions without approval;
- use deterministic tests and appropriate fakes for failure paths;
- respect platform permissions and user intent;
- avoid collecting or logging unrelated app content;
- keep recovery bounded and avoid duplicate side effects.
At completion
Require a report containing:
- changed/created files;
- behavior implemented;
- exact test/build commands that were executed;
- actual pass/fail/block results;
- test artifacts or output paths;
- tests not run and why;
- known limitations;
- unresolved decisions;
- documentation and traceability updates.
Never accept “all tests pass” without evidence of which commands ran and what they returned.
11. Common Setup Failures
Gradle wrapper missing or unusable
Possible causes: project bootstrap incomplete, wrapper files missing, or wrapper distribution unavailable.

Approach:
- confirm repository root and branch;
- inspect gradle/wrapper/gradle-wrapper.properties;
- use the checked-in wrapper when present;
- verify network/proxy access if distribution download fails;
- ask the owner to repair/bootstrap the wrapper if required files are absent.
Do not use a random local Gradle version to silently mask a missing wrapper.
Gradle/JDK compatibility error
Possible causes: command-line JDK differs from Android Studio's JDK, or project Gradle/plugins require a different JDK.

Approach:
- record java -version;
- inspect Gradle's reported JVM and Android Studio Gradle JDK configuration;
- inspect Gradle and Android Gradle Plugin versions;
- use the version documented for the project;
- only change versions after reviewing compatibility and running the suite.
Android SDK or build-tools missing
Approach:
- inspect compileSdk/plugin configuration;
- install the required package using the supported SDK Manager;
- confirm SDK environment configuration;
- do not lower compileSdk, targetSdk, or minSdk just to bypass an environment issue.
Gradle sync succeeds but build fails
Approach:
- identify the first meaningful compiler/configuration error;
- check the exact module and variant;
- confirm the command is valid for the project;
- distinguish source errors from dependency-download or SDK failures;
- avoid editing unrelated files to silence the error.
Emulator is slow or unavailable
Approach:
- confirm virtualization and emulator requirements for the host environment;
- select an installed compatible system image;
- run unit tests while the emulator issue is investigated;
- mark integration/device-dependent checks BLOCKED, not PASS;
- use a physical device only if approved and available.
Device not detected
Approach:
- use a suitable USB cable/port or approved wireless debugging setup;
- enable the normal developer options/USB debugging flow if permitted;
- authorize the host on the device;
- confirm Android Studio/ADB sees the intended device;
- do not disable device security protections globally.
Permission flow does not match expectation
Approach:
- record the exact app build and Android/manufacturer build;
- use normal system settings;
- verify the app re-checks the permission when returning;
- capture the UI state and safe diagnostics;
- don't conclude a platform or manufacturer root cause from one observation.
Tests pass locally but fail in CI
Approach:
- compare commit, JDK, Gradle wrapper, SDK packages, environment variables, and test command;
- inspect the first assertion/configuration error;
- check for timing, ordering, locale/time-zone, or filesystem assumptions;
- use deterministic clocks and fakes where possible;
- do not repeatedly rerun until green without recording flakiness.
No build/test task matches this guide
This is expected if the repository has a different module/variant structure. Run projects and tasks, inspect the build files, then update this guide with verified commands. Do not alter project architecture to fit the guide.
12. Local Configuration and Secrets
- Keep personal local configuration out of source control.
- Do not place API keys/passwords in code, Gradle files, screenshots, issue reports, or agent prompts.
- Do not use real credentials for tests when synthetic fixtures are enough.
- Use approved secret-management tooling for any future secret-required workflow.
- Treat APKs and diagnostic archives as potentially inspectable; do not embed secrets and assume users cannot extract them.
- Ensure .gitignore is aligned with actual Android/Gradle outputs and signing files.
- Before sharing logs/screenshots, remove tokens, personal data, unrelated app content, and machine-specific sensitive paths where appropriate.
13. Before Asking for Help
Include:
- operating system and relevant tool versions;
- repository branch and commit;
- exact command;
- first meaningful error plus useful surrounding lines;
- what you already tried;
- whether the issue reproduces in Android Studio, terminal, CI, emulator, or physical device;
- whether the failure blocks build, tests, or only a specific device case.
Do not include signing keys, passwords, access tokens, personal data, unredacted private logs, or a full device dump.
14. First-Time Setup Checklist
Environment
- [ ] Android Studio installed from an official source.
- [ ] JDK compatibility checked against the actual project.
- [ ] Required SDK packages installed.
- [ ] Git is available.
- [ ] Repository cloned or opened at its root.
- [ ] Current branch and working-tree status recorded.
Project
- [ ] Wrapper and Gradle configuration inspected.
- [ ] Application module and ID identified.
- [ ] SDK levels and plugin versions recorded.
- [ ] Gradle sync completed or the blocker documented.
- [ ] A valid compile/build task identified.
- [ ] Test tasks identified.
- [ ] No unapproved product/toolchain decisions made.
Verification
- [ ] Initial build command executed and result captured.
- [ ] Relevant unit tests executed and result captured.
- [ ] Emulator or physical-device run performed if required and available.
- [ ] Permission flows checked where relevant.
- [ ] Device/build details recorded for physical-device claims.
- [ ] Skipped or blocked checks documented.
- [ ] Local secrets/configuration are not staged for commit.
15. Setup Record Template
# AssistX Development Setup Record

- Date:
- Developer/agent:
- Repository path/URL:
- Branch:
- Commit:
- Operating system:
- Android Studio version:
- JDK / Gradle JVM:
- Gradle wrapper version:
- Android Gradle Plugin:
- Kotlin plugin:
- Android SDK packages:
- Application module:
- Application ID:
- UI toolkit:
- Build command and result:
- Unit-test command and result:
- Lint/static-check command and result:
- Emulator/device details:
- Blockers / deviations:
- Documentation updates needed:
Do not place personal credentials or secrets in this record. If setup is blocked, keep the record partial and describe the exact blocker.
16. Owner Decisions / Follow-up
ID	Decision	Status
DEV-001	Actual repository URL and default branch	Approved (main branch, local git)	Local workspace
DEV-002	Supported development OS environments	Approved (Windows 11 / PowerShell, Linux, macOS)
DEV-003	Android Studio/JDK/Gradle/plugin versions	Approved (Android Studio Hedgehog/Iguana+, JDK 17, Gradle 8.4+, AGP 8.3+)
DEV-004	Minimum and compile/target SDK policy	Approved (minSdk 28, targetSdk 34, compileSdk 34)
DEV-005	UI toolkit (Compose or Views)	Approved (Jetpack Compose with Material 3)
DEV-006	Test device/emulator configuration	Approved (Realme Narzo 30 4G / RMX2156, realme UI 3.0, Android 12)
DEV-007	Canonical build/test/lint commands	Approved (gradlew assembleDebug, testDebugUnitTest, connectedDebugAndroidTest)
DEV-008	CI workflow and required quality gates	Approved (Local gradlew quality gates + GitHub Actions on push to main)
DEV-009	Repository secret and signing-key policy	Approved (No credentials/keystores committed; local debug keystore for development)


Owner: Anuj Pawar (@anujp125)
Repository: Local (AssistX)
Guide validated against commit: Initial Architecture Baseline
Review date: 2026-10-29
17. Maintenance Rule
After the initial Android project is created and a known-good build exists, validate every command in this guide against that repository. Replace generic placeholders with verified commands and known tool versions; preserve explicit warnings for device-dependent behavior.

Update the guide whenever the repository structure, build toolchain, module names, test commands, or supported development process changes. A setup guide that has not been checked against the current repository must not be treated as authoritative build evidence.