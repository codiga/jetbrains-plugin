package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerHourTest extends TestVariableTransformerGeneric {

  public void testTransformerHour() {
    super.performTest(CodingAssistantContext.DATE_CURRENT_HOUR, myFixture);
  }
}
