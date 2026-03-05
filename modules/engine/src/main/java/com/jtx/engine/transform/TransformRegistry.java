package com.jtx.engine.transform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TransformRegistry {
    private final Map<String, TransformOp> ops = new ConcurrentHashMap<>();

    public void register(TransformOp op) {
        ops.put(op.type(), op);
    }

    public TransformOp get(String type) {
        return ops.get(type);
    }
}
