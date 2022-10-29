package io.codiga.plugins.jetbrains.completion.shortcuts;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;

public class CodigaCompletion extends CompletionContributor {
    public CodigaCompletion() {
        extend(CompletionType.BASIC,
            PlatformPatterns.not(PlatformPatterns.alwaysFalse()),
            new CodigaCompletionProvider());
    }
}



