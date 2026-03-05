package com.jtx.cli;

import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        int code = new CommandLine(new TransformCommand()).execute(args);
        System.exit(code);
    }
}
