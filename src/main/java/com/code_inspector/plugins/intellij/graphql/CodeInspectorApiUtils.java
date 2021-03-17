package com.code_inspector.plugins.intellij.graphql;

import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotation;
import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotationKind;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils;
import com.code_inspector.plugins.intellij.model.FileLinePair;
import com.code_inspector.plugins.intellij.model.FileOffset;
import com.code_inspector.plugins.intellij.model.LineOffset;
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


import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.Constants.NO_ANNOTATION;
import static com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils.getPatchesForWorkingDirectoryForFile;
import static com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils.indexPatchHunks;

/**
 * Utility class to convert data from the GraphQL API into data we can use for annotating the
 * source code.
 */
public class CodeInspectorApiUtils {

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
    private static final Optional<CodeInspectionAnnotation> mapViolation(Long projectId, Long analysisId, GetFileDataQuery.Violation violation, FileOffset fileOffset, Map<FileLinePair, FileLinePair> changesFromWorkingDirectory) {
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

        if (lineOffset.isEmpty()){
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
     * Get all the annotations to surface in IntelliJ based on the results from the GraphQL API.
     * @param query - the query results from the GraphQL API.
     * @param psiFile - the file being edited in IntelliJ
     * @return a list of annotation (empty if there is any problem).
     */
    public static List<CodeInspectionAnnotation> getAnnotationsFromQueryResult(GetFileDataQuery.Project query, PsiFile psiFile) {
        Optional<GetFileDataQuery.Analysis> analysisOptional = query.analyses().stream().findFirst();
        if(analysisOptional.isEmpty()) {
            LOGGER.info("no analysis present");
            return NO_ANNOTATION;
        }
        Optional<VirtualFile> repositoryRoot = CodeInspectorGitUtils.getRepositoryRoot(psiFile);

        if (repositoryRoot.isEmpty()) {
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


            // TODO - map duplicates, long functions and complex functions

            Long analysisId = ((java.math.BigDecimal)analysis.id()).toBigInteger().longValue();
            Long projectId = ((java.math.BigDecimal)query.id()).toBigInteger().longValue();

            return analysis.violations()
                .stream()
                .map(v -> mapViolation(projectId, analysisId, v, fileOffset, changesFromWorkingDirectory))
                .filter( v -> v.isPresent())
                .map(v -> v.get())
                .collect(Collectors.toList());
        } catch (IOException ioe){
            ioe.printStackTrace();
            LOGGER.debug("cannot read file");
            return NO_ANNOTATION;
        }

    }
}
