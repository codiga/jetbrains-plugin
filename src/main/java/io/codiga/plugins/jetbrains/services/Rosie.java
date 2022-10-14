package io.codiga.plugins.jetbrains.services;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Service for retrieving Rosie specific information from the Codiga API.
 */
public interface Rosie {

    /**
     * Returns the annotations from the Codiga API based on the argument file, based on which code annotation
     * will be applied in the currently selected and active editor.
     *
     * @param psiFile the file to query Rosie information for
     * @param project the current project
     * @return the list of Rosie annotations
     */
    @NotNull
    List<RosieAnnotation> getAnnotations(@NotNull PsiFile psiFile, @NotNull Project project);
}
