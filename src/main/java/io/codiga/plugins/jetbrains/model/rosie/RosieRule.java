package io.codiga.plugins.jetbrains.model.rosie;

import io.codiga.api.GetRulesetsForClientQuery;
import lombok.ToString;

/**
 * The rule descriptor converted from {@link GetRulesetsForClientQuery.Rule}, and sent to Rosie.
 */
@ToString
public class RosieRule {
    public String id;
    public String contentBase64;
    public String language;
    public String type;
    public String entityChecked;
    public String pattern;

    public RosieRule(String rulesetName, GetRulesetsForClientQuery.Rule rule) {
        this.id = rulesetName + "/" + rule.name();
        this.contentBase64 = rule.content();
        this.language = rule.language().rawValue();
        this.type = rule.ruleType().rawValue();
        this.entityChecked = RosieRuleAstTypes.elementCheckedToRosieEntityChecked(rule.elementChecked());
        this.pattern = rule.pattern();
    }
}
