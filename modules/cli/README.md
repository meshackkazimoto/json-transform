# CLI Module

Picocli-based command-line interface for file-to-file JSON transformation.

## Coordinates

- Artifact: `com.jtx:cli:0.1.0`
- Location: `modules/cli`

## Responsibilities

- Load spec and input JSON from files
- Execute engine transformation
- Write output JSON to a file
- Print issues to `stderr`
- Return process exit codes for automation usage

## Command

Command name: `jtx`

Required options:

- `-s, --spec` path to spec JSON file
- `-i, --in` path to input JSON file
- `-o, --out` path to output JSON file

## Run

```bash
mvn -pl modules/cli -am exec:java \
  -Dexec.mainClass=com.jtx.cli.Main \
  -Dexec.args="--spec spec.json --in input.json --out output.json"
```

## Exit Codes

- `0`: transform completed and no error-level issues
- `1`: runtime error (I/O, parse, or unexpected exception)
- `2`: transform completed but produced error-level issues

## Output Behavior

- Writes pretty-printed transformed JSON to output path
- Prints issue summary to `stderr` when issues exist

Reference documentation:

- [`../../docs/cli-usage.md`](../../docs/cli-usage.md)
- [`../../docs/spec-format.md`](../../docs/spec-format.md)
