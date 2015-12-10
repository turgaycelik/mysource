package com.atlassian.jira.issue.index.analyzer;

import java.io.Reader;

import com.google.common.base.Function;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKTokenizer;

/*
 * Extends the functionality of the Standard (language) Analyser provided by Lucene
 * by using the ClassicAnalyser and adding the SubtokenFilter.
 * <p>
 * This is useful for Chinese, Japanese and Korean languages.
 * <p>
 * Note: checked for Lucene 3.2 compatibility.
 */
public class CJKAnalyzer extends TextAnalyzer
{
    private final Function<TokenStream, TokenStream> stopWordFilter;

    public CJKAnalyzer(final boolean indexing, final Function<TokenStream, TokenStream> stopWordFilter)
    {
        super(indexing);
        this.stopWordFilter = stopWordFilter;
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public final TokenStream tokenStream(final String fieldname, final Reader reader)
    {
        TokenStream result = new CJKTokenizer(reader);
        result = wrapStreamForIndexing(result);
        result = stopWordFilter.apply(result);

        return result;
    }
}
