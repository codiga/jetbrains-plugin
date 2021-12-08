package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VariableTransformerDayName implements VariableTransformer {
  /**
   * Get the name of weekday
   *
   * @param code
   * @param CodigaTransformationContext
   * @return code with replaced value
   */
  @Override
  public String transform(String code, CodingAssistantContext CodigaTransformationContext){
    final Calendar calendar = Calendar.getInstance();
    final Date date = calendar.getTime();
    return code.replace(CodingAssistantContext.DATE_DAY_NAME,
      new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime()));
  }
}
