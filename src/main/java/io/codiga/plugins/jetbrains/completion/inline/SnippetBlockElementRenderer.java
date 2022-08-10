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

    private final int MARGIN = 5;
    private final int DEFAULT_SPACE_BETWEEN_BOXES = 50;
    private final String NEXT_STRING = "Next (ALT + ])";
    private final String PREVIOUS_STRING = "Previous (ALT + [)";
    private int currentIndex = 0;
    private int numberOfSnippets = 0;

    public SnippetBlockElementRenderer(@NotNull Editor editor, List<String> textToInsert, int currentIndex, int numberofSnippets) {
        this.textToInsert = textToInsert;
        this.editor = editor;
        this.currentIndex = currentIndex;
        this.numberOfSnippets = numberofSnippets;
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

        Color snippetColor = Color.lightGray;
        Color frontgroundColor = editor.getColorsScheme().getDefaultForeground();
        Color backgroundColor = editor.getColorsScheme().getDefaultBackground();
        Font font = getFont(editor);

        g.setFont(font);
        g.setColor(snippetColor);

        String longestString = textToInsert.stream().sorted((o1, o2) -> o2.length() - o1.length()).findFirst().get();
        int totalWidth = g.getFontMetrics().stringWidth(longestString);

        System.out.println(longestString);
        System.out.println(font.getSize());

        for (int i = 0 ; i < textToInsert.size() ; i++) {
            g.drawString(textToInsert.get(i),
                0, targetRegion.y + i * editor.getLineHeight() + editor.getAscent());
        }


        Font fontAnnotation = editor.getColorsScheme().getFont(EditorFontType.PLAIN);
        g.setFont(fontAnnotation);
        // compute distance
        String suggestion = String.format("snippet %d/%d", this.currentIndex, this.numberOfSnippets);
        int previousBoxWidth = g.getFontMetrics().stringWidth(PREVIOUS_STRING) + MARGIN * 2;
        int suggestionBoxWidth = g.getFontMetrics().stringWidth(suggestion) + MARGIN * 2;
        int nextBoxWidth = g.getFontMetrics().stringWidth(NEXT_STRING) + MARGIN * 2;
        int spaceBetweenBoxes = DEFAULT_SPACE_BETWEEN_BOXES;
        int initialSpace = MARGIN;
        if (totalWidth > previousBoxWidth + suggestionBoxWidth + nextBoxWidth) {
            spaceBetweenBoxes = (totalWidth - previousBoxWidth - suggestionBoxWidth - nextBoxWidth) / 4;
            initialSpace = spaceBetweenBoxes;
        }


        // previous
        int previousBoxStartX = initialSpace;
        int previousBoxStartY = targetRegion.y + textToInsert.size() * editor.getLineHeight() + editor.getAscent();
        int previousBoxHeight = editor.getLineHeight() * 2;
        g.setColor(frontgroundColor);

        g.drawRect(previousBoxStartX,
                previousBoxStartY,
                previousBoxWidth,
                previousBoxHeight);
        g.drawString(PREVIOUS_STRING, previousBoxStartX + MARGIN, previousBoxStartY + editor.getLineHeight() + MARGIN);

        g.setColor(frontgroundColor);

        // suggestion i out of n
        int suggestionBoxStartX = previousBoxStartX + previousBoxWidth + spaceBetweenBoxes;
        int suggestionBoxStartY = targetRegion.y + textToInsert.size() * editor.getLineHeight() + editor.getAscent();
        int suggestionBoxHeight = editor.getLineHeight() * 2;

        g.drawString(suggestion, suggestionBoxStartX + MARGIN, suggestionBoxStartY + editor.getLineHeight() + MARGIN);
        g.drawRect(suggestionBoxStartX,
                suggestionBoxStartY,
                suggestionBoxWidth,
                suggestionBoxHeight);


        // next
        int nextBoxStartX = suggestionBoxStartX + suggestionBoxWidth + spaceBetweenBoxes;
        int nextBoxStartY = targetRegion.y + textToInsert.size() * editor.getLineHeight() + editor.getAscent();
        int nextBoxHeight = editor.getLineHeight() * 2;
        g.drawString(NEXT_STRING, nextBoxStartX + MARGIN, nextBoxStartY + editor.getLineHeight() + MARGIN);
        g.drawRect(nextBoxStartX,
                nextBoxStartY,
                nextBoxWidth,
                nextBoxHeight);
    }
}
