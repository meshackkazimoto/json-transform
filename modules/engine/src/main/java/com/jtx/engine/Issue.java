package com.jtx.engine;

public record Issue(
        IssueLevel level,
        IssueCode code,
        String message,
        String path,
        Integer ruleIndex
) {
}
