package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;
import io.codiga.plugins.jetbrains.testutils.TestVariableTransformerGeneric;
import org.junit.Test;

public class VariableTransformerDayNameShortTest extends TestVariableTransformerGeneric {

  @Test
  public void testTransformerDayNameShort() {
    super.performTest(CodingAssistantContext.DATE_DAY_NAME_SHORT, myFixture);
  }
}
