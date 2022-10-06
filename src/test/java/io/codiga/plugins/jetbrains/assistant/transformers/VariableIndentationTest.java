package io.codiga.plugins.jetbrains.assistant.transformers;

import com.intellij.codeInsight.completion.CompletionType;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import io.codiga.plugins.jetbrains.testutils.TestBase;

public class VariableIndentationTest extends TestBase {

  @Override
  protected String getTestDataRelativePath() {
    return "src/test/data/transformers";
  }

  public void testIndentation() {
    AppSettingsState.getInstance().setUseInlineCompletion(false);
    myFixture.testCompletionVariants("spawn_thread.rs");
    myFixture.type(".spawn");
    myFixture.complete(CompletionType.BASIC);


    myFixture.checkResultByFile("spawn_thread_indent_result.rs");
    AppSettingsState.getInstance().setUseInlineCompletion(true);
  }
}
