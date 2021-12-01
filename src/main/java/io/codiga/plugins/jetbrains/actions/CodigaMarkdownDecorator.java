package io.codiga.plugins.jetbrains.actions;

import com.github.rjeschke.txtmark.DefaultDecorator;

public class CodigaMarkdownDecorator extends DefaultDecorator {

    private static final String style = " style=\"font-family: Arial;\" ";

    @Override
    public void openHeadline(final StringBuilder out, final int level)
    {
        out.append("<h");
        out.append(level);
        out.append(style);
    }

    @Override
    public void openParagraph(final StringBuilder out)
    {

        out.append("<p");
        out.append(style);
        out.append(">");
    }
}
