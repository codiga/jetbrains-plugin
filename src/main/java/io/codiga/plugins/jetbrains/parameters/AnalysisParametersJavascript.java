package io.codiga.plugins.jetbrains.parameters;

import io.codiga.plugins.jetbrains.dependencies.JavascriptDependency;
import io.codiga.plugins.jetbrains.model.Dependency;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;

public final class AnalysisParametersJavascript {

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Map that associate a dependency with a given configuration directive.
     */
    public static ImmutableMap<String, String> DEPENDENCY_TO_CONFIGURATION_DIRECTIVE =
            new ImmutableMap.Builder<String, String>()
                    .put("react", "ENGINE_ESLINT_REACT_ENABLED=true")
                    .put("@apollo/client", "ENGINE_ESLINT_APOLLO_CLIENT_ENABLED=true")
                    .put("graphql", "ENGINE_ESLINT_GRAPHQL_ENABLED=true")
                    .put("express", "ENGINE_ESLINT_EXPRESS_ENABLED=true")
                    .put("aws-sdk", "ENGINE_ESLINT_AWS_SDK_ENABLED=true")
                    .put("typeorm", "ENGINE_ESLINT_TYPEORM_ENABLED=true")
                    .put("jest", "ENGINE_ESLINT_JEST_ENABLED=true")
                    .build();

    private AnalysisParametersJavascript() {}

    @VisibleForTesting
    public static Optional<String> getParametersFromDependencies(List<Dependency> dependencies) {
        List<String> stringParameters = new ArrayList<String>();

        for (Dependency dependency: dependencies){
            if(DEPENDENCY_TO_CONFIGURATION_DIRECTIVE.containsKey(dependency.getName())) {
                stringParameters.add(DEPENDENCY_TO_CONFIGURATION_DIRECTIVE.get(dependency.getName()));
            }
        }
        if(!stringParameters.isEmpty()) {
            return Optional.of(String.join(";", stringParameters));
        }
        return Optional.empty();
    }

    /**
     * Get the parameters for a particular file. It opens the package.json file,
     * look at the dependencies and add the necessary parameters.
     *
     * @param psiFile - the file being analyzed
     * @return - the list of parameters if any.
     */
    public static Optional<String> getAnalysisParameters(PsiFile psiFile) {
        JavascriptDependency javascriptDependency = new JavascriptDependency();
        List<Dependency> dependencies = javascriptDependency.getDependencies(psiFile);
        return getParametersFromDependencies(dependencies);
    }
}
