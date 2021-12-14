package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroFileRelativePathTest extends TestVariableMacroGeneric {

  public void testMacroFileRelativePath() {
    super.performTest(Constants.FILE_RELATIVE_PATH, myFixture);
  }
}
