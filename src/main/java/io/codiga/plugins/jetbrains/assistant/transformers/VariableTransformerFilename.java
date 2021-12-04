package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

public class VariableTransformerFilename implements VariableTransformer {

  @Override
  public String transform(String code, CodingAssistantContext CodigaTransformationContext){
    final String filename = CodigaTransformationContext.virtualFile.getName();
    return code.replace(CodingAssistantContext.GET_FILENAME, filename);
  }
}
