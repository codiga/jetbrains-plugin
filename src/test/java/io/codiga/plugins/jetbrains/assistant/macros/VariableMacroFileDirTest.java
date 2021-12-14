package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroFileDirTest extends TestVariableMacroGeneric {

  public void testMacroFileDir() {
    super.performTest(Constants.FILE_DIR, myFixture);
  }
}
