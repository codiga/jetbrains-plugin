package io.codiga.plugins.jetbrains.utils;

import java.awt.*;

public final class DesktopUtils {
    private DesktopUtils() {
        // empty, nothing needed here
    }
    public static boolean isBrowsingSupported() {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        boolean result = false;
        Desktop desktop = java.awt.Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            result = true;
        }
        return result;

    }
}
