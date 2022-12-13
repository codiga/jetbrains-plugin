package io.codiga.plugins.jetbrains.utils;

import io.codiga.api.type.LanguageEnumeration;

import java.util.Set;

/**
 * Utility for the Rosie integration.
 */
public final class RosieLanguageSupport {

    /**
     * Currently supported languages by Rosie.
     */
    private static final Set<LanguageEnumeration> SUPPORTED_LANGUAGES = Set.of(
        LanguageEnumeration.PYTHON,
        LanguageEnumeration.JAVASCRIPT,
        LanguageEnumeration.TYPESCRIPT);

    /**
     * Returns whether the argument language is supported by Rosie.
     *
     * @param fileLanguage the language to check
     */
    public static boolean isLanguageSupported(LanguageEnumeration fileLanguage) {
        return SUPPORTED_LANGUAGES.contains(fileLanguage);
    }

    /**
     * Returns the Rosie language string of the argument Codiga language.
     *
     * @param language the Codiga language to map to Rosie language
     */
    public static String getRosieLanguage(LanguageEnumeration language) {
        switch (language) {
            case PYTHON:
                return "python";
            case JAVASCRIPT:
            case TYPESCRIPT:
                return "javascript";
            default:
                return "unknown";
        }
    }

    private RosieLanguageSupport() {
        //Utility class
    }
}
