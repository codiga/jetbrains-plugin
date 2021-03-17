package com.code_inspector.plugins.intellij.model;

/**
 * A class that represent where to start annotating the code. For each line, outline
 */
public class LineOffset {
    public final int startOffset;
    public final int endOffset;
    public final int codeStartOffset;
    
    public LineOffset(int _startOffset, int _endOffset, int _codeStartOffset){
        startOffset = _startOffset;
        endOffset = _endOffset;
        codeStartOffset = _codeStartOffset;
    }
}
