package com.code_inspector.plugins.intellij.graphql;

import com.code_inspector.api.GetFileAnalysisQuery;
import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotation;
import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotationKind;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils;
import com.code_inspector.plugins.intellij.model.FileLinePair;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.Constants.NO_ANNOTATION;
import static com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils.getPatchesForWorkingDirectoryForFile;
import static com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils.indexPatchHunks;

/**
 * Utility class to convert data from the GraphQL API into data we can use for annotating the
 * source code.
 */
public final class CodeInspectorApiUtils {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Private construction to avoid class instantiation.
     */
    private CodeInspectorApiUtils() {
        // should not instantiate that class
    }

    /**
     * Map a violation from the GraphQL API into an annotation that is later surfaced in IntelliJ
     * This is done for the file analysis endpoint.
     * @param violation - the violation to map
     * @param psiFile - the file where we have the violation.
     * @param projectId - Code Inspector project identifier that is associated with this IntelliJ project
     * @return
     */
    private static final Optional<CodeInspectionAnnotation> mapViolationFromFileAnalysis(
        GetFileAnalysisQuery.Violation violation,
        PsiFile psiFile,
        String filename,
        Optional<Long> projectId) {
        LOGGER.debug(String.format("mapping violation %s", violation));
        int violationLine = ((BigDecimal)violation.line()).toBigInteger().intValue();

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(psiFile.getProject());

        if(documentManager == null){
            LOGGER.debug("documentManager null");
            return Optional.empty();
        }

        Document document = documentManager.getDocument(psiFile);

        if(document == null){
            LOGGER.debug("document null");
            return Optional.empty();
        }

        /**
         * If the line is 0, violationLine - 1 will be -1 and
         * throw an exception.
         */
        if (violationLine < 1) {
            return Optional.empty();
        }

        int startOffset = document.getLineStartOffset(violationLine - 1);
        int endOffset = document.getLineEndOffset(violationLine - 1);

        String lineText = document.getText(new TextRange(startOffset, endOffset));
        int pos = 0;
        while (pos < lineText.length() && Character.isSpaceChar(lineText.charAt(pos))) {
            startOffset = startOffset + 1;
            pos = pos + 1;
        }

        /**
         * No point of adding a violation where the start offset is equal or bigger
         * than the end offset.
         */
        if (startOffset >= endOffset) {
            return Optional.empty();
        }

        TextRange range = new TextRange(startOffset, endOffset);
        return Optional.of(
            new CodeInspectionAnnotation(
                projectId,
                Optional.empty(),
                CodeInspectionAnnotationKind.Violation,
                violation.description(),
                filename,
                ((java.math.BigDecimal)violation.severity()).toBigInteger().longValue(),
                violation.category().toString(),
                Optional.ofNullable(violation.language()),
                Optional.ofNullable(violation.rule()),
                Optional.ofNullable(violation.ruleUrl()),
                Optional.ofNullable(violation.tool()),
                Optional.of(violation.description()),
                range));
    }

    /**
     * Get all the annotations to surface in IntelliJ based on the results from the GraphQL API.
     * @param query - the query results from the GraphQL API.
     * @param psiFile - the file being edited in IntelliJ
     * @param projectId - the Code Inspector project id being used and associated
     * @return a list of annotation (empty if there is any problem).
     */
    public static List<CodeInspectionAnnotation> getAnnotationsFromFileAnalysisQueryResult(
        GetFileAnalysisQuery.GetFileAnalysis query, PsiFile psiFile, Optional<Long> projectId) {

        LOGGER.debug(String.format("received %s annotations", query.violations().size()));

        final String filename = query.filename();

        List<Optional<CodeInspectionAnnotation>> allAnnotations = query.violations()
            .stream()
            .map(v -> mapViolationFromFileAnalysis(v, psiFile, filename, projectId))
            .collect(Collectors.toList());

        List<CodeInspectionAnnotation> allAnnotationsWithoutEmpty = allAnnotations
            .stream()
            .filter(v -> v.isPresent())
            .map(v -> v.get())
            .collect(Collectors.toList());

        /**
         * Before returning the list of errors, make sure we filter
         * the error with the exact same message on the same text range.
         */
        return CodeInspectionAnnotation.filterDuplicatesByFileNameLineAndDescription(allAnnotationsWithoutEmpty);
    }
}
