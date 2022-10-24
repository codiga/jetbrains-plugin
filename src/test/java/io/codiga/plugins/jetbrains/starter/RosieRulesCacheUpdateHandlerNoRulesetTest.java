package io.codiga.plugins.jetbrains.starter;

import io.codiga.plugins.jetbrains.annotators.RosieRulesCache;
import io.codiga.plugins.jetbrains.testutils.TestBase;

/**
 * Integration test for {@link RosieRulesCacheUpdateHandler}.
 */
public class RosieRulesCacheUpdateHandlerNoRulesetTest extends TestBase {

    @Override
    protected String getTestDataRelativePath() {
        return TEST_DATA_BASE_PATH + "/rosiecacheupdater/noruleset";
    }

    public void testDoesntUpdateCacheIfNoRulesetNameIsConfigured() {
        myFixture.copyFileToProject("codiga.yml");
        RosieRulesCache rulesCache = RosieRulesCache.getInstance(getProject());

        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        validateThatCacheIsEmpty(rulesCache);
    }

    protected static void validateThatCacheIsEmpty(RosieRulesCache rulesCache) {
        assertTrue(rulesCache.isEmpty());
        assertEquals(-1, rulesCache.getLastUpdatedTimeStamp());
    }
}
