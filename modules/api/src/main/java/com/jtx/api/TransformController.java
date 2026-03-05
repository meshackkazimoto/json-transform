package com.jtx.api;

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

import com.jtx.api.dto.TransformRequest;
import com.jtx.api.dto.TransformResponse;
import com.jtx.engine.TransformResult;
import com.jtx.engine.TransformerEngine;
import com.jtx.engine.spec.PipelineSpec;
import com.jtx.engine.spec.SpecParser;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class TransformController {
    @PostMapping(value = "/transform", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TransformResponse transform(@Valid @RequestBody TransformRequest req) throws Exception {
        PipelineSpec spec = SpecParser.parse(req.spec());
        TransformerEngine engine = TransformerEngine.compile(spec);

        TransformResult result = engine.transform(req.input());
        return new TransformResponse(result.ok(), result.output(), result.issues());
    }
}
