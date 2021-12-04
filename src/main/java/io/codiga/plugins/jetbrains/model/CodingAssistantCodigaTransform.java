package io.codiga.plugins.jetbrains.model;

import io.codiga.plugins.jetbrains.assistant.transformers.VariableTransformer;
import io.codiga.plugins.jetbrains.assistant.transformers.VariableTransformerFilename;
import io.codiga.plugins.jetbrains.assistant.transformers.VariableTransformerFilenameNoExtension;

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
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.GET_FILENAME_NO_EXT,
      new VariableTransformerFilenameNoExtension());
    VARIABLE_TO_TRANSFORMER.put(CodingAssistantContext.GET_FILENAME,
      new VariableTransformerFilename());

    String processedCode = code;
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


