package io.codiga.plugins.jetbrains.rosie.model.codiga;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a codiga.yml configuration file.
 */
@Data
public class CodigaYmlConfig {
    public static final CodigaYmlConfig EMPTY = new CodigaYmlConfig();

    List<String> rulesets;
    /**
     * Stores [ruleset name -> ruleset ignore configuration] mappings.
     * <p>
     * Using a map instead of a {@code List<RulesetIgnore>}, so that we can query the ruleset
     * configs by name, without having to filter the list by the ruleset name.
     */
    Map<String, RulesetIgnore> ignore;

    @VisibleForTesting
    public CodigaYmlConfig() {
        this.rulesets = Collections.emptyList();
        this.ignore = Collections.emptyMap();
    }

    /**
     * Sets the {@link #rulesets} field after filtering out null and blank ruleset names.
     *
     * @param rulesets the list of ruleset names that is deserialized automatically by Jackson
     */
    @JsonSetter("rulesets")
    public void setRulesets(List<String> rulesets) {
        this.rulesets = rulesets == null
            ? Collections.emptyList()
            : rulesets.stream()
            .filter(Objects::nonNull)
            .filter(ruleset -> !ruleset.isBlank())
            .collect(toList());
    }

    /**
     * Converts the argument ignore configuration to a map, and sets the {@link #ignore} field.
     *
     * @param rulesetIgnores the list of {@link RulesetIgnore}s that is deserialized automatically by Jackson
     */
    @JsonSetter("ignore")
    public void setIgnore(List<RulesetIgnore> rulesetIgnores) {
        this.ignore = rulesetIgnores == null
            ? Collections.emptyMap()
            : rulesetIgnores
            .stream()
            .filter(Objects::nonNull)
            .collect(toMap(RulesetIgnore::getRulesetName, rulesetIgnore -> rulesetIgnore));
    }

    @Nullable
    public RulesetIgnore getIgnore(String rulesetName) {
        return ignore.get(rulesetName);
    }
}
