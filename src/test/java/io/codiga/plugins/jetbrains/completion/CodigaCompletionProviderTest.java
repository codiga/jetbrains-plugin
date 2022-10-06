package io.codiga.plugins.jetbrains.completion;

import com.intellij.codeInsight.completion.CompletionType;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import io.codiga.plugins.jetbrains.testutils.TestBase;

public class CodigaCompletionProviderTest extends TestBase {

    @Override
    protected String getTestDataRelativePath() {
        return "src/test/data/completion";
    }

    public void testAcceptRecipeSuggestion() {
        AppSettingsState.getInstance().setUseInlineCompletion(true);
        myFixture.testCompletionVariants("spawn_thread.rs");
        myFixture.type(".spawn");
        myFixture.complete(CompletionType.BASIC);
        myFixture.checkResultByFile("spawn_thread_accept_result.rs");
        AppSettingsState.getInstance().setUseInlineCompletion(true);
    }
}
