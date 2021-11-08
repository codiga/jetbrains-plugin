package io.codiga.plugins.jetbrains.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

public class CodigaCompletionProvider extends CompletionProvider<CompletionParameters> {
    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    CodigaCompletionProvider() {
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        LOGGER.debug("Triggering completion");

        result.addElement(LookupElementBuilder.create("CLADAMOUSSE"));
    }
}
