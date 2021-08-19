package com.code_inspector.plugins.intellij.parameters;

import com.code_inspector.api.AddViolationToIgnoreMutation;
import com.code_inspector.api.GetFileAnalysisQuery;
import com.code_inspector.api.GetFileDataQuery;
import com.code_inspector.api.GetProjectsQuery;
import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.git.CodeInspectorGitUtilsTest;
import com.code_inspector.plugins.intellij.graphql.CodeInspectorApi;
import com.code_inspector.plugins.intellij.testutils.TestBase;
import com.google.common.collect.ImmutableList;
import git4idea.ui.branch.L;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.code_inspector.plugins.intellij.graphql.LanguageUtils.getLanguageFromFilename;
import static com.code_inspector.plugins.intellij.parameters.AnalysisParametersJavascript.getAnalysisParametersInputStream;


public class AnalysisParametersJavascriptTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodeInspectorGitUtilsTest.class);

    @Test
    public void testParsePackageFrontendJsonValid() throws IOException {
        FileInputStream fileInputStream = getInputStream("package-frontend.json");
        Optional<String> result = getAnalysisParametersInputStream(fileInputStream);
        Assertions.assertTrue(result.isPresent());
        String parameters = result.get();
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_REACT_ENABLED=true"));
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_APOLLO_CLIENT_ENABLED=true"));
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_GRAPHQL_ENABLED=true"));
        Assertions.assertFalse(parameters.contains("ENGINE_ESLINT_AWS_SDK_ENABLED=true"));
        Assertions.assertFalse(parameters.contains("ENGINE_ESLINT_TYPEORM_ENABLED=true"));

        fileInputStream.close();
    }

    @Test
    public void testParsePackageBackendJsonValid() throws IOException {
        FileInputStream fileInputStream = getInputStream("package-backend.json");
        Optional<String> result = getAnalysisParametersInputStream(fileInputStream);
        Assertions.assertTrue(result.isPresent());
        String parameters = result.get();
        Assertions.assertFalse(parameters.contains("ENGINE_ESLINT_REACT_ENABLED=true"));
        Assertions.assertFalse(parameters.contains("ENGINE_ESLINT_APOLLO_CLIENT_ENABLED=true"));
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_GRAPHQL_ENABLED=true"));
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_AWS_SDK_ENABLED=true"));
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_TYPEORM_ENABLED=true"));

        fileInputStream.close();
    }

    @Test
    public void testParsePackageInvalid() throws IOException{
        FileInputStream fileInputStream = getInputStream("package-invalid.json");

        Optional<String> result = getAnalysisParametersInputStream(fileInputStream);
        Assertions.assertFalse(result.isPresent());
        fileInputStream.close();
    }

    @Test
    public void testParsePackageWithoutDependencies() throws IOException{
        FileInputStream fileInputStream = getInputStream("package-without-dependencies.json");
        Optional<String> result = getAnalysisParametersInputStream(fileInputStream);
        Assertions.assertFalse(result.isPresent());

        fileInputStream.close();
    }
}
