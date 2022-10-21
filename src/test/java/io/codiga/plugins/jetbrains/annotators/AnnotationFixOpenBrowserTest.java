package io.codiga.plugins.jetbrains.annotators;

import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotation;
import io.codiga.plugins.jetbrains.model.rosie.RosieAnnotationJetBrains;
import io.codiga.plugins.jetbrains.model.rosie.RosiePosition;
import io.codiga.plugins.jetbrains.model.rosie.RosieViolation;
import io.codiga.plugins.jetbrains.testutils.TestBase;

import java.util.Collections;

/**
 * Integration test for {@link AnnotationFixOpenBrowser}.
 */
public class AnnotationFixOpenBrowserTest extends TestBase {

    public void testReturnsFormattedRulePageURL() {
        myFixture.configureByText("open_browser_fix.py",
            "class Person:\n" +
                "  def __init__(self, name):\n" +
                "    self.name = name\n");
        var rosieViolation = new RosieViolation(
            "open_browser_fix",
            new RosiePosition(1, 5),
            new RosiePosition(1, 10),
            "INFORMATIONAL",
            "CODE_STYLE",
            Collections.emptyList());

        var rosieAnnotation = new RosieAnnotation("rule-for-open-browser", "custom-ruleset-name", rosieViolation);
        var rosieAnnotationJetBrains = new RosieAnnotationJetBrains(rosieAnnotation, myFixture.getEditor());

        String urlString = new AnnotationFixOpenBrowser(rosieAnnotationJetBrains).getUrlString();

        assertEquals("https://app.codiga.io/hub/ruleset/custom-ruleset-name/rule-for-open-browser", urlString);
    }
}
