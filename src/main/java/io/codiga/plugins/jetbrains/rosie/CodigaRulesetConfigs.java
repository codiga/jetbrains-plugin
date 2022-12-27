package io.codiga.plugins.jetbrains.rosie;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.utils.LanguageUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides default ruleset configurations for the Codiga config file.
 *
 * @see io.codiga.plugins.jetbrains.starter.RosieStartupActivity#showConfigureDefaultConfigFileNotification(com.intellij.openapi.project.Project)
 */
public final class CodigaRulesetConfigs {

    /**
     * This id comes via {@code com.jetbrains.python.sdk.PythonSdkType},
     * from {@code com.jetbrains.python.PyNames#PYTHON_SDK_ID_NAME}.
     */
    private static final String PYTHON_SDK_ID_NAME = "Python SDK";
    /**
     * File extensions associated to the currently supported Rosie languages.
     */
    private static final List<String> FILE_EXTENSIONS = List.of("py", "js", "jsx", "ts", "tsx");

    private static final String DEFAULT_PYTHON_RULESET_CONFIG =
        "rulesets:\n" +
            "  - python-security\n" +
            "  - python-best-practices\n" +
            "  - python-code-style";

    private static final String DEFAULT_JAVASCRIPT_RULESET_CONFIG =
        "rulesets:\n" +
            "  - jsx-a11y\n" +
            "  - jsx-react\n" +
            "  - react-best-practices";

    /**
     * Based on different project settings, it returns the default ruleset configuration to populate
     * codiga.yml with.
     * <p>
     * First, it checks if a Python SDK is configured, then if it isn't, it moves on to check if a file
     * with any of the Python/JavaScript/TypeScript file extensions is present in the project.
     *
     * @param project the current project
     * @return the ruleset configuration, or empty Optional, if no suitable project configuration is found
     */
    public static Optional<String> getDefaultRulesetsForProject(Project project) {
        var language = isPythonSdkConfigured(project) ? LanguageEnumeration.PYTHON : findSupportedFile(project);
        if (language == null || language == LanguageEnumeration.UNKNOWN) {
            return Optional.empty();
        }

        return language == LanguageEnumeration.PYTHON
            ? Optional.of(DEFAULT_PYTHON_RULESET_CONFIG)
            : Optional.of(DEFAULT_JAVASCRIPT_RULESET_CONFIG);
    }

    private static boolean isPythonSdkConfigured(Project project) {
        //First, check if a Python SDK is configured on project level
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        boolean isPythonSdkConfigured = projectSdk != null && PYTHON_SDK_ID_NAME.equals(projectSdk.getSdkType().getName());
        //If Python is not configured on project level, there might still be one or more modules that have Python SDK configured.
        if (!isPythonSdkConfigured) {
            Module[] modules = ModuleManager.getInstance(project).getModules();
            isPythonSdkConfigured = Arrays.stream(modules)
                .map(ModuleRootManager::getInstance)
                .map(ModuleRootModel::getSdk)
                .filter(Objects::nonNull)
                .anyMatch(moduleSdk -> PYTHON_SDK_ID_NAME.equals(moduleSdk.getSdkType().getName()));
        }
        return isPythonSdkConfigured;
    }

    /**
     * Iterates through the supported file extensions and checks if there is at least one file in the project with that extension.
     *
     * @param project the current project
     * @return the language for a file found with a supported extension, or null of no such file was found
     */
    @Nullable
    private static LanguageEnumeration findSupportedFile(Project project) {
        return FILE_EXTENSIONS.stream()
            .filter(extension -> !FilenameIndex.getAllFilesByExt(project, extension, GlobalSearchScope.projectScope(project)).isEmpty())
            .map(LanguageUtils::getLanguageFromExtension)
            .findFirst()
            .orElse(null);
    }

    private CodigaRulesetConfigs() {
        //Utility class
    }
}
