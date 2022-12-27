package io.codiga.plugins.jetbrains.dependencies;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.model.Dependency;
import com.google.common.collect.ImmutableList;
import io.codiga.plugins.jetbrains.utils.LanguageUtils;

import java.util.List;

/**
 * This is the main entrypoint to manage dependency. It just needs
 * to be called with the PsiFile and all dependencies in the project
 * are being retrieved.
 *
 */
public class DependencyManagement {
    JavascriptDependency javascriptDependency = new JavascriptDependency();
    PythonDependency pythonDependency = new PythonDependency();
    RubyDependency rubyDependency = new RubyDependency();
    PhpDependency phpDependency = new PhpDependency();

    private static DependencyManagement _INSTANCE = new DependencyManagement();

    public static DependencyManagement getInstance() {
        return _INSTANCE;
    }

    /**
     * Get all the dependencies for a particular file.
     * @param project - the project being edited
     * @return
     */
    public List<Dependency> getDependencies(Project project, VirtualFile virtualFile) {
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());
        switch (language) {
            case JAVASCRIPT:
            case TYPESCRIPT:
                return javascriptDependency.getDependencies(project);
            case PYTHON:
                return pythonDependency.getDependencies(project);
            case RUBY:
                return rubyDependency.getDependencies(project);
            case PHP:
                return phpDependency.getDependencies(project);
            default:
                return ImmutableList.of();
        }
    }
}
