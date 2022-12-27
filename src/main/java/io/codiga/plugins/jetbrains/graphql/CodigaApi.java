package io.codiga.plugins.jetbrains.graphql;

import com.intellij.openapi.application.ApplicationManager;
import io.codiga.api.GetRecipesForClientByShortcutQuery;
import io.codiga.api.GetRecipesForClientQuery;
import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.GetRulesetsForClientQuery;
import io.codiga.api.type.LanguageEnumeration;

import java.util.List;
import java.util.Optional;

/**
 * This class implements the Codiga API, which is a GraphQL API.
 * We are using apollo-android to query the API.
 * <p>
 * <a href="https://github.com/apollographql/apollo-android">Apollo Android client</a>.
 * <p>
 * This class is declared as a service to be retrieved as an application
 * service within the plugin. To retrieve it, just use {@code CodigaApi api = CodigaApi.getInstance();}.
 * <p>
 * See <a href="https://plugins.jetbrains.com/docs/intellij/plugin-services.html#declaring-a-service">Declaring a service</a>
 */
public interface CodigaApi {

    static CodigaApi getInstance() {
        return ApplicationManager.getApplication().getService(CodigaApi.class);
    }

    /**
     * Indicate of the API is working, which means we have a correct access and secret key.
     * To do that, we check that the API is returning a correct user when we issue a request.
     * If not defined, it means the API is not working, either because the keys are not
     * correct or any other transient problem.
     *
     * @return true if the API is working (mearning API keys are correctly configured).
     */
    boolean isWorking();


    /**
     * Get the username of the identified user
     *
     * @return the username of the logged in user if the API works correctly (and API keys are correct).
     */
    Optional<String> getUsername();

    //Recipes

    List<GetRecipesForClientQuery.GetRecipesForClient> getRecipesForClient(List<String> keywords,
                                                                                  List<String> dependencies,
                                                                                  Optional<String> parameters,
                                                                                  LanguageEnumeration language,
                                                                                  String filename);

    List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> getRecipesForClientByShotcurt(Optional<String> term,
                                                                                  List<String> dependencies,
                                                                                  Optional<String> parameters,
                                                                                  LanguageEnumeration language,
                                                                                  String filename,
                                                                                  Optional<Boolean> onlyPublic,
                                                                                  Optional<Boolean> onlyPrivate,
                                                                                  Optional<Boolean> onlySubscribed);

    Optional<Long> getRecipesForClientByShotcurtLastTimestmap(List<String> dependencies, LanguageEnumeration language);

    List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> getRecipesSemantic(Optional<String> term,
                                                                                                     List<String> dependencies,
                                                                                                     Optional<String> parameters,
                                                                                                     LanguageEnumeration language,
                                                                                                     String filename,
                                                                                                     Optional<Boolean> onlyPublic,
                                                                                                     Optional<Boolean> onlyPrivate,
                                                                                                     Optional<Boolean> onlySubscribed);

    void recordRecipeUse(Long recipeId);

    // Rulesets

    Optional<Long> getRulesetsLastTimestamp(List<String> ruleNames);

    /**
     * Retrieves the rulesets from the Codiga server for the provided ruleset names configured in {@code codiga.yml}.
     *
     * @param ruleNames the ruleset names
     * @return the list of rulesets, or empty optional if there was an error during data retrieval
     */
    Optional<List<GetRulesetsForClientQuery.RuleSetsForClient>> getRulesetsForClient(List<String> ruleNames);

    /**
     * Sends a request to Codiga that a rule fix quick fix was invoked by the user.
     */
    void recordRuleFix();

    /**
     * Sends a request to Codiga that the Codiga config file was created by the user with default rulesets,
     * via the notification popup shown in {@link io.codiga.plugins.jetbrains.starter.RosieStartupActivity}.
     */
    void recordCreateCodigaYaml();
}
