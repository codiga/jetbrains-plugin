package io.codiga.plugins.jetbrains.model.rosie;

import io.codiga.api.type.ElementCheckedEnumeration;

/**
 * Provides values and mapping logic for rule AST types.
 */
final class RosieRuleAstTypes {
    private final static String ENTITY_CHECKED_FUNCTION_CALL = "functioncall";
    private final static String ENTITY_CHECKED_IF_CONDITION = "ifcondition";
    private final static String ENTITY_CHECKED_IMPORT = "import";
    private final static String ENTITY_CHECKED_ASSIGNMENT = "assign";
    private final static String ENTITY_CHECKED_FOR_LOOP = "forloop";
    private final static String ENTITY_CHECKED_FUNCTION_DEFINITION = "functiondefinition";
    private final static String ENTITY_CHECKED_TRY_BLOCK = "tryblock";
    private final static String ENTITY_CHECKED_TYPE = "type";
    private final static String ENTITY_CHECKED_INTERFACE = "interface";
    private final static String ENTITY_CHECKED_HTML_ELEMENT = "htmlelement";
    private final static String ENTITY_CHECKED_CLASS_DEFINITION = "classdefinition";
    private final static String ENTITY_CHECKED_FUNCTION_EXPRESSION = "functionexpression";

    /**
     * Maps the argument element checked to its Rosie counterpart value.
     */
    static String elementCheckedToRosieEntityChecked(ElementCheckedEnumeration elementChecked) {
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
            case TYPE:
                return ENTITY_CHECKED_TYPE;
            case INTERFACE:
                return ENTITY_CHECKED_INTERFACE;
            case HTMLELEMENT:
                return ENTITY_CHECKED_HTML_ELEMENT;
            case CLASSDEFINITION:
                return ENTITY_CHECKED_CLASS_DEFINITION;
            case FUNCTIONEXPRESSION:
                return ENTITY_CHECKED_FUNCTION_EXPRESSION;
            default:
                return null;
        }
    }

    private RosieRuleAstTypes() {
        //Utility class
    }
}
