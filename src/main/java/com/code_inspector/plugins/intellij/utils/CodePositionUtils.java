package com.code_inspector.plugins.intellij.utils;

import com.code_inspector.api.type.LanguageEnumeration;

import java.util.ArrayList;
import java.util.List;

import static com.code_inspector.plugins.intellij.Constants.*;

/**
 * Utility class to manipulate code and their position/indentation.
 */
public final class CodePositionUtils {

    private CodePositionUtils() {}

    /**
     * Indent all the lines but the first. Keep the first as it is.
     * @param code - the code
     * @param indentation - number of space we should use to indent.
     * @return - the new code.
     */
    public static String indentOtherLines(String code, int indentation) {
        String[] codeArray = code.split(LINE_SEPARATOR);
        List<String> codeArrayUpdated = new ArrayList<String>();
        if(codeArray.length <= 1) {
            return code;
        }
        codeArrayUpdated.add(codeArray[0]);
        for (int i = 1 ; i < codeArray.length ; i++){
            StringBuilder sb = new StringBuilder();
            for (int j = 0 ; j < indentation ; j++) {
                sb.append(CHARACTER_SPACE);
            }
            sb.append(codeArray[i]);
            codeArrayUpdated.add(sb.toString());
        }
        return String.join(LINE_SEPARATOR, codeArrayUpdated);
    }

    /**
     * Get the indentation and the number of spaces
     *
     * @param lineOfCode - the line of code.
     * @return - the number of spaces used to indent code.
     */
    public static int getIndentation(String lineOfCode) {
        int res = 0;
        for (int i = 0 ; i < lineOfCode.length() && lineOfCode.charAt(i) == CHARACTER_SPACE; i++) {
            res++;
        }
        return res;
    }

    /**
     * Report the first position to insert code in the file. It basically skips comments
     *
     * @param code - the code
     * @param languageEnumeration - the langauge
     * @return - the first position in the code.
     */
    public static int firstPositionToInsert(String code, LanguageEnumeration languageEnumeration) {
        String[] codeArray = code.split(LINE_SEPARATOR);
        int start = 0;
        if (languageEnumeration == LanguageEnumeration.PYTHON) {

            for (String line: codeArray) {
                if (line.startsWith(PYTHON_COMMENT_CHARACTER)) {
                    start = line.length() + 1;
                } else {
                    break;
                }
            }

        }
        return start;
    }
}
