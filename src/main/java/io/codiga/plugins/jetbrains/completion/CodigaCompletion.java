package io.codiga.plugins.jetbrains.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.diagnostic.Logger;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

public class CodigaCompletion extends CompletionContributor {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    public CodigaCompletion() {
        extend(null,
            psiElement(),
            new CodigaCompletionProvider());
    }
}
