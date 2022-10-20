package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;
import java.util.Objects;

/**
 * Represents a code violation found by Rosie.
 */
public class RosieViolation {
    public String message;
    /**
     * The position of the violation from which the annotation begins.
     */
    public RosiePosition start;
    /**
     * The position of the violation at which the annotation ends.
     */
    public RosiePosition end;
    /**
     * See {@code ROSIE_SEVERITY_*} constants in {@link RosieConstants}.
     */
    public String severity;
    public String category;
    public List<RosieViolationFix> fixes;


    public RosieViolation(RosiePosition start, RosiePosition end, String message, String severity, String category, List<RosieViolationFix> fixes) {
        this.message = message;
        this.start = start;
        this.end = end;
        this.severity = severity;
        this.category = category;
        this.fixes = fixes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RosieViolation that = (RosieViolation) o;
        return Objects.equals(message, that.message) && Objects.equals(start, that.start) && Objects.equals(end, that.end) && Objects.equals(severity, that.severity) && Objects.equals(category, that.category) && Objects.equals(fixes, that.fixes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, start, end, severity, category, fixes);
    }
}

