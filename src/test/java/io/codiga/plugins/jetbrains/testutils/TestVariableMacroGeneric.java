package io.codiga.plugins.jetbrains.testutils;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import org.junit.Ignore;

import java.util.List;

/**
 * This Class works as Parent for all the Variable Macro tests, only
 * `performTest` is being call from the test file passing the variable
 * of the test and the current myFixture of the running test at the moment.
 *
 * This approach prevents extensive code re definition per test file and easier
 * maintainability. Only to test variables in the format `$_$`.
 */
@Ignore
public class TestVariableMacroGeneric extends BasePlatformTestCase {

  public TestVariableMacroGeneric() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void performTest(String variable, CodeInsightTestFixture fixture) {
    fixture.testCompletionVariants("spawn_thread.rs");
    fixture.type("testMacro");
    fixture.complete(CompletionType.BASIC);
    List<String> lookupElementStrings = fixture.getLookupElementStrings();

    assertNotNull(lookupElementStrings);

    fixture.type('\t');

    // We only make sure the Variable Transform is not inside the recipe code to be inserted.
    // There's no validation about the Variable being expanded to an expected value.
    assertFalse(fixture.getEditor().getDocument().getText().contains(variable));
  }

  @Override
  protected String getTestDataPath() {
    String communityPath = PlatformTestUtil.getCommunityPath();
    String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
    if (communityPath.startsWith(homePath)) {
      return communityPath.substring(homePath.length()) + "src/test/data/transformers";
    }
    return "src/test/data/transformers";
  }
}
