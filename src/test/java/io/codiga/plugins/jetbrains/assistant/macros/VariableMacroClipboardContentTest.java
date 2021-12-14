package io.codiga.plugins.jetbrains.assistant.macros;

import io.codiga.plugins.jetbrains.testutils.Constants;
import io.codiga.plugins.jetbrains.testutils.TestVariableMacroGeneric;

public class VariableMacroClipboardContentTest extends TestVariableMacroGeneric {

  public void testMacroClipboardContent() {
    super.performTest(Constants.CLIPBOARD_CONTENT, myFixture);
  }
}
