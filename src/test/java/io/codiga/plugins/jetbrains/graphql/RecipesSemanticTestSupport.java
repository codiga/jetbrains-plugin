package io.codiga.plugins.jetbrains.graphql;

import io.codiga.api.GetRecipesForClientSemanticQuery;
import io.codiga.api.type.AccountType;
import io.codiga.api.type.LanguageEnumeration;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Test utility for creating {@link GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch} based on the
 * provided language.
 */
public final class RecipesSemanticTestSupport {

    /**
     * Creates test recipes semantic for the argument language.
     *
     * @param language the language to create the recipes for
     */
    static List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> getRecipesSemanticForLanguage(LanguageEnumeration language) {
        switch (language) {
            case RUST:
                return getRecipesSemanticForRust(); //Used for returning multiple snippets
            case YAML:
                return getRecipesSemanticForYaml(); //Used for returning a single snippet
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Creates multiple test recipes semantic for the RUST language.
     */
    private static List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> getRecipesSemanticForRust() {
        String __typename = "AssistantRecipe";
        var idSpawn = BigDecimal.valueOf(42069L);
        var keywordsSpawn = Collections.singletonList("spawn");
        var imports = Collections.singletonList("use std::thread;");

        var idPrint = BigDecimal.valueOf(9684L);
        var keywordsPrint = Collections.singletonList("spawn");

        var idPrintMulti = BigDecimal.valueOf(46557L);
        var keywordsPrintMulti = Collections.singletonList("spawn");

        var owner = new GetRecipesForClientSemanticQuery.Owner(__typename, 123, null, null, false, AccountType.REGULAR);
        var groups = List.of(new GetRecipesForClientSemanticQuery.Group(__typename, 345, "Group name"));
        return List.of(
            new GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch(
                __typename,
                idSpawn,
                "Spawn threads",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXS8vIHRocmVhZCBjb2RlIGhlcmUKJltDT0RJR0FfSU5ERU5UXTQyCn0pOw==",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXS8vIHRocmVhZCBjb2RlIGhlcmUKJltDT0RJR0FfSU5ERU5UXTQyCn0pOw==",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXS8vIHRocmVhZCBjb2RlIGhlcmUKJltDT0RJR0FfSU5ERU5UXTQyCn0pOw==",
                keywordsSpawn,
                imports,
                LanguageEnumeration.RUST,
                true,
                "Quickly spawn a thread using the std library",
                "spawn",
                owner,
                groups),
            new GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch(
                __typename,
                idPrint,
                "Spawn thread and print message",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXXByaW50bG4hKCJ0aGlzIGlzIGEgbWVzc2FnZSIpOwp9KTs=",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXXByaW50bG4hKCJ0aGlzIGlzIGEgbWVzc2FnZSIpOwp9KTs=",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXXByaW50bG4hKCJ0aGlzIGlzIGEgbWVzc2FnZSIpOwp9KTs=",
                keywordsPrint,
                imports,
                LanguageEnumeration.RUST,
                true,
                "Spawns a thread and prints a single message",
                "spawn",
                owner,
                groups
            ),
            new GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch(
                __typename,
                idPrintMulti,
                "Spawn thread and print multiple messages",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXXByaW50bG4hKCJ0aGlzIGlzIGEgbWVzc2FnZSIpOwomW0NPRElHQV9JTkRFTlRdcHJpbnRsbiEoInRoaXMgaXMgYW5vdGhlciBtZXNzYWdlIik7Cn0pOw==",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXXByaW50bG4hKCJ0aGlzIGlzIGEgbWVzc2FnZSIpOwomW0NPRElHQV9JTkRFTlRdcHJpbnRsbiEoInRoaXMgaXMgYW5vdGhlciBtZXNzYWdlIik7Cn0pOw==",
                "dGhyZWFkOjpzcGF3bihtb3ZlIHx8IHsKJltDT0RJR0FfSU5ERU5UXXByaW50bG4hKCJ0aGlzIGlzIGEgbWVzc2FnZSIpOwomW0NPRElHQV9JTkRFTlRdcHJpbnRsbiEoInRoaXMgaXMgYW5vdGhlciBtZXNzYWdlIik7Cn0pOw==",
                keywordsPrintMulti,
                imports,
                LanguageEnumeration.RUST,
                true,
                "Spawns a thread and prints multiple messages",
                "spawn",
                owner,
                groups
            )
        );
    }

    /**
     * Creates a single test recipe semantic for the YAML language.
     */
    private static List<GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch> getRecipesSemanticForYaml() {
        String __typename = "AssistantRecipe";
        var id = BigDecimal.valueOf(96972L);
        var keywords = Collections.singletonList("fetch");

        var owner = new GetRecipesForClientSemanticQuery.Owner(__typename, 123, null, null, false, AccountType.REGULAR);
        var groups = List.of(new GetRecipesForClientSemanticQuery.Group(__typename, 345, "Group name"));

        return List.of(
            new GetRecipesForClientSemanticQuery.AssistantRecipesSemanticSearch(
                __typename,
                id,
                "Fetch sources",
                "c3RlcHM6CiAgLSBuYW1lOiBGZXRjaCBTb3VyY2VzCiAgICB1c2VzOiBhY3Rpb25zL2NoZWNrb3V0QHYy",
                "c3RlcHM6CiAgLSBuYW1lOiBGZXRjaCBTb3VyY2VzCiAgICB1c2VzOiBhY3Rpb25zL2NoZWNrb3V0QHYy",
                "c3RlcHM6CiAgLSBuYW1lOiBGZXRjaCBTb3VyY2VzCiAgICB1c2VzOiBhY3Rpb25zL2NoZWNrb3V0QHYy",
                keywords,
                Collections.emptyList(),
                LanguageEnumeration.YAML,
                true,
                "Adds a fetch sources action",
                "fetch",
                owner,
                groups));
    }

    private RecipesSemanticTestSupport() {
        //utility class
    }
}
