package com.code_inspector.plugins.intellij.parameters;

import com.code_inspector.api.type.LanguageEnumeration;
import com.intellij.psi.PsiFile;

import java.util.Optional;

import static com.code_inspector.plugins.intellij.graphql.LanguageUtils.getLanguageFromFilename;

public final class AnalysisParameters {

    private AnalysisParameters() {}

    public static Optional<String> getAnalysisParameters(PsiFile psiFile) {

        LanguageEnumeration language = getLanguageFromFilename(psiFile.getVirtualFile().getCanonicalPath());

        switch (language) {
            case JAVASCRIPT:
            case TYPESCRIPT:
                return AnalysisParametersJavascript.getAnalysisParameters(psiFile);
            default:
                return Optional.empty();
        }

    }
}
