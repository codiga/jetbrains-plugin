package com.code_inspector.plugins.intellij.annotators;

import com.code_inspector.api.type.LanguageEnumeration;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This class represent an annotation from Code Inspector with the different attributes coming from the API
 * and how they are surfaced in the UI.
 */
public class CodeInspectionAnnotation {

    private final @NotNull CodeInspectionAnnotationKind annotationKind;
    private final @NotNull Long projectId;
    private final @NotNull Long analysisId;
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

    @NotNull public CodeInspectionAnnotationKind getAnnotationKind() { return annotationKind; };
    @NotNull public Long getProjectId() { return projectId; }
    @NotNull public Long getAnalysisId() { return analysisId; }
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

    public CodeInspectionAnnotation(
        final @NotNull Long _projectId,
        final @NotNull Long _analysisId,
        final @NotNull CodeInspectionAnnotationKind _annotationKind,
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

    public CodeInspectionAnnotation(
        final @NotNull Long _projectId,
        final @NotNull Long _analysisId,
        final @NotNull CodeInspectionAnnotationKind _annotationKind,
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
}
