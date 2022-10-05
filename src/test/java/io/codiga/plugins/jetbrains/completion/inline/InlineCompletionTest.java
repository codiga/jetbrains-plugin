package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;

/**
 * Integration test for {@link AcceptInlineAction}, {@link ShowNextInlineCompletion} and {@link ShowPreviousInlineCompletion}.
 */
public class InlineCompletionTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        String communityPath = PlatformTestUtil.getCommunityPath();
        String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
        if (communityPath.startsWith(homePath)) {
            return communityPath.substring(homePath.length()) + "src/test/data/completion/inline";
        }
        return "src/test/data/completion/inline";
    }

    //Multiple snippets

    public void testAcceptFirstSnippet() {
        myFixture.configureByFile("spawn_thread.rs");
        myFixture.type("spawn thread");
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("spawn_thread_accepted_first_snippet.rs");
    }

    public void testAcceptPreviousSnippet() {
        myFixture.configureByFile("spawn_thread.rs");
        myFixture.type("spawn thread");
        myFixture.testAction(new ShowPreviousInlineCompletion());
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("spawn_thread_accepted_previous_snippet.rs");
    }

    public void testAcceptNextSnippet() {
        myFixture.configureByFile("spawn_thread.rs");
        myFixture.type("spawn thread");
        myFixture.testAction(new ShowNextInlineCompletion());
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("spawn_thread_accepted_next_snippet.rs");
    }

    public void testCircularPaginationBackwards() {
        myFixture.configureByFile("spawn_thread.rs");
        myFixture.type("spawn thread");
        myFixture.testAction(new ShowPreviousInlineCompletion());
        myFixture.testAction(new ShowPreviousInlineCompletion());
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("spawn_thread_accepted_next_snippet.rs");
    }

    public void testCircularPaginationForwards() {
        myFixture.configureByFile("spawn_thread.rs");
        myFixture.type("spawn thread");
        myFixture.testAction(new ShowNextInlineCompletion());
        myFixture.testAction(new ShowNextInlineCompletion());
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("spawn_thread_accepted_previous_snippet.rs");
    }

    public void testCircularPaginationMixedDirection() {
        myFixture.configureByFile("spawn_thread.rs");
        myFixture.type("spawn thread");
        myFixture.testAction(new ShowPreviousInlineCompletion());
        myFixture.testAction(new ShowNextInlineCompletion());
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("spawn_thread_accepted_first_snippet.rs");
    }

    //Single snippet

    public void testAcceptSingleSnippet() {
        myFixture.configureByFile("github_ci_config.yml");
        myFixture.type("fetch sources");
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("github_ci_config_accepted_single_snippet.yml");
    }

    public void testNoPaginationForwardForSingleSnippet() {
        myFixture.configureByFile("github_ci_config.yml");
        myFixture.type("fetch sources");
        myFixture.testAction(new ShowPreviousInlineCompletion());
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("github_ci_config_accepted_single_snippet.yml");
    }

    public void testNoPaginationBackwardsForSingleSnippet() {
        myFixture.configureByFile("github_ci_config.yml");
        myFixture.type("fetch sources");
        myFixture.testAction(new ShowNextInlineCompletion());
        myFixture.testAction(new AcceptInlineAction());
        myFixture.checkResultByFile("github_ci_config_accepted_single_snippet.yml");
    }

    //No snippet

    public void testNoSnippetPreviewWhenNoSnippetIsFound() {
        //Uses a file type for which there is no test snippet is returned
        myFixture.configureByFile("NoSnippetPreview.java");
        //The typing here is only to trigger the inline document listener
        myFixture.type("do it");

        assertNull(SnippetPreview.getInstance(myFixture.getEditor()));
    }

    //Negative cases

    public void testNoSnippetPreviewWhenCompletionKeywordsHasNoWords() {
        myFixture.configureByFile("github_ci_config.yml");
        myFixture.type(" ");

        assertNull(SnippetPreview.getInstance(myFixture.getEditor()));
    }

    public void testNoSnippetPreviewWhenCompletionKeywordsHasASingleWord() {
        myFixture.configureByFile("github_ci_config.yml");
        myFixture.type("do");

        assertNull(SnippetPreview.getInstance(myFixture.getEditor()));
    }

    //Settings based cases

    public void testNoSnippetPreviewWhenCodigaIsDisabled() {
        AppSettingsState.getInstance().setCodigaEnabled(false);
        myFixture.configureByFile("github_ci_config.yml");
        myFixture.type("do");

        assertNull(SnippetPreview.getInstance(myFixture.getEditor()));

        AppSettingsState.getInstance().setCodigaEnabled(true);
    }

    public void testNoSnippetPreviewWhenInlineCompletionIsDisabled() {
        AppSettingsState.getInstance().setUseInlineCompletion(false);
        myFixture.configureByFile("github_ci_config.yml");
        myFixture.type("do");

        assertNull(SnippetPreview.getInstance(myFixture.getEditor()));

        AppSettingsState.getInstance().setUseInlineCompletion(true);
    }
}
