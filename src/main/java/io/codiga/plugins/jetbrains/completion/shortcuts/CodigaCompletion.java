package io.codiga.plugins.jetbrains.completion.shortcuts;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.patterns.PlatformPatterns;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

public class CodigaCompletion extends CompletionContributor {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    public CodigaCompletion() {
        extend(CompletionType.BASIC,
            PlatformPatterns.not(PlatformPatterns.alwaysFalse()),
            new CodigaCompletionProvider());
    }
}



