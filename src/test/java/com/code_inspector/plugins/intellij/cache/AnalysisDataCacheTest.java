package com.code_inspector.plugins.intellij.cache;

import com.code_inspector.api.GetFileAnalysisQuery;
import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtilsTest;
import com.code_inspector.plugins.intellij.graphql.GraphQlQueryException;
import com.code_inspector.plugins.intellij.testutils.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AnalysisDataCacheTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodeInspectorGitUtilsTest.class);

    @Test
    public void testGetViolationsFromFileAnalysis() throws GraphQlQueryException {
        AnalysisDataCache analysisDataCache = AnalysisDataCache.getInstance();
        analysisDataCache.invalidateCache();
        assertEquals(0, analysisDataCache.getCacheFileAnalysis().size());
        assertEquals(0, analysisDataCache.getCacheProjectAnalysis().size());
        Optional<GetFileAnalysisQuery.GetFileAnalysis> res =
            analysisDataCache.getViolationsFromFileAnalysis(Optional.empty(), "filename.java", "code", Optional.empty());
        assertTrue(!res.isPresent());
        assertEquals(1, analysisDataCache.getCacheFileAnalysis().size());
        assertEquals(0, analysisDataCache.getCacheProjectAnalysis().size());
    }

    @Test
    public void testFetchViolationsFromProjectAnalysis() throws GraphQlQueryException {
        AnalysisDataCache analysisDataCache = AnalysisDataCache.getInstance();
        analysisDataCache.invalidateCache();
        assertEquals(0, analysisDataCache.getCacheFileAnalysis().size());
        assertEquals(0, analysisDataCache.getCacheProjectAnalysis().size());
        Optional<GetFileDataQuery.Project> res =
            analysisDataCache.getViolationsFromProjectAnalysis(1L, "revision", "path");
        assertTrue(!res.isPresent());
        assertEquals(0, analysisDataCache.getCacheFileAnalysis().size());
        assertEquals(1, analysisDataCache.getCacheProjectAnalysis().size());
    }

    @Test
    public void testByPassCacheIsLanguageIsUnknown() throws GraphQlQueryException {
        AnalysisDataCache analysisDataCache = AnalysisDataCache.getInstance();
        analysisDataCache.invalidateCache();
        assertEquals(0, analysisDataCache.getCacheFileAnalysis().size());
        assertEquals(0, analysisDataCache.getCacheProjectAnalysis().size());
        Optional<GetFileAnalysisQuery.GetFileAnalysis> res =
            analysisDataCache.getViolationsFromFileAnalysis(Optional.empty(), "filename", "code", Optional.empty());
        assertTrue(!res.isPresent());
        assertEquals(0, analysisDataCache.getCacheFileAnalysis().size());
        assertEquals(0, analysisDataCache.getCacheProjectAnalysis().size());
    }
}
