package io.codiga.plugins.jetbrains.parameters;

import io.codiga.plugins.jetbrains.git.CodigaGitUtilsTest;
import io.codiga.plugins.jetbrains.model.Dependency;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public class AnalysisParametersJavascriptTest extends TestBase {

    private static Logger LOGGER = LoggerFactory.getLogger(CodigaGitUtilsTest.class);

    @Test
    public void testGetParametersFromDependencies() throws IOException {
        List<Dependency> dependencies = ImmutableList.of(
          new Dependency("react", Optional.empty()),
                new Dependency("@apollo/client", Optional.empty()),
                new Dependency("graphql", Optional.empty())
        );
        Optional<String> result = AnalysisParametersJavascript.getParametersFromDependencies(dependencies);
        Assertions.assertTrue(result.isPresent());
        String parameters = result.get();
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_REACT_ENABLED=true"));
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_APOLLO_CLIENT_ENABLED=true"));
        Assertions.assertTrue(parameters.contains("ENGINE_ESLINT_GRAPHQL_ENABLED=true"));
        Assertions.assertFalse(parameters.contains("ENGINE_ESLINT_AWS_SDK_ENABLED=true"));
        Assertions.assertFalse(parameters.contains("ENGINE_ESLINT_TYPEORM_ENABLED=true"));
    }

}
