package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ShowPreviousInlineCompletion extends BaseCodeInsightAction implements DumbAware {
    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return (project, editor, psiFile) -> {
            SnippetPreview snippetPreview = SnippetPreview.getInstance(editor);
            if (snippetPreview != null) {
                snippetPreview.showPrevious();
            }
        };
    }

    @Override
    public boolean isValidForLookup() {
        return true;
    }
}
