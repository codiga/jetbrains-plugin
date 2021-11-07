package io.codiga.plugins.jetbrains.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static io.codiga.plugins.jetbrains.ui.UIConstants.API_STATUS_TITLE;

public class DialogApiStatus extends DialogWrapper {

    private final String text;

    public DialogApiStatus(String t) {
        super(true); // use current window as parent
        this.text = t;
        init();
        setTitle(API_STATUS_TITLE);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }

    @Override
    protected Action @NotNull [] createActions() {
        Action helpAction = getHelpAction();
        return helpAction == myHelpAction && getHelpId() == null ?
            new Action[]{getOKAction()} :
            new Action[]{getOKAction(), helpAction};
    }
}
