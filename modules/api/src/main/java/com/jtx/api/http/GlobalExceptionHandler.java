package com.jtx.api.http;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jtx.api.dto.ApiResponse;
import com.jtx.engine.Issue;
import com.jtx.engine.IssueCode;
import com.jtx.engine.IssueLevel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String requestId(HttpServletRequest req) {
        Object v = req.getAttribute(RequestIdFilter.ATTR);
        return v == null ? "unknown" : v.toString();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Issue> issues = new ArrayList<>();

        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            issues.add(new Issue(
                    IssueLevel.ERROR,
                    IssueCode.SPEC_INVALID,
                    "Validation error: " + fe.getField() + " " + fe.getDefaultMessage(),
                    fe.getField(),
                    null
            ));
        }
        ObjectNode empty = MAPPER.createObjectNode();
        return ResponseEntity.ok(ApiResponse.fail(empty, issues, requestId(req)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        List<Issue> issues = List.of(new Issue(
                IssueLevel.ERROR,
                IssueCode.SPEC_INVALID,
                "Invalid JSON request body",
                null,
                null
        ));
        ObjectNode empty = MAPPER.createObjectNode();
        return ResponseEntity.ok(ApiResponse.fail(empty, issues, requestId(req)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        List<Issue> issues = List.of(new Issue(
                IssueLevel.ERROR,
                IssueCode.SPEC_INVALID,
                ex.getMessage(),
                null,
                null
        ));
        ObjectNode empty = MAPPER.createObjectNode();
        return ResponseEntity.ok(ApiResponse.fail(empty, issues, requestId(req)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnknown(Exception ex, HttpServletRequest req) {
        List<Issue> issues = List.of(new Issue(
                IssueLevel.ERROR,
                IssueCode.SPEC_INVALID,
                "Internal server error",
                null,
                null
        ));
        ObjectNode empty = MAPPER.createObjectNode();
        return ResponseEntity.status(500).body(ApiResponse.fail(empty, issues, requestId(req)));
    }
}
