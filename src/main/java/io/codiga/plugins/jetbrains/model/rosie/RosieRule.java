package io.codiga.plugins.jetbrains.model.rosie;

import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.api.type.ElementCheckedEnumeration;
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

    public final static String ENTITY_CHECKED_FUNCTION_CALL = "functioncall";
    public final static String ENTITY_CHECKED_IF_CONDITION = "ifcondition";
    public final static String ENTITY_CHECKED_IMPORT = "import";
    public final static String ENTITY_CHECKED_ASSIGNMENT = "assign";
    public final static String ENTITY_CHECKED_FOR_LOOP = "forloop";
    public final static String ENTITY_CHECKED_FUNCTION_DEFINITION = "functiondefinition";
    public final static String ENTITY_CHECKED_TRY_BLOCK = "tryblock";

    private String elementCheckedToRosieEntityChecked(ElementCheckedEnumeration elementChecked) {
        if (elementChecked == null) {
            return null;
        }
        switch (elementChecked) {
            case FORLOOP:
                return ENTITY_CHECKED_FOR_LOOP;
            case ASSIGNMENT:
                return ENTITY_CHECKED_ASSIGNMENT;
            case FUNCTIONDEFINITION:
                return ENTITY_CHECKED_FUNCTION_DEFINITION;
            case TRYBLOCK:
                return ENTITY_CHECKED_TRY_BLOCK;
            case IMPORT:
                return ENTITY_CHECKED_IMPORT;
            case IFCONDITION:
                return ENTITY_CHECKED_IF_CONDITION;
            case FUNCTIONCALL:
                return ENTITY_CHECKED_FUNCTION_CALL;
            default:
                return null;
        }
    }

    public RosieRule(String rulesetName, GetRulesetsForClientQuery.Rule rule) {
        this.id = rulesetName + "/" + rule.name();
        this.contentBase64 = rule.content();
        this.language = rule.language().rawValue();
        this.type = rule.ruleType().rawValue();
        this.entityChecked = elementCheckedToRosieEntityChecked(rule.elementChecked());
        this.pattern = rule.pattern();
    }
}
