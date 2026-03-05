package com.jtx.engine.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.jtx.engine.path.CompiledPath;

public interface TransformContext {
    JsonNode read(CompiledPath path);
}
