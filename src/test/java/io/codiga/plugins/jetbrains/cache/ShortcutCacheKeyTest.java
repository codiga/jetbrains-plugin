package io.codiga.plugins.jetbrains.cache;

import com.google.common.collect.ImmutableList;
import io.codiga.api.type.LanguageEnumeration;
import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.junit.Test;

import java.util.Arrays;


public class ShortcutCacheKeyTest extends TestBase {


    @Test
    public void testKeyEquals()  {
        ShortcutCacheKey cacheKey1 = new ShortcutCacheKey(LanguageEnumeration.C, "myfile", ImmutableList.of("dep1", "dep2"));
        ShortcutCacheKey cacheKey2 = new ShortcutCacheKey(LanguageEnumeration.C, "myfile", ImmutableList.of("dep1", "dep2"));
        ShortcutCacheKey cacheKey3 = new ShortcutCacheKey(LanguageEnumeration.C, "myotherfile", ImmutableList.of("dep1", "dep2"));
        ShortcutCacheKey cacheKey4 = new ShortcutCacheKey(LanguageEnumeration.C, "myfile", ImmutableList.of("dep1", "dep3"));
        ShortcutCacheKey cacheKey5 = new ShortcutCacheKey(LanguageEnumeration.JAVA, "myfile", ImmutableList.of("dep1", "dep2"));

        assertEquals(cacheKey1, cacheKey2);
        assertEquals(cacheKey1.hashCode(), cacheKey2.hashCode());

        assertFalse(cacheKey1.equals(cacheKey3));
        assertFalse(cacheKey1.equals(cacheKey4));
        assertFalse(cacheKey1.equals(cacheKey5));

        for (ShortcutCacheKey shortcutCacheKey : Arrays.asList(cacheKey3, cacheKey4, cacheKey5)) {
            assertTrue(cacheKey1.hashCode() != shortcutCacheKey.hashCode());
        }
    }

}
