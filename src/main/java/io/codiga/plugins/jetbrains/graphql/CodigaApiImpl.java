package io.codiga.plugins.jetbrains.graphql;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.intellij.openapi.components.Service;
import io.codiga.api.*;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static io.codiga.api.type.AnalysisResultStatus.DONE;
import static io.codiga.api.type.AnalysisResultStatus.ERROR;
import static io.codiga.plugins.jetbrains.Constants.*;

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
public final class CodigaApiImpl implements CodigaApi {

    private static final ApolloClient apolloClient = ApolloClient.builder()
        .serverUrl(Constants.ENDPOINT_URL)
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
        final String apiToken = settings == null || settings.getApiToken() == null ? "" : settings.getApiToken();

        /**
         * If the API token is available, and defined, use them. Otherwise, use the old
         * method to connect with the ACCESS_KEY and SECRET_KEY.
          */
        if (settings.getApiToken() != null && settings.getApiToken().length() > 0) {
            return RequestHeaders
                    .builder()
                    .addHeader(Constants.API_TOKEN_HEADER, apiToken)
                    .build();
        }
        return RequestHeaders
            .builder()
            .addHeader(Constants.ACCESS_KEY_HEADER, accessKey)
            .addHeader(Constants.SECRET_KEY_HEADER, secretKey)
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

    @Override
    public List<GetRecipesForClientQuery.GetRecipesForClient> getRecipesForClient(List<String> keywords, List<String> dependencies, Optional<String> parameters, LanguageEnumeration language, String filename) {
        ApiRequest<List<GetRecipesForClientQuery.GetRecipesForClient>> apiRequest = new ApiRequest();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);

