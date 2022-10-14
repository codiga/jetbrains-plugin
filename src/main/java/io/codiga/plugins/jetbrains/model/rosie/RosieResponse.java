package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;

/**
 * The Rosie response object returned by the Codiga API.
 */
public class RosieResponse {
    public List<RosieRuleResponse> ruleResponses;
    public List<String> errors;

    public RosieResponse(List<RosieRuleResponse> ruleResponses) {
        this.ruleResponses = ruleResponses;
    }
}
