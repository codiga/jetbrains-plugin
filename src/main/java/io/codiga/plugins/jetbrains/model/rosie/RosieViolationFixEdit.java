package io.codiga.plugins.jetbrains.model.rosie;

import lombok.EqualsAndHashCode;

/**
 * Represents a single quick fix edit in an Editor.
 */
@EqualsAndHashCode
public class RosieViolationFixEdit {
    /**
     * The position of the edit from where the fix will begin.
     */
    public RosiePosition start;
    /**
     * The position of the edit at where the fix will end.
     */
    public RosiePosition end;
    /**
     * Content for string insertion and replacement in the editor. Not used for removal edits.
     */
    public String content;
    /**
     * The type of edit to apply. See {@link RosieConstants#ROSIE_FIX_ADD}, {@link RosieConstants#ROSIE_FIX_UPDATE}
     * and {@link RosieConstants#ROSIE_FIX_REMOVE}.
     */
    public String editType;


    public RosieViolationFixEdit(RosiePosition start, RosiePosition end, String editType, String content) {
        this.start = start;
        this.end = end;
        this.content = content;
        this.editType = editType;
    }
}
