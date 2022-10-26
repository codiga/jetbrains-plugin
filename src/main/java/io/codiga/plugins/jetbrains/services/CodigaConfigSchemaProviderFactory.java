package io.codiga.plugins.jetbrains.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import com.jetbrains.jsonSchema.extension.SchemaType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Provides JSOn/YAML schema for the Codiga config file.
 */
public class CodigaConfigSchemaProviderFactory implements JsonSchemaProviderFactory {
    //Path is relative to src/main/resources
    private static final @NonNls String CONFIG_FILE_SCHEMA = "/schema/codiga-config.yaml";
    private static final String CODIGA_CONFIG_FILE_NAME = "codiga.yml";

    @NotNull
    @Override
    public List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
        return Collections.singletonList(CodigaConfigFileSchemaProvider.INSTANCE);
    }

    static final class CodigaConfigFileSchemaProvider implements JsonSchemaFileProvider {
        private static final CodigaConfigFileSchemaProvider INSTANCE = new CodigaConfigFileSchemaProvider();

        @Override
        public boolean isAvailable(@NotNull VirtualFile file) {
            return CODIGA_CONFIG_FILE_NAME.equals(file.getName());
        }

        @NotNull
        @Override
        public String getName() {
            return "Codiga Config";
        }

        @Nullable
        @Override
        public VirtualFile getSchemaFile() {
            //This is a hack to prevent integration tests to fail with the schema file being accessed outside allowed roots.
            // In some integration tests it surfaces into the test result, while in others it doesn't.
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                try {
                    return getMappingSchemaFile();
                } catch (AssertionError e) {
                    return null;
                }
            } else {
                return getMappingSchemaFile();
            }
        }

        @Nullable
        private static VirtualFile getMappingSchemaFile() {
            return JsonSchemaProviderFactory.getResourceFile(CodigaConfigFileSchemaProvider.class, CONFIG_FILE_SCHEMA);
        }

        @NotNull
        @Override
        public SchemaType getSchemaType() {
            return SchemaType.schema;
        }
    }
}
