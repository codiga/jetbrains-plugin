package io.codiga.plugins.jetbrains.annotators;

import io.codiga.api.type.LanguageEnumeration;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represent an annotation from Codiga with the different attributes coming from the API
 * and how they are surfaced in the UI.
 */
public class Annotation {

    private final @NotNull AnnotationKind annotationKind;
    private final @NotNull Optional<Long> projectId;
    private final @NotNull Optional<Long> analysisId;
    private final @NotNull String message;
    private final @NotNull Optional<String> rule;
    private final @NotNull Optional<String> ruleUrl;
    private final @NotNull String filename;
    private final @NotNull Optional<String> tool;
    private final @NotNull Optional<String> description;
    private final @NotNull Optional<LanguageEnumeration> language;
    private final @NotNull Optional<Long> severity;
    private final @NotNull Optional<String> category;
    private final @NotNull TextRange range;

    @NotNull public AnnotationKind getAnnotationKind() { return annotationKind; };
    @NotNull public Optional<Long> getProjectId() { return projectId; }
    @NotNull public Optional<Long> getAnalysisId() { return analysisId; }
    @NotNull public String getMessage() { return message; }
    @NotNull public Optional<String> getRule() { return rule; }
    @NotNull public Optional<String> getRuleUrl() { return ruleUrl; }
    @NotNull public String getFilename() { return filename; }
    @NotNull public Optional<String> getTool() { return tool; }
    @NotNull public Optional<String> getDescription() { return description; }
    @NotNull public Optional<LanguageEnumeration> getLanguage() { return language; }
    @NotNull public Optional<Long> getSeverity() { return severity; }
    @NotNull public Optional<String> getCategory() { return category; }
    @NotNull public TextRange range() { return range; }

    public Annotation(
        final @NotNull Optional<Long> _projectId,
        final @NotNull Optional<Long> _analysisId,
        final @NotNull AnnotationKind _annotationKind,
        final @NotNull String msg,
        final @NotNull String filename,
        final @NotNull Long _severity,
        final @NotNull String _category,
        final @NotNull Optional<LanguageEnumeration> _language,
        final @NotNull Optional<String> _rule,
        final @NotNull Optional<String> _ruleUrl,
        final @NotNull Optional<String> _tool,
        final @NotNull Optional<String> _description,
        final @NotNull TextRange textRange) {
        this.projectId = _projectId;
        this.analysisId = _analysisId;
        this.annotationKind = _annotationKind;
        this.message = msg;
        this.filename = filename;
        this.severity = Optional.of(_severity);
        this.category = Optional.of(_category);
        this.range = textRange;
        this.language = _language;
        this.description = _description;
        this.rule = _rule;
        this.ruleUrl  = _ruleUrl;
        this.tool = _tool;
    }

    public Annotation(
        final @NotNull Optional<Long> _projectId,
        final @NotNull Optional<Long> _analysisId,
        final @NotNull AnnotationKind _annotationKind,
        final @NotNull String msg,
        final @NotNull String _filename,
        final @NotNull TextRange textRange) {
        this.projectId = _projectId;
        this.analysisId = _analysisId;
        this.annotationKind = _annotationKind;
        this.message = msg;
        this.filename = _filename;
        this.severity = Optional.empty();
        this.category = Optional.empty();
        this.language = Optional.empty();
        this.ruleUrl = Optional.empty();
        this.range = textRange;
        this.description = Optional.empty();
        this.rule = Optional.empty();
        this.tool = Optional.empty();
    }

    /**
     * Filter annotations and removes duplicates if they are
     *  - in the same file
     *  - at the same line
     *  - have the same description
     * @param annotations - list of annotations
     * @return - list of unique annotations
     */
    public static List<Annotation> filterDuplicatesByFileNameLineAndDescription(List<Annotation> annotations) {

        /**
         * Class to distinguish violation per file, message and range.
         */
        class HashValue {
            String filename;
            String message;
            TextRange range;

            HashValue(String f, String m, TextRange t) {
                this.filename = f;
                this.message = m;
                this.range = t;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                HashValue hashValue = (HashValue) o;
                return Objects.equals(filename, hashValue.filename) && Objects.equals(message, hashValue.message) && Objects.equals(range, hashValue.range);
            }

            @Override
            public int hashCode() {
                return Objects.hash(filename, message, range);
            }
        }

        Map<HashValue, List<Annotation>> grouped =
                annotations
                        .stream()
                        .collect(Collectors.groupingBy(v -> new HashValue(v.getFilename(), v.getMessage(), v.range())));
        return grouped.values().stream().map(v -> v.stream().findFirst()).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

    }

}
