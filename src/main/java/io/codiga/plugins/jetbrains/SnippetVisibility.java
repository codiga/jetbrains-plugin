package io.codiga.plugins.jetbrains;

import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;

import java.util.Optional;

/**
 * Calculates and stores the snippet visibility.
 *
 * @see io.codiga.plugins.jetbrains.completion.inline.InlineDocumentListener
 * @see io.codiga.plugins.jetbrains.actions.snippet_search.SnippetToolWindow
 */
public final class SnippetVisibility {
    private boolean allSnippets;
    private Boolean onlyPublic;
    private Boolean onlyPrivate;
    private Boolean onlyFavorite;

    /**
     * Configures this snippet visibility based on the configuration in {@link AppSettingsState}.
     */
    public SnippetVisibility() {
        var settings = AppSettingsState.getInstance();
        setVisibilities(true, settings.getPrivateSnippetsOnly(), settings.getPublicSnippetsOnly(), settings.getFavoriteSnippetsOnly());
    }

    public SnippetVisibility(boolean allSnippets, Boolean onlyPrivate, Boolean onlyPublic, Boolean onlyFavorite) {
        setVisibilities(allSnippets, onlyPrivate, onlyPublic, onlyFavorite);
    }

    /**
     * Sets all the visibility options at once.
     */
    public void setVisibilities(boolean allSnippets, Boolean onlyPrivate, Boolean onlyPublic, Boolean onlyFavorite) {
        this.allSnippets = allSnippets;
        this.onlyPrivate = onlyPrivate;
        this.onlyPublic = onlyPublic;
        this.onlyFavorite = onlyFavorite;
    }

    /**
     * Creates a new {@code SnippetVisibility} instance configured to be used by {@code CodigaApi#getRecipesSemantic()}
     * query.
     * <p>
     * Private-only snippets take precedence over public-only ones.
     */
    public SnippetVisibility prepareForQuery() {
        Boolean publicOnly = null;
        Boolean privateOnly = null;
        Boolean favoriteOnly = null;

        if (Boolean.TRUE.equals(onlyPublic)) {
            publicOnly = true;
            privateOnly = null;
        }
        if (Boolean.TRUE.equals(onlyPrivate)) {
            privateOnly = true;
            publicOnly = null;
        }
        if (Boolean.TRUE.equals(onlyFavorite)) {
            favoriteOnly = true;
        }
        return new SnippetVisibility(allSnippets, privateOnly, publicOnly, favoriteOnly);
    }

    public boolean isAllSnippets() {
        return allSnippets;
    }

    public boolean isOnlyPublic() {
        return onlyPublic;
    }

    public boolean isOnlyPrivate() {
        return onlyPrivate;
    }

    public boolean isOnlyFavorite() {
        return onlyFavorite;
    }

    public Optional<Boolean> getOnlyPublic() {
        return Optional.ofNullable(onlyPublic);
    }

    public Optional<Boolean> getOnlyPrivate() {
        return Optional.ofNullable(onlyPrivate);
    }

    public Optional<Boolean> getOnlyFavorite() {
        return Optional.ofNullable(onlyFavorite);
    }
}
