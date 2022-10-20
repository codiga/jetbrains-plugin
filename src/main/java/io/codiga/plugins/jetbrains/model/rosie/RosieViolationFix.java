package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;
import java.util.Objects;

/**
 * Represents a "quick fix" in an editor with one or more potential edits.
 * <p>
 * {@link io.codiga.plugins.jetbrains.annotators.RosieAnnotationFix} works based on the information stored here.
 */
public final class RosieViolationFix {
    public String description;
    public List<RosieViolationFixEdit> edits;

    public RosieViolationFix(String description, List<RosieViolationFixEdit> edits) {
        this.description = description;
        this.edits = edits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RosieViolationFix that = (RosieViolationFix) o;
        return Objects.equals(description, that.description) && Objects.equals(edits, that.edits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, edits);
    }
}
