package com.jtx.engine.path;

public sealed interface PathToken permits PathToken.Field, PathToken.Index {
    record Field(String name) implements PathToken {}
    record Index(int i) implements PathToken {}
}
