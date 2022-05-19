package io.codiga.plugins.jetbrains.utils;

import io.codiga.api.type.LanguageEnumeration;

public final class LanguageUtils {

    private LanguageUtils() {}


    public static String getLanguageName(LanguageEnumeration languageEnumeration) {
        switch (languageEnumeration) {
            case HASKELL:
                return "Haskell";
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
}
