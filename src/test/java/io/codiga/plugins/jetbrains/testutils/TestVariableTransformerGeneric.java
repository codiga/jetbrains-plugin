package io.codiga.plugins.jetbrains.testutils;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;

import java.util.List;

/**
 * This Class works as Parent for all the Variable Transform tests, only
 * `performTest` is being call from the test file passing the variable
 * of the test and the current myFixture of the running test at the moment.
 *
 * This approach prevents extensive code re definition per test file and easier
 * maintainability. Only to test variables in the format `&[_]`.
 */
public abstract class TestVariableTransformerGeneric extends TestBase {

  public void performTest(String variable, CodeInsightTestFixture fixture) {
    fixture.testCompletionVariants("spawn_thread.rs");
    fixture.type("testTransformer");
    fixture.complete(CompletionType.BASIC);
    List<String> lookupElementStrings = fixture.getLookupElementStrings();

    assertNotNull(lookupElementStrings);

    fixture.type('\t');

    // We only make sure the Variable Macro is not inside the recipe code to be inserted.
    // There's no validation about the Variable being transformed to an expected value.
    assertFalse(fixture.getEditor().getDocument().getText().contains(variable));
  }

  @Override
  protected String getTestDataRelativePath() {
    return "src/test/data/transformers";
  }

}
