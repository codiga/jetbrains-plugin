package io.codiga.plugins.jetbrains.model.rosie;

import com.intellij.openapi.editor.Editor;
import lombok.Getter;

import java.util.List;

/**
 * An updated version of {@link RosieAnnotation}. It stores the start and end offsets based
 * on the current Editor. This is used in {@link io.codiga.plugins.jetbrains.annotators.RosieAnnotator}
 * to provide the annotation information.
 *
 * @see RosieAnnotation
 */
@Getter
public class RosieAnnotationJetBrains {
    private final String rulesetName;
    private final String ruleName;
    private final String message;
    private final String severity;
    private final String category;
    private final int start;
    private final int end;
    private final List<RosieViolationFix> fixes;


    public RosieAnnotationJetBrains(RosieAnnotation annotation, Editor editor) {
        this.rulesetName = annotation.getRulesetName();
        this.ruleName = annotation.getRuleName();
        this.message = annotation.getMessage();
        this.severity = annotation.getSeverity();
        this.category = annotation.getCategory();
        this.start = annotation.getStart().getOffset(editor);
        this.end = annotation.getEnd().getOffset(editor);
        this.fixes = List.copyOf(annotation.getFixes());
    }
}
