# Transformation Specification Format

This document defines the JSON spec consumed by the engine.

## Top-Level Schema

```json
{
  "version": 1,
  "mode": "LENIENT",
  "mappings": []
}
```

Fields:

- `version` (number): spec version. Default is `1`.
- `mode` (string): `LENIENT` or `STRICT`. Default is `LENIENT`.
- `mappings` (array): ordered mapping rules.

## Mapping Rule Schema

```json
{
  "from": "$.user.id",
  "value": "constant",
  "to": "$.id",
  "required": true,
  "transforms": [
    { "type": "trim" }
  ]
}
```

Fields:

- `to` (string, required): destination path.
- `from` (string, optional): source path.
- `value` (any JSON scalar/object/array, optional): literal value.
- `required` (boolean, optional): whether missing `from` source is considered a required-field issue.
- `transforms` (array, optional): ordered transform operations.

Behavior notes:

- If `value` is present, it overwrites the value resolved from `from`.
- If resolved value is missing and not required, the rule is skipped.
- If resolved value is missing and required:
  - `STRICT`: error issue
  - `LENIENT`: warning issue

## Path Syntax

Supported syntax:

- Root object prefix: `$.`
- Field access: `$.user.profile.firstName`
- Array index access: `$.items[0].price`

Invalid path formats generate `PATH_INVALID` issues.

## Built-In Transforms

| Type | Input | Output | Failure Behavior |
| --- | --- | --- | --- |
| `trim` | Any value | trimmed text | never fails |
| `lowercase` | Any value | lowercase text | never fails |
| `uppercase` | Any value | uppercase text | never fails |
| `to_int` | number or numeric text | integer (`long`) | returns `TRANSFORM_FAILED` when parse fails |
| `concat` | ignores input, uses `args.parts` | concatenated text | empty string when parts missing |

### `concat` Arguments

```json
{
  "type": "concat",
  "args": {
    "parts": ["$.user.profile.firstName", " ", "$.user.profile.lastName"]
  }
}
```

- Strings beginning with `$.` are treated as input path references.
- Other entries are treated as literal text.

## Issue Model

Issue structure:

```json
{
  "level": "ERROR",
  "code": "MISSING_FIELD",
  "message": "Required input missing: $.user.phone",
  "path": "$.user.phone",
  "ruleIndex": 0
}
```

Issue codes:

- `SPEC_INVALID`
- `PATH_INVALID`
- `MISSING_FIELD`
- `TRANSFORM_FAILED`

## End-to-End Example

Spec:

```json
{
  "version": 1,
  "mode": "LENIENT",
  "mappings": [
    { "from": "$.user.id", "to": "$.id" },
    { "from": "$.user.profile.firstName", "to": "$.first_name", "transforms": [{ "type": "trim" }] },
    { "from": "$.user.profile.lastName", "to": "$.last_name" },
    {
      "to": "$.full_name",
      "transforms": [
        { "type": "concat", "args": { "parts": ["$.user.profile.firstName", " ", "$.user.profile.lastName"] } }
      ]
    },
    { "from": "$.user.age", "to": "$.age", "transforms": [{ "type": "to_int" }] },
    { "value": "active", "to": "$.status" }
  ]
}
```

Input:

```json
{
  "user": {
    "id": "123",
    "profile": { "firstName": "  Meshack  ", "lastName": "Kazimoto" },
    "age": "24"
  }
}
```

Output:

```json
{
  "id": "123",
  "first_name": "Meshack",
  "last_name": "Kazimoto",
  "full_name": "Meshack Kazimoto",
  "age": 24,
  "status": "active"
}
```
