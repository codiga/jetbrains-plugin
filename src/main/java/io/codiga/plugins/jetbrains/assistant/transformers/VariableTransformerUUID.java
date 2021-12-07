package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.util.UUID;

public class VariableTransformerUUID implements VariableTransformer {
  @Override
  public String transform(String code, CodingAssistantContext CodigaTransformationContext){
    final String uuid = UUID.randomUUID().toString();
    return code.replace(CodingAssistantContext.RANDOM_UUID, uuid);
  }
}
