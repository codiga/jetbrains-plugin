package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroSelectedTextTest extends TestVariableMacroGeneric {

  public void testMacroSelectedText() {
    super.performTest(Constants.SELECTED_TEXT, myFixture);
  }
}
