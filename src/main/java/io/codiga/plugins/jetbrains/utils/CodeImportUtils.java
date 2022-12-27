package io.codiga.plugins.jetbrains.utils;

import io.codiga.api.type.LanguageEnumeration;

import java.util.Arrays;
import java.util.List;

import static io.codiga.plugins.jetbrains.Constants.LINE_SEPARATOR;

/**
 * All methods to handle imports in code. This is specifally used in the coding
 * assistant to import code.
 */
public final class CodeImportUtils {

    private CodeImportUtils() {}


    /**
     * Return if a given code has the given import statement. It does it by
     * checking that the code contains the dependency statement.
     * @param code - the code in IntelliJ
     * @param importStatement - the name of the dependency.
     * @param languageEnumeration - the langauge.
     * @return
     */
    public static boolean hasImport(String code, String importStatement, LanguageEnumeration languageEnumeration) {
        List<String> codeArray = Arrays.asList(code.split(LINE_SEPARATOR));
        return codeArray.stream().anyMatch(s -> s.equalsIgnoreCase(importStatement));
    }
}
