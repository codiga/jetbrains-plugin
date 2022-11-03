package io.codiga.plugins.jetbrains.rosie;

import com.intellij.openapi.project.Project;

/**
 * Provides default ruleset configurations for the Codiga config file.
 *
 * @see io.codiga.plugins.jetbrains.starter.RosieStartupActivity#showConfigureDefaultConfigFileNotification(Project)
 */
public final class CodigaRulesetConfigs {

    public static final String DEFAULT_PYTHON_RULESET_CONFIG =
        "rulesets:\n" +
            "  - python-security\n" +
            "  - python-best-practices\n" +
            "  - python-code-style";

    private CodigaRulesetConfigs() {
        //Utility class
    }
}
