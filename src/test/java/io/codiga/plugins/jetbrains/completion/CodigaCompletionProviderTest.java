package io.codiga.plugins.jetbrains.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;

public class CodigaCompletionProviderTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        String communityPath = PlatformTestUtil.getCommunityPath();
        String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
        if (communityPath.startsWith(homePath)) {
            return communityPath.substring(homePath.length()) + "src/test/data/completion";
        }
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
