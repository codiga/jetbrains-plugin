package io.codiga.plugins.jetbrains.utils;

import io.codiga.api.type.LanguageEnumeration;

/**
 * Utility for the Rosie integration.
 */
public final class RosieUtils {

    /**
     * Returns the Rosie language version String of the argument Codiga language.
     *
     * @param language the Codiga language to map to Rosie language
     */
    public static String getRosieLanguage(LanguageEnumeration language) {
        switch (language) {
            case PYTHON:
                return "python";
            default:
                return "unknown";
        }
    }

    private RosieUtils() {
        //Utility class
    }
}
