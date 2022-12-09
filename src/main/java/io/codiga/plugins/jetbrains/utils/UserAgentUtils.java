package io.codiga.plugins.jetbrains.utils;

import com.intellij.openapi.application.ApplicationInfo;

public class UserAgentUtils {
    /**
     * Return the user-agent for the current product.
     * @return
     */
    public static String getUserAgent() {
        return String.format("%s/%s.%s",
            ApplicationInfo.getInstance().getVersionName(),
            ApplicationInfo.getInstance().getMajorVersion(),
            ApplicationInfo.getInstance().getMinorVersion());
    }
}
