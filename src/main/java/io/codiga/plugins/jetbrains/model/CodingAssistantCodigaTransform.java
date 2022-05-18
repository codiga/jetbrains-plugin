package io.codiga.plugins.jetbrains.model;

import io.codiga.plugins.jetbrains.assistant.transformers.*;
import io.codiga.plugins.jetbrains.assistant.user_variables.UserVariables;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodingAssistantCodigaTransform {
  private final CodingAssistantContext codigaTransformationContext;
  private static Map<String, VariableTransformer> VARIABLE_TO_TRANSFORMER;
  static {
    VARIABLE_TO_TRANSFORMER = new HashMap();
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.RANDOM_UUID,
      new VariableTransformerUUID());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.RANDOM_BASE_10,
      new VariableTransformerRndNumber());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.RANDOM_BASE_16,
      new VariableTransformerRndNumberHex());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_DAY_NAME,
      new VariableTransformerDayName());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_DAY_NAME_SHORT,
      new VariableTransformerDayNameShort());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_MONTH_NAME,
      new VariableTransformerMonthName());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_MONTH_NAME_SHORT,
      new VariableTransformerMonthNameShort());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_CURRENT_YEAR,
      new VariableTransformerYear());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_CURRENT_YEAR_SHORT,
      new VariableTransformerYearShort());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_CURRENT_HOUR,
      new VariableTransformerHour());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_CURRENT_MINUTE,
      new VariableTransformerMinute());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_CURRENT_SECOND,
      new VariableTransformerSecond());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_CURRENT_SECONDS_UNIX,
      new VariableTransformerSecondsUnix());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_MONTH_TWO_DIGITS,
      new VariableTransformerMonthTwoDigits());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.DATE_CURRENT_DAY,
      new VariableTransformerDay());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.CODIGA_INDENT,
      new VariableIndentation());
  }

  public CodingAssistantCodigaTransform(CodingAssistantContext codigaTransformationContext) {
    this.codigaTransformationContext = codigaTransformationContext;
  }

  /**
   * Search for Codiga's recipe variables in raw code string and transform
   * any if found into expected value or behavior. If a new variable is
   * supported, include it in the VARIABLE_TO_TRANSFORMER HashMap and create
   * a new class for it with its `transform` method inside
   * assistant.transformers.
   *
   * @param code
   * @return code string with Codiga's recipe variables transform to local if
   * any was found.
   */
  public String findAndTransformVariables (@NotNull String code) {
    String processedCode = code;

    // expand macros first
//    try {
//      processedCode = MacroManager.getInstance().expandMacrosInString(code,
//        true,
//        codigaTransformationContext.getDataContext());
//    } catch (Macro.ExecutionCancelledException e) {
//      e.printStackTrace();
//    }

    // detect any codiga variable that is not a macro and resolve it
    List<String> detectedVariables = detectVariablesInsideCode(code);
    for (String variableName: VARIABLE_TO_TRANSFORMER.keySet()){
      if (detectedVariables.contains(variableName)){
        processedCode = VARIABLE_TO_TRANSFORMER.get(variableName)
          .transform(processedCode, codigaTransformationContext);
      }
    }

    return UserVariables.getInstance().transformCode(processedCode);
  }

  /**
   * Detect any Codiga's recipe variables inside recipe code.
   *
   * @param code: raw code from API
   * @return list of found variables in code string
   */
  private List detectVariablesInsideCode(@NotNull String code) {
    List<String> supportedVariables = CodingAssistantContext.SUPPORTED_VARIABLES;
    return supportedVariables.stream()
      .filter(code::contains)
      .collect(Collectors.toList());
  }
}


