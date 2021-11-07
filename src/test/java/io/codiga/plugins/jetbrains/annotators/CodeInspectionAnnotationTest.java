package io.codiga.plugins.jetbrains.annotators;

import io.codiga.api.GetFileAnalysisQuery;
import io.codiga.api.GetFileDataQuery;
import io.codiga.plugins.jetbrains.git.CodigaGitUtilsTest;
import io.codiga.plugins.jetbrains.graphql.GraphQlQueryException;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.util.TextRange;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CodeInspectionAnnotationTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodigaGitUtilsTest.class);

    @Test
    public void testFilterDuplicatesByFileNameLineAndDescription() throws GraphQlQueryException {
        Annotation annotation1 = new Annotation(
                Optional.empty(),
                Optional.empty(),
                AnnotationKind.Violation,
                "msg1",
                "filename",
                new TextRange(1,2)
                );
        Annotation annotation2 = new Annotation(
                Optional.empty(),
                Optional.empty(),
                AnnotationKind.Violation,
                "msg1",
                "filename",
                new TextRange(1,2)
        );
        Annotation annotation3 = new Annotation(
                Optional.empty(),
                Optional.empty(),
                AnnotationKind.Violation,
                "msg1",
                "filename",
                new TextRange(2,3)
        );
        Annotation annotation4 = new Annotation(
                Optional.empty(),
                Optional.empty(),
                AnnotationKind.Violation,
                "msg2",
                "filename",
                new TextRange(1,2)
        );
        Annotation annotation5 = new Annotation(
                Optional.empty(),
                Optional.empty(),
                AnnotationKind.Violation,
                "msg1",
                "filename2",
                new TextRange(1,2)
        );
        assertEquals(1,
                Annotation.filterDuplicatesByFileNameLineAndDescription(
                        ImmutableList.of(annotation1, annotation2)
                ).size());
        assertEquals(2,
                Annotation.filterDuplicatesByFileNameLineAndDescription(
                        ImmutableList.of(annotation1, annotation3)
                ).size());
        assertEquals(2,
                Annotation.filterDuplicatesByFileNameLineAndDescription(
                        ImmutableList.of(annotation1, annotation4)
                ).size());
        assertEquals(2,
                Annotation.filterDuplicatesByFileNameLineAndDescription(
                        ImmutableList.of(annotation1, annotation5)
                ).size());
    }

}
