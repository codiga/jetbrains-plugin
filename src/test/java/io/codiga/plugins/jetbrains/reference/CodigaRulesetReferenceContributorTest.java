package io.codiga.plugins.jetbrains.reference;

import com.intellij.openapi.paths.WebReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import io.codiga.plugins.jetbrains.testutils.TestBase;

/**
 * Integration test for {@link CodigaRulesetReferenceContributor}.
 */
public class CodigaRulesetReferenceContributorTest extends TestBase {

    public void testAddsReferenceForValidRulesetName() {
        myFixture.configureByText("codiga.yml",
            "rulesets:\n" +
                "  - valid-ruleset\n" +
                "  - oth<caret>er-valid-ruleset");

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
        PsiReference[] references = element.getReferences();

        assertSize(1, references);
        assertTrue(references[0] instanceof WebReference);
        assertEquals(((WebReference) references[0]).getUrl(), "https://app.codiga.io/hub/ruleset/other-valid-ruleset");
    }

    public void testDoesntAddReferenceForInvalidRulesetName() {
        myFixture.configureByText("codiga.yml",
            "rulesets:\n" +
                "  - valid-ruleset\n" +
                "  - INval<caret>id-rule!!set");

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
        PsiReference[] references = element.getReferences();

        assertEmpty(references);
    }

    public void testDoesntAddReferenceForIgnorePrefixPath() {
        myFixture.configureByText("codiga.yml",
            "rulesets:\n" +
                "  - valid-ruleset\n" +
                "ignore:" +
                "  - valid-ruleset:" +
                "    - rule:" +
                "      - prefix:" +
                "        - some<caret>path");

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
        PsiReference[] references = element.getReferences();

        assertEmpty(references);
    }
}
