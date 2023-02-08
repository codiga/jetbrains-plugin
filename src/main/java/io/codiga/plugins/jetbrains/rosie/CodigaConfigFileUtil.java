package io.codiga.plugins.jetbrains.rosie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiManager;
import io.codiga.plugins.jetbrains.rosie.model.codiga.CodigaYmlConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;

import java.util.Optional;

/**
 * Utility for retrieving information about and from the codiga.yml config file in the project.
 */
public final class CodigaConfigFileUtil {

    /**
     * Combines the following validations for the ruleset name:
     * <ul>
     *     <li>it must be at least 5 characters long</li>
     *     <li>it must be at most 32 character long</li>
     *     <li>it must start with a lowercase letter or number, but not a dash</li>
     *     <li>it must consist of lowercase alphanumerical characters and dash</li>
     * </ul>
     *
     * @see <a href="https://regexr.com/730qs">Test cases on regexr</a>
     */
    private static final String CODIGA_RULESET_NAME_PATTERN = "^[a-z0-9][a-z0-9-]{4,31}$";
    public static final String CODIGA_CONFIG_FILE_NAME = "codiga.yml";

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
     * Parses the argument codiga.yml file into a config object.
     * <p>
     * If the file is malformed, it returns an {@link CodigaYmlConfig#EMPTY} configuration.
     *
     * @param codigaConfigFile the codiga.yml file as PSI
     * @return the configuration, or empty if the file is malformed
     */
    public static CodigaYmlConfig parseCodigaYml(@NotNull YAMLFile codigaConfigFile) {
        //Since 'RosieRulesCacheUpdater' is not DumbAware, it is executed on EDT, thus needed to wrap the PSI operation in a ReadAction.
        return ApplicationManager.getApplication().runReadAction((Computable<CodigaYmlConfig>) () -> {
            var mapper = new ObjectMapper(new YAMLFactory());
            try {
                return Optional.ofNullable(mapper.readValue(codigaConfigFile.getText(), CodigaYmlConfig.class))
                    .orElse(CodigaYmlConfig.EMPTY);
            } catch (JsonProcessingException e) {
                return CodigaYmlConfig.EMPTY;
            }
        });
    }

    public static boolean isRulesetNameValid(String rulesetName) {
        return rulesetName.matches(CODIGA_RULESET_NAME_PATTERN);
    }

    private CodigaConfigFileUtil() {
        //Utility class
    }
}
