package io.codiga.plugins.jetbrains.dependencies;

import io.codiga.plugins.jetbrains.model.Dependency;
import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import io.codiga.plugins.jetbrains.Constants;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JavascriptDependency extends AbstractDependency{

    public static final Logger LOGGER = Logger.getInstance(Constants.LOGGER_NAME);

    @VisibleForTesting
    @Override
    public List<Dependency> getDependenciesFromInputStream(InputStream inputStream) {
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
    public String getDependencyFilename() {
        return Constants.JAVASCRIPT_DEPENDENCY_FILE;
    }
}
