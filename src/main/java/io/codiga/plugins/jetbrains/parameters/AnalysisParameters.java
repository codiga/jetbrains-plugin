package io.codiga.plugins.jetbrains.parameters;

import io.codiga.api.type.LanguageEnumeration;
import com.intellij.psi.PsiFile;

import java.util.Optional;

import static io.codiga.plugins.jetbrains.graphql.LanguageUtils.getLanguageFromFilename;

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
