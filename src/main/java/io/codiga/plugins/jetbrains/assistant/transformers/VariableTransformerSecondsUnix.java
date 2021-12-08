package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VariableTransformerSecondsUnix implements VariableTransformer {
  /**
   * Generate representation of current seconds timestamp in Unix format
   *
   * @param code
   * @param CodigaTransformationContext
   * @return code with replaced value
   */
  @Override
  public String transform(String code, CodingAssistantContext CodigaTransformationContext){
    long seconds = Instant.now().getEpochSecond();
    return code.replace(CodingAssistantContext.DATE_CURRENT_SECONDS_UNIX,
      String.format("%d", seconds));
  }
}
