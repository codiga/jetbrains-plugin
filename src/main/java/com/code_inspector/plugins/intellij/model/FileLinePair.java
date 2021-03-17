package com.code_inspector.plugins.intellij.model;

import java.util.Objects;

/**
 * A data class to represent the pair filename/lineNumber. This is used later in a HashMap in order
 * to see what files actually changed.
 */
public class FileLinePair {
    private String filename;
    private Integer lineNumber;

    public FileLinePair(String _filename, Integer _lineNumber) {
        this.filename = _filename;
        this.lineNumber = _lineNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "FileLinePair{" +
            "filename='" + filename + '\'' +
            ", lineNumber=" + lineNumber +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileLinePair that = (FileLinePair) o;
        return Objects.equals(filename, that.filename) && Objects.equals(lineNumber, that.lineNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, lineNumber);
    }
}
