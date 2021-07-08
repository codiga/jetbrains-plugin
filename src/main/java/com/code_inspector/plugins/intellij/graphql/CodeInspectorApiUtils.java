package com.code_inspector.plugins.intellij.graphql;

import com.code_inspector.api.GetFileAnalysisQuery;
import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotation;
import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotationKind;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils;
import com.code_inspector.plugins.intellij.model.FileLinePair;
import com.code_inspector.plugins.intellij.model.FileOffset;
import com.code_inspector.plugins.intellij.model.LineOffset;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.vcsUtil.VcsUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
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
     * @param projectId - the project identifier on Code Inspector
     * @param analysisId - the analysis identifier on Code Inspector
     * @param violation - the violation to map
     * @param fileOffset - the offset of the violation in the file (in IntelliJ term).
     * @return
     */
    private static final Optional<CodeInspectionAnnotation> mapViolation(Optional<Long> projectId, Optional<Long> analysisId, GetFileDataQuery.Violation violation, FileOffset fileOffset, Map<FileLinePair, FileLinePair> changesFromWorkingDirectory) {
        LOGGER.debug(String.format("mapping violation %s", violation));
        Integer violationLine = ((BigDecimal)violation.line()).toBigInteger().intValue();
        FileLinePair key = new FileLinePair(violation.filename(), violationLine);
        if (changesFromWorkingDirectory.containsKey(key)) {
            FileLinePair value = changesFromWorkingDirectory.get(key);
            String.format("value %s, violation %s", value.getFilename(), violation.filename());
            if (value.getFilename().equals(violation.filename())) {
                violationLine = value.getLineNumber();
            }
        }

        Optional<LineOffset> lineOffset = fileOffset.getLineOffsetAtLine(violationLine);

        if (!lineOffset.isPresent()){
            LOGGER.debug("line offset not found");
            return Optional.empty();
        }

        TextRange range = new TextRange(lineOffset.get().codeStartOffset, lineOffset.get().endOffset);
        return Optional.of(
            new CodeInspectionAnnotation(
                projectId,
                analysisId,
                CodeInspectionAnnotationKind.Violation,
                violation.description(),
                violation.filename(),
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
     * Map a violation from the GraphQL API into an annotation that is later surfaced in IntelliJ
     * This is done for the file analysis endpoint.
     * @param violation - the violation to map
     * @param fileOffset - the offset of the violation in the file (in IntelliJ term).
     * @param projectId - Code Inspector project identifier that is associated with this IntelliJ project
     * @return
     */
    private static final Optional<CodeInspectionAnnotation> mapViolationFromFileAnalysis(
        GetFileAnalysisQuery.Violation violation,
        FileOffset fileOffset,
        String filename,
        Optional<Long> projectId) {
        LOGGER.debug(String.format("mapping violation %s", violation));
        Integer violationLine = ((BigDecimal)violation.line()).toBigInteger().intValue();

        Optional<LineOffset> lineOffset = fileOffset.getLineOffsetAtLine(violationLine);

        if (!lineOffset.isPresent()){
            LOGGER.debug("line offset not found");
            return Optional.empty();
        }

        TextRange range = new TextRange(lineOffset.get().codeStartOffset, lineOffset.get().endOffset);
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
     * Map a complex function into a CodeInspectionAnnotation to be surfaced in IntelliJ
     * @param projectId - the project identifier on Code Inspector
     * @param analysisId - the analysis identifier on Code Inspector
     * @param complexFunction - the complex function
     * @param fileOffset - the offset of the violation in the file (in IntelliJ term).
     * @return
     */
    @VisibleForTesting
    public static final Optional<CodeInspectionAnnotation> mapComplexFunction(
        @NotNull final Optional<Long> projectId,
        @NotNull final Optional<Long> analysisId,
        @NotNull final GetFileDataQuery.ComplexFunction complexFunction,
        @NotNull final FileOffset fileOffset,
        @NotNull final Map<FileLinePair, FileLinePair> changesFromWorkingDirectory) {
        LOGGER.debug(String.format("mapping complex function %s", complexFunction));
        Integer startLine = ((BigDecimal)complexFunction.lineStart()).toBigInteger().intValue();
        Integer complexity = ((BigDecimal)complexFunction.complexity()).toBigInteger().intValue();

        /**
         * Search if there was a change from Git and if then, take the change where it was.
         */
        FileLinePair key = new FileLinePair(complexFunction.filename(), startLine);
        if (changesFromWorkingDirectory.containsKey(key)) {
            FileLinePair value = changesFromWorkingDirectory.get(key);
            if (value.getFilename().equals(complexFunction.filename())) {
                startLine = value.getLineNumber();
            }
        }

        Optional<LineOffset> lineOffset = fileOffset.getLineOffsetAtLine(startLine);

        if (!lineOffset.isPresent()){
            LOGGER.debug("line offset not found");
            return Optional.empty();
        }

        String description = String.format(
            "Function %s is too complex with a cyclomatic complexity of %s. Consider reducing the complexity of the function" +
                "by refactoring it or simplifying its logic.",
            complexFunction.functionName(),
            complexity);

        TextRange range = new TextRange(lineOffset.get().codeStartOffset, lineOffset.get().endOffset);
        return Optional.of(
            new CodeInspectionAnnotation(projectId, analysisId, CodeInspectionAnnotationKind.ComplexFunction,
                description, complexFunction.filename(), range));
    }

    /**
     * Map a long function from the GraphQL API into a CodeInspectionAnnotation
     * @param projectId - the project identifier on Code Inspector
     * @param analysisId - the analysis identifier on Code Inspector
     * @param longFunction - long function complex function
     * @param fileOffset - the offset of the violation in the file (in IntelliJ term).
     * @return
     */
    @VisibleForTesting
    public static final Optional<CodeInspectionAnnotation> mapLongFunction(
        @NotNull final Optional<Long> projectId,
        @NotNull final Optional<Long> analysisId,
        @NotNull final GetFileDataQuery.LongFunction longFunction,
        @NotNull final FileOffset fileOffset,
        @NotNull final Map<FileLinePair, FileLinePair> changesFromWorkingDirectory) {
        LOGGER.debug(String.format("mapping long function %s", longFunction));
        Integer startLine = ((BigDecimal)longFunction.lineStart()).toBigInteger().intValue();
        Integer length = ((BigDecimal)longFunction.length()).toBigInteger().intValue();

        /**
         * Search if there was a change from Git and if then, take the change where it was.
         */
        FileLinePair key = new FileLinePair(longFunction.filename(), startLine);
        if (changesFromWorkingDirectory.containsKey(key)) {
            FileLinePair value = changesFromWorkingDirectory.get(key);
            if (value.getFilename().equals(longFunction.filename())) {
                startLine = value.getLineNumber();
            }
        }

        Optional<LineOffset> lineOffset = fileOffset.getLineOffsetAtLine(startLine);

        if (!lineOffset.isPresent()){
            LOGGER.debug("line offset not found");
            return Optional.empty();
        }

        String description = String.format(
            "Function %s is too long with a length of %s lines. Consider refactoring the function into sub-functions.",
            longFunction.functionName(),
            length);

        TextRange range = new TextRange(lineOffset.get().codeStartOffset, lineOffset.get().endOffset);
        return Optional.of(
            new CodeInspectionAnnotation(
                projectId, analysisId, CodeInspectionAnnotationKind.LongFunction,
                description, longFunction.filename(), range));
    }


    /**
     * Get all the annotations to surface in IntelliJ based on the results from the GraphQL API.
     * @param query - the query results from the GraphQL API.
     * @param psiFile - the file being edited in IntelliJ
     * @return a list of annotation (empty if there is any problem).
     */
    public static List<CodeInspectionAnnotation> getAnnotationsFromProjectQueryResult(GetFileDataQuery.Project query, PsiFile psiFile) {
        Optional<GetFileDataQuery.Analysis> analysisOptional = query.analyses().stream().findFirst();
        if(!analysisOptional.isPresent()) {
            LOGGER.info("no analysis present");
            return NO_ANNOTATION;
        }
        Optional<VirtualFile> repositoryRoot = CodeInspectorGitUtils.getRepositoryRoot(psiFile);

        if (!repositoryRoot.isPresent()) {
            LOGGER.debug("no root found");
            return NO_ANNOTATION;
        }

        GetFileDataQuery.Analysis analysis = analysisOptional.get();

        LOGGER.debug(String.format("received %s annotations", analysis.violations().size()));

        try{
            final String fileContent = new String(psiFile.getVirtualFile().contentsToByteArray(), StandardCharsets.UTF_8);
            final FileOffset fileOffset = new FileOffset(Arrays.asList(fileContent.split("\n")));

            LOGGER.debug(String.format("fileContent = %s", fileContent));

            Map<FileLinePair, FileLinePair> changesFromWorkingDirectory = indexPatchHunks(getPatchesForWorkingDirectoryForFile(psiFile));


            // TODO - map duplicates

            Long analysisId = ((java.math.BigDecimal)analysis.id()).toBigInteger().longValue();
            Long projectId = ((java.math.BigDecimal)query.id()).toBigInteger().longValue();

            List<Optional<CodeInspectionAnnotation>> violationsAnnotations = analysis.violations()
                .stream()
                .map(v -> mapViolation(Optional.of(projectId), Optional.of(analysisId), v, fileOffset, changesFromWorkingDirectory))
                .collect(Collectors.toList());

            List<Optional<CodeInspectionAnnotation>> complexFunctionsAnnotations = analysis.complexFunctions()
                .stream()
                .map(v -> mapComplexFunction(Optional.of(projectId), Optional.of(analysisId), v, fileOffset, changesFromWorkingDirectory))
                .collect(Collectors.toList());

            List<Optional<CodeInspectionAnnotation>> longFunctionsAnnotations = analysis.longFunctions()
                .stream()
                .map(v -> mapLongFunction(Optional.of(projectId), Optional.of(analysisId), v, fileOffset, changesFromWorkingDirectory))
                .collect(Collectors.toList());

            Stream<Optional<CodeInspectionAnnotation>> allAnnotations =
                Stream.concat(Stream.concat(violationsAnnotations.stream(), complexFunctionsAnnotations.stream()), longFunctionsAnnotations.stream());
            return allAnnotations
                    .filter(v -> v.isPresent())
                    .map(v -> v.get())
                    .collect(Collectors.toList());
        } catch (IOException ioe){
            ioe.printStackTrace();
            LOGGER.debug("cannot read file");
            return NO_ANNOTATION;
        }
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

        try{
            final String fileContent = new String(psiFile.getVirtualFile().contentsToByteArray(), StandardCharsets.UTF_8);
            final FileOffset fileOffset = new FileOffset(Arrays.asList(fileContent.split("\n")));
            final String filename = query.filename();

            List<Optional<CodeInspectionAnnotation>> allAnnotations = query.violations()
                .stream()
                .map(v -> mapViolationFromFileAnalysis(v, fileOffset, filename, projectId))
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
        } catch (IOException ioe){
            ioe.printStackTrace();
            LOGGER.debug("cannot read file");
            return NO_ANNOTATION;
        }
    }
}
