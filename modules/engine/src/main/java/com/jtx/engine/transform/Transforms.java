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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

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

    public static TransformOp toFloat() {
        return new TransformOp() {
            public String type() { return "to_float"; }
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input == null || input.isNull()) return NullNode.getInstance();
                if (input.isFloat() || input.isDouble() || input.isBigDecimal()) return new DoubleNode(input.asDouble());
                String s = input.asText(null);
                if (s == null) return NullNode.getInstance();
                try {
                    double v = Double.parseDouble(s.trim());
                    return new DoubleNode(v);
                } catch (Exception e) {
                    return MissingNode.getInstance();
                }
            }
        };
    }

    public static TransformOp toBool() {
        return new TransformOp() {
            public String type() { return "to_bool"; }
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input == null || input.isNull()) return NullNode.getInstance();
                if (input.isBoolean()) return BooleanNode.valueOf(input.asBoolean());

                String s = input.asText("").trim().toLowerCase();
                if (s.isEmpty()) return NullNode.getInstance();

                if (s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("y")) return BooleanNode.TRUE;
                if (s.equals("false") || s.equals("0") || s.equals("no") || s.equals("n")) return BooleanNode.FALSE;

                return MissingNode.getInstance();
            }
        };
    }

    public static TransformOp defaultIfNull() {
        return new TransformOp() {
            public String type() { return "default_if_null"; }
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input != null && !input.isNull() && !input.isMissingNode()) return input;
                Object def = args.get("value");
                if (def == null) return NullNode.getInstance();
                return new ObjectMapper().valueToTree(def);
            }
        };
    }

    // args: { "paths": ["$.a", "$.b", "$.c"] }
    public static TransformOp coalesce() {
        return new TransformOp() {
            public String type() { return "coalesce"; }
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                Object p = args.get("paths");
                if (!(p instanceof List<?> paths)) return NullNode.getInstance();

                for (Object o : paths) {
                    if (!(o instanceof String s)) continue;
                    if (!s.startsWith("$.")) continue;
                    JsonNode v = ctx.read(com.jtx.engine.path.CompiledPath.compile(s));
                    if (v != null && !v.isNull() && !v.isMissingNode()) return v;
                }
                return NullNode.getInstance();
            }
        };
    }

    // args: { "fields": ["id","name"] }
    public static TransformOp pick() {
        return new TransformOp() {
            public String type() { return "pick"; }
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input == null || input.isNull() || input.isMissingNode()) return NullNode.getInstance();
                if (!input.isObject()) return MissingNode.getInstance();

                Object f = args.get("fields");
                if (!(f instanceof List<?> fields)) return MissingNode.getInstance();

                ObjectNode out = new ObjectMapper().createObjectNode();
                for (Object o : fields) {
                    if (!(o instanceof String name)) continue;
                    JsonNode v = input.get(name);
                    if (v != null) out.set(name, v);
                }
                return out;
            }
        };
    }

    // args: { "fields": ["password","token"] }
    public static TransformOp omit() {
        return new TransformOp() {
            public String type() { return "omit"; }
            public JsonNode apply(JsonNode input, Map<String, Object> args, TransformContext ctx) {
                if (input == null || input.isNull() || input.isMissingNode()) return NullNode.getInstance();
                if (!input.isObject()) return MissingNode.getInstance();

                Object f = args.get("fields");
                if (!(f instanceof List<?> fields)) return MissingNode.getInstance();

                ObjectMapper mapper = new ObjectMapper();
                ObjectNode out = mapper.createObjectNode();
                out.setAll((ObjectNode) input.deepCopy());

                for (Object o : fields) {
                    if (o instanceof String name) out.remove(name);
                }
                return out;
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
        r.register(toFloat());
        r.register(toBool());
        r.register(defaultIfNull());
        r.register(coalesce());
        r.register(pick());
        r.register(omit());
        return r;
    }
}
