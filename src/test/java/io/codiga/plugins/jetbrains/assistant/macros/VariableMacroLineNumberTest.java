package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroLineNumberTest extends TestVariableMacroGeneric {

  public void testMacroLineNumber() {
    super.performTest(Constants.LINE_NUMBER, myFixture);
  }
}
