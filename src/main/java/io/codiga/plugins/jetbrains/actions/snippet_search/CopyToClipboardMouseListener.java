package io.codiga.plugins.jetbrains.actions.snippet_search;

import io.codiga.api.GetRecipesForClientSemanticQuery;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Mouse listener to redirect the user to the login page.
 */
public class CopyToClipboardMouseListener implements MouseListener {
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

    @Override
    public void mousePressed(MouseEvent e) {
        // empty, nothing needed here
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // empty, nothing needed here
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // empty, nothing needed here
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // empty, nothing needed here
    }
}
