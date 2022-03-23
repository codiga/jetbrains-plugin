package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerYearTest extends TestVariableTransformerGeneric {


  public void testTransformerYear() {
    super.performTest(CodingAssistantContext.DATE_CURRENT_YEAR, myFixture);
  }
}
