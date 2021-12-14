package io.codiga.plugins.jetbrains.completion;


import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.List;

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

  public void testOneAutoCompleteSuggestion() {
    myFixture.testCompletionVariants("spawn_thread.rs");
    myFixture.type("testOneAutoCompleteSuggestion");
    myFixture.complete(CompletionType.BASIC);
    List<String> lookupElementStrings = myFixture.getLookupElementStrings();

    assertNotNull(lookupElementStrings);
    assertContainsElements(lookupElementStrings, "Spawn a thread");
  }

  public void testMultipleAutoCompleteSuggestion() {
    myFixture.testCompletionVariants("spawn_thread.rs");
    myFixture.type("testMultipleAutoCompleteSuggestion");
    myFixture.complete(CompletionType.BASIC);
    List<String> lookupElementStrings = myFixture.getLookupElementStrings();

    assertNotNull(lookupElementStrings);
    assertContainsElements(lookupElementStrings, "Spawn a thread", "Spawn a thread 2");
    assertDoesntContain(lookupElementStrings, "Spawn a thread 3");
  }

  public void testAcceptRecipeSuggestion() {
    myFixture.testCompletionVariants("spawn_thread.rs");
    myFixture.type("testAcceptRecipeSuggestion");
    myFixture.complete(CompletionType.BASIC);
    List<String> lookupElementStrings = myFixture.getLookupElementStrings();

    assertNotNull(lookupElementStrings);

    myFixture.type('\t');
    myFixture.checkResultByFile("spawn_thread_accept_result.rs");
  }
}
