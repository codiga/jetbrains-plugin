package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroFileNameTest extends TestVariableMacroGeneric {

  public void testMacroFileName() {
    super.performTest(Constants.FILE_NAME, myFixture);
  }
}
