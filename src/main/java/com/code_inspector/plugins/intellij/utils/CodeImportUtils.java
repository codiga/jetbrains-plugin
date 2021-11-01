package com.code_inspector.plugins.intellij.utils;

import com.code_inspector.api.type.LanguageEnumeration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.code_inspector.plugins.intellij.Constants.LINE_SEPARATOR;
import static com.code_inspector.plugins.intellij.Constants.PYTHON_IMPORT_KEYWORD;

/**
 * All methods to handle imports in code. This is specifally used in the coding
 * assistant to import code.
 */
public final class CodeImportUtils {

    private CodeImportUtils() {}

    /**
     * Generate the import statement for a language and a package. It just
     * generates the statement that will be generated for the specific language.
     *
     * @param importName - name to import.
     * @param languageEnumeration - language.
     * @return
     */
    public static Optional<String> generateImportStatement(String importName, LanguageEnumeration languageEnumeration) {
        if(languageEnumeration == LanguageEnumeration.PYTHON) {
            return Optional.of(String.format("%s %s", PYTHON_IMPORT_KEYWORD, importName));
        }
        return Optional.empty();
    }

    /**
     * Return if a given code has the dependency. It does it by
     * checking that the code contains the dependency statement.
     * @param code - the code in IntelliJ
     * @param dependencyName - the name of the dependency.
     * @param languageEnumeration - the langauge.
     * @return
     */
    public static boolean hasDependency(String code, String dependencyName, LanguageEnumeration languageEnumeration) {
        List<String> codeArray = Arrays.asList(code.split(LINE_SEPARATOR));
        if (languageEnumeration == LanguageEnumeration.PYTHON) {
            Optional<String> importStatementOptional = generateImportStatement(dependencyName, languageEnumeration);
            if (importStatementOptional.isPresent()) {
                return codeArray.stream().anyMatch(s -> {
                    return s.equalsIgnoreCase(importStatementOptional.get());
                });
            }
        }
        return false;
    }
}
