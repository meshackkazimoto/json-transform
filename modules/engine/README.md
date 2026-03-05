# Engine Module

Core transformation runtime for converting an input JSON document into an output JSON document using a declarative spec.

## Coordinates

- Artifact: `com.jtx:engine:0.1.0`
- Location: `modules/engine`

## Responsibilities

- Parse a transformation spec into a typed model
- Read source values using JSON path-like expressions (for example `$.user.id`)
- Apply ordered transforms per mapping rule
- Write results to output paths (objects and arrays)
- Emit structured issues and success status

## Public Entry Points

- `SpecParser.parse(String specJson)` -> `PipelineSpec`
- `TransformerEngine.compile(PipelineSpec spec)` -> `TransformerEngine`
- `TransformerEngine.transform(JsonNode input)` -> `TransformResult`

## Spec Concepts

Each mapping rule supports:

- `from`: source path
- `value`: constant value (used when present)
- `to`: destination path (required)
- `required`: whether missing source should be reported
- `transforms`: ordered list of transformation operations

## Supported Transforms

| Transform | Description |
| --- | --- |
| `trim` | Trims leading/trailing whitespace from text |
| `lowercase` | Converts text to lowercase |
| `uppercase` | Converts text to uppercase |
| `to_int` | Parses numeric text into integer (stored as long) |
| `concat` | Joins `args.parts` where entries can be literals or `$.path` lookups |

## Modes

- `LENIENT`: missing required fields are warnings and rule is skipped
- `STRICT`: missing required fields are errors and `ok=false`

## Result Model

`TransformResult` contains:

- `ok`: true only if no `ERROR` issues were produced
- `output`: transformed JSON object
- `issues`: full list of warnings/errors/info entries
- `errors()`: convenience filter for `ERROR` issues

## Running Tests

```bash
mvn -pl modules/engine test
```

## Extension Pattern

To add a custom transform:

1. Implement `TransformOp`
2. Register it in a `TransformRegistry`
3. Construct `new TransformerEngine(spec, registry)`

Reference documentation:

- [`../../docs/spec-format.md`](../../docs/spec-format.md)
- [`../../docs/development.md`](../../docs/development.md)
