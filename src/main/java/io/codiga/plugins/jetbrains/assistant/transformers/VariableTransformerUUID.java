package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.util.UUID;

public class VariableTransformerUUID implements VariableTransformer {
  /**
   * Generate a UUID v4 string
   *
   * @param code
   * @param codigaTransformationContext
   * @return code with replaced value
   */
  @Override
  public String transform(String code, CodingAssistantContext codigaTransformationContext){
    final String uuid = UUID.randomUUID().toString();
    return code.replace(CodingAssistantContext.RANDOM_UUID, uuid);
  }
}
