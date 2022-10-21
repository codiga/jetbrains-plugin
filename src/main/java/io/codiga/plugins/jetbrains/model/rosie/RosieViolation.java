package io.codiga.plugins.jetbrains.model.rosie;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Represents a code violation found by Rosie.
 */
@EqualsAndHashCode
@AllArgsConstructor
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
}
