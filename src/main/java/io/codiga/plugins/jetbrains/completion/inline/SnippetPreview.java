package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class SnippetPreview implements Disposable {

    public static Key<SnippetPreview> CODIGA_SNIPPET_PREVIEW = Key.create("CODIGA_SNIPPET_PREVIEW");

    List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> suggestions;
    @NotNull Editor editor;
    int offset;


    public SnippetPreview(@NotNull Editor editor, int offset, List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> suggestions) {
        this.editor = editor;
        this.offset = offset;
        this.suggestions = suggestions;
        EditorUtil.disposeWithEditor(editor, this);

        editor.putUserData(CODIGA_SNIPPET_PREVIEW, this);
    }

    public void display() {
        GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet = suggestions.get(0);

        String decodedCode = new String(Base64.getDecoder().decode(snippet.presentableFormat().getBytes(StandardCharsets.UTF_8)));


        SnippetBlockElementRenderer snippetBlockElementRenderer = new SnippetBlockElementRenderer(editor, Arrays.asList(decodedCode.split("\n")));
        Inlay<SnippetBlockElementRenderer> editorCustomElementRenderer = this.editor.getInlayModel().addBlockElement(offset, true, false, 1, snippetBlockElementRenderer);
        Disposer.register(this, editorCustomElementRenderer);

    }

    public static void clear(@NotNull Editor editor) {
        SnippetPreview snippetPreview = getInstance(editor);
        if (snippetPreview != null) {
            Disposer.dispose(snippetPreview);
        }
    }

    public void dispose() {
        editor.putUserData(CODIGA_SNIPPET_PREVIEW, null);
    }


    @Nullable
    public static SnippetPreview getInstance(@NotNull Editor editor) {
        return editor.getUserData(CODIGA_SNIPPET_PREVIEW);
    }
}
