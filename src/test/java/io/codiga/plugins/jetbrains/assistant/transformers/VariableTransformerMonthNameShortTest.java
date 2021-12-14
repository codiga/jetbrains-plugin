package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerMonthNameShortTest extends TestVariableTransformerGeneric {

  public void testTransformerMonthNameShort() {
    super.performTest(CodingAssistantContext.DATE_MONTH_NAME_SHORT, myFixture);
  }
}
