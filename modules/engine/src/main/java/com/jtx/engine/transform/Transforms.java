package com.jtx.engine.transform;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.List;
import java.util.Map;

public final class Transforms {
    private Transforms() {
    }

    public static TransformOp trim() {
        return new TransformOp() {
            @Override
            public String type() {
                return "trim";
            }

            @Override
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input == null || input.isNull()) return NullNode.getInstance();
                String s = input.asText("");
                return new TextNode(s.trim());
            }
        };
    }

    public static TransformOp lowercase() {
        return new TransformOp() {
            @Override
            public String type() {
                return "lowercase";
            }

            @Override
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input == null || input.isNull()) return NullNode.getInstance();
                return new TextNode(input.asText("").toLowerCase());
            }
        };
    }

    public static TransformOp uppercase() {
        return new TransformOp() {
            @Override
            public String type() {
                return "uppercase";
            }

            @Override
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input == null || input.isNull()) return NullNode.getInstance();
                return new TextNode(input.asText("").toUpperCase());
            }
        };
    }

    public static TransformOp toInt() {
        return new TransformOp() {
            @Override
            public String type() {
                return "to_int";
            }

            @Override
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input == null || input.isNull()) return NullNode.getInstance();
                if (input.isInt() || input.isLong()) return new LongNode(input.asLong());
                String s = input.asText(null);
                if (s == null) return NullNode.getInstance();
                try {
                    long v = Long.parseLong(s.trim());
                    return new LongNode(v);
                } catch (Exception e) {
                    return MissingNode.getInstance();
                }
            }
        };
    }

    // args: { "parts": ["$.a.b", " ", "$.x"] }
    public static TransformOp concat() {
        return new TransformOp() {
            public String type() {
                return "concat";
            }

            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                Object p = args.get("parts");
                if (!(p instanceof List<?> parts)) return new TextNode("");
                StringBuilder sb = new StringBuilder();
                for (Object part : parts) {
                    if (part == null) continue;
                    if (part instanceof String s) {
                        if (s.startsWith("$.")) {
                            JsonNode v = ctx.read(com.jtx.engine.path.CompiledPath.compile(s));
                            if (v != null && !v.isNull() && !v.isMissingNode()) sb.append(v.asText("").trim());
                        } else {
                            sb.append(s);
                        }
                    } else {
                        sb.append(String.valueOf(part));
                    }
                }
                return new TextNode(sb.toString());
            }
        };
    }

    public static TransformRegistry defaultRegistry() {
        TransformRegistry r = new TransformRegistry();
        r.register(trim());
        r.register(lowercase());
        r.register(uppercase());
        r.register(toInt());
        r.register(concat());
        return r;
    }
}
