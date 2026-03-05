# CLI Usage

This document describes the command-line interface in `modules/cli`.

## Command Purpose

Run JSON transformations from files without starting the API service.

## Command Options

| Option | Required | Description |
| --- | --- | --- |
| `-s`, `--spec` | Yes | Path to spec JSON file |
| `-i`, `--in` | Yes | Path to input JSON file |
| `-o`, `--out` | Yes | Path to output JSON file |

## Run with Maven

```bash
mvn -pl modules/cli -am exec:java \
  -Dexec.mainClass=com.jtx.cli.Main \
  -Dexec.args="--spec spec.json --in input.json --out output.json"
```

## Example Files

Repository root includes ready-to-run examples:

- `spec.json`
- `input.json`

Expected output file:

- `output.json`

## Exit Codes

| Code | Meaning |
| --- | --- |
| `0` | Success (no error-level issues) |
| `1` | Runtime failure (I/O, parsing, unexpected exception) |
| `2` | Completed transformation but contains error-level issues |

## Runtime Output

- Transformed JSON is written to the output file using pretty formatting.
- Issues are printed to `stderr` in a readable list.

## Automation Recommendations

- Treat exit code `2` as a functional failure in CI/CD.
- Persist both output file and stderr logs for audit/debug workflows.
- Keep spec and input files under version control when transformations are part of a release process.
