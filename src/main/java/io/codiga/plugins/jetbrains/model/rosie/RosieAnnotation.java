package io.codiga.plugins.jetbrains.model.rosie;

import lombok.Getter;

import java.util.List;

/**
 * Annotation information created by {@link io.codiga.plugins.jetbrains.rosie.RosieImpl} based on the
 * information retrieved in {@link RosieResponse} from the Codiga API.
 *
 * @see RosieAnnotationJetBrains
 */
@Getter
public class RosieAnnotation {
    private final String rulesetName;
    private final String ruleName;
    private final String message;
    private final String severity;
    private final String category;
    private final RosiePosition start;
    private final RosiePosition end;
    private final List<RosieViolationFix> fixes;


    public RosieAnnotation(String name, String rulesetName, RosieViolation violation) {
        this.rulesetName = rulesetName;
        this.ruleName = name;
        this.message = violation.message;
        this.severity = violation.severity;
        this.category = violation.category;
        this.start = violation.start;
        this.end = violation.end;
        this.fixes = violation.fixes;
    }
}
