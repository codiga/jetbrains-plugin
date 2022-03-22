package io.codiga.plugins.jetbrains.assistant.user_variables;

import com.intellij.codeInsight.template.impl.Variable;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import io.codiga.plugins.jetbrains.utils.CodePositionUtils;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class UserVariablesTest extends TestBase {

  @Test
  public void testVariableDetection() {
    String code = "public class &[USER_INPUT:42:blabla] blibli";

    assertEquals(UserVariables.getInstance().getVariablesFromCode(code).size(), 1);
    assertEquals(UserVariables.getInstance().getVariablesFromCode(code).get(0).getName(), "42");
    assertEquals(UserVariables.getInstance().getVariablesFromCode(code).get(0).getDefaultValueString(), "blabla");
  }

  @Test
  public void testVariableDetectionWithMultipleVariables() {
    String code = "public class &[USER_INPUT:42:blabla] blibli \n \n &[USER_INPUT:51:blo] \n &[USER_INPUT:0]";
    List<Variable> variables = UserVariables.getInstance().getVariablesFromCode(code);

    assertEquals(variables.size(), 3);
    assertEquals(variables.get(0).getName(), "42");
    assertEquals(variables.get(0).getDefaultValueString(), "blabla");

    assertEquals(variables.get(1).getName(), "51");
    assertEquals(variables.get(1).getDefaultValueString(), "blo");

    assertEquals(variables.get(2).getName(), "0");
  }

  @Test
  public void testVariableReplaceCode() {
    String code = "public class &[USER_INPUT:42:blabla] blibli \n \n &[USER_INPUT:51:blo] \n &[USER_INPUT:0]";
    String expected = "public class $42$ blibli \n \n $51$ \n $0$";
    String updatedCode = UserVariables.getInstance().transformCode(code);

    assertEquals(expected, updatedCode);
  }

  @Test
  public void testVariableReplaceCodeMultipleOccurrences() {
    String code = "public class &[USER_INPUT:42:blabla] blibli \n \n &[USER_INPUT:42:blabla]";
    String expected = "public class $42$ blibli \n \n $42$";
    String updatedCode = UserVariables.getInstance().transformCode(code);

    assertEquals(expected, updatedCode);
  }
}
