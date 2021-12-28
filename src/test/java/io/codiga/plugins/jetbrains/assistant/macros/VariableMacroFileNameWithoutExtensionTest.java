package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroFileNameWithoutExtensionTest extends TestVariableMacroGeneric {

  public void testMacroFileNameWithoutExtension() {
    super.performTest(Constants.FILE_NAME_WITHOUT_EXTENSION, myFixture);
  }
}
