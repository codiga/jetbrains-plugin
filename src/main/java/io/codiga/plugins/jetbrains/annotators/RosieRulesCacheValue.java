package io.codiga.plugins.jetbrains.annotators;

import io.codiga.plugins.jetbrains.model.rosie.RosieRule;

import java.util.List;
import java.util.Objects;

/**
 * Cache value for the {@link RosieRulesCache}.
 */
public final class RosieRulesCacheValue {

    /**
     * Caching {@link RosieRule} instances, as the number of times the {@link RosieRulesCache} is updated is much
     * less than the number of requests to the Rosie service. Therefore, only one mapping to
     * {@code RosieRule} instances is performed per cache update, instead of for each Rosie service request.
     */
    private final List<RosieRule> rules;

    public RosieRulesCacheValue(List<RosieRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<RosieRule> getRules() {
        return rules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RosieRulesCacheValue that = (RosieRulesCacheValue) o;
        return Objects.equals(rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rules);
    }
}
