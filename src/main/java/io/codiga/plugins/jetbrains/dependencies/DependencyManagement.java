package io.codiga.plugins.jetbrains.dependencies;

import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.model.Dependency;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.PsiFile;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;

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

    /**
     * Get all the dependencies for a particular file.
     * @param psiFile
     * @return
     */
    public List<Dependency> getDependencies(PsiFile psiFile) {
        LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(psiFile.getVirtualFile().getCanonicalPath());
        switch (language) {
            case JAVASCRIPT:
            case TYPESCRIPT:
                return javascriptDependency.getDependencies(psiFile);
            case PYTHON:
                return pythonDependency.getDependencies(psiFile);
            case RUBY:
                return rubyDependency.getDependencies(psiFile);
            case PHP:
                return phpDependency.getDependencies(psiFile);
            default:
                return ImmutableList.of();
        }
    }
}
