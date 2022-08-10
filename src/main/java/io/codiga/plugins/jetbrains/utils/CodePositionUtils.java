package io.codiga.plugins.jetbrains.utils;

import io.codiga.api.type.LanguageEnumeration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.codiga.plugins.jetbrains.Constants.*;

/**
 * Utility class to manipulate code and their position/indentation.
 */
public final class CodePositionUtils {

    private CodePositionUtils() {}

    /**
     * Indent all the lines but the first. Keep the first as it is.
     * @param code - the code
     * @param indentation - number of space we should use to indent.
     * @param usesTabs - know if editor uses tabs or spaces
     * @return - the new code.
     */
    public static String indentOtherLines(String code, int indentation, boolean usesTabs) {
        String[] codeArray = code.split(LINE_SEPARATOR);
        List<String> codeArrayUpdated = new ArrayList<String>();
        if(codeArray.length <= 1) {
            return code;
        }
        codeArrayUpdated.add(codeArray[0]);
        for (int i = 1 ; i < codeArray.length ; i++){
            StringBuilder sb = new StringBuilder();
            for (int j = 0 ; j < indentation ; j++) {
                sb.append(usesTabs ? CHARACTER_TAB : CHARACTER_SPACE);
            }
            sb.append(codeArray[i]);
            codeArrayUpdated.add(sb.toString());
        }
        return String.join(LINE_SEPARATOR, codeArrayUpdated);
    }

    public static String indentAllLines(String code, int indentation, boolean usesTabs) {
        String[] codeArray = code.split(LINE_SEPARATOR);
        List<String> codeArrayUpdated = new ArrayList<String>();

        for (int i = 0 ; i < codeArray.length ; i++){
            StringBuilder sb = new StringBuilder();
            for (int j = 0 ; j < indentation ; j++) {
                sb.append(usesTabs ? CHARACTER_TAB : CHARACTER_SPACE);
            }
            sb.append(codeArray[i]);
            codeArrayUpdated.add(sb.toString());
        }
        return String.join(LINE_SEPARATOR, codeArrayUpdated);
    }

    /**
     * Know if the current line has tabs indentation
     *
     * @param lineOfCode - the line of code.
     * @return - boolean response if it is
     */
    public static boolean detectIfTabs(String lineOfCode) {
      return lineOfCode.length() > 0 && lineOfCode.charAt(0) == CHARACTER_TAB;
    }

    /**
     * Get the indentation and the number of spaces
     *
     * @param lineOfCode - the line of code.
     * @return - the number of spaces used to indent code.
     */
    public static int getIndentation(String lineOfCode, boolean isTabs) {
      int res = 0;
      if (isTabs) {
        for (int i = 0 ; i < lineOfCode.length() && lineOfCode.charAt(i) == CHARACTER_TAB; i++) {
          res++;
        }
        return res;
      } else {
        for (int i = 0 ; i < lineOfCode.length() && lineOfCode.charAt(i) == CHARACTER_SPACE; i++) {
            res++;
        }
        return res;
      }
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
        if (languageEnumeration == LanguageEnumeration.JAVA || languageEnumeration == LanguageEnumeration.SCALA) {

            boolean inComment = false;
            for (String line: codeArray) {
                final String lineWithoutSpace = line.replaceAll(" ", "");

                if (line.startsWith(JAVA_PACKAGE_KEYWORD)) {
                    start = start + line.length() + 1;
                    continue;
                }

                /**
                 * We do not want to add code on top of a comment
                 */
                if (lineWithoutSpace.startsWith("/*")) {
                    start = start + line.length() + 1;
                    inComment = true;
                    continue;
                }
                if (lineWithoutSpace.startsWith("*/") && inComment) {
                    inComment = false;
                    start = start + line.length() + 1;
                    continue;
                }
                if (lineWithoutSpace.startsWith("//")) {
                    inComment = false;
                    start = start + line.length() + 1;
                    continue;
                }

                if (inComment) {
                    start = start + line.length() + 1;
                    continue;
                }

                break;
            }
        }

        if (start < code.length()) {
            return start;
        } else {
            return 0;
        }
    }

    public static Optional<String> getKeywordFromLine(String line, int position) {
        try {
            if (line == null) {
                return Optional.empty();
            }
            int startPosition = position;
            while (startPosition > 0 && line.charAt(startPosition) != ' ') {

                startPosition = startPosition - 1;
            }
            if (startPosition > 0 && line.charAt(startPosition) == ' ') {
                startPosition = startPosition + 1;
            }
            return Optional.of(line.substring(startPosition, position + 1));

        } catch (StringIndexOutOfBoundsException e){
            return Optional.empty();
        }
    }

    /**
     * We should only auto-complete if we start with a point but not if there was a previous word before.
     * @param line
     * @param position
     * @return
     */
    public static boolean shouldAutocomplete(String line, int position) {
        try{
            int pos = position;
            boolean spaceMet = false;

            while(pos > 0){
                char c = line.charAt(pos);
                pos = pos - 1;

                if(c == ' ') {
                    spaceMet = true;
                    continue;
                }

                if(Character.isAlphabetic(c) || Character.isDigit(c) || c == '.' || c == '/') {
                    if (spaceMet) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } catch (StringIndexOutOfBoundsException e){
            return false;
        }
    }
}
