package com.jtx.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtx.engine.TransformResult;
import com.jtx.engine.TransformerEngine;
import com.jtx.engine.spec.PipelineSpec;
import com.jtx.engine.spec.SpecParser;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(name = "jtx", mixinStandardHelpOptions = true, description = "JSON Transformation Engine CLI")
public class TransformCommand implements Runnable {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @CommandLine.Option(names = {"-s", "--spec"}, required = true, description = "Path to spec JSON file")
    Path specPath;

    @CommandLine.Option(names = {"-i", "--in"}, required = true, description = "Path to input JSON file")
    Path inPath;

    @CommandLine.Option(names = {"-o", "--out"}, required = true, description = "Path to output JSON file")
    Path outPath;

    @Override
    public void run() {
        try {
            String specJson = Files.readString(specPath);
            JsonNode input = MAPPER.readTree(Files.readString(inPath));

            PipelineSpec spec = SpecParser.parse(specJson);
            TransformerEngine engine = TransformerEngine.compile(spec);

            TransformResult res = engine.transform(input);

            Files.writeString(outPath, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(res.output()));

            if (!res.issues().isEmpty()) {
                System.err.println("Issues:");
                res.issues().forEach(i -> System.err.println("- " + i.level() + " " + i.code() + " " + i.message()));
            }

            if (!res.ok()) {
                System.exit(2);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
