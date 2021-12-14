package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroFilePathTest extends TestVariableMacroGeneric {

  public void testMacroFilePath() {
    super.performTest(Constants.FILE_PATH, myFixture);
  }
}
