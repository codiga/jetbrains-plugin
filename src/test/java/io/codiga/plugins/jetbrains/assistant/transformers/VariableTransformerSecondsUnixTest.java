package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;

public class VariableTransformerSecondsUnixTest extends TestVariableTransformerGeneric {

  public void testTransformerSecondsUnix() {
    super.performTest(CodingAssistantContext.DATE_CURRENT_SECONDS_UNIX, myFixture);
  }
}
