package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.utils.RecipeUtils.addRecipeInEditor;

public class SnippetPreview implements Disposable {

    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);

    public static Key<SnippetPreview> CODIGA_SNIPPET_PREVIEW = Key.create("CODIGA_SNIPPET_PREVIEW");

    private Inlay<SnippetBlockElementRenderer> currentInlay = null;
    List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> suggestions;
    @NotNull Editor editor;
    int offset;

    int currentIndex = 0;

    public SnippetPreview(@NotNull Editor editor, int offset, List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> suggestions) {
        this.editor = editor;
        this.offset = offset;
        this.suggestions = suggestions;
        this.currentIndex = 0;
        this.currentInlay = null;
        EditorUtil.disposeWithEditor(editor, this);

        editor.putUserData(CODIGA_SNIPPET_PREVIEW, this);
    }

    public void display() {
        if(this.currentIndex >= suggestions.size()) {
            return;
        }
        GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet = suggestions.get(this.currentIndex);

        String decodedCode = new String(Base64.getDecoder().decode(snippet.presentableFormat().getBytes(StandardCharsets.UTF_8)));


        SnippetBlockElementRenderer snippetBlockElementRenderer = new SnippetBlockElementRenderer(editor, Arrays.asList(decodedCode.split("\n")));
        currentInlay = this.editor.getInlayModel().addBlockElement(offset, true, false, 1, snippetBlockElementRenderer);
        Disposer.register(this, currentInlay);

    }


    public void showPrevious() {
        LOGGER.info("show previous");
        int nextIndex = (this.currentIndex - 1) % suggestions.size();
        if (nextIndex < 0) {
            nextIndex = this.suggestions.size() - 1;
        }

        Disposer.dispose(currentInlay);
        LOGGER.info("show next");
        this.currentIndex = nextIndex;
        display();
    }

    public void showNext() {
        int nextIndex = (this.currentIndex + 1) % suggestions.size();

        Disposer.dispose(currentInlay);
        LOGGER.info("show next");
        this.currentIndex = nextIndex;
        display();
    }

    public static void clear(@NotNull Editor editor) {
        SnippetPreview snippetPreview = getInstance(editor);
        if (snippetPreview != null) {
            Disposer.dispose(snippetPreview);
        }
    }

    public void apply(Caret caret){
        GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet = suggestions.get(currentIndex);

        addRecipeInEditor(this.editor, snippet.name(), snippet.jetbrainsFormat(), ((BigDecimal)snippet.id()).longValue(), snippet.imports(), snippet.language(), 0, true, codigaApi);
        Disposer.dispose(this);
    }

    public void dispose() {
        editor.putUserData(CODIGA_SNIPPET_PREVIEW, null);
    }


    @Nullable
    public static SnippetPreview getInstance(@NotNull Editor editor) {
        return editor.getUserData(CODIGA_SNIPPET_PREVIEW);
    }
}
