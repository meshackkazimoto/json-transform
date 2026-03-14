package io.github.dmesha3.jtx.cli;

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
import io.github.dmesha3.jtx.engine.TransformResult;
import io.github.dmesha3.jtx.engine.TransformerEngine;
import io.github.dmesha3.jtx.engine.spec.PipelineSpec;
import io.github.dmesha3.jtx.engine.spec.SpecParser;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(name = "jtx", mixinStandardHelpOptions = true, description = "JTX (JSON Transform) CLI")
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
