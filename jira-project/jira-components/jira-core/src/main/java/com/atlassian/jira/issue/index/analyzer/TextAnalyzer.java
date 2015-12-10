package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.KeywordRepeatFilter;

import java.io.IOException;
import java.io.Reader;

/**
 * An {@link Analyzer} base class that provides the facility to apply the {@link SubtokenFilter}
 * during indexing and duplicating the original tokens before any stemming filter is applied to support wildcard
 * queries and exact phrase queries on document fields.
 */
abstract class TextAnalyzer extends Analyzer
{
    private final boolean indexing; //or searching

    public TextAnalyzer(boolean indexing)
    {
        this.indexing = indexing;
    }

    public boolean isIndexing()
    {
        return indexing;
    }

    /**
     * Applies a {@link SubtokenFilter} to the input token stream at document indexing time.
     *
     * @param input token stream
     * @return A TokenStream filtered by the sub-token filter during indexing, otherwise the input token stream is
     * returned.
     */
    protected TokenStream wrapStreamForIndexing(final TokenStream input)
    {
        if (isIndexing())
        {
            return new SubtokenFilter(input);
        }
        else
        {
            return input;
        }
    }

    /**
     * Applies a {@link KeywordRepeatFilter} to the input token stream at document indexing time to store the original
     * tokens as keywords before any stemming filter is applied and therefore support wildcard searches and exact phrase
     * queries on document fields.
     *
     * @param input token stream
     * @return A TokenStream filtered by the sub-token filter during indexing, otherwise the input token stream is
     * returned.
     */
    protected TokenStream wrapStreamForWilcardSearchSupport(final TokenStream input)
    {
        if (isIndexing())
        {
            return new KeywordRepeatFilter(input);
        }
        else
        {
            return input;
        }
    }

    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
    {
        return super.reusableTokenStream(fieldName, reader);
    }
}
