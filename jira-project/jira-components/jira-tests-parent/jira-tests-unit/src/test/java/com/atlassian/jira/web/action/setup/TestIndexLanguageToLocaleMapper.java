package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.config.properties.APKeys;

import org.junit.Test;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

/**
 * @since v6.0.4
 */
public class TestIndexLanguageToLocaleMapper
{
    @Test
    public void shouldReturnSpanishIndexingLanguageKeyForSpanishFromSpainLocale()
    {
        final IndexLanguageToLocaleMapper indexLanguageToLocaleMapper = new IndexLanguageToLocaleMapperImpl();

        final String actualKey = indexLanguageToLocaleMapper.getLanguageForLocale("es_ES");

        assertEquals
                (
                        format("The indexing language for locale [es_ES] should be: [%s]", APKeys.Languages.SPANISH),
                        APKeys.Languages.SPANISH, actualKey
                );
    }

    @Test
    public void shouldReturnFrenchIndexingLanguageKeyForFrenchFromFranceLocale()
    {
        final IndexLanguageToLocaleMapper indexLanguageToLocaleMapper = new IndexLanguageToLocaleMapperImpl();

        final String actualKey = indexLanguageToLocaleMapper.getLanguageForLocale("fr_FR");

        assertEquals
                (
                        format("The indexing language for locale [fr_FR] should be: [%s]", APKeys.Languages.FRENCH),
                        APKeys.Languages.FRENCH, actualKey
                );
    }
}
