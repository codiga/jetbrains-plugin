package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.dependencies.DependencyManagement;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.LanguageUtils;
import io.codiga.plugins.jetbrains.model.Dependency;
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
    private final CodigaApi codigaApi = ApplicationManager.getApplication().getService(CodigaApi.class);
    private final DependencyManagement dependencyManagement = new DependencyManagement();

    @Override
    public void documentChanged(@NotNull DocumentEvent documentEvent) {
        LOGGER.info("document changed");

        if(documentEvent.getDocument().isInBulkUpdate()) {
            LOGGER.debug("bulk update");
            return;
        }

        Document document = documentEvent.getDocument();
        Editor editor = getActiveEditor(document);

        if (editor == null){
            LOGGER.debug("editor is null");
            return;
        }

        ApplicationManager.getApplication()
            .invokeLater(
                () -> {
                    int offset = editor.getCaretModel().getOffset();

                    SnippetPreview previousPreview = SnippetPreview.getInstance(editor);

                    if (previousPreview != null) {
                        LOGGER.debug("dispose previous preview");
                        Disposer.dispose(previousPreview);
                    }


                    VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
                    ProjectManager.getInstance().getDefaultProject();


                    if (virtualFile == null){
                        LOGGER.debug("no virtual file");
                        return;
                    }

                    LanguageEnumeration language = LanguageUtils.getLanguageFromFilename(virtualFile.getCanonicalPath());

                    int lineStart = editor.getCaretModel().getVisualLineStart();
                    int lineEnd = editor.getCaretModel().getVisualLineEnd();
                    int caretOfset = editor.getCaretModel().getCurrentCaret().getOffset();

                    String currentLine =  document.getText(new TextRange(lineStart, caretOfset));


                    if(!lineStartsWithComment(language, currentLine)){
                        LOGGER.debug("line is not a comment");
                        return;
                    }

                    if(numberOfWordsInComment(currentLine) < 1) {
                        LOGGER.debug("not enough keywords");
                        return;
                    }

                    Optional<String> searchTerm = Optional.of(removeLineFromCommentsSymbols(currentLine));

                    LOGGER.debug("search term: " + searchTerm);
                    List<String> dependenciesName = dependencyManagement.getDependencies(editor.getProject(), virtualFile)
                        .stream().map(Dependency::getName)
                        .collect(Collectors.toList());
                    String filename = getUnitRelativeFilenamePathFromEditorForVirtualFile(editor.getProject(), virtualFile);
                    List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> snippets = codigaApi.getRecipesSemantic(searchTerm, dependenciesName, Optional.empty(), language, filename, Optional.empty(), Optional.empty(), Optional.empty());




                    SnippetPreview snippetPreview = new SnippetPreview(editor, offset, snippets);
                    snippetPreview.display();


                });
    }

}
