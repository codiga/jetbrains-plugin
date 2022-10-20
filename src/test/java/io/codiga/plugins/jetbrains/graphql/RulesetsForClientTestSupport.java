package io.codiga.plugins.jetbrains.graphql;

import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.api.type.RosieRuleTypeEnumeration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Test utility for creating {@link io.codiga.api.GetRulesetsForClientQuery.RuleSetsForClient}.
 */
public final class RulesetsForClientTestSupport {

    private static final GetRulesetsForClientQuery.Rule PYTHON_RULE_1 = createAstRule("typename_1",
        "10",
        "python_rule_1",
        "ZnVuY3Rpb24gdmlzaXQocGF0dGVybiwgZmlsZW5hbWUsIGNvZGUpIHsKfQ==",
        LanguageEnumeration.PYTHON);
    private static final GetRulesetsForClientQuery.Rule PYTHON_RULE_2 = createAstRule("typename_1",
        "11",
        "python_rule_2",
        "ZnVuY3Rpb24gdmlzaXQocGF0dGVybiwgZmlsZW5hbWUsIGNvZGUpIHsKICAgIGFkZEVycm9yKGJ1aWxkRXJyb3IobW9kZS5zdGFydC5saW5lLCBtb2RlLnN0YXJ0LmNvbCwgbW9kZS5lbmQubGluZSwgbW9kZS5lbmQuY29sLCAiZXJyb3IgbWVzc2FnZSIsICJDUklUSUNBTCIsICJzZWN1cml0eSIpKTsKICB9Cn0=",
        LanguageEnumeration.PYTHON);
    private static final GetRulesetsForClientQuery.Rule PYTHON_RULE_3 = createAstRule("typename_1",
        "12",
        "python_rule_3",
        "ZnVuY3Rpb24gdmlzaXQocGF0dGVybiwgZmlsZW5hbWUsIGNvZGUpIHsKICAgIGNvbnN0IGVycm9yID0gYnVpbGRFcnJvcihtb2RlLnN0YXJ0LmxpbmUsIG1vZGUuc3RhcnQuY29sLCBtb2RlLmVuZC5saW5lLCBtb2RlLmVuZC5jb2wsICJlcnJvciBtZXNzYWdlIiwgIkNSSVRJQ0FMIiwgInNlY3VyaXR5Iik7CiAgICBhZGRFcnJvcihlcnJvcik7CiAgfQp9",
        LanguageEnumeration.PYTHON);
    private static final GetRulesetsForClientQuery.Rule PYTHON_RULE_4 = createAstRule("typename_1",
        "30",
        "python_rule_4",
        "ZnVuY3Rpb24gdmlzaXQocGF0dGVybiwgZmlsZW5hbWUsIGNvZGUpIHsKfQ==",
        LanguageEnumeration.PYTHON);
    private static final GetRulesetsForClientQuery.Rule PYTHON_RULE_5 = createAstRule("typename_1",
        "31",
        "python_rule_5",
        "ZnVuY3Rpb24gdmlzaXQocGF0dGVybiwgZmlsZW5hbWUsIGNvZGUpIHsKICAgIGNvbnN0IGVycm9yID0gYnVpbGRFcnJvcihtb2RlLnN0YXJ0LmxpbmUsIG1vZGUuc3RhcnQuY29sLCBtb2RlLmVuZC5saW5lLCBtb2RlLmVuZC5jb2wsICJlcnJvciBtZXNzYWdlIiwgIkNSSVRJQ0FMIiwgInNlY3VyaXR5Iik7CiAgICBhZGRFcnJvcihlcnJvcik7CiAgfQp9",
        LanguageEnumeration.PYTHON);
    private static final GetRulesetsForClientQuery.Rule JAVA_RULE_1 = createAstRule("typename_2",
        "20",
        "java_rule_1",
        "ZnVuY3Rpb24gdmlzaXQocGF0dGVybiwgZmlsZW5hbWUsIGNvZGUpIHsKICAgIGFkZEVycm9yKGJ1aWxkRXJyb3IobW9kZS5zdGFydC5saW5lLCBtb2RlLnN0YXJ0LmNvbCwgbW9kZS5lbmQubGluZSwgbW9kZS5lbmQuY29sLCAiZXJyb3IgbWVzc2FnZSIsICJDUklUSUNBTCIsICJzZWN1cml0eSIpKTsKICB9Cn0=",
        LanguageEnumeration.JAVA);

