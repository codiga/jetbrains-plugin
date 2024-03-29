package io.codiga.plugins.jetbrains.model.rosie;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * Represents a position in an editor by its line number and its column number within that line.
 */
@EqualsAndHashCode
@AllArgsConstructor
public final class RosiePosition {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    public int line;
    public int col;

    /**
     * Returns the position offset within the Document of the argument Editor.
     *
     * @param editor the editor in which the offset is calculated
     */
    public int getOffset(@NotNull Editor editor) {
        Document document = editor.getDocument();
        int offset = document.getLineStartOffset(this.line - 1) + adjustColumnOffset(this.col);
        LOGGER.info(String.format("Converting line %s, col %s to offset: %s", this.line, adjustColumnOffset(this.col), offset));
        return offset;
    }

    /**
     * Adjusts the column offset by -1 since the column index returned by Codiga is 1-based, while the IDE editor is 0-based.
     * <p>
     * It doesn't adjust the offset if it is 0, so at the beginning of a line.
     *
     * @param columnOffset the adjusted offset
     */
    private int adjustColumnOffset(int columnOffset) {
        return columnOffset != 0 ? (columnOffset - 1) : columnOffset;
    }
}
