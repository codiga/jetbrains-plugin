package io.codiga.plugins.jetbrains.assistant.transformers;

import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VariableTransformerYearShort implements VariableTransformer {
  /**
   * Transforms and replaces with the current year (shortened)
   *
   * @param code
   * @param codigaTransformationContext
   * @return code with replaced value
   */
  @Override
  public String transform(String code, CodingAssistantContext codigaTransformationContext){
    final Calendar calendar = Calendar.getInstance();
    final Date date = calendar.getTime();
    return code.replace(CodingAssistantContext.DATE_CURRENT_YEAR,
      new SimpleDateFormat("yy", Locale.ENGLISH).format(date.getTime()));
  }
}
