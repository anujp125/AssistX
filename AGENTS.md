# AssistX — AI Coding Agent Instructions

This repository follows strict, verified AI coding agent workflows.

- **Central Agent Guidelines:** See [`docs/ai_development/agents.md`](docs/ai_development/agents.md)
- **Approved Decision Register:** See [`docs/ai_development/decision_log.md`](docs/ai_development/decision_log.md) and [`PRODUCT_DECISIONS_CHECKLIST.md`](PRODUCT_DECISIONS_CHECKLIST.md)
- **Product Architecture & ADRs:** See [`docs/architecture/system_architecture.md`](docs/architecture/system_architecture.md)
- **Testing Strategy & Device Verification:** See [`docs/testing/test_strategy.md`](docs/testing/test_strategy.md) and [`docs/testing/real_device_test.md`](docs/testing/real_device_test.md)

### Rules for Future AI Coding Agents
1. Inspect the codebase, git status, and existing verified tests before writing code.
2. Adhere to approved decisions; do not introduce unauthorized permissions or bypass platform constraints.
3. Preserve the 0dp Metro / Modern UI design language and WCAG AA contrast standards.
4. Keep user telemetry strictly local (no network permissions, no external logging).
5. Always run automated unit tests (`./gradlew test`) and confirm verification on physical hardware before concluding tasks.

