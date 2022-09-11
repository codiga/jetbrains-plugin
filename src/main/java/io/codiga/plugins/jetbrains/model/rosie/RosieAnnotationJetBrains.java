package io.codiga.plugins.jetbrains.model.rosie;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;

import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

/**
 * An updated  version of the annotation that contains an offset for JetBrains
 */
public class RosieAnnotationJetBrains {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final String ruleName;
    private final String message;
    private final String severity;
    private final int start;
    private final int end;
    List<RosieViolationFix> fixes;
    private String category;


    public RosieAnnotationJetBrains(String ruleName, String message, String severity, String category, RosiePosition start, RosiePosition end, List<RosieViolationFix> fixes, Editor editor) {
        this.ruleName = ruleName;
        this.message = message;
        this.severity = severity;
        this.category = category;
        this.start = start.getOffset(editor);
        this.end = end.getOffset(editor);
        this.fixes = List.copyOf(fixes);
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

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public List<RosieViolationFix> getFixes() {
        return this.fixes;
    }
}