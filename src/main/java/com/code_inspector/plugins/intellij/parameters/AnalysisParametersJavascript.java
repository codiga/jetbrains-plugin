package com.code_inspector.plugins.intellij.parameters;

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
    public static Optional<String> getAnalysisParametersInputStream(InputStream inputStream) {
        List<String> stringParameters = new ArrayList<String>();
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");

            JsonElement jsonElement = JsonParser.parseReader(inputStreamReader);

            if(!jsonElement.isJsonObject()) {
                return Optional.empty();
            }
            JsonElement dependenciesElement = jsonElement.getAsJsonObject().get("dependencies");

            if(dependenciesElement == null || !dependenciesElement.isJsonObject()) {
                return Optional.empty();
            }

            JsonObject dependencies = dependenciesElement.getAsJsonObject();
            for(Map.Entry<String, String> entry: DEPENDENCY_TO_CONFIGURATION_DIRECTIVE.entrySet()) {
                if (dependencies.has(entry.getKey())) {
                    stringParameters.add(entry.getValue());
                }
            }

            if(!stringParameters.isEmpty()) {
                return Optional.of(String.join(";", stringParameters));
            }
        }
        catch (UnsupportedEncodingException | JsonIOException | JsonSyntaxException e) {
            LOGGER.info("error when parsing the JSON file");
            return Optional.empty();
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
        // Get all the files in the root directory of the project
        List<VirtualFile> rootFiles = Arrays.stream(ProjectRootManager.getInstance(psiFile.getProject())
                .getContentRoots())
                .flatMap(v -> Arrays.stream(VfsUtil.getChildren(v)))
                .collect(Collectors.toList());

        // Find package.json in these files
        Optional<VirtualFile> packageFileOptional = rootFiles.stream().filter(v -> v.getName().equalsIgnoreCase(JAVASCRIPT_DEPENDENCY_FILE)).findFirst();

        // if no package present, we do not have any option to show
        if (!packageFileOptional.isPresent()) {
            return Optional.empty();
        }

        try {
            InputStream inputStream = packageFileOptional.get().getInputStream();
            Optional<String> result = getAnalysisParametersInputStream(inputStream);
            inputStream.close();
            return result;
        } catch (IOException e){
            LOGGER.error("error when opening the file");
            return Optional.empty();
        }
    }
}
