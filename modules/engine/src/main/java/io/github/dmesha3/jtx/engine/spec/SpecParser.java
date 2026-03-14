package io.github.dmesha3.jtx.engine.spec;

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
import io.github.dmesha3.jtx.engine.Mode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpecParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SpecParser() {}

    public static PipelineSpec parse(String specJSON) throws IOException {
        JsonNode root = MAPPER.readTree(specJSON);

        int version = root.path("version").asInt(1);
        Mode mode = Mode.valueOf(root.path("mode").asText("LENIENT"));


        List<MappingRuleSpec> mappings = new ArrayList<>();
        for (JsonNode m : root.path("mappings")) {
            String from = m.has("from") ? m.get("from").asText(null) : null;
            Object value = null;
            if (m.has("value")) {
                value = MAPPER.convertValue(m.get("value"), Object.class);
            }
            String to = m.path("to").asText(null);

            Boolean required = m.has("required") ? m.get("required").asBoolean() : null;

            List<TransformSpec> transforms = null;
            if (m.has("transforms") && m.get("transforms").isArray()) {
                transforms = new ArrayList<>();
                for (JsonNode t : m.get("transforms")) {
                    String type = t.path("type").asText(null);
                    Map<String, Object> args = new HashMap<>();
                    if (t.has("args")) {
                        args = MAPPER.convertValue(t.get("args"), Map.class);
                    }
                    transforms.add(new TransformSpec(type, args));
                }
            }

            mappings.add(new MappingRuleSpec(from, value, to, required, transforms));
        }
        return new PipelineSpec(version, mode, mappings);
    }

}
