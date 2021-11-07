package io.codiga.plugins.jetbrains.model;

/**
 * Represent all the data when doing a code insertion. This is used
 * to handle all code insertion/deletion for the coding assistant.
 */
public class CodeInsertion {
    private final String code;
    private final int positionStart;
    private final int positionEnd;

    public CodeInsertion(String c, int ps, int pe) {
        this.code = c;
        this.positionStart = ps;
        this.positionEnd = pe;
    }

    public int getPositionEnd() {
        return positionEnd;
    }

    public int getPositionStart() {
        return positionStart;
    }

    public String getCode() {
        return code;
    }
}
