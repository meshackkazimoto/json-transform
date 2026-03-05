package com.jtx.engine;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record TransformResult (
        boolean ok,
        JsonNode output,
        List<Issue> issues
) {
    public List<Issue> errors() {
        return issues.stream().filter(i -> i.level() == IssueLevel.ERROR).toList();
    }
}
