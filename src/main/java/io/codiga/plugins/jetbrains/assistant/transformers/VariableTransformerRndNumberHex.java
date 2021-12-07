package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.util.Random;

public class VariableTransformerRndNumberHex implements VariableTransformer {
  @Override
  public String transform(String code, CodingAssistantContext CodigaTransformationContext){
    final Random rnd = new Random();
    final int number = rnd.nextInt(999999);
    final String hex = Integer.toHexString(number);
    return code.replace(CodingAssistantContext.RANDOM_BASE_16, hex);
  }
}
