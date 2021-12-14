package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerYearShortTest extends TestVariableTransformerGeneric {

  public void testTransformerYearShort() {
    super.performTest(CodingAssistantContext.DATE_CURRENT_YEAR_SHORT, myFixture);
  }
}
