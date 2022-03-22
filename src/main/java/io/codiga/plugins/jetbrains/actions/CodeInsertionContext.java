package io.codiga.plugins.jetbrains.actions;

import com.intellij.openapi.editor.markup.RangeHighlighter;
import io.codiga.plugins.jetbrains.model.CodeInsertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CodeInsertionContext {
    private List<CodeInsertion> codeInsertions = new ArrayList<>();
    private final List<RangeHighlighter> highlighters = new ArrayList<>();
    private Optional<Long> currentRecipeId = Optional.empty();

    public void clearHighlighters() {
        highlighters.clear();;
    }

    public void clearInsertions() {
        codeInsertions.clear();
    }

    public void clearAll() {
        clearHighlighters();
        clearInsertions();
        currentRecipeId = Optional.empty();
    }

    public void addHighlighter(RangeHighlighter highlighter) {
        highlighters.add(highlighter);
    }

    public void addCodeInsertion(CodeInsertion codeInsertion) {
        codeInsertions.add(codeInsertion);
    }

    public List<CodeInsertion> getCodeInsertions() {
        return this.codeInsertions;
    }

    public List<RangeHighlighter> getHighlighters() {
        return this.highlighters;
    }

    public Optional<Long> getCurrentRecipeId() {
        return currentRecipeId;
    }

    public void setCurrentRecipeId(Long recipeId) {
        this.currentRecipeId = Optional.of(recipeId);
    }
}
