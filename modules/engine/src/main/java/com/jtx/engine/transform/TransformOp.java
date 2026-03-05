package com.jtx.engine.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.util.Map;

public interface TransformOp {
    String type();

    default JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
        return input == null ? NullNode.getInstance() : input;
    }
}
