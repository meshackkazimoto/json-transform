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
}