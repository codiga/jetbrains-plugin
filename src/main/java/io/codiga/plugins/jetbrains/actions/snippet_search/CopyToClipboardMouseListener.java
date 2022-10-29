package io.codiga.plugins.jetbrains.actions.snippet_search;

import io.codiga.api.GetRecipesForClientSemanticQuery;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Mouse listener to redirect the user to the login page.
 */
public class CopyToClipboardMouseListener extends MouseAdapter {
    private final GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet;

    public CopyToClipboardMouseListener(GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch _snippet) {
        this.snippet = _snippet;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        String decodedCode = new String(Base64.getDecoder().decode(snippet.presentableFormat().getBytes(StandardCharsets.UTF_8)));
        StringSelection stringSelection = new StringSelection(decodedCode);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
