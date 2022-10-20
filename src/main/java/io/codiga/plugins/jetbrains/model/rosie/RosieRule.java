package io.codiga.plugins.jetbrains.model.rosie;

import io.codiga.api.GetRulesetsForClientQuery;

/**
 * The rule descriptor converted from {@link GetRulesetsForClientQuery.Rule}, and sent to Rosie.
 */
public class RosieRule {
    public String id;
    public String contentBase64;
    public String language;
    public String type;
    public String entityChecked;
    public String pattern;

    public RosieRule(GetRulesetsForClientQuery.Rule rule) {
        this.id = rule.id().toString();
        this.contentBase64 = rule.content();
        this.language = rule.language().rawValue();
        this.type = rule.ruleType().rawValue();
        this.entityChecked = rule.elementChecked() == null ? null : rule.elementChecked().rawValue();
        this.pattern = rule.pattern();
    }
}
