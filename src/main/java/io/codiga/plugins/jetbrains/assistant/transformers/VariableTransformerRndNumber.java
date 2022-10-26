package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.util.Random;

public class VariableTransformerRndNumber implements VariableTransformer {
  /**
   * Generate a 6 digit random number
   *
   * @param code
   * @param codigaTransformationContext
   * @return code with replaced value
   */
  @Override
  public String transform(String code, CodingAssistantContext codigaTransformationContext){
    final Random rnd = new Random();
    final int number = rnd.nextInt(999999);
    return code.replace(CodingAssistantContext.RANDOM_BASE_10,
      String.format("%d", number));
  }
}
