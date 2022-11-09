package io.codiga.plugins.jetbrains.inspection;

import static io.codiga.plugins.jetbrains.graphql.RulesetsForClientTestSupport.getRulesetsForClient;

import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCache;
import io.codiga.plugins.jetbrains.testutils.TestBase;

import java.util.List;

/**
 * Integration test for {@link CodigaRulesetContentInspection}.
 */
public class CodigaRulesetContentInspectionTest extends TestBase {

    public void testNoHighlightingWhenCacheIsNotInitialized() {
        doTest("rulesets:\n" +
            "  - non-existent\n" +
            "  - python-ruleset\n" +
            "  - InvaliD_ruleset-na!me");
    }

    public void testCodigaRulesetExistence() {
        var rulesets = getRulesetsForClient(List.of("multipleRulesetsMultipleLanguages", "non-existent")).get();
        RosieRulesCache.getInstance(getProject()).updateCacheFrom(rulesets);

        doTest("rulesets:\n" +
            "  - <error descr=\"This ruleset does not exist, or you do not have access to it.\">non-existent</error>\n" +
            "  - python-ruleset\n" +
            "  - InvaliD_ruleset-na!me");
    }

    public void testCodigaRulesetEmptiness() {
        var rulesets = List.of(
            new GetRulesetsForClientQuery.RuleSetsForClient("typename", 1234, "empty-ruleset", List.of()));

        RosieRulesCache.getInstance(getProject()).updateCacheFrom(rulesets);

        doTest("rulesets:\n" +
            "  - <warning descr=\"This ruleset has no rule.\">empty-ruleset</warning>");
    }

    private void doTest(String text) {
        myFixture.configureByText("codiga.yml", text);
        myFixture.enableInspections(new CodigaRulesetContentInspection());
        myFixture.testHighlighting(true, true, true);
    }
}
