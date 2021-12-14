package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerMinuteTest extends TestVariableTransformerGeneric {

  public void testTransformerMinute() {
    super.performTest(CodingAssistantContext.DATE_CURRENT_MINUTE, myFixture);
  }
}
