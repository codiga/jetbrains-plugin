package io.codiga.plugins.jetbrains.assistant.transformers;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import io.codiga.plugins.jetbrains.model.CodingAssistantContext;

public class VariableIndentation implements VariableTransformer {

  /**
   * Set file's correct indentation in the recipe by replacing &[CODIGA_INDENTATION] with the configuration set
   * by the user to use spaces or tabs for indentation
   *
   * @param code
   * @param CodigaTransformationContext
   * @return code with replaced value
   */
  @Override
  public String transform(String code, CodingAssistantContext CodigaTransformationContext) {
    String processedCode = code;

    if (processedCode.contains(CodigaTransformationContext.CODIGA_INDENT)) {
      /*
       * Get the current PsiFile instance instead of generating a new one, the snippet insertion/completion is called
       * from the same file where it needs to be inserted, so we are going to get all the details of the file, like file
       * type, tab and indent sizes because there are multiple FileTypes per extension where each one of these have
       * different values.
       */
      PsiFile currentFile = CodigaTransformationContext.dataContext.getData(LangDataKeys.PSI_FILE);

      // We want to know if the user set to use tabs or spaces in the file where the snippet is going to be inserted
      if (CodeStyle.getIndentOptions(currentFile).USE_TAB_CHARACTER) {
        // Replace indent variables with `\t`
        processedCode = processedCode.replace(CodigaTransformationContext.CODIGA_INDENT, "\t");
        /*
         * Create a new virtual file in memory with the same type of the current file where the insert was called,
         * interesting enough, for tabs we need to insert a `\t` in place of the indent variable, but they transcode only
         * during the creation of a file.
         *
         * A new _in memory file_ with the snippet source will be created to force the `\t` transcode and get the content
         * of the file back here.
         */
        PsiFile currentFileInMemory = PsiFileFactory
          .getInstance(CodigaTransformationContext.dataContext.getData(LangDataKeys.PROJECT))
          .createFileFromText("codiga_var_trans", currentFile.getFileType(), processedCode);

        // Get the recipe code with `\t` transcode ready for insertion
        processedCode = currentFileInMemory.getText();
      } else {
        // Get the indent size set for the file type of the current file where the recipe is going to be inserted
        int indentSize = CodeStyle.getIndentOptions(currentFile).INDENT_SIZE;
        // Dynamically generate an empty string (full of whitespaces) to represent space indentation
        String indentSizeAsWhitespace = String.format("%-" + indentSize + "s", "");
        // Replace indent variable with space indentation
        processedCode = processedCode.replace(CodigaTransformationContext.CODIGA_INDENT, indentSizeAsWhitespace);
      }
    }
    return processedCode;
  }
}
