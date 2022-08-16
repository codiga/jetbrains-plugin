package io.codiga.plugins.jetbrains.actions.snippet_search;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Mouse listener to redirect the user to the login page.
 */
public class OwnerMouseListener implements MouseListener {
    private final String slug;

    public OwnerMouseListener(String _slug) {
        this.slug = _slug;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        final String userLink = String.format("https://app.codiga.io/hub/user/%s", slug);
        try {
            Desktop.getDesktop().browse(new URI(userLink));
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
        }
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
