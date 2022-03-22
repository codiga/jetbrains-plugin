package io.codiga.plugins.jetbrains.graphql;

import com.intellij.openapi.components.Service;
import io.codiga.api.*;
import io.codiga.api.type.LanguageEnumeration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * This class implements the Codiga API, which is a GraphQL API.
 * We are using apollo-android to query the API.
 * <p>
 * Apollo Android client: https://github.com/apollographql/apollo-android
 * <p>
 * This class is declared as a service to be retrieved as an application
 * service within the plugin. To retrieve it, just to
 * CodigaApi api = ApplicationManager.getApplication().getService(CodigaApi.class);
 * <p>
 * See https://plugins.jetbrains.com/docs/intellij/plugin-services.html#declaring-a-service
 */
public interface CodigaApi {

    /**
     * Indicate of the API is working, which means we have a correct access and secret key.
     * To do that, we check that the API is returning a correct user when we issue a request.
     * If not defined, it means the API is not working, either because the keys are not
     * correct or any other transient problem.
     *
     * @return true if the API is working (mearning API keys are correctly configured).
     */
    public boolean isWorking();


    /**
     * Get the username of the identified user
     *
     * @return the username of the logged in user if the API works correctly (and API keys are correct).
     */
    public Optional<String> getUsername();

    /**
     * Get the list of projects the user has access to.
     *
     * @return
     */
    public List<GetProjectsQuery.Project> getProjects();

    public List<GetRecipesForClientQuery.GetRecipesForClient> getRecipesForClient(List<String> keywords,
                                                                                  List<String> dependencies,
                                                                                  Optional<String> parameters,
                                                                                  LanguageEnumeration language,
                                                                                  String filename);

    public List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> getRecipesForClientByShotcurt(Optional<String> term,
                                                                                  List<String> dependencies,
                                                                                  Optional<String> parameters,
                                                                                  LanguageEnumeration language,
                                                                                  String filename);

    public Optional<Long> getRecipesForClientByShotcurtLastTimestmap(List<String> dependencies, LanguageEnumeration language);

    public List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> getRecipesSemantic(Optional<String> term,
                                                                                                     List<String> dependencies,
                                                                                                     Optional<String> parameters,
                                                                                                     LanguageEnumeration language,
                                                                                                     String filename);

    /**
     * Get all the data from the API for a specific file
     *
     * @param projectId - the project identifier on Codiga (got from the preferences)
     * @param revision  - the revision of the repository (usually the latest version)
     * @param path      - the path of the file we are looking for.
     * @return - all the data we need to surface to the project. Data comes from the GraphQL generated data.
     */
    public Optional<GetFileDataQuery.Project> getDataForFile(Long projectId, String revision, String path) throws GraphQlQueryException;

    /**
     * Add a violation to ignore through the API.
     *
     * @param projectId   - the project identifier on Codiga
     * @param rule        - the rule to ignore
     * @param tool        - the tool used to detect this issue
     * @param language    - the language of the violation
     * @param filename    - the filename. If this is a prefix-based or project-based ignore, that should be empty.
     * @param prefix      - the prefix. If this is a file-based or project-based, that should be empty.
     * @param description - the description of the violation to ignore.
     * @return the results of the API call.
     */
    public Optional<AddViolationToIgnoreMutation.AddViolationToIgnore> addViolationToIgnore(
            @NotNull Long projectId, @NotNull String rule, @NotNull String tool,
            @NotNull LanguageEnumeration language, @NotNull Optional<String> filename, @NotNull Optional<String> prefix,
            @NotNull String description);

    /**
     * Remove an ignored violation from the API.
     *
     * @param projectId - the project identifier on Codiga
     * @param rule      - the rule to ignore
     * @param tool      - the tool used to detect this issue
     * @param language  - the language of the violation
     * @param filename  - the filename. If this is a prefix-based or project-based ignore, that should be empty.
     * @param prefix    - the prefix. If this is a file-based or project-based, that should be empty.
     * @return - a string (which is not really relevant)
     */
    public Optional<String> removeViolationToIgnore(Long projectId, String rule, String tool, LanguageEnumeration language, Optional<String> filename, Optional<String> prefix);

    /**
     * Analyze a file using the real-time feedback and return the result of the query
     * @param filename - the filename to analyze
     * @param code - the code we want to analyze
     * @param language - the language (using the GraphQL enumeration)
     * @param projectId - the optional project identifier.
     * @return - the list of potential issues.
     */
    public Optional<GetFileAnalysisQuery.GetFileAnalysis> getFileAnalysis(String filename, String code, LanguageEnumeration language, Optional<Long> projectId, Optional<String> parameters) throws GraphQlQueryException;

    public void recordRecipeUse(Long recipeId);
}
