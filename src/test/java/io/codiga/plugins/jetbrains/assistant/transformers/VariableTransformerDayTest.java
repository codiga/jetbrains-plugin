package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerDayTest extends TestVariableTransformerGeneric {

  public void testTransformerDay() {
    super.performTest(CodingAssistantContext.DATE_CURRENT_DAY, myFixture);
  }
}
