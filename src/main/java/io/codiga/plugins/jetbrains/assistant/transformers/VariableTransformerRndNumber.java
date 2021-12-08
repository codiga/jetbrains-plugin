package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class VariableTransformerRndNumber implements VariableTransformer {
  /**
   * Generate a 6 digit random number
   *
   * @param code
   * @param CodigaTransformationContext
   * @return code with replaced value
   */
  @Override
  public String transform(String code, CodingAssistantContext CodigaTransformationContext){
    final Random rnd = new Random();
    final int number = rnd.nextInt(999999);
    return code.replace(CodingAssistantContext.RANDOM_BASE_10,
      String.format("%d", number));
  }
}
