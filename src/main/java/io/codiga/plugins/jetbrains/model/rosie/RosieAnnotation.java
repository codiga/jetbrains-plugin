package io.codiga.plugins.jetbrains.model.rosie;

import com.intellij.openapi.diagnostic.Logger;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

public class RosieAnnotation {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final String ruleName;
    private final String message;
    private final String severity;
    private final String category;
    private final RosiePosition start;
    private final RosiePosition end;


    public RosieAnnotation(String name,
                           String message,
                           String severity,
                           String category,
                           RosiePosition positionStart,
                           RosiePosition positionEnd) {
        this.ruleName = name;
        this.message = message;
        this.severity = severity;
        this.category = category;
        this.start = positionStart;
        this.end = positionEnd;
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

}