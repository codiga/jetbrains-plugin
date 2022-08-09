package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;


public class SnippetBlockElementRenderer implements EditorCustomElementRenderer {

    private List<String> textToInsert;
    private Editor editor;

    public SnippetBlockElementRenderer(@NotNull Editor editor, List<String> textToInsert) {
        this.textToInsert = textToInsert;
        this.editor = editor;
    }

    private Font getFont(Editor editor) {
        Font font = editor.getColorsScheme().getFont(EditorFontType.ITALIC);
        return font;
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        String firstLine = this.textToInsert.get(0);

        return editor.getContentComponent()
            .getFontMetrics(getFont(editor)).stringWidth(firstLine);
    }

    @Override
    public int calcHeightInPixels(@NotNull Inlay inlay) {
        return editor.getLineHeight() * textToInsert.size();
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {

        Color color = Color.lightGray;
        Font font = getFont(editor);

        for (int i = 0 ; i < textToInsert.size() ; i++) {
            g.drawString(textToInsert.get(i),
                0, targetRegion.y + i * editor.getLineHeight() + editor.getAscent());
        }

    }
}
