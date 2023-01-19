package io.codiga.plugins.jetbrains.reference;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PlatformPatterns.psiFile;
import static io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil.isRulesetNameValid;

import com.intellij.openapi.paths.WebReference;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import io.codiga.plugins.jetbrains.rosie.CodigaConfigFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

/**
 * Provides a {@link WebReference} for all valid ruleset names in the Codiga config file,
 * so that users can navigate to the rulesets on Codiga Hub via a simple Ctrl+click.
 */
public class CodigaRulesetReferenceContributor extends PsiReferenceContributor {

    private static final PsiElementPattern.Capture<YAMLPlainTextImpl> CODIGA_RULESET_NAME =
        psiElement(YAMLPlainTextImpl.class)
            .withParent(psiElement(YAMLSequenceItem.class)
                .withSuperParent(2, psiElement(YAMLKeyValue.class)
                    .with(new PatternCondition<>("") {
                        @Override
                        public boolean accepts(@NotNull YAMLKeyValue element, ProcessingContext context) {
                            return "rulesets".equals(element.getKeyText());
                        }
                    })))
            .inFile(psiFile(YAMLFile.class)
                .withName(CodigaConfigFileUtil.CODIGA_CONFIG_FILE_NAME));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            CODIGA_RULESET_NAME,
            new PsiReferenceProvider() {
                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    return isRulesetNameValid(element.getText())
                        ? new PsiReference[]{new WebReference(element, "https://app.codiga.io/hub/ruleset/" + element.getText())}
                        : PsiReference.EMPTY_ARRAY;
                }
            });
    }
}
