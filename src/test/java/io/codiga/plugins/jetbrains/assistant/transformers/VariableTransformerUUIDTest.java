package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerUUIDTest extends TestVariableTransformerGeneric {

  public void testTransformerUUID() {
    super.performTest(CodingAssistantContext.RANDOM_UUID, myFixture);
  }
}
