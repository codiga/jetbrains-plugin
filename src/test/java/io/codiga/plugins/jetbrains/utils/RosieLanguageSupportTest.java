package io.codiga.plugins.jetbrains.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.codiga.api.type.LanguageEnumeration;
import org.junit.Test;

/**
 * Unit test for {@link RosieLanguageSupport}.
 */
public class RosieLanguageSupportTest {

    @Test
    public void shouldSupportLanguage() {
        assertTrue(RosieLanguageSupport.isLanguageSupported(LanguageEnumeration.PYTHON));
    }

    @Test
    public void shouldNotSupportLanguage() {
        assertFalse(RosieLanguageSupport.isLanguageSupported(LanguageEnumeration.CSS));
    }

    @Test
    public void shouldReturnRosieLanguageForPython() {
        assertEquals("python", RosieLanguageSupport.getRosieLanguage(LanguageEnumeration.PYTHON));
    }

    @Test
    public void shouldReturnJavaScriptRosieLanguageForTypeScript() {
        assertEquals("javascript", RosieLanguageSupport.getRosieLanguage(LanguageEnumeration.TYPESCRIPT));
    }

    @Test
    public void shouldReturnUnknownRosieLanguageForNotSupportedLanguage() {
        assertEquals("unknown", RosieLanguageSupport.getRosieLanguage(LanguageEnumeration.CSS));
    }
}
