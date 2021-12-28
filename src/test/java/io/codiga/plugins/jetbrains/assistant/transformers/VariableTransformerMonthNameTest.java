package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerMonthNameTest extends TestVariableTransformerGeneric {

  public void testTransformerMonthName() {
    super.performTest(CodingAssistantContext.DATE_MONTH_NAME, myFixture);
  }
}
