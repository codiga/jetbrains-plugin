package com.code_inspector.plugins.intellij.graphql;



import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotation;
import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotationKind;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtilsTest;
import com.code_inspector.plugins.intellij.model.FileOffset;
import com.code_inspector.plugins.intellij.model.LineOffset;
import com.code_inspector.plugins.intellij.testutils.TestBase;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.util.TextRange;
import org.junit.jupiter.api.Test;

import static com.code_inspector.plugins.intellij.graphql.CodeInspectorApiUtils.mapComplexFunction;
import static com.code_inspector.plugins.intellij.graphql.CodeInspectorApiUtils.mapLongFunction;
import static org.mockito.Mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigDecimal;
import java.util.Optional;


public class CodeInspectorApiUtilsTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodeInspectorGitUtilsTest.class);

    @Test
    public void testMapLongFunctionOffsetAbsent() {
        LOGGER.info(showTestHeader("testMapLongFunctionOffsetAbsent"));
        GetFileDataQuery.LongFunction longFunction = mock(GetFileDataQuery.LongFunction.class);
        FileOffset fileOffset = mock(FileOffset.class);
        when(fileOffset.getLineOffsetAtLine(anyInt())).thenReturn(Optional.empty());
        when(longFunction.lineStart()).thenReturn(new BigDecimal(1.0));
        when(longFunction.length()).thenReturn(new BigDecimal(40));

        Optional<CodeInspectionAnnotation> annotation = mapLongFunction(1L, 42L, longFunction, fileOffset, ImmutableMap.of());
        assertTrue(!annotation.isPresent());
    }

    @Test
    public void testMapLongFunctionOffsetPresent() {
        LOGGER.info(showTestHeader("testMapLongFunctionOffsetPresent"));
        String expectedDescription = "Function superfunction is too long with a length of 40 lines. Consider refactoring the function into sub-functions.";
        GetFileDataQuery.LongFunction longFunction = mock(GetFileDataQuery.LongFunction.class);
        FileOffset fileOffset = mock(FileOffset.class);
        LineOffset lineOffset = new LineOffset(42, 51, 45);

        when(fileOffset.getLineOffsetAtLine(anyInt())).thenReturn(Optional.of(lineOffset));
        when(longFunction.lineStart()).thenReturn(new BigDecimal(1.0));
        when(longFunction.length()).thenReturn(new BigDecimal(40));
        when(longFunction.filename()).thenReturn("foobar.txt");
        when(longFunction.functionName()).thenReturn("superfunction");

        Optional<CodeInspectionAnnotation> annotationOptional = mapLongFunction(1L, 42L, longFunction, fileOffset, ImmutableMap.of());
        assertTrue(annotationOptional.isPresent());
        CodeInspectionAnnotation annotation = annotationOptional.get();
        assertEquals(1L, annotation.getProjectId().longValue());
        assertEquals(42L, annotation.getAnalysisId().longValue());
        assertEquals("foobar.txt", annotation.getFilename());
        assertEquals(CodeInspectionAnnotationKind.LongFunction, annotation.getAnnotationKind());
        assertEquals(new TextRange(45, 51), annotation.range());
        assertEquals(Optional.empty(), annotation.getRule());
        assertEquals(Optional.empty(), annotation.getRuleUrl());
        assertEquals(Optional.empty(), annotation.getTool());
        assertEquals(Optional.empty(), annotation.getSeverity());
        assertEquals(Optional.empty(), annotation.getCategory());
        assertEquals(expectedDescription, annotation.getMessage());
    }

    @Test
    public void testMapComplexFunctionOffsetAbsent() {
        LOGGER.info(showTestHeader("testMapComplexFunctionOffsetAbsent"));
        GetFileDataQuery.ComplexFunction complexFunction = mock(GetFileDataQuery.ComplexFunction.class);
        FileOffset fileOffset = mock(FileOffset.class);
        when(fileOffset.getLineOffsetAtLine(anyInt())).thenReturn(Optional.empty());
        when(complexFunction.lineStart()).thenReturn(new BigDecimal(1.0));
        when(complexFunction.complexity()).thenReturn(new BigDecimal(51));

        Optional<CodeInspectionAnnotation> annotation = mapComplexFunction(1L, 42L, complexFunction, fileOffset, ImmutableMap.of());
        assertTrue(!annotation.isPresent());
    }

    @Test
    public void testMapComplexFunctionOffsetPresent() {
        LOGGER.info(showTestHeader("testMapComplexFunctionOffsetPresent"));
        String expectedDescription = "Function superfunction is too complex with a cyclomatic complexity of 51. Consider reducing the complexity of the function" +
            "by refactoring it or simplifying its logic.";
        GetFileDataQuery.ComplexFunction complexFunction = mock(GetFileDataQuery.ComplexFunction.class);
        FileOffset fileOffset = mock(FileOffset.class);
        LineOffset lineOffset = new LineOffset(42, 51, 45);

        when(fileOffset.getLineOffsetAtLine(anyInt())).thenReturn(Optional.of(lineOffset));
        when(complexFunction.lineStart()).thenReturn(new BigDecimal(1.0));
        when(complexFunction.complexity()).thenReturn(new BigDecimal(51));
        when(complexFunction.filename()).thenReturn("foobar.txt");
        when(complexFunction.functionName()).thenReturn("superfunction");

        Optional<CodeInspectionAnnotation> annotationOptional = mapComplexFunction(1L, 42L, complexFunction, fileOffset, ImmutableMap.of());
        assertTrue(annotationOptional.isPresent());
        CodeInspectionAnnotation annotation = annotationOptional.get();
        assertEquals(1L, annotation.getProjectId().longValue());
        assertEquals(42L, annotation.getAnalysisId().longValue());
        assertEquals("foobar.txt", annotation.getFilename());
        assertEquals(CodeInspectionAnnotationKind.ComplexFunction, annotation.getAnnotationKind());
        assertEquals(new TextRange(45, 51), annotation.range());
        assertEquals(Optional.empty(), annotation.getRule());
        assertEquals(Optional.empty(), annotation.getRuleUrl());
        assertEquals(Optional.empty(), annotation.getTool());
        assertEquals(Optional.empty(), annotation.getSeverity());
        assertEquals(Optional.empty(), annotation.getCategory());
        assertEquals(expectedDescription, annotation.getMessage());
    }
}
