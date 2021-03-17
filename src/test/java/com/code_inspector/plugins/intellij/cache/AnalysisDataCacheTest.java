package com.code_inspector.plugins.intellij.cache;

import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtilsTest;
import com.code_inspector.plugins.intellij.testutils.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AnalysisDataCacheTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodeInspectorGitUtilsTest.class);

    @Test
    public void testGetData() {
        LOGGER.info(showTestHeader("testGetData"));
        assertTrue(true);
    }
}
