package com.code_inspector.plugins.intellij.annotators;

import com.code_inspector.api.GetFileAnalysisQuery;
import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.cache.AnalysisDataCache;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils;
import com.code_inspector.plugins.intellij.graphql.GraphQlQueryException;
import com.code_inspector.plugins.intellij.settings.project.ProjectSettingsState;
import com.google.common.collect.ImmutableList;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.code_inspector.plugins.intellij.Constants.INVALID_PROJECT_ID;
import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.Constants.NO_ANNOTATION;
import static com.code_inspector.plugins.intellij.git.CodeInspectorGitUtils.getFileStatus;
import static com.code_inspector.plugins.intellij.graphql.CodeInspectorApiUtils.getAnnotationsFromFileAnalysisQueryResult;
import static com.code_inspector.plugins.intellij.graphql.CodeInspectorApiUtils.getAnnotationsFromProjectQueryResult;
import static com.code_inspector.plugins.intellij.ui.NotificationUtils.*;
import static com.code_inspector.plugins.intellij.ui.UIConstants.ANNOTATION_PREFIX;

public class CodeInspectorExternalAnnotator extends ExternalAnnotator<PsiFile, List<CodeInspectionAnnotation>> {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    @Nullable
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    /**
     * This function collects all the information at startup (see the doc of the abstract class).
     * For now, we are not doing anything, we rather use doAnnotate that runs the long running
     * computation.
     * @param psiFile - the file where information is being collected
     * @param editor - the editor where the function is triggered
     * @param hasErrors - report if it has error
     * @return the PsiFile - return the initial file, no modification is being done.
     */
    @Override
    @Nullable
    public PsiFile collectInformation(
        @NotNull PsiFile psiFile, @NotNull Editor editor, boolean hasErrors) {
        LOGGER.debug("call collectInformation()");
        return psiFile;
    }


    @Nullable
    private List<CodeInspectionAnnotation> getAnnotationFromFileAnalysis(PsiFile psiFile, Optional<Long> projectId) {
        final String filename = psiFile.getName();
        final String code = psiFile.getText();

        LOGGER.debug(String.format("calling doAnnotate on file %s, type %s", filename, psiFile.getLanguage()));

        if (code.isEmpty()) {
            return ImmutableList.of();
        }

        final long startAnalysisTimeMillis = System.currentTimeMillis();

        Optional<GetFileAnalysisQuery.GetFileAnalysis> queryResult;
        try {
            queryResult = AnalysisDataCache
                .getInstance()
                .getViolationsFromFileAnalysis(projectId, filename, code);
        } catch (GraphQlQueryException e) {
            LOGGER.debug("receive invalid graphql call, sending notification");
            notififyProjectOnce(psiFile.getProject(), NOTIFICATION_API_KEYS_INCORRECT, NOTIFICATION_GROUP_API);
            queryResult = Optional.empty();

        }

        final long endAnalysisTimeMillis = System.currentTimeMillis();

        LOGGER.debug(String.format("Analysis time for file %s: %s ms",
            filename,
            endAnalysisTimeMillis - startAnalysisTimeMillis));

        if (queryResult.isPresent()){
            List<CodeInspectionAnnotation> res = getAnnotationsFromFileAnalysisQueryResult(queryResult.get(), psiFile);
            LOGGER.debug(String.format("number of annotations for file %s: %s", filename, res.size()));
            return res;
        } else {
            LOGGER.debug(String.format("No result for file %s", filename));
            return ImmutableList.of();
        }

    }

    @Nullable
    private List<CodeInspectionAnnotation> getAnnotationFromProjectAnalysis(PsiFile psiFile, Long projectId) {
        LOGGER.debug(String.format(
            "calling getAnnotationFromExistingAnalysis on file %s, type %s",
            psiFile.getVirtualFile().getPath(),
            psiFile.getLanguage()));

        Optional<String> revision = CodeInspectorGitUtils.getGitRevision(psiFile);

        if (!revision.isPresent()) {
            LOGGER.info("cannot get file revision");
            return NO_ANNOTATION;
        }

        Optional<String> filePath = CodeInspectorGitUtils.getFilePathInRepository(psiFile);

        if (!filePath.isPresent()) {
            LOGGER.info("cannot get file patch");
            return NO_ANNOTATION;
        }

        Optional<GetFileDataQuery.Project> query;
        try {
            query = AnalysisDataCache
                .getInstance()
                .getViolationsFromProjectAnalysis(projectId, revision.get(), filePath.get());
        } catch (GraphQlQueryException e) {
            LOGGER.debug("receive invalid graphql call, sending notification");
            notififyProjectOnce(psiFile.getProject(), NOTIFICATION_API_KEYS_INCORRECT, NOTIFICATION_GROUP_API);
            query = Optional.empty();
        }


        if (!query.isPresent()) {
            LOGGER.info("no data from query");
            return NO_ANNOTATION;
        }

        // If the API took too long, check that this is still okay to proceed.
        ProgressManager.checkCanceled();

        return getAnnotationsFromProjectQueryResult(query.get(), psiFile);
    }

