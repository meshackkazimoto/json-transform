package io.github.dmesha3.jtx.engine.path;

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

import java.util.ArrayList;
import java.util.List;

public final class CompiledPath {
    private final String raw;
    private final List<PathToken> tokens;

    public CompiledPath(String raw, List<PathToken> tokens) {
        this.raw = raw;
        this.tokens = tokens;
    }

    public String raw() {return raw; }
    public List<PathToken> tokens() { return tokens; }

    // This function supports $.a.b.c and $.items[0].price
    public static CompiledPath compile(String path) {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("Path is blank");
        if (!path.startsWith("$.")) throw new IllegalArgumentException("Path must start with '$.'");

        String s = path.substring(2);
        List<PathToken> out = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            // read field name until '.' or '['
            int start = i;
            while (i < s.length() && s.charAt(i) != '.' && s.charAt(i) != '[') i++;
            if (i == start) throw new IllegalArgumentException("Invalid path near: " + s.substring(i));

            String field = s.substring(start, i);
            out.add(new PathToken.Field(field));

            // handle indexes like [0][1]
            while (i < s.length() && s.charAt(i) == '[') {
                i++; // skip '['
                int numStart = i;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
                if (numStart == i) throw new IllegalArgumentException("Empty index in brackets");
                if (i >= s.length() || s.charAt(i) != ']') throw new IllegalArgumentException("Missing closing ']'");
                int idx = Integer.parseInt(s.substring(numStart, i));
                out.add(new PathToken.Index(idx));
                i++; // skip ']'
            }

            if (i < s.length()) {
                if (s.charAt(i) == '.') i++;
                else throw new IllegalArgumentException("Invalid path char: " + s.charAt(i));
            }
        }

        return new CompiledPath(path, out);
    }
}
