package com.code_inspector.plugins.intellij.model;

import com.code_inspector.plugins.intellij.testutils.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class FileOffsetTest extends TestBase {
    private static Logger LOGGER = LoggerFactory.getLogger(FileOffsetTest.class);

    @Test
    public void testParsing() {
        LOGGER.info(showTestHeader("testParsing"));
        String fileContent = readFile("FileOffset.java");
        assertNotNull(fileContent);
        FileOffset fileOffset = new FileOffset(Arrays.asList(fileContent.split("\n")));

        Optional<LineOffset> lineOffset;

        lineOffset = fileOffset.getLineOffsetAtLine(2); // line 2 is empty
        assertTrue(lineOffset.isPresent());
        assertEquals(51, lineOffset.get().startOffset);

        lineOffset = fileOffset.getLineOffsetAtLine(3); // line 3 is "import java.util.ArrayList;"
        assertTrue(lineOffset.isPresent());
        assertEquals(52, lineOffset.get().startOffset);

        lineOffset = fileOffset.getLineOffsetAtLine(4); // line 4 is "import java.util.List;"
        assertTrue(lineOffset.isPresent());
        assertEquals(fileContent.indexOf("import java.util.List;"), lineOffset.get().startOffset);

        lineOffset = fileOffset.getLineOffsetAtLine(15); // line 15 is "      int offset = 0;"
        assertTrue(lineOffset.isPresent());
        assertEquals(fileContent.indexOf("int offset = 0;"), lineOffset.get().codeStartOffset);
    }

    /**
     * Make sure that if we pass null to the constructor, the program does
     * not crash.
     */
    @Test
    public void testParsingNull() {
        LOGGER.info(showTestHeader("testParsingNull"));
        FileOffset fileOffset = new FileOffset(null);
        assertTrue(!fileOffset.getLineOffsetAtLine(0).isPresent());
    }

    @Test
    public void testOutOfBounds() {
        LOGGER.info(showTestHeader("testOutOfBounds"));
        String fileContent = readFile("FileOffset.java");
        assertNotNull(fileContent);
        FileOffset fileOffset = new FileOffset(Arrays.asList(fileContent.split("\n")));

        Optional<LineOffset> lineOffset;

        lineOffset = fileOffset.getLineOffsetAtLine(1000); // line 1000 does not exist
        assertTrue(!lineOffset.isPresent());
    }
}
