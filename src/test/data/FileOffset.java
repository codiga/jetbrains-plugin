package com.code_inspector.plugins.intellij.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represent the offset for each line of a file. Where the offset for a line starts, where the code starts
 * and where the code ends.
 */
public class FileOffset {
    private final List<LineOffset> lineOffsets;

    public FileOffset(List<String> fileContent) {
        int offset = 0;
        lineOffsets = new ArrayList<>();
        for(String line: fileContent) {
            int offsetStart = offset;
            int offsetEnd = offset + line.length();
            int c = 0;
            while(c < line.length() && line.charAt(c) == ' ') {
                c = c + 1;
            }
            int offsetCode = offsetStart + c;

            lineOffsets.add(new LineOffset(offsetStart, offsetEnd, offsetCode));

            offset = offset + line.length() + 1;
        }
    }

    public Optional<LineOffset> getLineOffsetAtLine(int line) {
        int arrayIndex = line - 1;
        if (arrayIndex > lineOffsets.size() || arrayIndex < 0) {
            return Optional.empty();
        }
        return Optional.of(lineOffsets.get(arrayIndex));
    }
}
