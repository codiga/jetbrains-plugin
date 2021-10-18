package com.code_inspector.plugins.intellij.dependencies;

import com.code_inspector.plugins.intellij.model.Dependency;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.code_inspector.plugins.intellij.Constants.JAVASCRIPT_DEPENDENCY_FILE;
import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;

public class JavascriptDependency extends AbstractDependency{

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    @VisibleForTesting
    public static List<Dependency> getDependenciesFromInputStream(InputStream inputStream) {
        List<Dependency> result = new ArrayList<>();
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");

            JsonElement jsonElement = JsonParser.parseReader(inputStreamReader);

            if(!jsonElement.isJsonObject()) {
                return ImmutableList.of();
            }
            JsonElement dependenciesElement = jsonElement.getAsJsonObject().get("dependencies");

            if(dependenciesElement == null || !dependenciesElement.isJsonObject()) {
                return ImmutableList.of();
            }

            JsonObject dependencies = dependenciesElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry: dependencies.entrySet()) {
                result.add(new Dependency(entry.getKey(), Optional.empty()));
            }
            return result;
        }
        catch (UnsupportedEncodingException | JsonIOException | JsonSyntaxException e) {
            LOGGER.info("JavascriptDependency - getDependenciesFromInputStream - error when parsing the JSON file");
            return ImmutableList.of();
        }
    }

    @Override
    public List<Dependency> getDependencies(PsiFile psiFile) {
        Optional<VirtualFile> dependencyFile = this.getDependencyFile(psiFile, JAVASCRIPT_DEPENDENCY_FILE);
        if(!dependencyFile.isPresent()) {
            return ImmutableList.of();
        }

        try {
            InputStream inputStream = dependencyFile.get().getInputStream();
            List<Dependency> result = getDependenciesFromInputStream(inputStream);
            inputStream.close();
            return result;
        } catch (IOException e){
            LOGGER.error("JavascriptDependency - getDependenciesFromInputStream - error when opening the file");
            return ImmutableList.of();
        }

    }
}
