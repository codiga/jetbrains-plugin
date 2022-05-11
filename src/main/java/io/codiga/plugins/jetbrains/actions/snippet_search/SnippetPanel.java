package io.codiga.plugins.jetbrains.actions.snippet_search;

import io.codiga.api.GetRecipesForClientSemanticQuery;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class SnippetPanel {
    private JTextArea code;
    private JPanel mainPanel;
    private JButton insert;
    private JButton learnMore;
    private JLabel name;
    private JLabel userInformation;
    private JLabel description;

    public SnippetPanel(GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch snippet) {
        String decodedCode = new String(Base64.getDecoder().decode(snippet.presentableFormat().getBytes()));

//        code.setText(decodedCode);
//        description.setText(snippet.description());
        name.setText(snippet.name());
    }

    public Component getComponent() {
        return mainPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
