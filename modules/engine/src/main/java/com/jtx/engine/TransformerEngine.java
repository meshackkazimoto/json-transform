package com.jtx.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jtx.engine.path.CompiledPath;
import com.jtx.engine.path.PathToken;
import com.jtx.engine.spec.MappingRuleSpec;
import com.jtx.engine.spec.PipelineSpec;
import com.jtx.engine.spec.TransformSpec;
import com.jtx.engine.transform.TransformContext;
import com.jtx.engine.transform.TransformOp;
import com.jtx.engine.transform.TransformRegistry;
import com.jtx.engine.transform.Transforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TransformerEngine {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static PipelineSpec spec;
    private static TransformRegistry registry;

    public TransformerEngine(PipelineSpec spec, TransformRegistry registry) {
        this.spec = spec;
        this.registry = registry;
    }

    public static TransformerEngine compile(PipelineSpec spec) {
        return new TransformerEngine(spec, Transforms.defaultRegistry());
    }

    public static TransformResult transform(JsonNode input) {
        List<Issue> issues = new ArrayList<>();
        ObjectNode output = MAPPER.createObjectNode();

        for (int idx = 0; idx < spec.mappings().size(); idx++) {
            MappingRuleSpec rule = spec.mappings().get(idx);

            if (rule.to() == null || rule.to().isBlank()) {
                issues.add(new Issue(IssueLevel.ERROR, IssueCode.SPEC_INVALID, "Missing 'to' field", rule.to(), idx));
                continue;
            }

            CompiledPath toPath = null;
            try {
                toPath = CompiledPath.compile(rule.to());
            } catch (IllegalArgumentException e) {
                issues.add(new Issue(IssueLevel.ERROR, IssueCode.PATH_INVALID, "Invalid 'to' path: " + e.getMessage(), rule.to(), idx));
            }

            JsonNode valueNode = null;

            // source: from path
            if (rule.from() != null && !rule.from().isBlank()) {
                CompiledPath fromPath;
                try {
                    fromPath = CompiledPath.compile(rule.from());
                } catch (IllegalArgumentException e) {
                    issues.add(new Issue(IssueLevel.ERROR, IssueCode.PATH_INVALID, "Invalid 'from' path: " + e.getMessage(), rule.from(), idx));
                    continue;
                }

                valueNode = readPath(input, fromPath);
                boolean missing = (valueNode == null || valueNode.isMissingNode() || valueNode.isNull());

                boolean required = rule.required() != null && rule.required();
                if (missing && required) {
                    IssueLevel lvl = (spec.mode() == Mode.STRICT) ? IssueLevel.ERROR : IssueLevel.WARNING;
                    issues.add(new Issue(lvl, IssueCode.MISSING_FIELD, "Required input missing: " + rule.from(), rule.from(), idx));
                    if (spec.mode() == Mode.STRICT) continue;
                    // lenient: skip mapping
                    continue;
                }
                if (missing) {
                    // optional missing: skip silently
                    continue;
                }
            }

            // source: constant value
            if (rule.value() != null) {
                valueNode = MAPPER.valueToTree(rule.value());
            }

            // transforms
            if (rule.transforms() != null && !rule.transforms().isEmpty()) {
                JsonNode current = valueNode;
                TransformContext ctx = path -> readPath(input, path);

                for (TransformSpec t : rule.transforms()) {
                    String type = t.type();
                    Map<String, Object> args = t.args() == null ? Map.of() : t.args();
                    TransformOp op = registry.get(type);

                    if (op == null) {
                        issues.add(new Issue(IssueLevel.ERROR, IssueCode.SPEC_INVALID, "Unknown transform: " + type, null, idx));
                        current = MissingNode.getInstance();
                        break;
                    }

                    JsonNode out = op.apply(current, args, ctx);

                    if (out != null && out.isMissingNode()) {
                        issues.add(new Issue(IssueLevel.ERROR, IssueCode.TRANSFORM_FAILED, "Transform failed: " + type, null, idx));
                        current = MissingNode.getInstance();
                        break;
                    }

                    current = out;
                }

                valueNode = current;
            }

            if (valueNode == null || valueNode.isMissingNode()) {
                // if transforms failed we already recorded errors
                continue;
            }

            assert toPath != null;
            writePath(output, toPath, valueNode);
        }

        boolean ok = issues.stream().noneMatch(i -> i.level() == IssueLevel.ERROR);
        return new TransformResult(ok, output, issues);
    }

    private static JsonNode readPath(JsonNode root, CompiledPath path) {
        JsonNode cur = root;
        for (PathToken t : path.tokens()) {
            if (cur == null) return MissingNode.getInstance();

            if (t instanceof PathToken.Field f) {
                if (!cur.isObject()) return MissingNode.getInstance();
                cur = cur.get(f.name());
                if (cur == null) return MissingNode.getInstance();
            } else if (t instanceof PathToken.Index ix) {
                if (!cur.isArray()) return MissingNode.getInstance();
                int i = ix.i();
                if (i < 0 || i >= cur.size()) return MissingNode.getInstance();
                cur = cur.get(i);
            }
        }
        return cur;
    }

    private static void writePath(ObjectNode root, CompiledPath path, JsonNode value) {
        JsonNode cur = root;
        List<PathToken> tokens = path.tokens();

        for (int i = 0; i < tokens.size(); i++) {
            PathToken t = tokens.get(i);
            boolean last = (i == tokens.size() - 1);

            if (t instanceof PathToken.Field f) {
                if (!(cur instanceof ObjectNode obj)) {
                    // should never happen with our root structure
                    return;
                }
                if (last) {
                    obj.set(f.name(), value);
                    return;
                }
                JsonNode next = obj.get(f.name());
                if (next == null || next.isNull() || next.isMissingNode()) {
                    // decide next container based on upcoming token
                    JsonNode newNode = (tokens.get(i + 1) instanceof PathToken.Index) ? MAPPER.createArrayNode() : MAPPER.createObjectNode();
                    obj.set(f.name(), newNode);
                    cur = newNode;
                } else {
                    cur = next;
                }
            } else if (t instanceof PathToken.Index ix) {
                // for MVP, we only allow arrays if current node is ArrayNode
                if (!(cur instanceof ArrayNode arr)) {
                    return;
                }
                int index = ix.i();
                while (arr.size() <= index) arr.addNull();
                if (last) {
                    arr.set(index, value);
                    return;
                }
                JsonNode next = arr.get(index);
                if (next == null || next.isNull() || next.isMissingNode()) {
                    JsonNode newNode = (tokens.get(i + 1) instanceof PathToken.Index) ? MAPPER.createArrayNode() : MAPPER.createObjectNode();
                    arr.set(index, newNode);
                    cur = newNode;
                } else {
                    cur = next;
                }
            }
        }
    }
}
