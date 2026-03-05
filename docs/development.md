# Development Guide

This document covers local development workflows and extension points.

## Technology Stack

- Java 21
- Maven multi-module build
- Jackson for JSON processing
- JUnit 5 for testing
- Spring Boot for API module
- Picocli for CLI module

## Module Boundaries

- `engine`: transformation domain and execution logic; no web/CLI coupling
- `api`: HTTP transport and validation; delegates to engine
- `cli`: command-line transport and file I/O; delegates to engine

## Local Build Commands

Build all modules:

```bash
mvn clean verify
```

Build a single module with dependencies:

```bash
mvn -pl modules/api -am package
```

Run engine tests:

```bash
mvn -pl modules/engine test
```

## Coding Conventions

- Keep transformation behavior in `engine`; avoid duplicating logic in API/CLI layers.
- Prefer immutable value types (`record`) for payload/spec/issue models.
- Treat transform additions as backward-compatible by default.
- Add tests for both successful and failing rule scenarios.

## Adding a New Transform

1. Implement a new `TransformOp` in `modules/engine/src/main/java/com/jtx/engine/transform`.
2. Register it in `Transforms.defaultRegistry()`.
3. Add test coverage in `TransformerEngineTest` or new focused test class.
4. Update `docs/spec-format.md` with syntax and failure semantics.

## Error Handling Expectations

- Engine returns issues with severity and code instead of throwing for rule-level failures.
- API and CLI should surface issue lists clearly to callers/users.
- Use strict mode in workflows that require schema guarantees.

## Suggested Next Improvements

- Add global API exception mapping with consistent error envelope.
- Provide JSON Schema for request/spec validation.
- Package executable artifacts:
  - Spring Boot fat jar for API
  - shaded/native distribution for CLI
- Introduce integration tests for API and CLI modules.
