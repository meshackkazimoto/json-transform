package com.jtx.engine.spec;

import java.util.List;

public record MappingRuleSpec(
        String from,
        Object value,
        String to,
        Boolean required,
        List<TransformSpec> transforms
) {
}
