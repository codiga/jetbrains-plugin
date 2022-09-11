package io.codiga.plugins.jetbrains.model.rosie;

import java.util.List;

public class RosieRuleResponse {
    public String identifier;
    public List<RosieViolation> violations;
    public List<String> errors;
    public String executionError;
    public String output;

    public RosieRuleResponse() {
        this.identifier = null;
        this.violations = null;
        this.errors = null;
        this.executionError = null;
        this.output = null;
    }

    public RosieRuleResponse(String identifier, List<RosieViolation> violations, List<String> errors, String executionError, String output) {
        this.identifier = identifier;
        this.violations = violations;
        this.errors = errors;
        this.executionError = executionError;
        this.output = output;
    }

    @Override
    public String toString() {
        return "RuleResponse{" +
            "identifier='" + identifier + '\'' +
            ", violations=" + violations +
            ", errors=" + errors +
            ", executionError='" + executionError + '\'' +
            ", output='" + output + '\'' +
            '}';
    }
}
