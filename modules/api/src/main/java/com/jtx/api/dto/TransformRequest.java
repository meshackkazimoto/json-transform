package com.jtx.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransformRequest(
        @NotBlank String spec,
        @NotNull JsonNode input
) {
}
