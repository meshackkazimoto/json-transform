package com.jtx.engine.spec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtx.engine.Mode;

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
