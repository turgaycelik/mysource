package com.atlassian.jira.issue.index.indexers.phrase;

import com.atlassian.annotations.ExperimentalApi;
import org.apache.lucene.document.Field;

import static org.apache.commons.lang.StringUtils.startsWith;

/**
 * Encapsulates the information to create a special purpose {@link Field lucene field} to be used for quoted phrase 
 * query searches for a given JIRA text field.
 *
 * @since v6.0.8
 */
@ExperimentalApi
public class PhraseQuerySupportField
{
    private static String PHRASE_QUERY_SUPPORT_FIELD_PREFIX = "pq_support_";

    /**
     * Returns the name of the phrase query support field to build for a given field.
     *
     * @param indexFieldName The name of the original field.
     *
     * @return A {@code String} containing the name of the phrase query support field.
     */
    public static String forIndexField(final String indexFieldName)
    {
        return PHRASE_QUERY_SUPPORT_FIELD_PREFIX + indexFieldName;
    }

    /**
     * Determines whether a given document field is a phrase query support field.
     *
     * @param indexFieldName the name of the field to inspect.
     *
     * @return {@code true} if the passed in field is a phrase query support field; otherwise, {@code false}.
     */
    public static boolean isPhraseQuerySupportField(final String indexFieldName)
    {
        return startsWith(indexFieldName, PHRASE_QUERY_SUPPORT_FIELD_PREFIX);
    }
}
