package io.codiga.plugins.jetbrains.annotators;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.plugins.jetbrains.model.rosie.RosieRule;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Cache value for the {@link RosieRulesCache}.
 */
@EqualsAndHashCode
public final class RosieRulesCacheValue {

    /**
     * Maps the rule id to [rulesetName, ruleName, RosieRule].
     * <p>
     * This is necessary, so that in {@code RosieImpl#getAnnotations()} we can pass the rule name and ruleset name,
     * based on the rule id to {@link io.codiga.plugins.jetbrains.model.rosie.RosieAnnotation},
     * therefore we have access to those names in {@link AnnotationFixOpenBrowser}.
     * <p>
     * {@link io.codiga.plugins.jetbrains.model.rosie.RosieRuleResponse} doesn't contain the rule name,
     * only the rule id, so we have to cache the rule name as well.
     */
    private final Map<String, RuleWithNames> rules;
    /**
     * Caching {@link RosieRule} instances, as the number of times the {@link RosieRulesCache} is updated is much
     * less than the number of requests sent to the Rosie service. Therefore, only one mapping to
     * {@code RosieRule} instances is performed per cache update, instead of for each Rosie service request.
     */
    private final List<RosieRule> rosieRules;

    public RosieRulesCacheValue(List<RuleWithNames> rules) {
        this.rules = rules.stream().collect(toMap(rule -> rule.rosieRule.id, Function.identity()));
        this.rosieRules = this.rules.values().stream().map(rule -> rule.rosieRule).collect(toList());
    }

    public Map<String, RuleWithNames> getRules() {
        return rules;
    }

    public List<RosieRule> getRosieRules() {
        return rosieRules;
    }

    @EqualsAndHashCode
    public static final class RuleWithNames {
        public final String rulesetName;
        public final String ruleName;
        public final RosieRule rosieRule;

        public RuleWithNames(String rulesetName, GetRulesetsForClientQuery.Rule rule) {
            this.rulesetName = rulesetName;
            this.ruleName = rule.name();
            this.rosieRule = new RosieRule(rule);
        }
    }
}
