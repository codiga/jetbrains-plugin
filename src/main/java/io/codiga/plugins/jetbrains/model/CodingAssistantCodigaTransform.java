package io.codiga.plugins.jetbrains.model;

import com.intellij.ide.macro.Macro;
import com.intellij.ide.macro.MacroManager;
import io.codiga.plugins.jetbrains.assistant.transformers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodingAssistantCodigaTransform {
  private final CodingAssistantContext CodigaTransformationContext;

  public CodingAssistantCodigaTransform(CodingAssistantContext CodigaTransformationContext) {
    this.CodigaTransformationContext = CodigaTransformationContext;
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
  public String findAndTransformVariables (String code) {
    Map<String, VariableTransformer> VARIABLE_TO_TRANSFORMER = new HashMap();
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

    // expand macros first
    String processedCode = null;
    try {
      processedCode = MacroManager.getInstance().expandMacrosInString(code,
        true,
        CodigaTransformationContext.dataContext);
    } catch (Macro.ExecutionCancelledException e) {
      e.printStackTrace();
    }

    List<String> detectedVariables = detectVariablesInsideCode(code);
    for (String variableName: VARIABLE_TO_TRANSFORMER.keySet()){
      if (detectedVariables.contains(variableName)){
        processedCode = VARIABLE_TO_TRANSFORMER.get(variableName).transform(processedCode, CodigaTransformationContext);
      }
    }

    return processedCode;
  }

  /**
   * Detect any Codiga's recipe variables inside recipe code.
   *
   * @param code: raw code from API
   * @return list of found variables in code string
   */
  private List detectVariablesInsideCode(String code) {
    List<String> supportedVariables = CodingAssistantContext.SUPPORTED_VARIABLES;
    return supportedVariables.stream()
      .filter(code::contains)
      .collect(Collectors.toList());
  }
}


