package com.atlassian.jira.issue.index.analyzer;

import java.io.Reader;

import com.google.common.base.Function;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.el.GreekLowerCaseFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

/**
 * Extends the functionality of the Standard (language) Analyser provided by Lucene
 * by using the ClassicAnalyser and adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 3.2 compatibility.
 * 
 * @see SubtokenFilter
 */
public class GreekAnalyzer extends TextAnalyzer
{
    private final Version matchVersion;
    private final Function<TokenStream, TokenStream> stemmingFilter;
    private final Function<TokenStream, TokenStream> stopWordFilter;

    public GreekAnalyzer(final Version matchVersion, final boolean indexing, final Function<TokenStream, TokenStream> stemmingFilter, final Function<TokenStream, TokenStream> stopWordFilter)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        this.stemmingFilter = stemmingFilter;
        this.stopWordFilter = stopWordFilter;
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the provided Reader.
     *
     * @return A TokenStream build from a StandardTokenizer filtered with
     *         StandardFilter, LowerCaseFilter, StopFilter, GermanStemFilter
     */
    @Override
    public final TokenStream tokenStream(final String fieldName, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(matchVersion, reader);
        result = new StandardFilter(matchVersion, result);
        result = wrapStreamForIndexing(result);

        result = new GreekLowerCaseFilter(matchVersion, result);
        result = stopWordFilter.apply(result);

        result = wrapStreamForWilcardSearchSupport(result);
        result = stemmingFilter.apply(result);

        return result;
    }
}
