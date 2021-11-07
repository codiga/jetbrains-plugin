package io.codiga.plugins.jetbrains.graphql;

import io.codiga.api.*;
import io.codiga.api.type.LanguageEnumeration;
import com.google.common.collect.ImmutableList;
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
public final class CodigaApiTest implements CodigaApi {

    public boolean isWorking() {
        return true;
    }


    /**
     * Get the username of the identified user
     *
     * @return the username of the logged in user if the API works correctly (and API keys are correct).
     */
    public Optional<String> getUsername() {
        return Optional.empty();
    }

    /**
     * Get the list of projects the user has access to.
     *
     * @return
     */
    public List<GetProjectsQuery.Project> getProjects() {
        return ImmutableList.of();
    }

    @Override
    public List<GetRecipesForClientQuery.GetRecipesForClient> getRecipesForClient(List<String> keywords, List<String> dependencies, Optional<String> parameters, LanguageEnumeration language, String filename) {
        return null;
    }

    public Optional<GetFileDataQuery.Project> getDataForFile(Long projectId, String revision, String path) {
        return Optional.empty();
    }

    /**
     * Add a violation to ignore through the API.
     * @param projectId - the project identifier on Codiga
     * @param rule - the rule to ignore
     * @param tool - the tool used to detect this issue
     * @param language - the language of the violation
     * @param filename - the filename. If this is a prefix-based or project-based ignore, that should be empty.
     * @param prefix - the prefix. If this is a file-based or project-based, that should be empty.
     * @param description - the description of the violation to ignore.
     * @return the results of the API call.
     */
    public Optional<AddViolationToIgnoreMutation.AddViolationToIgnore> addViolationToIgnore(
        @NotNull Long projectId, @NotNull String rule, @NotNull String tool,
        @NotNull LanguageEnumeration language, @NotNull Optional<String> filename, @NotNull Optional<String> prefix,
        @NotNull String description) {
        return Optional.empty();
    }

    /**
     * Remove an ignored violation from the API.
     * @param projectId - the project identifier on Codiga
     * @param rule - the rule to ignore
     * @param tool - the tool used to detect this issue
     * @param language - the language of the violation
     * @param filename - the filename. If this is a prefix-based or project-based ignore, that should be empty.
     * @param prefix - the prefix. If this is a file-based or project-based, that should be empty.
     * @return - a string (which is not really relevant)
     */
    public Optional<String> removeViolationToIgnore(Long projectId, String rule, String tool, LanguageEnumeration language, Optional<String> filename, Optional<String> prefix) {
        return Optional.empty();
    }

    @Override
    public Optional<GetFileAnalysisQuery.GetFileAnalysis> getFileAnalysis(String filename, String code, LanguageEnumeration language, Optional<Long> projectId, Optional<String> parameters) {
        return Optional.empty();
    }

    @Override
    public void recordRecipeUse(Long recipeId) {

    }
}
