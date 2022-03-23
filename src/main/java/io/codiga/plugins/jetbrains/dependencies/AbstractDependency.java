package io.codiga.plugins.jetbrains.dependencies;

import com.intellij.openapi.project.Project;
import io.codiga.plugins.jetbrains.model.Dependency;
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

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;


public abstract class AbstractDependency {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Find the package file in the files hierarchy of the project.
     * @param project - the project we are editing
     * @return
     */
    Optional<VirtualFile> getDependencyFile(Project project) {
        List<VirtualFile> rootFiles = Arrays.stream(ProjectRootManager.getInstance(project)
                        .getContentRoots())
                .flatMap(v -> Arrays.stream(VfsUtil.getChildren(v)))
                .collect(Collectors.toList());

        // Find package.json in these files
        Optional<VirtualFile> packageFileOptional = rootFiles.stream().filter(v -> v.getName().equalsIgnoreCase(getDependencyFilename())).findFirst();
        return packageFileOptional;
    }

    public List<Dependency> getDependencies(Project project) {
        Optional<VirtualFile> dependencyFile = this.getDependencyFile(project);
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
