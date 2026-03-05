package com.jtx.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.jtx.engine.Issue;

import java.util.List;

public record TransformResponse(
        boolean ok,
        JsonNode output,
        List<Issue> issues
) {
}
