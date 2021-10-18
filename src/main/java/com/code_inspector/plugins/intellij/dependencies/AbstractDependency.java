package com.code_inspector.plugins.intellij.dependencies;

import com.code_inspector.plugins.intellij.model.Dependency;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public abstract class AbstractDependency {

    /**
     * Find the package file in the files hierarchy of the project.
     * @param psiFile
     * @param packageFileName
     * @return
     */
    Optional<VirtualFile> getDependencyFile(PsiFile psiFile, String packageFileName) {
        List<VirtualFile> rootFiles = Arrays.stream(ProjectRootManager.getInstance(psiFile.getProject())
                        .getContentRoots())
                .flatMap(v -> Arrays.stream(VfsUtil.getChildren(v)))
                .collect(Collectors.toList());

        // Find package.json in these files
        Optional<VirtualFile> packageFileOptional = rootFiles.stream().filter(v -> v.getName().equalsIgnoreCase(packageFileName)).findFirst();
        return packageFileOptional;
    }

    public abstract List<Dependency> getDependencies(PsiFile psiFile);
}
