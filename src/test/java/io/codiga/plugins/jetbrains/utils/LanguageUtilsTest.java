package io.codiga.plugins.jetbrains.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.codiga.api.type.LanguageEnumeration;
import org.junit.Test;

/**
 * Unit test for {@link LanguageUtils}.
 */
public class LanguageUtilsTest {

    //numberOfWordsInComment()

    @Test
    public void shouldReturnZeroForEmptyLine() {
        long numberOfWords = LanguageUtils.numberOfWordsInComment("");

        assertEquals(0, numberOfWords);
    }

    @Test
    public void shouldReturnZeroForEmptyCommentLine() {
        long numberOfWords = LanguageUtils.numberOfWordsInComment("//");

        assertEquals(0, numberOfWords);
    }

    @Test
    public void shouldReturnNumberOfNonEmptyComments() {
        long numberOfWords = LanguageUtils.numberOfWordsInComment("//this   is  a list of      words");

        assertEquals(6, numberOfWords);
    }

    //containsTodoKeyword()

    @Test
    public void shouldReturnTrueWhenContainsTodoKeywordAtLineStartAsStandaloneWord() {
        boolean containsTodoKeyword = LanguageUtils.containsTodoKeyword("//todo task");

        assertTrue(containsTodoKeyword);
    }

    @Test
    public void shouldReturnTrueWhenContainsTodoKeywordAtLineStartAsPartOfWord() {
        boolean containsTodoKeyword = LanguageUtils.containsTodoKeyword("//todo:task");

        assertTrue(containsTodoKeyword);
    }

    @Test
    public void shouldReturnTrueWhenContainsFixmeKeyword() {
        boolean containsTodoKeyword = LanguageUtils.containsTodoKeyword("//some keyword fixmeplease");

        assertTrue(containsTodoKeyword);
    }

    @Test
    public void shouldReturnTrueWhenContainsUppercaseTodoKeyword() {
        boolean containsTodoKeyword = LanguageUtils.containsTodoKeyword("//some keyword FIXME:please");

        assertTrue(containsTodoKeyword);
    }

    @Test
    public void shouldReturnFalseWhenDoesntContainTodoKeyword() {
        boolean containsTodoKeyword = LanguageUtils.containsTodoKeyword("//some keyword");

        assertFalse(containsTodoKeyword);
    }

    //getLanguageFromFilename

