package com.code_inspector.plugins.intellij.graphql;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.code_inspector.api.*;
import com.code_inspector.api.type.LanguageEnumeration;
import com.code_inspector.plugins.intellij.settings.application.AppSettingsState;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.code_inspector.plugins.intellij.Constants.LOGGER_NAME;
import static com.code_inspector.plugins.intellij.graphql.Constants.*;

/**
 * This class implements the Code Inspector API, which is a GraphQL API.
 * We are using apollo-android to query the API.
 * <p>
 * Apollo Android client: https://github.com/apollographql/apollo-android
 * <p>
 * This class is declared as a service to be retrieved as an application
 * service within the plugin. To retrieve it, just to
 * CodeInspectorApi api = ServiceManager.getService(CodeInspectorApi.class);
 * <p>
 * See https://plugins.jetbrains.com/docs/intellij/plugin-services.html#declaring-a-service
 */
public final class CodeInspectorApiImpl implements CodeInspectorApi{

    private static final ApolloClient apolloClient = ApolloClient.builder()
        .serverUrl(ENDPOINT_URL)
        .build();

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);

    /**
     * Set the header with access/secret keys so that we do an authenticated
     * request. to the API.
     *
     * @return the headers to use to make an API request.
     */
    private RequestHeaders getHeaders() {
        AppSettingsState settings = AppSettingsState.getInstance();

        final String accessKey = settings == null || settings.getAccessKey() == null ? "" : settings.getAccessKey();
        final String secretKey = settings == null || settings.getSecretKey() == null ? "" : settings.getSecretKey();

        return RequestHeaders
            .builder()
            .addHeader(ACCESS_KEY_HEADER, accessKey)
            .addHeader(SECRET_KEY_HEADER, secretKey)
            .build();
    }

    /**
     * Indicate of the API is working, which means we have a correct access and secret key.
     * To do that, we check that the API is returning a correct user when we issue a request.
     * If not defined, it means the API is not working, either because the keys are not
     * correct or any other transient problem.
     *
     * @return true if the API is working (mearning API keys are correctly configured).
     */
    public boolean isWorking() {
        return this.getUsername().isPresent();
    }


    /**
     * Get the username of the identified user
     *
     * @return the username of the logged in user if the API works correctly (and API keys are correct).
     */
    public Optional<String> getUsername() {
        ApiRequest<GetUserQuery.User> apiRequest = new ApiRequest();

        ApolloQueryCall<GetUserQuery.Data> queryCall = apolloClient.query(new GetUserQuery())
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        queryCall.enqueue(
            new ApolloCall.Callback<GetUserQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<GetUserQuery.Data> response) {
                    if (response.getData().user() == null) {
                        apiRequest.setError();

                    } else {
                        apiRequest.setData(response.getData().user());
                    }

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    apiRequest.setError();
                }
            });

        return apiRequest.getData().map(GetUserQuery.User::username);
    }

    /**
     * Get the list of projects the user has access to.
     *
     * @return
     */
    public List<GetProjectsQuery.Project> getProjects() {
        ApiRequest<List<GetProjectsQuery.Project>> apiRequest = new ApiRequest();

        ApolloQueryCall<GetProjectsQuery.Data> queryCall = apolloClient.query(new GetProjectsQuery())
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        queryCall.enqueue(
            new ApolloCall.Callback<GetProjectsQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<GetProjectsQuery.Data> response) {
                    if (response.getData().projects().isEmpty()) {
                        apiRequest.setError();
                    } else {
                        apiRequest.setData(response.getData().projects());
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    apiRequest.setError();
                }
            });

        return apiRequest.getData().orElse(ImmutableList.of());
    }

    /**
     * Get all the data from the API for a specific file
     *
     * @param projectId - the project identifier on Code Inspector (got from the preferences)
     * @param revision - the revision of the repository (usually the latest version)
     * @param path - the path of the file we are looking for.
     *
     * @return - all the data we need to surface to the project. Data comes from the GraphQL generated data.
     */
    public Optional<GetFileDataQuery.Project> getDataForFile(Long projectId, String revision, String path) {
        ApiRequest<GetFileDataQuery.Project> apiRequest = new ApiRequest();

        LOGGER.debug(String.format("getting data for project %s, revision %s, path %s", projectId, revision, path));

        ApolloQueryCall<GetFileDataQuery.Data> queryCall = apolloClient.query(new GetFileDataQuery(projectId, revision, path))
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        queryCall.enqueue(
            new ApolloCall.Callback<GetFileDataQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<GetFileDataQuery.Data> response) {
                    if (response.getData() == null || response.getData().project() == null) {
                        apiRequest.setError();
                    } else {
                        apiRequest.setData(response.getData().project());

                    }

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    apiRequest.setError();
                }
            });

        return apiRequest.getData();
    }

    /**
     * Add a violation to ignore through the API.
     * @param projectId - the project identifier on Code Inspector
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
        ApiRequest<AddViolationToIgnoreMutation.AddViolationToIgnore> apiRequest = new ApiRequest();

        final Input<String> inputFilename = filename.map(Input::fromNullable).orElseGet(Input::absent);
        final Input<String> inputPrefix = prefix.map(Input::fromNullable).orElseGet(Input::absent);


        ApolloMutationCall<AddViolationToIgnoreMutation.Data> queryCall = apolloClient.mutate(new AddViolationToIgnoreMutation(projectId, rule, tool, language, inputFilename, inputPrefix, Input.fromNullable(description)))
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        queryCall.enqueue(
            new ApolloCall.Callback<AddViolationToIgnoreMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<AddViolationToIgnoreMutation.Data> response) {
                    if (response.getData() == null || response.getData().addViolationToIgnore() == null) {
                        apiRequest.setError();
                    } else {
                        apiRequest.setData(response.getData().addViolationToIgnore());
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LOGGER.debug("api call to ignore failure fails");
                    LOGGER.debug(e.getMessage());
                    e.printStackTrace();
                    apiRequest.setError();
                }
            });

        return apiRequest.getData();
    }

    /**
     * Remove an ignored violation from the API.
     * @param projectId - the project identifier on Code Inspector
     * @param rule - the rule to ignore
     * @param tool - the tool used to detect this issue
     * @param language - the language of the violation
     * @param filename - the filename. If this is a prefix-based or project-based ignore, that should be empty.
     * @param prefix - the prefix. If this is a file-based or project-based, that should be empty.
     * @return - a string (which is not really relevant)
     */
    public Optional<String> removeViolationToIgnore(Long projectId, String rule, String tool, LanguageEnumeration language, Optional<String> filename, Optional<String> prefix) {
        ApiRequest<String> apiRequest = new ApiRequest<String>();

        final Input<String> inputFilename = filename.map(Input::fromNullable).orElseGet(Input::absent);
        final Input<String> inputPrefix = prefix.map(Input::fromNullable).orElseGet(Input::absent);

        ApolloMutationCall<RemoveViolationToIgnoreMutation.Data> queryCall = apolloClient.mutate(new RemoveViolationToIgnoreMutation(projectId, rule, tool, language, inputFilename))
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        queryCall.enqueue(
            new ApolloCall.Callback<RemoveViolationToIgnoreMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<RemoveViolationToIgnoreMutation.Data> response) {
                    if (response.getData() == null || response.getData().removeViolationToIgnore() == null) {
                        apiRequest.setError();
                    } else {
                        apiRequest.setData(response.getData().removeViolationToIgnore());
                    }

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LOGGER.debug("api call to ignore failure fails");
                    LOGGER.debug(e.getMessage());
                    e.printStackTrace();
                    apiRequest.setError();
                }
            });

        return apiRequest.getData();
    }
}
