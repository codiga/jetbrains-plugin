package io.codiga.plugins.jetbrains.model;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;
import java.util.List;

/**
 * Provide all the context required for CompletionProvider and RecipeAction to detect and transform
 * Codiga recipe variables. This Context must be pass only to init an instance of
 * CodingAssistantCodigaTransform.
 */
public class CodingAssistantContext {
  public static final String RANDOM_UUID = "&[RANDOM_UUID]";
  public static final String DATE_DAY_NAME = "&[DATE_DAY_NAME]";
  public static final String DATE_DAY_NAME_SHORT = "&[DATE_DAY_NAME_SHORT]";
  public static final String DATE_MONTH_NAME = "&[DATE_MONTH_NAME]";
  public static final String DATE_MONTH_NAME_SHORT = "&[DATE_MONTH_NAME_SHORT]";
  public static final String RANDOM_BASE_10 = "&[RANDOM_BASE_10]";
  public static final String RANDOM_BASE_16 = "&[RANDOM_BASE_16]";
  public static final List<String> SUPPORTED_VARIABLES = Arrays.asList(
    RANDOM_UUID, DATE_DAY_NAME, DATE_DAY_NAME_SHORT, DATE_MONTH_NAME,
    DATE_MONTH_NAME_SHORT, RANDOM_BASE_10, RANDOM_BASE_16);
  public final VirtualFile virtualFile;
  public final DataContext dataContext;

  public CodingAssistantContext(DataContext dataContext) {
    this.virtualFile = dataContext.getData(LangDataKeys.VIRTUAL_FILE);
    this.dataContext = dataContext;
  }

}
