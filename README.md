# JTX (JSON Transform)

JTX is a Java 21 multi-module project for declarative JSON transformation.
It lets you define a transformation spec and run it through a reusable engine,
a CLI, or a REST API.

## Modules

| Path | Purpose |
| --- | --- |
| `modules/engine` | Core transformation engine and tests |
| `modules/cli` | Picocli-based command-line runner |
| `modules/api` | Spring Boot API with `/v1/transform` |

## Features

- Source-to-target JSON mapping using JSON paths
- Literal value assignment
- Built-in transforms (`trim`, `lowercase`, `uppercase`, `to_int`, `concat`)
- Validation with issue levels (`ERROR`, `WARNING`, `INFO`)
- Execution modes: `STRICT` and `LENIENT`

## Requirements

- JDK 21
- Maven 3.9+

## Build and Test

```bash
mvn clean verify
```

Engine tests only:

```bash
mvn -pl modules/engine test
```

## CLI Usage

Sample files are provided at repository root:
- `spec.json`
- `input.json`

Run:

```bash
mvn -pl modules/cli -am exec:java \
  -Dexec.mainClass=io.github.dmesha3.jtx.cli.Main \
  -Dexec.args="--spec spec.json --in input.json --out output.json"
```

## API Usage

Start server:

```bash
mvn -pl modules/api -am spring-boot:run
```

Test endpoint:

```bash
curl -X POST "http://localhost:8080/v1/transform" \
  -H "Content-Type: application/json" \
  -d '{
    "spec":"{\"version\":1,\"mode\":\"LENIENT\",\"mappings\":[{\"from\":\"$.user.id\",\"to\":\"$.id\"}]}",
    "input":{"user":{"id":"123"}}
  }'
```

## License

This project is licensed under GNU General Public License v3.0 or later.
See `LICENSE` for full terms.
