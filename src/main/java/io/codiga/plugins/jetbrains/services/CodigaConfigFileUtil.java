package io.codiga.plugins.jetbrains.services;

import static java.util.stream.Collectors.toList;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;
import org.jetbrains.yaml.psi.impl.YAMLScalarImpl;

import java.util.List;
import java.util.Optional;

/**
 * Utility for retrieving information about and from the codiga.yml config file in the project.
 */
public final class CodigaConfigFileUtil {

    private static final String RULESETS = "rulesets";
    private static final String CODIGA_CONFIG_FILE_NAME = "codiga.yml";

    /**
     * Finds the codiga.yml config file in the argument project.
     * Currently, the config file can be placed, and is recognized only in a project's root directory.
     *
     * @param project the current project
     * @return the codiga.yml config file, or null in the following cases:
     * the project is null, the project root directory is not found,
     * or the codiga.yml config file is not found
     */
    @Nullable
    public static YAMLFile findCodigaConfigFile(@Nullable Project project) {
        if (project == null) {
            return null;
        }
        var projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) {
            return null;
        }

        return Optional.of(projectDir)
            .filter(projDir -> projDir.exists() && projDir.isValid())
            .map(projDir -> projDir.findFileByRelativePath(CODIGA_CONFIG_FILE_NAME))
            .map(configFile -> ReadAction.compute(() -> PsiManager.getInstance(project).findFile(configFile)))
            .filter(YAMLFile.class::isInstance)
            .map(YAMLFile.class::cast)
            .orElse(null);
    }

    /**
     * Returns the list of ruleset names configured in the codiga.yml file.
     * <p>
     * In case of integration tests the caching is dropped. For some reason they fail with the following error,
     * that doesn't occur outside test execution:
     * <p>
     * <i>"... is retaining PSI, causing memory leaks and possible invalid element access"</i>
     *
     * @param codigaConfigFile the codiga.yml file in the current project
     * @return the list of ruleset names, or empty list if the codiga.yml file is configured incorrectly
     */
    @NotNull
    public static List<String> collectRulesetNames(@NotNull YAMLFile codigaConfigFile) {
        return ApplicationManager.getApplication().isUnitTestMode()
            ? compute(() -> readRulesetNames(codigaConfigFile))
            : compute(() -> CachedValuesManager.getManager(codigaConfigFile.getProject())
            .getCachedValue(
                codigaConfigFile.getProject(),
                () -> CachedValueProvider.Result.create(readRulesetNames(codigaConfigFile), codigaConfigFile)));
    }

    private static List<String> compute(@NotNull Computable<List<String>> computation) {
        //Since 'RosieRulesCacheUpdater' is not DumbAware, it is executed on EDT, thus needed to wrap the PSI operation in a ReadAction.
        return ApplicationManager.getApplication().runReadAction(computation);
    }

    private static List<String> readRulesetNames(@NotNull YAMLFile codigaConfigFile) {
        var documents = codigaConfigFile.getDocuments();

        if (documents.size() == 1) {
            var topLevelValue = documents.get(0).getTopLevelValue();

            if (topLevelValue instanceof YAMLBlockMappingImpl) {
                var rulesetsKeyValue = ((YAMLBlockMappingImpl) topLevelValue).getFirstKeyValue();
                //If the top level mapping is called "rulesets"
                if (RULESETS.equals(rulesetsKeyValue.getName())) {
                    var rulesetsValue = rulesetsKeyValue.getValue();
                    if (rulesetsValue instanceof YAMLSequence) {
                        var rulesetNames = (YAMLSequence) rulesetsValue;

                        //Return the list of ruleset names.
                        // If the list contains items other than plain text ones, they are not included.
                        return rulesetNames.getItems()
                            .stream()
                            .map(YAMLSequenceItem::getValue)
                            .filter(YAMLPlainTextImpl.class::isInstance) //this handles items as well that have an empty value
                            .map(YAMLPlainTextImpl.class::cast)
                            .map(YAMLScalarImpl::getTextValue)
                            .collect(toList());
                    }
                }
            }
        }
        return List.of();
    }

    private CodigaConfigFileUtil() {
        //Utility class
    }
}
