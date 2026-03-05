# JSON Transform

`json-transform` is a Java 21 multi-module project for declarative JSON transformation.
It provides:

- A reusable transformation engine (`modules/engine`)
- A REST API wrapper (`modules/api`)
- A command-line interface (`modules/cli`)

## Project Structure

| Path | Purpose |
| --- | --- |
| `modules/engine` | Core transformation engine and unit tests |
| `modules/api` | Spring Boot HTTP service exposing `/v1/transform` |
| `modules/cli` | Picocli command for file-based transformations |
| `docs` | Product, API, CLI, and development documentation |

## Key Capabilities

- Mapping values from source paths to output paths
- Literal value assignments
- Rule-level transforms (`trim`, `lowercase`, `uppercase`, `to_int`, `concat`)
- Validation and issue reporting with severity (`ERROR`, `WARNING`, `INFO`)
- Strict and lenient execution modes

## Requirements

- JDK 21 (project is compiled with `maven.compiler.release=21`)
- Maven 3.9+

## Build and Test

```bash
mvn clean verify
```

Engine tests only:

```bash
mvn -pl modules/engine test
```

## Quick Start

### 1. Prepare Files

Example input files already exist at repository root:

- `spec.json`
- `input.json`

### 2. Run via CLI Module

```bash
mvn -pl modules/cli -am exec:java \
  -Dexec.mainClass=com.jtx.cli.Main \
  -Dexec.args="--spec spec.json --in input.json --out output.json"
```

The command writes transformed output to `output.json`.

### 3. Run via API Module

Start server:

```bash
mvn -pl modules/api -am spring-boot:run
```

Call endpoint:

```bash
curl -X POST "http://localhost:8080/v1/transform" \
  -H "Content-Type: application/json" \
  -d '{
    "spec":"{\"version\":1,\"mode\":\"LENIENT\",\"mappings\":[{\"from\":\"$.user.id\",\"to\":\"$.id\"}]}",
    "input":{"user":{"id":"123"}}
  }'
```

## Documentation

- Product docs index: [`docs/README.md`](docs/README.md)
- Specification reference: [`docs/spec-format.md`](docs/spec-format.md)
- API contract: [`docs/api-contract.md`](docs/api-contract.md)
- CLI usage: [`docs/cli-usage.md`](docs/cli-usage.md)
- Development guide: [`docs/development.md`](docs/development.md)

## Module Documentation

- Engine: [`modules/engine/README.md`](modules/engine/README.md)
- API: [`modules/api/README.md`](modules/api/README.md)
- CLI: [`modules/cli/README.md`](modules/cli/README.md)