    @Test
    public void shouldGetLanguageFromFilename() {
        assertEquals(LanguageEnumeration.APEX, LanguageUtils.getLanguageFromFilename("myfile.cls"));
        assertEquals(LanguageEnumeration.C, LanguageUtils.getLanguageFromFilename("myfile.c"));
        assertEquals(LanguageEnumeration.CPP, LanguageUtils.getLanguageFromFilename("myfile.cpp"));
        assertEquals(LanguageEnumeration.CSS, LanguageUtils.getLanguageFromFilename("myfile.css"));
        assertEquals(LanguageEnumeration.CSHARP, LanguageUtils.getLanguageFromFilename("myfile.cs"));
        assertEquals(LanguageEnumeration.DART, LanguageUtils.getLanguageFromFilename("myfile.dart"));
        assertEquals(LanguageEnumeration.DOCKER, LanguageUtils.getLanguageFromFilename("dockerfile.myfile"));
        assertEquals(LanguageEnumeration.DOCKER, LanguageUtils.getLanguageFromFilename("myfile.dockerfile"));
        assertEquals(LanguageEnumeration.DOCKER, LanguageUtils.getLanguageFromFilename("Dockerfile"));
        assertEquals(LanguageEnumeration.DOCKER, LanguageUtils.getLanguageFromFilename("dockerfile"));
        assertEquals(LanguageEnumeration.UNKNOWN, LanguageUtils.getLanguageFromFilename("dock"));

        assertEquals(LanguageEnumeration.GO, LanguageUtils.getLanguageFromFilename("myfile.go"));
        assertEquals(LanguageEnumeration.HASKELL, LanguageUtils.getLanguageFromFilename("myfile.hs"));
        assertEquals(LanguageEnumeration.HTML, LanguageUtils.getLanguageFromFilename("myfile.html"));
        assertEquals(LanguageEnumeration.HTML, LanguageUtils.getLanguageFromFilename("myfile.html"));
        assertEquals(LanguageEnumeration.JAVA, LanguageUtils.getLanguageFromFilename("myfile.java"));
        assertEquals(LanguageEnumeration.JAVASCRIPT, LanguageUtils.getLanguageFromFilename("myfile.js"));
        assertEquals(LanguageEnumeration.JAVASCRIPT, LanguageUtils.getLanguageFromFilename("myfile.jsx"));
        assertEquals(LanguageEnumeration.JSON, LanguageUtils.getLanguageFromFilename("myfile.json"));
        assertEquals(LanguageEnumeration.KOTLIN, LanguageUtils.getLanguageFromFilename("myfile.kt"));
        assertEquals(LanguageEnumeration.PERL, LanguageUtils.getLanguageFromFilename("myfile.pl"));
        assertEquals(LanguageEnumeration.OBJECTIVEC, LanguageUtils.getLanguageFromFilename("myfile.m"));
        assertEquals(LanguageEnumeration.OBJECTIVEC, LanguageUtils.getLanguageFromFilename("myfile.mm"));
        assertEquals(LanguageEnumeration.OBJECTIVEC, LanguageUtils.getLanguageFromFilename("myfile.M"));
        assertEquals(LanguageEnumeration.PYTHON, LanguageUtils.getLanguageFromFilename("myfile.py"));
        assertEquals(LanguageEnumeration.PYTHON, LanguageUtils.getLanguageFromFilename("myfile.py3"));
        assertEquals(LanguageEnumeration.PYTHON, LanguageUtils.getLanguageFromFilename("myfile.ipynb"));
        assertEquals(LanguageEnumeration.PERL, LanguageUtils.getLanguageFromFilename("myfile.pl"));
        assertEquals(LanguageEnumeration.PERL, LanguageUtils.getLanguageFromFilename("myfile.pm"));

        assertEquals(LanguageEnumeration.PHP, LanguageUtils.getLanguageFromFilename("myfile.php"));
        assertEquals(LanguageEnumeration.PHP, LanguageUtils.getLanguageFromFilename("myfile.php5"));
        assertEquals(LanguageEnumeration.RUST, LanguageUtils.getLanguageFromFilename("myfile.rs"));
        assertEquals(LanguageEnumeration.RUBY, LanguageUtils.getLanguageFromFilename("myfile.rb"));
        assertEquals(LanguageEnumeration.RUBY, LanguageUtils.getLanguageFromFilename("myfile.rhtml"));
        assertEquals(LanguageEnumeration.SCALA, LanguageUtils.getLanguageFromFilename("myfile.scala"));

        assertEquals(LanguageEnumeration.SHELL, LanguageUtils.getLanguageFromFilename("myfile.sh"));
        assertEquals(LanguageEnumeration.SHELL, LanguageUtils.getLanguageFromFilename("myfile.bash"));
        assertEquals(LanguageEnumeration.SQL, LanguageUtils.getLanguageFromFilename("myfile.sql"));
        assertEquals(LanguageEnumeration.SWIFT, LanguageUtils.getLanguageFromFilename("myfile.swift"));
        assertEquals(LanguageEnumeration.SOLIDITY, LanguageUtils.getLanguageFromFilename("myfile.sol"));
        assertEquals(LanguageEnumeration.TERRAFORM, LanguageUtils.getLanguageFromFilename("myfile.tf"));
        assertEquals(LanguageEnumeration.TYPESCRIPT, LanguageUtils.getLanguageFromFilename("myfile.ts"));
        assertEquals(LanguageEnumeration.TYPESCRIPT, LanguageUtils.getLanguageFromFilename("myfile.tsx"));
        assertEquals(LanguageEnumeration.UNKNOWN, LanguageUtils.getLanguageFromFilename("myfile"));
        assertEquals(LanguageEnumeration.YAML, LanguageUtils.getLanguageFromFilename("myfile.yaml"));
        assertEquals(LanguageEnumeration.YAML, LanguageUtils.getLanguageFromFilename("myfile.yml"));
    }
}
