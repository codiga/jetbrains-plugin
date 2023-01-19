package io.codiga.plugins.jetbrains.rosie.model.codiga;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents a ruleset ignore configuration element in the codiga.yml file.
 * <p>
 * This is the element right under the root-level {@code ignore} property, e.g.:
 * <pre>
 *   - my-python-ruleset:
 *       - rule1:
 *           - prefix: /path/to/file/to/ignore
 * </pre>
 */
@Data
public class RulesetIgnore {
    String rulesetName;
    /**
     * Stores [rule name -> rule ignore configuration] mappings.
     * <p>
     * Using a map instead of a {@code List<RuleIgnore>}, so that we can query the ruleset
     * configs by name, without having to filter the list by the ruleset name.
     */
    Map<String, RuleIgnore> ruleIgnores;

    /**
     * Saves the ruleset name and the rule ignore configuration from its value.
     * <p>
     * The {@link JsonAnySetter} annotation is used because ruleset name properties don't have fix
     * property names to map them to a class field during deserialization. It needs a custom logic to store
     * save their values.
     *
     * @param rulesetName the ruleset name
     * @param value       the value associated to the ruleset name property in codiga.yml
     */
    @JsonAnySetter
    public void set(String rulesetName, Object value) {
        this.rulesetName = rulesetName;

        if (value instanceof List) {
            this.ruleIgnores = ((List<?>) value).stream()
                .flatMap(ruleIgnore -> {
                    /*
                        A rule ignore config can be a single rule name without any prefix value:
                            - rulename
                     */
                    if (ruleIgnore instanceof String)
                        return Stream.of(new RuleIgnore((String) ruleIgnore, null));

                    /*
                        A rule ignore config can be a Map of the rule name and its object value,
                        with one or more prefix values:
                            - rulename:
                              - prefix: /path/to/file/to/ignore
                            as a {[rulename -> prefix: /path/to/file/to/ignore]} map

                            - rulename2:
                              - prefix:
                                - /path1
                                - /path2
                            as a {[rulename2 -> prefix: /path1, /path2]} map
                     */
                    if (ruleIgnore instanceof Map)
                        return ((Map<?, ?>) ruleIgnore)
                            .entrySet()
                            .stream()
                            .map(rule -> new RuleIgnore(rule.getKey().toString(), rule.getValue()));

                    return Stream.empty();
                })
                .collect(toMap(RuleIgnore::getRuleName, ruleIgnore -> ruleIgnore));
            return;
        }

        ruleIgnores = Collections.emptyMap();
    }

    @Nullable
    public RuleIgnore getRuleIgnore(String ruleName) {
        return ruleIgnores.get(ruleName);
    }
}
