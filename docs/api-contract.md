# API Contract

This document describes the HTTP interface provided by `modules/api`.

## Base URL

Local default:

`http://localhost:8080`

## Endpoint

- Method: `POST`
- Path: `/v1/transform`
- Content-Type: `application/json`
- Accept: `application/json`

## Request Schema

```json
{
  "spec": "string containing JSON specification",
  "input": {}
}
```

Field details:

- `spec` (required, non-empty string): serialized transformation spec JSON
- `input` (required, JSON): source payload to transform

## Success Response

```json
{
  "ok": true,
  "output": {
    "id": "123"
  },
  "issues": []
}
```

Field details:

- `ok`: true when no error-level issues were produced
- `output`: transformed JSON object
- `issues`: array of issue records (`level`, `code`, `message`, `path`, `ruleIndex`)

## Example Request

```bash
curl -X POST "http://localhost:8080/v1/transform" \
  -H "Content-Type: application/json" \
  -d '{
    "spec":"{\"version\":1,\"mode\":\"LENIENT\",\"mappings\":[{\"from\":\"$.user.id\",\"to\":\"$.id\"}]}",
    "input":{"user":{"id":"123"}}
  }'
```

## Validation and Error Behavior

- Bean validation rejects null/blank request fields.
- Invalid spec format and other runtime exceptions follow Spring Boot default error handling.
- Client logic should always inspect both `ok` and `issues`.

## Operational Guidance

- Keep specs versioned outside API payloads for reproducibility.
- Apply input payload size limits at the gateway/reverse-proxy layer.
- Add structured request logging with sensitive-field redaction for production deployments.
