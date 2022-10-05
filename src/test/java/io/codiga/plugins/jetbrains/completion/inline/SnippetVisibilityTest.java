package io.codiga.plugins.jetbrains.completion.inline;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import io.codiga.plugins.jetbrains.settings.application.AppSettingsState;

/**
 * Integration test for {@link SnippetVisibility}.
 */
public class SnippetVisibilityTest extends BasePlatformTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        var settings = AppSettingsState.getInstance();
        settings.setPublicSnippetsOnly(false);
        settings.setPrivateSnippetsOnly(false);
        settings.setFavoriteSnippetsOnly(false);
    }

    public void testDefaultState() {
        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().isEmpty());
        assertTrue(snippetVisibility.getOnlyPrivate().isEmpty());
        assertTrue(snippetVisibility.getOnlyFavorite().isEmpty());
    }

    public void testReturnOnlyPublic() {
        var settings = AppSettingsState.getInstance();
        settings.setPublicSnippetsOnly(true);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().get());
        assertTrue(snippetVisibility.getOnlyPrivate().isEmpty());
        assertTrue(snippetVisibility.getOnlyFavorite().isEmpty());
    }

    public void testReturnNotOnlyPublic() {
        var settings = AppSettingsState.getInstance();
        settings.setPublicSnippetsOnly(false);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().isEmpty());
        assertTrue(snippetVisibility.getOnlyPrivate().isEmpty());
        assertTrue(snippetVisibility.getOnlyFavorite().isEmpty());
    }

    public void testReturnOnlyPrivate() {
        var settings = AppSettingsState.getInstance();
        settings.setPrivateSnippetsOnly(true);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().isEmpty());
        assertTrue(snippetVisibility.getOnlyPrivate().get());
        assertTrue(snippetVisibility.getOnlyFavorite().isEmpty());
    }

    public void testReturnNotOnlyPrivate() {
        var settings = AppSettingsState.getInstance();
        settings.setPrivateSnippetsOnly(false);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().isEmpty());
        assertTrue(snippetVisibility.getOnlyPrivate().isEmpty());
        assertTrue(snippetVisibility.getOnlyFavorite().isEmpty());
    }

    public void testReturnOnlyPrivateTakingPrecedenceOverOnlyPublic() {
        var settings = AppSettingsState.getInstance();
        settings.setPublicSnippetsOnly(true);
        settings.setPrivateSnippetsOnly(true);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().isEmpty());
        assertTrue(snippetVisibility.getOnlyPrivate().get());
        assertTrue(snippetVisibility.getOnlyFavorite().isEmpty());
    }

    public void testReturnOnlyFavorites() {
        var settings = AppSettingsState.getInstance();
        settings.setFavoriteSnippetsOnly(true);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().isEmpty());
        assertTrue(snippetVisibility.getOnlyPrivate().isEmpty());
        assertTrue(snippetVisibility.getOnlyFavorite().get());
    }

    public void testReturnNotOnlyFavorites() {
        var settings = AppSettingsState.getInstance();
        settings.setFavoriteSnippetsOnly(false);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().isEmpty());
        assertTrue(snippetVisibility.getOnlyPrivate().isEmpty());
        assertTrue(snippetVisibility.getOnlyFavorite().isEmpty());
    }

    public void testReturnOnlyPublicDoesntInterfereWithOnlyFavorites() {
        var settings = AppSettingsState.getInstance();
        settings.setPublicSnippetsOnly(true);
        settings.setFavoriteSnippetsOnly(true);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().get());
        assertTrue(snippetVisibility.getOnlyPrivate().isEmpty());
        assertTrue(snippetVisibility.getOnlyFavorite().get());
    }

    public void testReturnOnlyPrivateDoesntInterfereWithOnlyFavorites() {
        var settings = AppSettingsState.getInstance();
        settings.setPrivateSnippetsOnly(true);
        settings.setFavoriteSnippetsOnly(true);

        var snippetVisibility = new SnippetVisibility();

        assertTrue(snippetVisibility.getOnlyPublic().isEmpty());
        assertTrue(snippetVisibility.getOnlyPrivate().get());
        assertTrue(snippetVisibility.getOnlyFavorite().get());
    }
}
