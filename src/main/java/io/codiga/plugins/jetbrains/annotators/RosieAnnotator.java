package io.codiga.plugins.jetbrains.annotators;


import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotationJetBrains;
import io.codiga.plugins.jetbrains.services.Rosie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.ui.UIConstants.ANNOTATION_PREFIX;

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

public class RosieAnnotator extends com.intellij.lang.annotation.ExternalAnnotator<RosieAnnotatorInformation, List<RosieAnnotationJetBrains>> {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private final Rosie rosieService = ApplicationManager.getApplication().getService(Rosie.class);


    /**
     * This function collects all the information at startup (see the doc of the abstract class).
     * For now, we are not doing anything, we rather use doAnnotate that runs the long running
     * computation.
     *
     * @param psiFile   - the file where information is being collected
     * @param editor    - the editor where the function is triggered
     * @param hasErrors - report if it has error
     * @return the PsiFile - return the initial file, no modification is being done.
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

        long startTime = System.currentTimeMillis();
        List<RosieAnnotationJetBrains> annotations = rosieService
            .getAnnotations(rosieAnnotatorInformation.psiFile, rosieAnnotatorInformation.project)
            .stream().map(annotation -> new RosieAnnotationJetBrains(
                annotation.getRuleName(),
                annotation.getMessage(),
                annotation.getSeverity(),
                annotation.getCategory(),
                annotation.getStart(),
                annotation.getEnd(),
                rosieAnnotatorInformation.editor))
            .collect(Collectors.toList());
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        LOGGER.info(String.format("rosie call time roundtrip: %s", executionTime));
        return annotations;
    }


    /**
     * Generate an annotation for a violation
     *
     * @param psiFile    - the file to annotate
     * @param annotation - the annotation we need
     * @param holder     - the holder of the annotation
     */
    private void generateAnnotationForViolation(
        @NotNull final PsiFile psiFile,
        @NotNull final RosieAnnotationJetBrains annotation,
        @NotNull AnnotationHolder holder) {

        final String message = String.format("%s (%s)", annotation.getMessage(), ANNOTATION_PREFIX);

        final TextRange textRange = psiFile.getTextRange();

        TextRange annotationRange = new TextRange(annotation.getStart(), annotation.getEnd());
        LOGGER.debug("annotation range: " + annotationRange);

        if (!textRange.contains(annotationRange.getEndOffset()) ||
            !textRange.contains(annotationRange.getStartOffset())) {
            LOGGER.debug("range outside of the scope");
            return;
        }

        AnnotationBuilder annotationBuilder = holder
            .newAnnotation(HighlightSeverity.ERROR, message)
            .highlightType(ProblemHighlightType.ERROR)
            .range(annotationRange);
        LOGGER.info("Creating annotation with range: " + annotationRange);
        annotationBuilder.create();
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
        @NotNull PsiFile psiFile,
        List<RosieAnnotationJetBrains> annotations,
        @NotNull AnnotationHolder holder) {
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
}