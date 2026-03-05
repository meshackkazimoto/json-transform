package com.jtx.api;

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
