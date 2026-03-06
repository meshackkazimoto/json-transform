package com.jtx.engine;

/*
 * Copyright (C) 2026 Meshack Kazimoto
 *
 * JTX (JSON Transform) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTX (JSON Transform) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JTX (JSON Transform).  If not, see <https://www.gnu.org/licenses/>.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtx.engine.spec.PipelineSpec;
import com.jtx.engine.spec.SpecParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransformerEngineTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void transforms_basic_mapping_and_concat() throws Exception {
        String specJson = """
        {
          "version": 1,
          "mode": "LENIENT",
          "mappings": [
            { "from": "$.user.id", "to": "$.id" },
            { "from": "$.user.profile.firstName", "to": "$.first_name", "transforms": [{"type":"trim"}] },
            { "from": "$.user.profile.lastName", "to": "$.last_name" },
            {
              "to": "$.full_name",
              "transforms": [{
                "type":"concat",
                "args": { "parts": ["$.user.profile.firstName", " ", "$.user.profile.lastName"] }
              }]
            },
            { "from": "$.user.age", "to": "$.age", "transforms": [{"type":"to_int"}] },
            { "value": "active", "to": "$.status" }
          ]
        }
        """;

        String inputJson = """
        {
          "user": {
            "id": "123",
            "profile": { "firstName": "  Meshack  ", "lastName": "Kazimoto" },
            "age": "24"
          }
        }
        """;

        PipelineSpec spec = SpecParser.parse(specJson);
        TransformerEngine engine = TransformerEngine.compile(spec);
        JsonNode input = MAPPER.readTree(inputJson);

        TransformResult result = engine.transform(input);

        assertTrue(result.ok());
        assertEquals("123", result.output().path("id").asText());
        assertEquals("Meshack", result.output().path("first_name").asText());
        assertEquals("Kazimoto", result.output().path("last_name").asText());
        assertEquals("Meshack Kazimoto", result.output().path("full_name").asText());
        assertEquals(24, result.output().path("age").asInt());
        assertEquals("active", result.output().path("status").asText());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void strict_required_missing_is_error() throws Exception {
        String specJson = """
        {
          "version": 1,
          "mode": "STRICT",
          "mappings": [
            { "from": "$.user.phone", "to": "$.phone", "required": true }
          ]
        }
        """;
        String inputJson = """
                { "user": { "id": "x" } }
         """;

        PipelineSpec spec = SpecParser.parse(specJson);
        TransformerEngine engine = TransformerEngine.compile(spec);

        TransformResult result = engine.transform(MAPPER.readTree(inputJson));

        assertFalse(result.ok());
        assertEquals(1, result.errors().size());
        assertEquals(IssueCode.MISSING_FIELD, result.errors().get(0).code());
    }

    @Test
    void transforms_to_bool_to_float_and_default_if_null() throws Exception {
        String specJson = """
        {
          "version": 1,
          "mode": "LENIENT",
          "mappings": [
            { "from": "$.flags.enabled", "to": "$.enabled", "transforms": [{"type":"to_bool"}] },
            { "from": "$.metrics.score", "to": "$.score", "transforms": [{"type":"to_float"}] },
            {
              "from": "$.user.nickname",
              "to": "$.nickname",
              "transforms": [{"type":"default_if_null", "args": {"value":"anonymous"}}]
            }
          ]
        }
        """;

        String inputJson = """
        {
          "flags": { "enabled": "yes" },
          "metrics": { "score": "98.75" },
          "user": { "nickname": null }
        }
        """;

        PipelineSpec spec = SpecParser.parse(specJson);
        TransformerEngine engine = TransformerEngine.compile(spec);
        TransformResult result = engine.transform(MAPPER.readTree(inputJson));

        assertTrue(result.ok());
        assertTrue(result.output().path("enabled").asBoolean());
        assertEquals(98.75d, result.output().path("score").asDouble(), 0.0001);
        assertEquals("", result.output().path("nickname").asText());
    }

    @Test
    void coalesce_picks_first_present_value() throws Exception {
        String specJson = """
        {
          "version": 1,
          "mode": "LENIENT",
          "mappings": [
            {
              "to": "$.contact",
              "transforms": [{
                "type":"coalesce",
                "args": { "paths": ["$.user.phone", "$.user.email", "$.user.username"] }
              }]
            }
          ]
        }
        """;

        String inputJson = """
        {
          "user": {
            "email": "meshack@example.com",
            "username": "meshack"
          }
        }
        """;

        PipelineSpec spec = SpecParser.parse(specJson);
        TransformerEngine engine = TransformerEngine.compile(spec);
        TransformResult result = engine.transform(MAPPER.readTree(inputJson));

        assertTrue(result.ok());
        assertEquals("meshack@example.com", result.output().path("contact").asText());
    }

    @Test
    void pick_and_omit_transform_objects() throws Exception {
        String pickSpecJson = """
        {
          "version": 1,
          "mode": "LENIENT",
          "mappings": [
            {
              "from": "$.user",
              "to": "$.public_user",
              "transforms": [{
                "type":"pick",
                "args": { "fields": ["id", "name"] }
              }]
            }
          ]
        }
        """;

        String omitSpecJson = """
        {
          "version": 1,
          "mode": "LENIENT",
          "mappings": [
            {
              "from": "$.user",
              "to": "$.safe_user",
              "transforms": [{
                "type":"omit",
                "args": { "fields": ["password", "token"] }
              }]
            }
          ]
        }
        """;

        String inputJson = """
        {
          "user": {
            "id": "u1",
            "name": "Meshack",
            "password": "secret",
            "token": "abc123",
            "role": "admin"
          }
        }
        """;

        PipelineSpec pickSpec = SpecParser.parse(pickSpecJson);
        TransformerEngine pickEngine = TransformerEngine.compile(pickSpec);
        TransformResult pickResult = pickEngine.transform(MAPPER.readTree(inputJson));

        assertTrue(pickResult.ok());
        assertEquals("u1", pickResult.output().path("public_user").path("id").asText());
        assertEquals("Meshack", pickResult.output().path("public_user").path("name").asText());
        assertTrue(pickResult.output().path("public_user").path("password").isMissingNode());

        PipelineSpec omitSpec = SpecParser.parse(omitSpecJson);
        TransformerEngine omitEngine = TransformerEngine.compile(omitSpec);
        TransformResult omitResult = omitEngine.transform(MAPPER.readTree(inputJson));

        assertTrue(omitResult.ok());
        assertEquals("u1", omitResult.output().path("safe_user").path("id").asText());
        assertEquals("Meshack", omitResult.output().path("safe_user").path("name").asText());
        assertEquals("admin", omitResult.output().path("safe_user").path("role").asText());
        assertTrue(omitResult.output().path("safe_user").path("password").isMissingNode());
        assertTrue(omitResult.output().path("safe_user").path("token").isMissingNode());
    }
}