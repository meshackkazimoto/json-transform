package io.github.dmesha3.jtx.api;

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

import io.github.dmesha3.jtx.api.dto.ApiResponse;
import io.github.dmesha3.jtx.api.dto.TransformRequest;
import io.github.dmesha3.jtx.api.http.RequestIdFilter;
import io.github.dmesha3.jtx.engine.TransformResult;
import io.github.dmesha3.jtx.engine.TransformerEngine;
import io.github.dmesha3.jtx.engine.spec.PipelineSpec;
import io.github.dmesha3.jtx.engine.spec.SpecParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TransformController {
    @PostMapping(value = "/transform", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse transform(@Valid @RequestBody TransformRequest req, HttpServletRequest httpReq) throws Exception {
        String requestId = String.valueOf(httpReq.getAttribute(RequestIdFilter.ATTR));

        PipelineSpec spec = SpecParser.parse(req.spec());
        TransformerEngine engine = TransformerEngine.compile(spec);

        TransformResult result = engine.transform(req.input());

        if (result.ok()) return ApiResponse.ok(result.output(), result.issues(), requestId);
        return ApiResponse.fail(result.output(), result.issues(), requestId);
    }
}
