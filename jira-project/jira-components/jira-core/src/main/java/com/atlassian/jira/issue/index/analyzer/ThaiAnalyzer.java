package com.atlassian.jira.issue.index.analyzer;

import java.io.Reader;

import com.google.common.base.Function;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.th.ThaiWordFilter;
import org.apache.lucene.util.Version;

/**
 * Extends the functionality of the standard ThaiAnalyser provided by Lucene by adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 2.9 compatibility.
 * 
 * @see SubtokenFilter
 */
public class ThaiAnalyzer extends TextAnalyzer
{
    private final Version matchVersion;
    private final Function<TokenStream, TokenStream> stopWordFilter;

    public ThaiAnalyzer(final Version matchVersion, final boolean indexing,
            final Function<TokenStream, TokenStream> stopWordFilter)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        this.stopWordFilter = stopWordFilter;
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the provided Reader.
     *
     * @return A TokenStream build from a ClassicTokenizer and appropriate filters for the
     * language. See
     */
    @Override
    public final TokenStream tokenStream(final String fieldName, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(matchVersion, reader);
        result = new StandardFilter(matchVersion, result);
        result = wrapStreamForIndexing(result);

        result = new LowerCaseFilter(matchVersion, result);
        result = stopWordFilter.apply(result);

        result = new ThaiWordFilter(matchVersion, result);

        return result;
    }
}
