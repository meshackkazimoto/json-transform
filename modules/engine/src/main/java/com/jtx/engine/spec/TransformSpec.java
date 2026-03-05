package com.jtx.engine.spec;

import java.util.Map;

public record TransformSpec(
        String type,
        Map<String, Object> args
) {
}
