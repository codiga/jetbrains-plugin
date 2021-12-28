package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerSecondTest extends TestVariableTransformerGeneric {

  public void testTransformerSecond() {
    super.performTest(CodingAssistantContext.DATE_CURRENT_SECOND, myFixture);
  }
}
