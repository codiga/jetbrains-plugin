package com.code_inspector.plugins.intellij.parameters;

import com.code_inspector.plugins.intellij.dependencies.JavascriptDependency;
import com.code_inspector.plugins.intellij.model.Dependency;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.ResourceUtil;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import static com.code_inspector.plugins.intellij.Constants.JAVASCRIPT_DEPENDENCY_FILE;
import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;

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