        ApolloQueryCall<GetRecipesForClientQuery.Data> queryCall = apolloClient.query(
                new GetRecipesForClientQuery(fingerprint, Input.fromNullable(filename), keywords, dependencies, Input.absent(), language))
                .toBuilder()
                .requestHeaders(getHeaders())
                .build();
        queryCall.enqueue(
                new ApolloCall.Callback<GetRecipesForClientQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetRecipesForClientQuery.Data> response) {
                        if (response.getData() == null) {
                            apiRequest.setError();
                        } else {
                            apiRequest.setData(response.getData().getRecipesForClient());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        apiRequest.setError();
                    }
                });

        return apiRequest.getData().orElse(ImmutableList.of());
    }

    @Override
    public List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> getRecipesForClientByShotcurt(Optional<String> term, List<String> dependencies, Optional<String> parameters, LanguageEnumeration language, String filename) {
        ApiRequest<List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut>> apiRequest = new ApiRequest();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);
        final Input<String> termParameter = term.map(Input::fromNullable).orElse(Input.absent());


        ApolloQueryCall<GetRecipesForClientByShortcutQuery.Data> queryCall = apolloClient.query(
                        new GetRecipesForClientByShortcutQuery(fingerprint, Input.fromNullable(filename), termParameter, dependencies, Input.absent(), language))
                .toBuilder()
                .requestHeaders(getHeaders())
                .build();
        queryCall.enqueue(
                new ApolloCall.Callback<GetRecipesForClientByShortcutQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetRecipesForClientByShortcutQuery.Data> response) {
                        if (response.getData() == null) {
                            apiRequest.setError();
                        } else {
                            apiRequest.setData(response.getData().getRecipesForClientByShortcut());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        apiRequest.setError();
                    }
                });

        return apiRequest.getData().orElse(ImmutableList.of());
    }

    @Override
    public List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> getRecipesSemantic(Optional<String> term, List<String> dependencies, Optional<String> parameters, LanguageEnumeration language, String filename) {
        ApiRequest<List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch>> apiRequest = new ApiRequest();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);
        final Input<String> termParameter = term.map(Input::fromNullable).orElse(Input.absent());


        ApolloQueryCall<GetRecipesForClientSemanticQuery.Data> queryCall = apolloClient.query(
                        new GetRecipesForClientSemanticQuery(
                                termParameter, Input.fromNullable(filename),
                                dependencies, Input.absent(),
                                Input.optional(ImmutableList.of(language)),
                                100,
                                0))
                .toBuilder()
                .requestHeaders(getHeaders())
                .build();
        queryCall.enqueue(
                new ApolloCall.Callback<GetRecipesForClientSemanticQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetRecipesForClientSemanticQuery.Data> response) {
                        if (response.getData() == null) {
                            apiRequest.setError();
                        } else {
                            apiRequest.setData(response.getData().assistantRecipesSemanticSearch());
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
     * @param projectId - the project identifier on Codiga (got from the preferences)
     * @param revision - the revision of the repository (usually the latest version)
     * @param path - the path of the file we are looking for.
     *
     * @return - all the data we need to surface to the project. Data comes from the GraphQL generated data.
     */
    public Optional<GetFileDataQuery.Project> getDataForFile(Long projectId, String revision, String path) throws GraphQlQueryException {
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

        Optional<GetFileDataQuery.Project> result = apiRequest.getData();
        if (!result.isPresent()) {
            throw new GraphQlQueryException("invalid-query");
        }
        return result;
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
     * @param projectId - the project identifier on Codiga
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

    /**
     * Initiate the real-time feedback query using the [[getFileAnalysis]] method.
     * @param filename - the filename to analyze
     * @param code - the code we want to analyze
     * @param language - the language (using the GraphQL enumeration)
     * @param projectId - the optional project identifier.
     * @return the list of violations.
     */
    @Override
    public Optional<GetFileAnalysisQuery.GetFileAnalysis> getFileAnalysis(String filename, String code, LanguageEnumeration language, Optional<Long> projectId, Optional<String> parameters) throws GraphQlQueryException {
        ApiRequest<Object> apiRequestSendFileForAnalysis = new ApiRequest<Object>();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);

        final Input<Object> inputProjectId = projectId.<Input<Object>>map(Input::fromNullable).orElseGet(Input::absent);

        final Input<String> inputParameters = parameters.map(Input::fromNullable).orElse(Input.absent());


        /**
         * Send the analysis query
         */
        ApolloMutationCall<CreateFileAnalysisMutation.Data> mutationCall =
            apolloClient.mutate(new CreateFileAnalysisMutation(inputProjectId, filename, code, language, fingerprint, inputParameters))
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        mutationCall.enqueue(
            new ApolloCall.Callback<CreateFileAnalysisMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<CreateFileAnalysisMutation.Data> response) {
                    if (response.getData() == null) {
                        LOGGER.info(String.format("CreateFileAnalysisMutation response %s", response));

                        apiRequestSendFileForAnalysis.setError();
                    } else {
                        LOGGER.info(String.format("CreateFileAnalysisMutation response data: %s ", response.getData()));
                        LOGGER.info(String.format("CreateFileAnalysisMutation response data: %s ", response.getData().createFileAnalysis()));
                        apiRequestSendFileForAnalysis.setData(response.getData().createFileAnalysis());
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LOGGER.debug("api call to ignore failure fails");
                    LOGGER.debug(e.getMessage());
                    e.printStackTrace();
                    apiRequestSendFileForAnalysis.setError();
                }
            });


        Optional<Object> fileAnalysisId = apiRequestSendFileForAnalysis.getData();
        if (!fileAnalysisId.isPresent()) {
            LOGGER.debug("no data found from the createFileAnalysis call");
            LOGGER.debug(fileAnalysisId.toString());

            throw new GraphQlQueryException("invalid request");
        }

        LOGGER.debug(String.format("Got data from createFileAnalysis call %s", fileAnalysisId.get()));


        long currentTimestamp = System.currentTimeMillis();
        long deadline = currentTimestamp + REAL_TIME_FEEDBACK_TIMEOUT_MILLIS;

        try {
            Thread.sleep(FILE_ANALYSIS_INITIAL_SLEEP_MILLIS);
        } catch (InterruptedException e) {
            LOGGER.debug("interupted during initial sleep");
        }

        while (currentTimestamp < deadline) {
            /**
             * Regularly poll until we get the results
             */
            ApiRequest<GetFileAnalysisQuery.GetFileAnalysis> getFileAnalysisData = new ApiRequest<GetFileAnalysisQuery.GetFileAnalysis>();


            LOGGER.info(String.format("Doing GetFileAnalysisQuery for file %s", filename));
            ApolloQueryCall<GetFileAnalysisQuery.Data> queryCall =
                apolloClient.query(new GetFileAnalysisQuery(fileAnalysisId.get(), fingerprint))
                    .toBuilder()
                    .requestHeaders(getHeaders())
                    .build();
            queryCall.enqueue(
                new ApolloCall.Callback<GetFileAnalysisQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetFileAnalysisQuery.Data> response) {
                        if (response.getData() == null) {
                            LOGGER.info(String.format("GetFileAnalysisQuery response %s", response));
                            getFileAnalysisData.setError();
                        } else {
                            getFileAnalysisData.setData(response.getData().getFileAnalysis());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        LOGGER.debug("GetFileAnalysisQuery - api call to ignore failure fails");
                        LOGGER.debug(e.getMessage());
                        e.printStackTrace();
                        getFileAnalysisData.setError();
                    }
                });

            /**
             * If no data, just return since there is definitely something wrong.
             */
            LOGGER.debug(String.format("Waiting for data from GetFileAnalysisQuery for file %s", filename));
            Optional<GetFileAnalysisQuery.GetFileAnalysis> returnedData = getFileAnalysisData.getData();

            if (returnedData.isPresent()) {
                /**
                 * Loop until we get something.
                 */
                if (returnedData.get().status() == DONE) {
                    return returnedData;
                }
                if (returnedData.get().status() == ERROR) {
                    LOGGER.debug("error when getting the request");
                    return Optional.empty();
                }
            }
            else {
                LOGGER.debug("error getting data from the analysis");
                return Optional.empty();
            }

            /**
             * Sleep between we poll the API.
             */
            try {
                Thread.sleep(SLEEP_BETWEEN_FILE_ANALYSIS_MILLIS);
            } catch (InterruptedException e) {
                LOGGER.debug("cannot sleep");
            }
            currentTimestamp = System.currentTimeMillis();
        }
        if (currentTimestamp >= deadline){
            LOGGER.warn("deadline missed during analysis");
        }
        return Optional.empty();
    }

    @Override
    public void recordRecipeUse(Long recipeId) {
        AppSettingsState settings = AppSettingsState.getInstance();

        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);
        ApiRequest<String> apiRecordRecipeUse = new ApiRequest<String>();

        ApolloMutationCall<RecordRecipeUseMutation.Data> mutationCall =
                apolloClient.mutate(new RecordRecipeUseMutation(recipeId, fingerprint))
                        .toBuilder()
                        .requestHeaders(getHeaders())
                        .build();
        mutationCall.enqueue(
                new ApolloCall.Callback<RecordRecipeUseMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<RecordRecipeUseMutation.Data> response) {
                        if (response.getData() == null) {
                            LOGGER.info(String.format("RecordRecipeUseMutation response %s", response));
                            apiRecordRecipeUse.setError();
                        } else {
                            LOGGER.info(String.format("RecordRecipeUseMutation response data: %s ", response.getData()));
                            LOGGER.info(String.format("RecordRecipeUseMutation response data: %s ", response.getData().recordAccess()));
                            apiRecordRecipeUse.setData(response.getData().recordAccess());
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        LOGGER.debug("api call to ignore failure fails");
                        LOGGER.debug(e.getMessage());
                        e.printStackTrace();
                        apiRecordRecipeUse.setError();
                    }
                });
    }
}
