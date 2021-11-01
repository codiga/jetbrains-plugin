package com.code_inspector.plugins.intellij.dependencies;

import com.code_inspector.plugins.intellij.model.Dependency;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.Constants.PYTHON_DEPENDENCY_FILE;


public abstract class AbstractDependency {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Find the package file in the files hierarchy of the project.
     * @param psiFile
     * @return
     */
    Optional<VirtualFile> getDependencyFile(PsiFile psiFile) {
        List<VirtualFile> rootFiles = Arrays.stream(ProjectRootManager.getInstance(psiFile.getProject())
                        .getContentRoots())
                .flatMap(v -> Arrays.stream(VfsUtil.getChildren(v)))
                .collect(Collectors.toList());

        // Find package.json in these files
        Optional<VirtualFile> packageFileOptional = rootFiles.stream().filter(v -> v.getName().equalsIgnoreCase(getDependencyFilename())).findFirst();
        return packageFileOptional;
    }

    public List<Dependency> getDependencies(PsiFile psiFile) {
        Optional<VirtualFile> dependencyFile = this.getDependencyFile(psiFile);
        if(!dependencyFile.isPresent()) {
            return ImmutableList.of();
        }

        try {
            InputStream inputStream = dependencyFile.get().getInputStream();
            List<Dependency> result = getDependenciesFromInputStream(inputStream);
            inputStream.close();
            return result;
        } catch (IOException e){
            LOGGER.error("AbstractDependency - getDependenciesFromInputStream - error when opening the file");
            return ImmutableList.of();
        }
    }


    public abstract List<Dependency> getDependenciesFromInputStream(InputStream inputStream);

    public abstract String getDependencyFilename();
}
