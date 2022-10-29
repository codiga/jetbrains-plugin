package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

/**
 * Show the next snippet if we have a current snippet suggestion.
 */
public class ShowNextInlineCompletion extends BaseCodeInsightAction implements DumbAware {
    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return (project, editor, psiFile) -> {
            SnippetPreview snippetPreview = SnippetPreview.getInstance(editor);
            if (snippetPreview != null) {
                snippetPreview.showNext();
            }
        };
    }

    @Override
    public boolean isValidForLookup() {
        return true;
    }
}
