package io.codiga.plugins.jetbrains.completion.inline;

import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;

import java.util.Optional;

/**
 * Calculates and stores the snippet visibility based on the current {@link AppSettingsState}.
 */
final class SnippetVisibility {
    private Boolean onlyPublic;
    private Boolean onlyPrivate;
    private Boolean onlyFavorite;

    SnippetVisibility() {
        var settings = AppSettingsState.getInstance();
        if (settings.getPublicSnippetsOnly()) {
            onlyPublic = true;
            onlyPrivate = null;
        }
        if (settings.getPrivateSnippetsOnly()) {
            onlyPrivate = true;
            onlyPublic = null;
        }
        if (settings.getFavoriteSnippetsOnly()) {
            onlyFavorite = true;
        }
    }

    Optional<Boolean> getOnlyPublic() {
        return Optional.ofNullable(onlyPublic);
    }

    Optional<Boolean> getOnlyPrivate() {
        return Optional.ofNullable(onlyPrivate);
    }

    Optional<Boolean> getOnlyFavorite() {
        return Optional.ofNullable(onlyFavorite);
    }
}
