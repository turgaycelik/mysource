package com.atlassian.jira.issue.index.analyzer;

import java.io.Reader;

import com.google.common.base.Function;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

/**
 * A {@link TextAnalyzer} that takes in a set of stop words and a stem filter as a function to apply to the text being
 * analyzed.
 *
 */
public class StemmingAnalyzer extends TextAnalyzer
{
    private final Version matchVersion;
    private final Function<TokenStream, TokenStream> stemmingFilter;
    private final Function<TokenStream, TokenStream> stopWordFilter;

    public StemmingAnalyzer(final Version matchVersion, final boolean indexing,
            final Function<TokenStream, TokenStream> stemmingFilter,
            final Function<TokenStream, TokenStream> stopWordFilter)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        this.stemmingFilter = stemmingFilter;
        this.stopWordFilter = stopWordFilter;
    }

    @Override
    public final TokenStream tokenStream(final String fieldname, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(matchVersion, reader);
        result = new StandardFilter(matchVersion, result);
        result = wrapStreamForIndexing(result);

        result = new LowerCaseFilter(matchVersion, result);
        result = stopWordFilter.apply(result);

        result = wrapStreamForWilcardSearchSupport(result);
        result = stemmingFilter.apply(result);

        return result;
    }
}
