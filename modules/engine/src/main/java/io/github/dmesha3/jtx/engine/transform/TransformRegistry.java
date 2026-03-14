package io.github.dmesha3.jtx.engine.transform;

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
