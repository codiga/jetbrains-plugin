package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import io.codiga.plugins.jetbrains.model.Dependency;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.actions.ActionUtils.getUnitRelativeFilenamePathFromEditorForVirtualFile;
import static io.codiga.plugins.jetbrains.utils.EditorUtils.getActiveEditor;
import static io.codiga.plugins.jetbrains.utils.LanguageUtils.*;

public class InlineDocumentListener implements DocumentListener {
    private static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private static final int TIMEOUT_REQUEST_POLLING_MILLISECONDS = 500;
    private final DependencyManagement dependencyManagement = new DependencyManagement();
    private final Alarm updateListAlarm = new Alarm();

    private long lastRequestTimestamp = 0;

    @Override
    public void documentChanged(@NotNull DocumentEvent documentEvent) {
        final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
        final AppSettingsState settings = AppSettingsState.getInstance();
        if (documentEvent.getDocument().isInBulkUpdate()) {
            LOGGER.debug("bulk update - skipping");
            return;
        }

        Document document = documentEvent.getDocument();
        Editor editor = getActiveEditor(document);


        if (editor == null) {
            LOGGER.debug("editor is null");
            return;
        }
        
        Project project = editor.getProject();

        if (project == null) {
            LOGGER.debug("project is null");
            return;
        }

        if (!settings.getCodigaEnabled()) {
            LOGGER.debug("codiga disabled");
            return;
        }

        if (!settings.getUseInlineCompletion()) {
            LOGGER.debug("inline completion disabled");
            return;
        }

        this.lastRequestTimestamp = System.currentTimeMillis();
        long requestTimestamp = this.lastRequestTimestamp;


        ApplicationManager.getApplication()
            .invokeLater(() -> {
                int offset = editor.getCaretModel().getOffset();

                SnippetPreview previousPreview = SnippetPreview.getInstance(editor);

                if (previousPreview != null) {
                    Disposer.dispose(previousPreview);
                }

                VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
                ProjectManager.getInstance().getDefaultProject();

                if (virtualFile == null) {
                    return;
                }

                LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());

                int lineStart = editor.getCaretModel().getVisualLineStart();
                int lineEnd = editor.getCaretModel().getVisualLineEnd();
                int caretOfset = editor.getCaretModel().getCurrentCaret().getOffset();

                String currentLine = document.getText(new TextRange(lineStart, caretOfset));


                if (!lineStartsWithComment(language, currentLine)) {
                    LOGGER.debug("line is not a comment");
                    return;
                }

                long numberOfWords = numberOfWordsInComment(currentLine);
                if (numberOfWords <= 1) {
                    LOGGER.debug("not enough keywords: " + numberOfWords);
                    return;
                }

                // what we are looking for on the API
                Optional<String> searchTerm = Optional.of(removeLineFromCommentsSymbols(currentLine));

                List<String> dependenciesName = dependencyManagement.getDependencies(editor.getProject(), virtualFile)
                    .stream().map(Dependency::getName)
                    .collect(Collectors.toList());
                String filename = getUnitRelativeFilenamePathFromEditorForVirtualFile(editor.getProject(), virtualFile);

                // Finally, make the request. We make a request periodically and then
                // only get the latest request. We poll
                updateListAlarm.addRequest(() -> {
                    if (requestTimestamp != this.lastRequestTimestamp) {
                        return;
                    }
                    Optional<Boolean> onlyPublic = Optional.empty();
                    Optional<Boolean> onlyPrivate = Optional.empty();
                    Optional<Boolean> onlyFavorite = Optional.empty();

                    if (settings.getPublicSnippetsOnly()) {
                        onlyPublic = Optional.of(true);
                        onlyPrivate = Optional.empty();
                    }
                    if (settings.getPrivateSnippetsOnly()) {
                        onlyPrivate = Optional.of(true);
                        onlyPublic = Optional.empty();
                    }
                    if (settings.getFavoriteSnippetsOnly()) {
                        onlyFavorite = Optional.of(true);
                    }
                    LOGGER.debug(String.format("[InlineDocumentListener] initiate search with onlyPublic %s, onlyPrivate %s, onlyFavorite %s", onlyPublic, onlyPrivate, onlyFavorite));

                    List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> snippets = codigaApi.getRecipesSemantic(searchTerm,
                        dependenciesName,
                        Optional.empty(),
                        language,
                        filename,
                        onlyPublic,
                        onlyPrivate,
                        onlyFavorite);

                    if (snippets.isEmpty()) {
                        return;
                    }

                    SnippetPreview snippetPreview = new SnippetPreview(editor, offset, snippets);
                    snippetPreview.display();
                }, TIMEOUT_REQUEST_POLLING_MILLISECONDS);
            }, project.getDisposed());
    }

}
