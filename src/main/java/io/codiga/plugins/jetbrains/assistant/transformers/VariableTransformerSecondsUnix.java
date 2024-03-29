package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.time.Instant;

public class VariableTransformerSecondsUnix implements VariableTransformer {
  /**
   * Generate representation of current seconds timestamp in Unix format
   *
   * @param code
   * @param codigaTransformationContext
   * @return code with replaced value
   */
  @Override
  public String transform(String code, CodingAssistantContext codigaTransformationContext){
    long seconds = Instant.now().getEpochSecond();
    return code.replace(CodingAssistantContext.DATE_CURRENT_SECONDS_UNIX,
      String.format("%d", seconds));
  }
}
