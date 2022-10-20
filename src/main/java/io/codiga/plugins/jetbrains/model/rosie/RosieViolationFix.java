package io.codiga.plugins.jetbrains.model.rosie;

import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents a "quick fix" in an editor with one or more potential edits.
 * <p>
 * {@link io.codiga.plugins.jetbrains.annotators.RosieAnnotationFix} works based on the information stored here.
 */
@EqualsAndHashCode
public final class RosieViolationFix {
    public String description;
    public List<RosieViolationFixEdit> edits;

    public RosieViolationFix(String description, List<RosieViolationFixEdit> edits) {
        this.description = description;
        this.edits = edits;
    }
}
