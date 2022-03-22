package io.codiga.plugins.jetbrains.assistant.user_variables;

import com.intellij.codeInsight.template.impl.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserVariables {

    private Pattern pattern = Pattern.compile(UserVariablesConstants.REGEXP);

    private UserVariables() {};

    private static UserVariables _INSTANCE = new UserVariables();

    public static UserVariables getInstance() {return _INSTANCE;}

    public List<Variable> getVariablesFromCode(String code) {
        List<Variable> variables = new ArrayList<Variable>();

        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {

            if(matcher.groupCount() == 2) {
                String variableName = matcher.group(1);
                String variableValue = matcher.group(2);

                /**
                 * Variable name and value are present.
                 */
                if (variableName != null && variableValue != null) {

                    Variable newVariable = new Variable(matcher.group(1), null, variableValue.replaceAll(":", ""), true);
                    variables.add(newVariable);
                    continue;
                }

                /**
                 * Only variable name is here
                 */
                if (variableName != null) {
                    Variable newVariable = new Variable(matcher.group(1), null, null, true);
                    variables.add(newVariable);
                    continue;
                }

            }
        }
        return variables;
    }

    /**
     * Transform the code by removing the variable from Codiga into a variable name
     * that is suitable for IntelliJ.
     *
     * The String &[USER_INPUT:42:bla] is then replace by $42$
     * @param code - the initial code
     * @return - the transformed code.
     */
    public String transformCode(String code) {
        String returnedCode = code;
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {

            if(matcher.groupCount() == 2) {
                String variableName = matcher.group(1);
                String codePart = code.substring(matcher.start(), matcher.end());

                if (variableName != null) {
                    returnedCode = returnedCode.replace(codePart, String.format("$%s$", variableName));
                }
            }
        }
        return returnedCode;
    }
}
