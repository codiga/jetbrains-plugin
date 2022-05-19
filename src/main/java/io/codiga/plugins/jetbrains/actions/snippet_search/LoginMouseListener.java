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
public class LoginMouseListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            Desktop.getDesktop().browse(new URI("https://app.codiga.io"));
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
