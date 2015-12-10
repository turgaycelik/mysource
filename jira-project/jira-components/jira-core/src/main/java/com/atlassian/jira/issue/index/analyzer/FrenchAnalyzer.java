package com.atlassian.jira.issue.index.analyzer;

import java.io.Reader;

import com.google.common.base.Function;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.fr.ElisionFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

/*
 * Extends the functionality of the Standard (language) Analyser provided by Lucene
 * by using the ClassicAnalyser and adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 3.2 compatibility.
 */
public class FrenchAnalyzer extends TextAnalyzer
{
    private final Version matchVersion;
    private final Function<TokenStream, TokenStream> stemmingFilter;
    private final Function<TokenStream, TokenStream> stopWordFilter;

    public FrenchAnalyzer(final Version matchVersion, final boolean indexing,
            final Function<TokenStream, TokenStream> stemmingFilter,
            final Function<TokenStream, TokenStream> stopWordFilter)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        this.stemmingFilter = stemmingFilter;
        this.stopWordFilter = stopWordFilter;
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public final TokenStream tokenStream(final String fieldname, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(matchVersion, reader);
        result = new StandardFilter(matchVersion, result);
        result = wrapStreamForIndexing(result);

        result = new ElisionFilter(matchVersion, result);
        result = new LowerCaseFilter(matchVersion, result);
        result = stopWordFilter.apply(result);

        result = wrapStreamForWilcardSearchSupport(result);
        result = stemmingFilter.apply(result);

        return result;
    }
}
