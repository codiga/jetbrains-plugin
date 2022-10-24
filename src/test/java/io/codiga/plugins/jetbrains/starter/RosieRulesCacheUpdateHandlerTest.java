package io.codiga.plugins.jetbrains.starter;

import static io.codiga.plugins.jetbrains.services.CodigaConfigFileUtil.collectRulesetNames;
import static io.codiga.plugins.jetbrains.services.CodigaConfigFileUtil.findCodigaConfigFile;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.WaitFor;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.annotators.RosieRulesCache;
import io.codiga.plugins.jetbrains.graphql.CodigaApi;
import io.codiga.plugins.jetbrains.graphql.RulesetsForClientTestSupport;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Integration test for {@link RosieRulesCacheUpdateHandler}.
 */
public class RosieRulesCacheUpdateHandlerTest extends TestBase {

    @Override
    protected String getTestDataRelativePath() {
        return TEST_DATA_BASE_PATH + "/rosiecacheupdater";
    }

    /**
     * This case, that there is something in the cache before there was any codiga.yml file in the first place,
     * isn't supposed to happen, but decided to still leave it here.
     */
    public void testClearsCacheIfThereWasNeverACodigaConfigFile() {
        //Don't add codiga.yml, just manually set something in the cache
        RosieRulesCache rulesCache = RosieRulesCache.getInstance(getProject());
        rulesCache.updateCacheFrom(RulesetsForClientTestSupport.singleRulesetSingleLanguage());
        rulesCache.setLastUpdatedTimeStamp(101L);

        assertSize(3, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));

        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        validateThatCacheIsEmpty(rulesCache);
    }

    public void testClearsCacheIfCodigaConfigFileIsDeleted() {
        myFixture.copyFileToProject("codiga.yml");

        var codigaConfigFile = findCodigaConfigFile(getProject());
        RosieRulesCache rulesCache = initializeCache(collectRulesetNames(codigaConfigFile));

        assertSize(3, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));

        //Delete the config file
        WriteAction.runAndWait(() -> CommandProcessor.getInstance()
            .executeCommand(getProject(), codigaConfigFile::delete, "Delete", "group.id"));
        //Waits for the deletion to actually take effect
        new WaitFor(2000, 2000) {
            @Override
            protected boolean condition() {
                return !codigaConfigFile.getVirtualFile().exists();
            }
        };
        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        validateThatCacheIsEmpty(rulesCache);
    }

    public void testClearsCacheIfTheCodigaConfigFileIsModifiedButItIsEmpty() {
        RosieRulesCache rulesCache = initializeCacheFromCodigaConfigFile();

        assertSize(3, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));

        replaceContentsOfCodigaConfigFileWith("");
        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        validateThatCacheIsEmpty(rulesCache);
    }

    public void testDoesntUpdateCacheIfTheCodigaConfigFileIsModifiedButNoRulesetsForClientIsReceivedDueToError() {
        RosieRulesCache rulesCache = initializeCacheFromCodigaConfigFile();
        long originalModificationStamp = rulesCache.getConfigFileModificationStamp();

        replaceContentsOfCodigaConfigFileWith("rulesets:\n  - erroredRuleset");

        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        assertFalse(rulesCache.isEmpty());
        assertEquals(100L, rulesCache.getLastUpdatedTimeStamp());
        //Asserting not equals, in case the new modification stamp would not be consistently the same value.
        assertNotEquals(rulesCache.getConfigFileModificationStamp(), originalModificationStamp);
    }

    public void testClearsCachedRulesIfTheCodigaConfigFileIsModifiedButEmptyListOfRulesetsForClientIsReceived() {
        RosieRulesCache rulesCache = initializeCacheFromCodigaConfigFile();

        replaceContentsOfCodigaConfigFileWith("rulesets:\n  - non-existent-ruleset");
        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        assertTrue(rulesCache.isEmpty());
        assertEquals(-1, rulesCache.getLastUpdatedTimeStamp());
    }

    public void testUpdatesCacheIfTheCodigaConfigFileIsModifiedAndDoesntUpdateUnchangedTimestamp() {
        RosieRulesCache rulesCache = initializeCacheFromCodigaConfigFile();

        replaceContentsOfCodigaConfigFileWith("rulesets:\n  - singleRulesetMultipleLanguagesDefaultTimestamp");
        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        assertSize(2, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));
        assertEquals(100, rulesCache.getLastUpdatedTimeStamp());
    }

    public void testUpdatesCacheIfTheCodigaConfigFileIsModifiedAndUpdatesChangedTimestamp() {
        RosieRulesCache rulesCache = initializeCacheFromCodigaConfigFile();

        replaceContentsOfCodigaConfigFileWith("rulesets:\n  - singleRulesetMultipleLanguages");
        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        assertSize(2, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));
        assertEquals(102, rulesCache.getLastUpdatedTimeStamp());
    }

    //Changes on the server

    public void testDoesntUpdateCacheIfSameTimestampIsReceivedFromServer() {
        myFixture.copyFileToProject("codiga.yml");

        RosieRulesCache rulesCache = RosieRulesCache.getInstance(getProject());
        rulesCache.updateCacheFrom(RulesetsForClientTestSupport.singleRulesetMultipleLanguages());
        rulesCache.setLastUpdatedTimeStamp(101L);

        assertSize(2, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));
        assertEquals(101, rulesCache.getLastUpdatedTimeStamp());

        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        assertSize(2, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));
        assertEquals(101, rulesCache.getLastUpdatedTimeStamp());
    }

    public void testUpdatesCacheIfDifferentLatestTimestampIsReceived() {
        myFixture.copyFileToProject("codiga.yml");

        RosieRulesCache rulesCache = RosieRulesCache.getInstance(getProject());
        rulesCache.updateCacheFrom(RulesetsForClientTestSupport.singleRulesetMultipleLanguages());
        rulesCache.setLastUpdatedTimeStamp(102L);

        assertSize(2, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));
        assertEquals(102, rulesCache.getLastUpdatedTimeStamp());

        new RosieRulesCacheUpdateHandler(rulesCache, getProject()).handleCacheUpdate();

        assertSize(3, rulesCache.getRosieRulesForLanguage(LanguageEnumeration.PYTHON));
        assertEquals(101, rulesCache.getLastUpdatedTimeStamp());
    }

    //Helpers

    @NotNull
    private RosieRulesCache initializeCacheFromCodigaConfigFile() {
        myFixture.copyFileToProject("codiga.yml");

        var codigaConfigFile = findCodigaConfigFile(getProject());
        var rulesetNames = collectRulesetNames(codigaConfigFile);
        return initializeCache(rulesetNames);
    }

    /**
     * Initializes the cache with based on the provided test ruleset names.
     */
    @NotNull
    private RosieRulesCache initializeCache(List<String> rulesetNames) {
        var rulesetsFromCodigaAPI = CodigaApi.getInstance().getRulesetsForClient(rulesetNames).get();
        var rulesCache = RosieRulesCache.getInstance(getProject());

        rulesCache.updateCacheFrom(rulesetsFromCodigaAPI);
        rulesCache.setLastUpdatedTimeStamp(100L);
        rulesCache.setConfigFileModificationStamp(1);
        return rulesCache;
    }

    /**
     * Replace the whole content of the Codiga config file, esentially doing a modification on the file.
     */
    private void replaceContentsOfCodigaConfigFileWith(String replacement) {
        myFixture.configureByFile("codiga.yml");

        WriteAction.runAndWait(() -> CommandProcessor.getInstance()
            .executeCommand(getProject(),
                () -> {
                    Document document = myFixture.getEditor().getDocument();
                    document.replaceString(0, document.getTextLength(), replacement);
                    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
                },
                "Modify File Content", "group.id"));
    }

    protected static void validateThatCacheIsEmpty(RosieRulesCache rulesCache) {
        assertTrue(rulesCache.isEmpty());
        assertEquals(-1, rulesCache.getLastUpdatedTimeStamp());
    }
}