    /**
     * Gather all the annotations from the Code Inspector API and generates a list of annotation
     * to surface later in the UI.
     * @param psiFile - the file to inspect.
     * @return the list of annotation to surface.
     */
    @Nullable
    @Override
    public List<CodeInspectionAnnotation> doAnnotate(PsiFile psiFile) {
        final FileStatus fileStatus = getFileStatus(psiFile);

        LOGGER.info("calling doAnnotate on file: " + psiFile.getName());
        final ProjectSettingsState PROJECT_SETTINGS = ProjectSettingsState.getInstance(psiFile.getProject());

        final ProjectSettingsState settings = ProjectSettingsState.getInstance(psiFile.getProject());

        ProgressManager.checkCanceled();

        /*
         * If the project is analyzed by Code Inspector and the file not modified, get that data from
         * the existing Code Inspector Analysis from our backend.
         */
        if(fileStatus == FileStatus.NOT_CHANGED &&
            PROJECT_SETTINGS.isEnabled && !settings.projectId.equals(INVALID_PROJECT_ID)) {
            LOGGER.debug("Get data from project analysis");
            return getAnnotationFromProjectAnalysis(psiFile, settings.projectId);
        } else {
            Optional<Long> projectId = Optional.empty();
            if(settings.projectId.equals(INVALID_PROJECT_ID)) {
                projectId = Optional.of(settings.projectId);
            }
            LOGGER.debug("Get data from file analysis");
            return getAnnotationFromFileAnalysis(psiFile, projectId);
        }
    }

    /**
     * Get the HighlightSeverity for an annotation
     * @param annotation - the annotation we want to inspect
     * @return the corresponding HighlightSeverity value
     */
    private HighlightSeverity getHighlightSeverityForViolation(CodeInspectionAnnotation annotation) {
        if (annotation.getSeverity().isPresent()) {
            switch (annotation.getSeverity().get().intValue()) {
                case 1:
                    return HighlightSeverity.ERROR;
                case 2:
                    return HighlightSeverity.WARNING;
                case 3:
                default:
                    return HighlightSeverity.WEAK_WARNING;
            }
        }
        return HighlightSeverity.WEAK_WARNING;
    }

    /**
     * Get the ProblemHighlightType for an annotation
     * @param annotation - the annotation we want to inspect
     * @return the corresponding ProblemHighlightType value
     */
    private ProblemHighlightType getProblemHighlightTypeForViolation(CodeInspectionAnnotation annotation) {
        if (annotation.getSeverity().isPresent()) {
            switch (annotation.getSeverity().get().intValue()) {
                case 1:
                    return ProblemHighlightType.ERROR;
                case 2:
                    return ProblemHighlightType.WARNING;
                default:
                    return ProblemHighlightType.WEAK_WARNING;
            }
        }
        return ProblemHighlightType.WEAK_WARNING;
    }

    /**
     * Generate an annotation for a violation
     * @param psiFile - the file to annotate
     * @param annotation - the annotation we need
     * @param holder - the holder of the annotation
     */
    private void generateAnnotationForViolation(
        @NotNull final PsiFile psiFile,
        @NotNull final CodeInspectionAnnotation annotation,
        @NotNull AnnotationHolder holder) {
        final Optional<Long> projectId = annotation.getProjectId();

        final String message = String.format("%s (%s)", annotation.getMessage(), ANNOTATION_PREFIX);

        final TextRange textRange = psiFile.getTextRange();

        if (!textRange.contains(annotation.range().getEndOffset()) ||
            !textRange.contains(annotation.range().getStartOffset())) {
            LOGGER.debug("range outside of the scope");
            return;
        }

        AnnotationBuilder annotationBuilder = holder
            .newAnnotation(getHighlightSeverityForViolation(annotation), message)
            .highlightType(getProblemHighlightTypeForViolation(annotation))
            .range(annotation.range());

        /*
         * We create two fixes to ignore the violation (if possible): one to ignore the violation
         * at the file level (with Optional.of(filename)) and one without the file (with Optional.empty()).
         */
        if (annotation.getRule().isPresent() && annotation.getTool().isPresent() &&
            annotation.getDescription().isPresent() && annotation.getLanguage().isPresent() &&
            projectId.isPresent()) {
            LOGGER.debug("Adding fix for annotation");
            annotationBuilder = annotationBuilder
                .withFix(
                    new CodeInspectionAnnotationFixIgnore(
                        psiFile, projectId.get(), Optional.of(annotation.getFilename()), annotation.getRule().get(),
                        annotation.getLanguage().get(), annotation.getTool().get()))
                .withFix(
                    new CodeInspectionAnnotationFixIgnore(
                        psiFile, projectId.get(), Optional.empty(), annotation.getRule().get(),
                        annotation.getLanguage().get(), annotation.getTool().get()));
        }

        /*
         * If there is a URL associated with the rule, add an action to send the user to the description
         * of the rule.
         */
        if (annotation.getRuleUrl().isPresent()) {
            annotationBuilder = annotationBuilder.withFix(
                new CodeInspectionAnnotationFixLearnMore(annotation.getRuleUrl().get()));
        }

        if (projectId.isPresent() && annotation.getAnalysisId().isPresent()) {
            annotationBuilder = annotationBuilder.withFix(
                new CodeInspectionAnnotationFixOpenBrowser(
                    projectId.get(),
                    annotation.getAnalysisId().get(),
                    annotation.getFilename()));

        }

        annotationBuilder.create();
    }

    /**
     * Create all the UI elements to create an annotation.
     * @param psiFile - the file to annotate
     * @param annotations - the list of annotations previously reported by doAnnotate
     * @param holder object to add annotations
     */
    @Override
    public void apply(
        @NotNull PsiFile psiFile,
        List<CodeInspectionAnnotation> annotations,
        @NotNull AnnotationHolder holder) {
        // No annotation = nothing to do, just return now. If not enabled for this project, we return no annotations
        // and will stop here.
        if (annotations == null || annotations.isEmpty()) {
            return;
        }

        LOGGER.debug(String.format("Received %s annotations", annotations.size()));
        for (CodeInspectionAnnotation annotation : annotations) {
            generateAnnotationForViolation(psiFile, annotation, holder);
        }
    }
}
