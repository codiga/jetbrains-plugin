package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroProjectFileDirTest extends TestVariableMacroGeneric {

  public void testMacroProjectFileDir() {
    super.performTest(Constants.PROJECT_FILE_DIR, myFixture);
  }
}
