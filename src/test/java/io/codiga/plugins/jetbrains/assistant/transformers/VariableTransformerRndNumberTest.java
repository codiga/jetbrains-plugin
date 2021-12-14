package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerRndNumberTest extends TestVariableTransformerGeneric {

  public void testTransformerRndNumber() {
    super.performTest(CodingAssistantContext.RANDOM_BASE_10, myFixture);
  }
}
