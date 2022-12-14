package io.codiga.plugins.jetbrains.annotators;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotation;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotationJetBrains;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolationFix;
import io.codiga.plugins.jetbrains.rosie.RosieApi;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.model.rosie.RosieConstants.*;
import static io.codiga.plugins.jetbrains.ui.UIConstants.ANNOTATION_PREFIX;
import static java.util.stream.Collectors.toList;

/**
 * Source type for {@code RosieAnnotator#collectInformation()}.
 */
class RosieAnnotatorInformation {
    public Project project;
    public PsiFile psiFile;
    public Editor editor;

    public RosieAnnotatorInformation(Project project, PsiFile psiFile, Editor editor) {
        this.project = project;
        this.psiFile = psiFile;
        this.editor = editor;
    }
}

/**
 * Annotates the current Editor with information returned by the Rosie service,
 * and when applicable, provides quick fixes for those code violations.
 * <p>
 * Type hierarchy of an annotation fix:
 * <pre>
 * - {@link RosieAnnotator}
 *   - {@link RosieAnnotationFix}
 *     - {@link RosieViolationFix}
 *       - {@link io.codiga.plugins.jetbrains.model.rosie.RosieViolationFixEdit}
 *         - {@link io.codiga.plugins.jetbrains.model.rosie.RosiePosition}
 * </pre>
 */
public class RosieAnnotator extends ExternalAnnotator<RosieAnnotatorInformation, List<RosieAnnotationJetBrains>> {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final RosieApi rosieApi = ApplicationManager.getApplication().getService(RosieApi.class);
    private final AppSettingsState settings = AppSettingsState.getInstance();

    /**
     * This function collects all the information at startup (see the doc of the abstract class).
     * For now, we are not doing anything, we rather use doAnnotate that runs the long running
     * computation.
     *
     * @param psiFile   - the file where information is being collected
     * @param editor    - the editor where the function is triggered
     * @param hasErrors - report if it has error
     */
    @Override
    @Nullable
    public RosieAnnotatorInformation collectInformation(
        @NotNull PsiFile psiFile, @NotNull Editor editor, boolean hasErrors) {
        LOGGER.debug("call collectInformation()");
        return new RosieAnnotatorInformation(editor.getProject(), psiFile, editor);
    }


    /**
     * Gather all the annotations from the Codiga API and generates a list of annotation
     * to surface later in the UI.
     *
     * @param rosieAnnotatorInformation - annotations necessary
     * @return the list of annotation to surface.
     */
    @Nullable
    @Override
    public List<RosieAnnotationJetBrains> doAnnotate(RosieAnnotatorInformation rosieAnnotatorInformation) {
        if (!settings.getCodigaEnabled()) {
            LOGGER.debug("codiga disabled");
            return List.of();
        }

        long startTime = System.currentTimeMillis();
        List<RosieAnnotationJetBrains> annotations = rosieApi
            .getAnnotations(rosieAnnotatorInformation.psiFile, rosieAnnotatorInformation.project)
            .stream()
            .map(annotation -> convertToAnnotationJetBrains(annotation, rosieAnnotatorInformation.editor))
            .filter(Objects::nonNull)
            .collect(toList());
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        LOGGER.info(String.format("rosie call time roundtrip: %s", executionTime));
        return annotations;
    }

    /**
     * Converts the argument {@code RosieAnnotation} to {@code RosieAnnotationJetBrains}.
     * <p>
     * If there is an {@code IndexOutOfBoundsException} during conversion, specifically when trying to calculate
     * the line start index in the given editor for a {@code RosiePosition.line} value that is outside the editor's
     * range, it returns null, so that it can be filtered out.
     */
    @Nullable
    private static RosieAnnotationJetBrains convertToAnnotationJetBrains(RosieAnnotation annotation, Editor editor) {
        try {
            return new RosieAnnotationJetBrains(annotation, editor);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.warn(String.format(
                "[RosieAnnotator] Issue during calculating the line start offset in the editor, for start line %d, or for end line %d.",
                annotation.getStart().line,
                annotation.getEnd().line));
            return null;
        }
    }

