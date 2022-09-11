package io.codiga.plugins.jetbrains.services;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Rosie {

    public List<RosieAnnotation> getAnnotations(@NotNull PsiFile psiFile, @NotNull Project project);
}
