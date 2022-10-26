package io.codiga.plugins.jetbrains.model.rosie;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RosieRuleResponse {
    public String identifier;
    public List<RosieViolation> violations;
    public List<String> errors;
    public String executionError;
    public String output;
}
