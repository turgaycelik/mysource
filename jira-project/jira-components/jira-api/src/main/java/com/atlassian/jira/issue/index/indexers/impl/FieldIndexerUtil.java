package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.annotations.Internal;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Used for doing simple indexing stuff.
 *
 * @since v4.0
 */
@Internal
public class FieldIndexerUtil
{
    private static final int MAX_SORT_LENGTH = 50;

    /**
     * Index a single keyword field, with a default if the issue field is not set
     *
     * shared with CommentDocument
     */
    public static void indexKeywordWithDefault(final Document doc, final String indexField, final String fieldValue, final String defaultValue)
    {
        indexKeywordWithDefault(doc, indexField, fieldValue, defaultValue, true);
    }

    public static void indexKeywordWithDefault(final Document doc, final String indexField, final Long aLong, final String defaultValue)
    {
        final String value = aLong != null ? aLong.toString() : null;
        indexKeywordWithDefault(doc, indexField, value, defaultValue);
    }

    public static void indexKeywordWithDefault(final Document doc, final String indexField, final String fieldValue, final String defaultValue, final boolean searchable)
    {
        doc.add(getField(indexField, fieldValue, defaultValue, searchable));
    }

    private static Field getField(final String indexField, final String fieldValue, final String defaultValue, final boolean searchable)
    {
        final String value = (StringUtils.isNotBlank(fieldValue)) ? fieldValue : defaultValue;
        final Field.Index index = (searchable) ? Field.Index.NOT_ANALYZED_NO_NORMS : Field.Index.NO;
        return new Field(indexField, value, Field.Store.YES, index);
    }

    public static String getValueForSorting(final String fieldValue)
    {
        final String trimmed = (fieldValue == null) ? null : fieldValue.trim();
        if (!StringUtils.isBlank(trimmed))
        {
            if (trimmed.length() > MAX_SORT_LENGTH)
            {
                return trimmed.substring(0, MAX_SORT_LENGTH);
            }
            return trimmed;
        }
        else
        {

            return String.valueOf('\ufffd');
        }
    }
}
