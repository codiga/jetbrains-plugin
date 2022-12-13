package io.codiga.plugins.jetbrains.graphql;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloMutationCall;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import io.codiga.api.*;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static io.codiga.plugins.jetbrains.Constants.LOGGER_NAME;
import static io.codiga.plugins.jetbrains.graphql.Constants.USER_AGENT;
import static io.codiga.plugins.jetbrains.utils.UserAgentUtils.getUserAgent;

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

    public static final Logger LOGGER = Logger.getInstance(LOGGER_NAME);
    private static final ApolloClient apolloClient = ApolloClient.builder()
        .serverUrl(Constants.ENDPOINT_URL)
        .build();



    /**
     * Set the header with access/secret keys so that we do an authenticated
     * request. to the API.
     *
     * @return the headers to use to make an API request.
     */
    private RequestHeaders getHeaders() {
        AppSettingsState settings = AppSettingsState.getInstance();


        /**
         * Use the API token
         */
        final String apiToken = settings == null || settings.getApiToken() == null ? "" : settings.getApiToken();
        if (apiToken != null && apiToken.length() > 0) {
            return RequestHeaders
                .builder()
                .addHeader(Constants.API_TOKEN_HEADER, apiToken)
                .addHeader(USER_AGENT, getUserAgent())
                .build();
        }
        return RequestHeaders
            .builder()
            .addHeader(USER_AGENT, getUserAgent())
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
    public List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut> getRecipesForClientByShotcurt(Optional<String> term,
                                                                                                                List<String> dependencies,
                                                                                                                Optional<String> parameters,
                                                                                                                LanguageEnumeration language,
                                                                                                                String filename,
                                                                                                                Optional<Boolean> onlyPublic,
                                                                                                                Optional<Boolean> onlyPrivate,
                                                                                                                Optional<Boolean> onlySubscribed) {
        ApiRequest<List<GetRecipesForClientByShortcutQuery.GetRecipesForClientByShortcut>> apiRequest = new ApiRequest();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);
        final Input<String> termParameter = term.map(Input::fromNullable).orElse(Input.absent());
        final Input<Boolean> onlyPublicParameter = onlyPublic.map(Input::fromNullable).orElse(Input.absent());
        final Input<Boolean> onlyPrivateParameter = onlyPrivate.map(Input::fromNullable).orElse(Input.absent());
        final Input<Boolean> onlySubscribedParameter = onlySubscribed.map(Input::fromNullable).orElse(Input.absent());


        ApolloQueryCall<GetRecipesForClientByShortcutQuery.Data> queryCall = apolloClient.query(
                new GetRecipesForClientByShortcutQuery(fingerprint, Input.fromNullable(filename), termParameter, dependencies, Input.absent(), language, onlyPublicParameter, onlyPrivateParameter, onlySubscribedParameter))
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
    public Optional<Long> getRecipesForClientByShotcurtLastTimestmap(List<String> dependencies, LanguageEnumeration language) {
        ApiRequest<Optional<Long>> apiRequest = new ApiRequest();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);


        ApolloQueryCall<GetRecipesForClientByShortcutLastTimestampQuery.Data> queryCall = apolloClient.query(
                new GetRecipesForClientByShortcutLastTimestampQuery(fingerprint, dependencies, language))
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        queryCall.enqueue(
            new ApolloCall.Callback<GetRecipesForClientByShortcutLastTimestampQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<GetRecipesForClientByShortcutLastTimestampQuery.Data> response) {
                    if (response.getData() == null) {
                        apiRequest.setError();
                    } else {
                        Object responseObject = response.getData().getRecipesForClientByShortcutLastTimestamp();
                        if (responseObject == null) {
                            apiRequest.setData(Optional.empty());
                        } else {
                            Long longValue = ((BigDecimal) responseObject).longValue();
                            apiRequest.setData(Optional.of(longValue));
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    apiRequest.setError();
                }
            });

        return apiRequest.getData().orElse(Optional.empty());
    }

    @Override
    public List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> getRecipesSemantic(Optional<String> term, List<String> dependencies, Optional<String> parameters, LanguageEnumeration language, String filename, Optional<Boolean> onlyPublic, Optional<Boolean> onlyPrivate, Optional<Boolean> onlySubscribed) {
        ApiRequest<List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch>> apiRequest = new ApiRequest();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);
        final Input<String> termParameter = term.map(Input::fromNullable).orElse(Input.absent());
        final Input<Boolean> onlyPublicParameter = onlyPublic.map(Input::fromNullable).orElse(Input.absent());
        final Input<Boolean> onlyPrivateParameter = onlyPrivate.map(Input::fromNullable).orElse(Input.absent());
        final Input<Boolean> onlySubscribedParameter = onlySubscribed.map(Input::fromNullable).orElse(Input.absent());


        ApolloQueryCall<GetRecipesForClientSemanticQuery.Data> queryCall = apolloClient.query(
                new GetRecipesForClientSemanticQuery(
                    termParameter,
                    onlyPublicParameter,
                    onlyPrivateParameter,
                    onlySubscribedParameter,
                    Input.fromNullable(filename),
                    dependencies, Input.absent(),
                    Input.optional(ImmutableList.of(language)),
                    10,
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

    @Override
    public Optional<Long> getRulesetsLastTimestamp(List<String> ruleNames) {
        ApiRequest<Optional<Long>> apiRequest = new ApiRequest();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);


        ApolloQueryCall<GetRulesetsForClientLastTimestampQuery.Data> queryCall = apolloClient.query(
                new GetRulesetsForClientLastTimestampQuery(ruleNames, fingerprint))
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        queryCall.enqueue(
            new ApolloCall.Callback<GetRulesetsForClientLastTimestampQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<GetRulesetsForClientLastTimestampQuery.Data> response) {
                    if (response.getData() == null) {
                        apiRequest.setError();
                    } else {
                        Object responseObject = response.getData().ruleSetsLastUpdatedTimestamp();
                        if (responseObject == null) {
                            apiRequest.setData(Optional.empty());
                        } else {
                            Long longValue = ((BigDecimal) responseObject).longValue();
                            apiRequest.setData(Optional.of(longValue));
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    apiRequest.setError();
                }
            });

        return apiRequest.getData().orElse(Optional.empty());
    }

    @Override
    public Optional<List<GetRulesetsForClientQuery.RuleSetsForClient>> getRulesetsForClient(List<String> ruleNames) {
        ApiRequest<List<GetRulesetsForClientQuery.RuleSetsForClient>> apiRequest = new ApiRequest();
        AppSettingsState settings = AppSettingsState.getInstance();
        String fingerPrintText = settings.getFingerprint();


        ApolloQueryCall<GetRulesetsForClientQuery.Data> queryCall = apolloClient.query(
                new GetRulesetsForClientQuery(ruleNames, fingerPrintText))
            .toBuilder()
            .requestHeaders(getHeaders())
            .build();
        queryCall.enqueue(
            new ApolloCall.Callback<GetRulesetsForClientQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<GetRulesetsForClientQuery.Data> response) {
                    if (response.getData() == null) {
                        apiRequest.setError();
                    } else {
                        apiRequest.setData(response.getData().ruleSetsForClient());
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    apiRequest.setError();
                }
            });

        return apiRequest.getData();
    }

    @Override
    public void recordRuleFix() {
        AppSettingsState settings = AppSettingsState.getInstance();

        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);
        ApiRequest<String> apiRecordRuleFix = new ApiRequest<>();

        ApolloMutationCall<RecordRuleFixMutation.Data> mutationCall =
            apolloClient.mutate(new RecordRuleFixMutation(fingerprint))
                .toBuilder()
                .requestHeaders(getHeaders())
                .build();
        mutationCall.enqueue(
            new ApolloCall.Callback<RecordRuleFixMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<RecordRuleFixMutation.Data> response) {
                    if (response.getData() == null) {
                        LOGGER.info(String.format("RecordRuleFixMutation response %s", response));
                        apiRecordRuleFix.setError();
                    } else {
                        LOGGER.info(String.format("RecordRuleFixMutation response data: %s ", response.getData()));
                        LOGGER.info(String.format("RecordRuleFixMutation response data: %s ", response.getData().recordAccess()));
                        apiRecordRuleFix.setData(response.getData().recordAccess());
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LOGGER.debug("api call to ignore failure fails");
                    LOGGER.debug(e.getMessage());
                    e.printStackTrace();
                    apiRecordRuleFix.setError();
                }
            });
    }

    @Override
    public void recordCreateCodigaYaml() {
        AppSettingsState settings = AppSettingsState.getInstance();

        String fingerPrintText = settings.getFingerprint();
        Input<String> fingerprint = Input.fromNullable(fingerPrintText);
        ApiRequest<String> apiRecordCreateCodigaYaml = new ApiRequest<>();

        ApolloMutationCall<RecordCreateCodigaYamlMutation.Data> mutationCall =
            apolloClient.mutate(new RecordCreateCodigaYamlMutation(fingerprint))
                .toBuilder()
                .requestHeaders(getHeaders())
                .build();
        mutationCall.enqueue(
            new ApolloCall.Callback<RecordCreateCodigaYamlMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<RecordCreateCodigaYamlMutation.Data> response) {
                    if (response.getData() == null) {
                        LOGGER.info(String.format("RecordCreateCodigaYamlMutation response %s", response));
                        apiRecordCreateCodigaYaml.setError();
                    } else {
                        LOGGER.info(String.format("RecordCreateCodigaYamlMutation response data: %s ", response.getData()));
                        LOGGER.info(String.format("RecordCreateCodigaYamlMutation response data: %s ", response.getData().recordAccess()));
                        apiRecordCreateCodigaYaml.setData(response.getData().recordAccess());
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    LOGGER.debug("api call to ignore failure fails");
                    LOGGER.debug(e.getMessage());
                    e.printStackTrace();
                    apiRecordCreateCodigaYaml.setError();
                }
            });
    }
}
