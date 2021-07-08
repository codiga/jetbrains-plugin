package com.code_inspector.plugins.intellij.annotators;

import com.code_inspector.api.GetFileAnalysisQuery;
import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.cache.AnalysisDataCache;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtilsTest;
import com.code_inspector.plugins.intellij.graphql.GraphQlQueryException;
import com.code_inspector.plugins.intellij.testutils.TestBase;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.util.TextRange;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CodeInspectionAnnotationTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodeInspectorGitUtilsTest.class);

    @Test
    public void testFilterDuplicatesByFileNameLineAndDescription() throws GraphQlQueryException {
        CodeInspectionAnnotation annotation1 = new CodeInspectionAnnotation(
                Optional.empty(),
                Optional.empty(),
                CodeInspectionAnnotationKind.Violation,
                "msg1",
                "filename",
                new TextRange(1,2)
                );
        CodeInspectionAnnotation annotation2 = new CodeInspectionAnnotation(
                Optional.empty(),
                Optional.empty(),
                CodeInspectionAnnotationKind.Violation,
                "msg1",
                "filename",
                new TextRange(1,2)
        );
        CodeInspectionAnnotation annotation3 = new CodeInspectionAnnotation(
                Optional.empty(),
                Optional.empty(),
                CodeInspectionAnnotationKind.Violation,
                "msg1",
                "filename",
                new TextRange(2,3)
        );
        CodeInspectionAnnotation annotation4 = new CodeInspectionAnnotation(
                Optional.empty(),
                Optional.empty(),
                CodeInspectionAnnotationKind.Violation,
                "msg2",
                "filename",
                new TextRange(1,2)
        );
        CodeInspectionAnnotation annotation5 = new CodeInspectionAnnotation(
                Optional.empty(),
                Optional.empty(),
                CodeInspectionAnnotationKind.Violation,
                "msg1",
                "filename2",
                new TextRange(1,2)
        );
        assertEquals(1,
                CodeInspectionAnnotation.filterDuplicatesByFileNameLineAndDescription(
                        ImmutableList.of(annotation1, annotation2)
                ).size());
        assertEquals(2,
                CodeInspectionAnnotation.filterDuplicatesByFileNameLineAndDescription(
                        ImmutableList.of(annotation1, annotation3)
                ).size());
        assertEquals(2,
                CodeInspectionAnnotation.filterDuplicatesByFileNameLineAndDescription(
                        ImmutableList.of(annotation1, annotation4)
                ).size());
        assertEquals(2,
                CodeInspectionAnnotation.filterDuplicatesByFileNameLineAndDescription(
                        ImmutableList.of(annotation1, annotation5)
                ).size());
    }

}
