package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerMonthTwoDigitsTest extends TestVariableTransformerGeneric {

  public void testTransformerMonthTwoDigits() {
    super.performTest(CodingAssistantContext.DATE_MONTH_TWO_DIGITS, myFixture);
  }
}
