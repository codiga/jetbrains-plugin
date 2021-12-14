package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroProjectNameTest extends TestVariableMacroGeneric {

  public void testMacroProjectName() {
    super.performTest(Constants.PROJECT_NAME, myFixture);
  }
}
