package io.codiga.plugins.jetbrains.assistant.transformers;


import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;

import java.util.List;

public class VariableIndentationTest extends BasePlatformTestCase {

  @Override
  protected String getTestDataPath() {
    String communityPath = PlatformTestUtil.getCommunityPath();
    String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
    if (communityPath.startsWith(homePath)) {
      return communityPath.substring(homePath.length()) + "src/test/data/transformers";
    }
    return "src/test/data/transformers";
  }

  public void testIndentation() {
    myFixture.testCompletionVariants("spawn_thread.rs");
    myFixture.type("testIndentation");
    myFixture.complete(CompletionType.BASIC);
    List<String> lookupElementStrings = myFixture.getLookupElementStrings();

    assertNotNull(lookupElementStrings);

    myFixture.type('\t');
    myFixture.checkResultByFile("spawn_thread_indent_result.rs");
  }
}
