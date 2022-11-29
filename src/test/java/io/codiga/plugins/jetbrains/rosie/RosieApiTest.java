package io.codiga.plugins.jetbrains.rosie;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotation;
import io.codiga.plugins.jetbrains.model.rosie.RosiePosition;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolation;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFix;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFixEdit;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Test service implementation of the Rosie service.
 */
public class RosieApiTest implements RosieApi {

    @Override
    @NotNull
    public List<RosieAnnotation> getAnnotations(@NotNull PsiFile psiFile, @NotNull Project project) {
        switch (psiFile.getName()) {
            case "no_highlight_for_no_violation.py":
                return List.of();
            case "highlight_for_single_violation.py":
                return singleViolation();
            case "highlight_for_multiple_violations.py":
                return multipleViolations();
            case "text_insertion_fix.py":
                return textInsertionFix();
            case "text_insertion_fix_null_end.py":
                return textInsertionFixNullEnd();
            case "text_replacement_fix_ranges_matching.py":
                return textReplaceRangesMatching();
            case "text_replacement_fix_ranges_not_matching.py":
                return textReplaceRangesNotMatching();
            case "text_removal_fix_ranges_matching.py":
                return textRemoveRangesMatching();
            case "text_removal_fix_ranges_not_matching.py":
                return textRemoveRangesNotMatching();
            case "no_highlight_for_start_offset_outside.py":
                return startOffsetOutside();
            case "no_highlight_for_end_offset_outside.py":
                return endOffsetOutside();
            case "add_top_level_disable_codiga.py":
            case "add_top_level_disable_codiga.java":
                return topLevelDisableCodigaComment();
            case "add_nested_disable_codiga.py":
            case "add_nested_disable_codiga.java":
                return nestedDisableCodigaComment();
            default:
                return List.of();
        }
    }

    private List<RosieAnnotation> singleViolation() {
        var rosieViolation = new RosieViolation(
            "single_violation",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "INFORMATIONAL",
            "CODE_STYLE",
            Collections.emptyList());

        return List.of(new RosieAnnotation("single_rule", "single_ruleset", rosieViolation));
    }

    /**
     * Also includes test cases for different severity/highlight levels.
     */
    private List<RosieAnnotation> multipleViolations() {
        var rosieViolationCritical = new RosieViolation(
            "critical_violation",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "CRITICAL",
            "ERROR_PRONE",
            Collections.emptyList());
        var rosieViolationError = new RosieViolation(
            "error_violation",
            new RosiePosition(2, 5),
            new RosiePosition(2, 10),
            "ERROR",
            "SAFETY",
            Collections.emptyList());
        var rosieViolationWarning = new RosieViolation(
            "warning_violation",
            new RosiePosition(3, 5),
            new RosiePosition(3, 10),
            "WARNING",
            "BEST_PRACTICE",
            Collections.emptyList());

        return List.of(
            new RosieAnnotation("critical_rule", "ruleset_name", rosieViolationCritical),
            new RosieAnnotation("error_rule", "ruleset_name", rosieViolationError),
            new RosieAnnotation("warning_rule", "ruleset_name", rosieViolationWarning)
        );
    }

