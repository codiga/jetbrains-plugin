package io.codiga.plugins.jetbrains.rosie.model.codiga;

import static java.util.stream.Collectors.toList;

import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents a rule ignore configuration element in the codiga.yml file.
 * <p>
 * This is the element right under a ruleset name property, e.g.:
 * <pre>
 *       - rule1:
 *           - prefix: /path/to/file/to/ignore
 * </pre>
 * or
 * <pre>
 *       - rule2:
 *           - prefix:
 *               - /path1
 *               - /path2
 * </pre>
 */
@Value
public class RuleIgnore {
    String ruleName;
    /**
     * The list of prefix values under the {@code prefix} property.
     * <p>
     * In case multiple {@code prefix} properties are defined under the same rule config,
     * they are all added to this list.
     * <p>
     * For example, in case of:
     * <pre>
     * ignore:
     *   - my-python-ruleset:
     *     - rule1:
     *       - prefix:
     *         - /path1
     *         - /path2
     *       - prefix: /path3
     * </pre>
     * all of {@code /path1}, {@code /path2} and {@code /path3} are stored here.
     * <p>
     * In case a {@code prefix} property contains the same value multiple times,
     * they are deduplicated and only once instance is stored, for example:
     * <pre>
     * ignore:
     *   - my-python-ruleset:
     *     - rule1:
     *       - prefix:
     *         - /path1
     *         - /path1
     * </pre>
     */
    List<String> prefixes;

    public RuleIgnore(String ruleName, Object prefixIgnore) {
        this.ruleName = ruleName;

        if (prefixIgnore instanceof List) {
            prefixes = ((List<?>) prefixIgnore).stream()
                .filter(Map.class::isInstance)
                .flatMap(prefix -> ((Map<?, ?>) prefix)
                    .values()
                    .stream()
                    .flatMap(prefixValue -> {
                        /*
                            A 'prefix' property can have a single String value:
                                - prefix: /path/to/file/to/ignore
                         */
                        if (prefixValue instanceof String)
                            return Stream.of((String) prefixValue);

                        /*
                            A 'prefix' property can also have multiple String values as a list:
                                - prefix:
                                  - /path1
                                  - /path2
                         */
                        if (prefixValue instanceof List)
                            //It filters out null and non-String prefix values
                            return ((List<?>) prefixValue).stream()
                                .filter(String.class::isInstance)
                                .map(String.class::cast);

                        return Stream.empty();
                    })
                ).distinct()
                .collect(toList());
            return;
        }

        prefixes = Collections.emptyList();
    }
}
