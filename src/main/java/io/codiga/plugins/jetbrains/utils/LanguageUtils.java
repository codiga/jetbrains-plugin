package io.codiga.plugins.jetbrains.utils;

import io.codiga.api.type.LanguageEnumeration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class LanguageUtils {

    private LanguageUtils() {}


    public static String getLanguageName(LanguageEnumeration languageEnumeration) {
        switch (languageEnumeration) {
            case HASKELL:
                return "Haskell";
            case COLDFUSION:
                return "Coldfusion";
            case HTML:
                return "HTML";
            case DOCKER:
                return "Docker";
            case TERRAFORM:
                return "Terraform";
            case JSON:
                return "Json";
            case YAML:
                return "YAML";
            case TYPESCRIPT:
                return "TypeScript";
            case JAVASCRIPT:
                return "JavaScript";
            case SQL:
                return "SQL";
            case SHELL:
                return "Shell";
            case SCALA:
                return "Scala";
            case RUST:
                return "Rust";
            case RUBY:
                return "Ruby";
            case OBJECTIVEC:
                return "Objective-C";
            case PHP:
                return "PHP";
            case PYTHON:
                return "Python";
            case CSHARP:
                return "C#";
            case DART:
                return "Dart";
            case CSS:
                return "CSS";
            case CPP:
                return "C++";
            case C:
                return "C";
            case APEX:
                return "Apex";
            case GO:
                return "Go";
            case JAVA:
                return "Java";
            default:
                return "Unknown";
        }
    }

    /**
     * Indicate if a line of code starts as a comment.
     * @param languageEnumeration
     * @param line
     * @return
     */
    public static boolean lineStartsWithComment(@NotNull LanguageEnumeration languageEnumeration, @NotNull String line) {
        String filteredLine = line.replaceAll(" ", "");
        switch (languageEnumeration){
            case JAVASCRIPT:
            case TYPESCRIPT:
            case C:
            case APEX:
            case CPP:
            case SCALA:
            case DART:
            case GO:
            case OBJECTIVEC:
            case KOTLIN:
            case JAVA:
            case SWIFT:
            case SOLIDITY:
            case RUST:
                return filteredLine.startsWith("//");
            case PYTHON:
            case SHELL:
            case PERL:
            case YAML:
                return filteredLine.startsWith("#");
            case TERRAFORM:
            case PHP:
                return filteredLine.startsWith("#") || filteredLine.startsWith("//");
            case COLDFUSION:
                return filteredLine.startsWith("<!---");
            case HASKELL:
                return filteredLine.startsWith("--");
            case CSS:
                return filteredLine.startsWith("/*");
            default:
                return false;
        }
    }

    public static @NotNull String removeLineFromCommentsSymbols(@NotNull String line) {
        return line.replaceAll("#", "").replaceAll("//", "");
    }

    public static long numberOfWordsInComment(@NotNull String line) {
        return Arrays.asList(removeLineFromCommentsSymbols(line).split(" ")).stream().filter(s -> s.length() > 0).count();
    }
}
