package io.codiga.plugins.jetbrains.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
