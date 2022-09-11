package io.codiga.plugins.jetbrains.model.rosie;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class RosiePosition {
    public int line;
    public int col;


    public RosiePosition(int line, int col) {
        this.line = line;
        this.col = col;
    }

    public int getOffset(@NotNull Editor editor) {
        Document document = editor.getDocument();
        return document.getLineStartOffset(this.line - 1) + this.col;
    }

}