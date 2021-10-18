package com.code_inspector.plugins.intellij.dependencies;

import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.model.Dependency;
import com.google.common.collect.ImmutableList;
import com.intellij.psi.PsiFile;

import java.util.List;

import static com.code_inspector.plugins.intellij.graphql.LanguageUtils.getLanguageFromFilename;

public class DependencyManagement {
    JavascriptDependency javascriptDependency = new JavascriptDependency();
    PythonDependency pythonDependency = new PythonDependency();
    RubyDependency rubyDependency = new RubyDependency();

    /**
     * Get all the dependencies for a particular file.
     * @param psiFile
     * @return
     */
    public List<Dependency> getDependencies(PsiFile psiFile) {
        LanguageEnumeration language = getLanguageFromFilename(psiFile.getVirtualFile().getCanonicalPath());
        switch (language) {
            case JAVASCRIPT:
            case TYPESCRIPT:
                return javascriptDependency.getDependencies(psiFile);
            case PYTHON:
                return pythonDependency.getDependencies(psiFile);
            case RUBY:
                return rubyDependency.getDependencies(psiFile);
            default:
                return ImmutableList.of();
        }
    }
}
