package io.codiga.plugins.jetbrains.model.rosie;

import com.intellij.openapi.diagnostic.Logger;

import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * Annotation information created by {@link io.codiga.plugins.jetbrains.services.RosieImpl} based on the
 * information retrieved in {@link RosieResponse} from the Codiga API.
 *
 * @see RosieAnnotationJetBrains
 */
public class RosieAnnotation {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final String ruleName;
    private final String message;
    private final String severity;
    private final String category;
    private final RosiePosition start;
    private final RosiePosition end;
    List<RosieViolationFix> fixes;


    public RosieAnnotation(String name, RosieViolation violation) {
        this.ruleName = name;
        this.message = violation.message;
        this.severity = violation.severity;
        this.category = violation.category;
        this.start = violation.start;
        this.end = violation.end;
        this.fixes = violation.fixes;
    }

    public String getMessage() {
        return this.message;
    }

    public String getRuleName() {
        return this.ruleName;
    }

    public String getSeverity() {
        return this.severity;
    }

    public String getCategory() {
        return this.category;
    }

    public RosiePosition getStart() {
        return this.start;
    }

    public RosiePosition getEnd() {
        return this.end;
    }

    public List<RosieViolationFix> getFixes() {
        return this.fixes;
    }

}
