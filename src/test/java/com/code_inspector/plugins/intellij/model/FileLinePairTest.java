package com.code_inspector.plugins.intellij.model;

import com.code_inspector.plugins.intellij.testutils.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLinePairTest extends TestBase {
    private static Logger LOGGER = LoggerFactory.getLogger(FileLinePairTest.class);

    @Test
    public void testEquals() {
        LOGGER.info(showTestHeader("testEquals"));
        FileLinePair flp1 = new FileLinePair("foo", 1);
        FileLinePair flp2 = new FileLinePair("foo", 1);
        FileLinePair flp3 = new FileLinePair("foo", 2);
        assertEquals(flp1, flp2);
        assertNotSame(flp1, flp3);
        assertNotSame(flp2, flp3);
    }
}