    private List<RosieAnnotation> textInsertionFix() {
        var rosieViolationFix = new RosieViolationFix(
            "Insert text",
            List.of(
                new RosieViolationFixEdit(
                    new RosiePosition(1, 5),
                    new RosiePosition(1, 10),
                    "This is the inserted text",
                    "add")));
        var rosieViolation = new RosieViolation(
            "has_text_insertion_fix",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "WARNING",
            "CODE_STYLE",
            List.of(rosieViolationFix));

        return List.of(new RosieAnnotation("text_insertion_rule", "text_insertion_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> textInsertionFixNullEnd() {
        var rosieViolationFix = new RosieViolationFix(
            "Insert text null end",
            List.of(
                new RosieViolationFixEdit(
                    new RosiePosition(1, 5),
                    null,
                    "This is the inserted text",
                    "add")));
        var rosieViolation = new RosieViolation(
            "has_text_insertion_fix",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "WARNING",
            "CODE_STYLE",
            List.of(rosieViolationFix));

        return List.of(new RosieAnnotation("text_insertion_rule", "text_insertion_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> textReplaceRangesMatching() {
        var rosieViolationFix = new RosieViolationFix(
            "Replace text",
            List.of(
                new RosieViolationFixEdit(
                    new RosiePosition(1, 5),
                    new RosiePosition(1, 10),
                    "This is the replacment text",
                    "update")));
        var rosieViolation = new RosieViolation(
            "has_text_replace_fix",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "WARNING",
            "CODE_STYLE",
            List.of(rosieViolationFix));

        return List.of(new RosieAnnotation("text_replacement_rule", "text_replacement_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> textReplaceRangesNotMatching() {
        var rosieViolationFix = new RosieViolationFix(
            "Replace text",
            List.of(
                new RosieViolationFixEdit(
                    new RosiePosition(1, 2),
                    new RosiePosition(1, 5),
                    "This is the replacement text",
                    "update")));
        var rosieViolation = new RosieViolation(
            "has_text_replace_fix",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "WARNING",
            "CODE_STYLE",
            List.of(rosieViolationFix));

        return List.of(new RosieAnnotation("text_replacement_rule", "text_replacement_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> textRemoveRangesMatching() {
        var rosieViolationFix = new RosieViolationFix(
            "Remove text",
            List.of(
                new RosieViolationFixEdit(
                    new RosiePosition(1, 5),
                    new RosiePosition(1, 10),
                    "Unused removal text",
                    "remove")));
        var rosieViolation = new RosieViolation(
            "has_text_remove_fix",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "WARNING",
            "CODE_STYLE",
            List.of(rosieViolationFix));

        return List.of(new RosieAnnotation("text_remove_rule", "text_remove_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> textRemoveRangesNotMatching() {
        var rosieViolationFix = new RosieViolationFix(
            "Remove text",
            List.of(
                new RosieViolationFixEdit(
                    new RosiePosition(1, 2),
                    new RosiePosition(1, 5),
                    "Unused removal text",
                    "remove")));
        var rosieViolation = new RosieViolation(
            "has_text_remove_fix",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "WARNING",
            "CODE_STYLE",
            List.of(rosieViolationFix));

        return List.of(new RosieAnnotation("text_remove_rule", "text_remove_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> startOffsetOutside() {
        var rosieViolation = new RosieViolation(
            "start_offset_outside",
            new RosiePosition(5, 15),
            new RosiePosition(1, 10),
            "WARNING",
            "CODE_STYLE",
            Collections.emptyList());

        return List.of(new RosieAnnotation("start_offset_rule", "start_offset_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> endOffsetOutside() {
        var rosieViolation = new RosieViolation(
            "end_offset_outside",
            new RosiePosition(1, 5),
            new RosiePosition(5, 10),
            "WARNING",
            "CODE_STYLE",
            Collections.emptyList());

        return List.of(new RosieAnnotation("start_offset_rule", "start_offset_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> topLevelDisableCodigaComment() {
        var rosieViolation = new RosieViolation(
            "single_violation",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "INFORMATIONAL",
            "CODE_STYLE",
            Collections.emptyList());

        return List.of(new RosieAnnotation("single_rule", "single_ruleset", rosieViolation));
    }

    private List<RosieAnnotation> nestedDisableCodigaComment() {
        var rosieViolation = new RosieViolation(
            "single_violation",
            new RosiePosition(5, 10),
            new RosiePosition(5, 30),
            "INFORMATIONAL",
            "CODE_STYLE",
            Collections.emptyList());

        return List.of(new RosieAnnotation("single_rule", "single_ruleset", rosieViolation));
    }
}
