package io.codiga.plugins.jetbrains.inspection;

import static io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil.isRulesetNameValid;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCache;
import io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

/**
 * Validates rulesets configured in the Codiga config file, if they exist and non-empty.
 * <p>
 * The data against which the ruleset names are validated is coming from {@link RosieRulesCache}.
 * <p>
 * These checks are implemented as an inspection for the following reasons:
 * <ul>
 *     <li>it provides automatic analysis of the config file,</li>
 *     <li>using this automatic analysis based on the current state of the cache, instead of an on-demand action,
 *     it prevents users from potentially spamming the Codiga server with requests.</li>
 * </ul>
 * <p>
 * Each time the {@link RosieRulesCache} is updated by {@link io.codiga.plugins.jetbrains.starter.RosieRulesCacheUpdateHandler},
 * the Codiga config file is re-analyzed to reflect the state of the cache.
 * <p>
 * If all (or many) rulesets are marked as non-existent by this inspection, that may be a sign that
 * the Codiga backend experiences issues.
 */
public class CodigaRulesetContentInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        if (!CodigaConfigFileUtil.CODIGA_CONFIG_FILE_NAME.equals(holder.getFile().getName())) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        return new YamlPsiElementVisitor() {
            @Override
            public void visitScalar(@NotNull YAMLScalar scalar) {
                if (scalar instanceof YAMLPlainTextImpl && scalar.getParent() instanceof YAMLSequenceItem) {
                    String rulesetName = scalar.getText();
                    var cache = RosieRulesCache.getInstance(holder.getProject());

                    //Since ruleset names are already validated by the associated JSON schema,
                    // there is no need to mark them as non-existent too.
                    if (isRulesetNameValid(rulesetName)) {
                        if (!cache.isRulesetExist(rulesetName)) {
                            holder.registerProblem(scalar,
                                "This ruleset does not exist, or you do not have access to it.",
                                ProblemHighlightType.GENERIC_ERROR);
                        } else if (cache.isRulesetEmpty(rulesetName)) {
                            holder.registerProblem(scalar,
                                "This ruleset has no rule.",
                                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                        }
                    }
                }
                super.visitScalar(scalar);
            }
        };
    }
}
