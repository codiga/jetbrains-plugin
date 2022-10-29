package io.codiga.plugins.jetbrains.actions.snippet_search;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Mouse listener to redirect the user to the login page.
 */
public class LoginMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            Desktop.getDesktop().browse(new URI("https://app.codiga.io"));
        } catch (IOException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }
}
