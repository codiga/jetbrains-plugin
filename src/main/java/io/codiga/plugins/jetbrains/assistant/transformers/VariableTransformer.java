package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

public interface VariableTransformer {
  /**
   * @param code
   * @param codigaTransformationContext
   * @return string with equivalent values between Codiga's recipe variables
   * and local expected result.
   */
  public String transform(String code, CodingAssistantContext codigaTransformationContext);
}