    /**
     * Returns test rulesets based on the argument ruleset names. Currently, rulesets are returned based on the first
     * value in the provided list.
     */
    public static Optional<List<GetRulesetsForClientQuery.RuleSetsForClient>> getRulesetsForClient(List<String> rulesetNames) {
        List<GetRulesetsForClientQuery.RuleSetsForClient> rulesets;
        switch (rulesetNames.get(0)) {
            case "singleRulesetSingleLanguage":
                rulesets = singleRulesetSingleLanguage(); //Python
                break;
            case "singleRulesetMultipleLanguages":
                rulesets = singleRulesetMultipleLanguages(); //Python, Java
                break;
            case "multipleRulesetsSingleLanguage":
                rulesets = multipleRulesetsSingleLanguage(); //Python
                break;
            case "multipleRulesetsMultipleLanguages":
                rulesets = multipleRulesetsMultipleLanguages(); //Python, Java
                break;
            default:
                rulesets = List.of();
        }
        return Optional.of(rulesets);
    }

    /**
     * Returns a single ruleset with a few rules, all configured for the same language.
     */
    private static List<GetRulesetsForClientQuery.RuleSetsForClient> singleRulesetSingleLanguage() {
        var rules = List.of(PYTHON_RULE_1, PYTHON_RULE_2, PYTHON_RULE_3);

        var ruleset = new GetRulesetsForClientQuery.RuleSetsForClient("typename", 1234, "python-ruleset", rules);

        return List.of(ruleset);
    }

    /**
     * Returns a single ruleset with a few rules configured for different languages.
     */
    private static List<GetRulesetsForClientQuery.RuleSetsForClient> singleRulesetMultipleLanguages() {
        var rules = List.of(PYTHON_RULE_1, JAVA_RULE_1, PYTHON_RULE_3);

        var ruleset = new GetRulesetsForClientQuery.RuleSetsForClient("typename", 2345, "mixed-ruleset", rules);

        return List.of(ruleset);
    }

    /**
     * Returns multiple rulesets with rules all configured for the same language.
     */
    private static List<GetRulesetsForClientQuery.RuleSetsForClient> multipleRulesetsSingleLanguage() {
        var rules = List.of(PYTHON_RULE_1, PYTHON_RULE_2);
        var rules2 = List.of(PYTHON_RULE_3);

        var ruleset = new GetRulesetsForClientQuery.RuleSetsForClient("typename", 1234, "python-ruleset", rules);
        var ruleset2 = new GetRulesetsForClientQuery.RuleSetsForClient("typename", 6789, "python-ruleset-2", rules2);

        return List.of(ruleset, ruleset2);
    }

    /**
     * Returns multiple rulesets with rules configured for different languages.
     */
    private static List<GetRulesetsForClientQuery.RuleSetsForClient> multipleRulesetsMultipleLanguages() {
        var rules = List.of(PYTHON_RULE_4, JAVA_RULE_1);
        var rules2 = List.of(PYTHON_RULE_5);

        var ruleset = new GetRulesetsForClientQuery.RuleSetsForClient("typename", 5678, "mixed-ruleset", rules);
        var ruleset2 = new GetRulesetsForClientQuery.RuleSetsForClient("typename", 6789, "python-ruleset", rules2);

        return List.of(ruleset, ruleset2);
    }

    private static GetRulesetsForClientQuery.Rule createAstRule(@NotNull String __typename,
                                                                @NotNull Object id,
                                                                @NotNull String name,
                                                                @NotNull String content,
                                                                @NotNull LanguageEnumeration language) {
        return new GetRulesetsForClientQuery.Rule(__typename, id, name, content, RosieRuleTypeEnumeration.AST, language, null, null);
    }

    private RulesetsForClientTestSupport() {
        //Utility class
    }
}
