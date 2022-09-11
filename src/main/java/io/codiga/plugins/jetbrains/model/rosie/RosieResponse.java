package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;

public class RosieResponse {
    public List<RosieRuleResponse> ruleResponses;
    public List<String> errors;

    public RosieResponse(List<RosieRuleResponse> ruleResponses) {
        this.ruleResponses = ruleResponses;
    }
}
