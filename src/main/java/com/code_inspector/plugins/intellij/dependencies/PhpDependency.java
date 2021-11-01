package com.code_inspector.plugins.intellij.dependencies;

import com.code_inspector.plugins.intellij.model.Dependency;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.code_inspector.plugins.intellij.Constants.*;

/**
 * This class gets all the dependency from PHP composer.json
 */
public class PhpDependency extends AbstractDependency{

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    @VisibleForTesting
    @Override
    public List<Dependency> getDependenciesFromInputStream(InputStream inputStream) {
        Set<Dependency> result = new HashSet<>();
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");

            JsonElement jsonElement = JsonParser.parseReader(inputStreamReader);

            if(!jsonElement.isJsonObject()) {
                return ImmutableList.of();
            }
            JsonElement requireElement = jsonElement.getAsJsonObject().get("require");
            JsonElement requireDevElement = jsonElement.getAsJsonObject().get("require-dev");

            if (requireElement != null && requireElement.isJsonObject()) {
                JsonObject dependencies = requireElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry: dependencies.entrySet()) {
                    result.add(new Dependency(entry.getKey(), Optional.empty()));
                }
            }

            if (requireDevElement != null && requireDevElement.isJsonObject()) {
                JsonObject dependencies = requireDevElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry: dependencies.entrySet()) {
                    result.add(new Dependency(entry.getKey(), Optional.empty()));
                }
            }

            return new ArrayList<>(result);
        }
        catch (UnsupportedEncodingException | JsonIOException | JsonSyntaxException e) {
            LOGGER.info("PhpDependency - getDependenciesFromInputStream - error when parsing the JSON file");
            return ImmutableList.of();
        }
    }

    @Override
    public String getDependencyFilename() {
        return PHP_DEPENDENCY_FILE;
    }
}