    /**
     * Create all the UI elements to create an annotation.
     *
     * @param psiFile     - the file to annotate
     * @param annotations - the list of annotations previously reported by doAnnotate
     * @param holder      object to add annotations
     */
    @Override
    public void apply(
        @NotNull PsiFile psiFile, List<RosieAnnotationJetBrains> annotations, @NotNull AnnotationHolder holder) {
        // No annotation = nothing to do, just return now. If not enabled for this project, we return no annotations
        // and will stop here.
        if (annotations == null || annotations.isEmpty()) {
            return;
        }

        LOGGER.debug(String.format("Received %s annotations", annotations.size()));
        for (RosieAnnotationJetBrains annotation : annotations) {
            generateAnnotationForViolation(psiFile, annotation, holder);
        }
    }

    /**
     * Generate an annotation for a violation with possible fixes.
     * <p>
     * The following quick fixes are always added:
     * <ul>
     *     <li>A fix for disabling the Rosie analysis for the current line, adding a {@code codiga-disable}
     *     comment line above the current one.</li>
     *     <li>A "fix" for opening the rule information in the browser is always added.</li>
     * </ul>
     *
     * @param psiFile    - the file to annotate
     * @param annotation - the annotation we need
     * @param holder     - the holder of the annotation
     */
    private void generateAnnotationForViolation(
        @NotNull final PsiFile psiFile,
        @NotNull final RosieAnnotationJetBrains annotation,
        @NotNull AnnotationHolder holder) {

        //If the annotation starts later than it ends, don't annotate
        if (annotation.getStart() > annotation.getEnd()) {
            return;
        }

        final String message = String.format("%s (%s)", annotation.getMessage(), ANNOTATION_PREFIX);
        final TextRange fileRange = psiFile.getTextRange();

        TextRange annotationRange = new TextRange(annotation.getStart(), annotation.getEnd());
        LOGGER.debug("annotation range: " + annotationRange);

        if (!fileRange.containsOffset(annotationRange.getEndOffset()) ||
            !fileRange.containsOffset(annotationRange.getStartOffset())) {
            LOGGER.debug("range outside of the scope");
            return;
        }

        var highlightTypes = getHighlightTypes(annotation.getSeverity());
        AnnotationBuilder annotationBuilder = holder
            .newAnnotation(highlightTypes.first, message)
            .highlightType(highlightTypes.second)
            .range(annotationRange);

        for (RosieViolationFix rosieViolationFix : annotation.getFixes()) {
            annotationBuilder.withFix(new RosieAnnotationFix(rosieViolationFix));
        }
        annotationBuilder
            .withFix(new DisableRosieAnalysisFix(annotation.getRuleName(), annotation))
            .withFix(new AnnotationFixOpenBrowser(annotation));


        LOGGER.info("Creating annotation with range: " + annotationRange);
        annotationBuilder.create();
    }

    private Pair<HighlightSeverity, ProblemHighlightType> getHighlightTypes(String rosieSeverity) {
        if (rosieSeverity.equalsIgnoreCase(ROSIE_SEVERITY_CRITICAL)) {
            return Pair.create(HighlightSeverity.ERROR, ProblemHighlightType.ERROR);
        }
        if (rosieSeverity.equalsIgnoreCase(ROSIE_SEVERITY_ERROR)) {
            return Pair.create(HighlightSeverity.WARNING, ProblemHighlightType.WARNING);
        }
        if (rosieSeverity.equalsIgnoreCase(ROSIE_SEVERITY_WARNING)) {
            return Pair.create(HighlightSeverity.WEAK_WARNING, ProblemHighlightType.WEAK_WARNING);
        }
        return Pair.create(HighlightSeverity.WEAK_WARNING, ProblemHighlightType.INFORMATION);
    }
}