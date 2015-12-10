package com.atlassian.jira.issue.index.analyzer;

import java.io.Reader;

import com.google.common.base.Function;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

/*
 * Note: checked for Lucene 2.9 compatibility.
 */
public class EnglishAnalyzer extends TextAnalyzer
{
    private final Version version;
    private final Function<TokenStream, TokenStream> stemmingAlgorithm;
    private final Function<TokenStream, TokenStream> stopWordFilter;

    public EnglishAnalyzer(final Version version, final boolean indexing,
            final Function<TokenStream, TokenStream> stemmingStrategy,
            final Function<TokenStream, TokenStream> stopWordFilter)
    {
        super(indexing);
        this.stemmingAlgorithm = stemmingStrategy;
        this.stopWordFilter = stopWordFilter;
        this.version = version;
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public final TokenStream tokenStream(final String fieldname, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(version, reader);
        result = new StandardFilter(version, result);
        result = wrapStreamForIndexing(result);

        result = new LowerCaseFilter(version, result);
        result = stopWordFilter.apply(result);

        result = wrapStreamForWilcardSearchSupport(result);
        result = stemmingAlgorithm.apply(result);

        return result;
    }

}
