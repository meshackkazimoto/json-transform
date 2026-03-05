package com.jtx.engine.spec;

import com.jtx.engine.Mode;

import java.util.List;

public record PipelineSpec(
        int version,
        Mode mode,
        List<MappingRuleSpec> mappings
) {
}
