package io.codiga.plugins.jetbrains.actions.snippet_search;

import io.codiga.api.GetRecipesForClientSemanticQuery;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Mouse listener to redirect the user to the login page.
 */
public class LearnMoreMouseListener implements MouseListener {
    private GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet;

    public LearnMoreMouseListener(GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch _snippet) {
        this.snippet = _snippet;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final String publicSnippetLink = String.format("https://app.codiga.io/hub/snippet/%s/view", snippet.id());
        final String privateSnippetLink = String.format("https://app.codiga.io/assistant/snippet/%s/view", snippet.id());

        try {
            final String link = snippet.isPublic() ? publicSnippetLink : privateSnippetLink;
            Desktop.getDesktop().browse(new URI(link));
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
