package io.codiga.plugins.jetbrains.model;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;
import java.util.List;

/**
 * Provide all the context required for CompletionProvider and RecipeAction to detect and transform
 * Codiga recipe variables. This Context must be pass only to init an instance of
 * CodingAssistantCodigaTransform.
 */
public class CodingAssistantContext {
  public static final String GET_FILENAME = "&[GET_FILENAME]";
  public static final String GET_FILENAME_NO_EXT = "&[GET_FILENAME_NO_EXT]";
  public static final List<String> SUPPORTED_VARIABLES = Arrays.asList(GET_FILENAME, GET_FILENAME_NO_EXT);
  public final VirtualFile virtualFile;

  public CodingAssistantContext(VirtualFile vf) {
    this.virtualFile = vf;
  }

}
