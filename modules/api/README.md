# API Module

Spring Boot module that exposes the transformation engine through HTTP.

## Coordinates

- Artifact: `com.jtx:api:0.1.0`
- Location: `modules/api`

## Responsibilities

- Accept transform requests over REST
- Parse spec and execute the engine
- Return transformed output with issue details

## Endpoint

- Method: `POST`
- Path: `/v1/transform`
- Content type: `application/json`

### Request Body

```json
{
  "spec": "{\"version\":1,\"mode\":\"LENIENT\",\"mappings\":[{\"from\":\"$.user.id\",\"to\":\"$.id\"}]}",
  "input": {
    "user": {
      "id": "123"
    }
  }
}
```

Notes:

- `spec` is a JSON string, not a nested JSON object.
- `input` is any valid JSON value, typically an object.

### Response Body

```json
{
  "ok": true,
  "output": {
    "id": "123"
  },
  "issues": []
}
```

## Run Locally

```bash
mvn -pl modules/api -am spring-boot:run
```

Default base URL: `http://localhost:8080`

## Validation and Errors

- Missing `spec` or `input` is rejected by bean validation.
- Parsing or execution exceptions propagate through Spring Boot default error handling.

Reference documentation:

- [`../../docs/api-contract.md`](../../docs/api-contract.md)
- [`../../docs/spec-format.md`](../../docs/spec-format.md)
