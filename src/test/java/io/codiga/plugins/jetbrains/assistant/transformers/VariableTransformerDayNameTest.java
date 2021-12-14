package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerDayNameTest extends TestVariableTransformerGeneric {

  public void testTransformerDayName() {
    super.performTest(CodingAssistantContext.DATE_DAY_NAME, myFixture);
  }
}
