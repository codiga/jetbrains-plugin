package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerRndNumberHexTest extends TestVariableTransformerGeneric {

  public void testTransformerRndNumberHex() {
    super.performTest(CodingAssistantContext.RANDOM_BASE_16, myFixture);
  }
}
