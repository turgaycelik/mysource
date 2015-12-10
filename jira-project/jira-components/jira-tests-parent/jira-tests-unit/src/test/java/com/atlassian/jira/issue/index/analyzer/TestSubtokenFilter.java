package com.atlassian.jira.issue.index.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import com.atlassian.jira.index.LuceneVersion;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

import static com.atlassian.jira.util.collect.CollectionBuilder.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestSubtokenFilter
{
    @Test
    public void testEmptyText() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("");
        assertFalse(new SubtokenFilter(tokenStream).incrementToken());
    }

    @Test
    public void testExceptionString() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("Throws java.lang.NullPointerException sometimes.");
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken("throw", "<ALPHANUM>", filter);
        // The Analyser thinks this is a server hostname like www.atlassian.com
        assertNextToken("java.lang.nullpointerexcept", "<HOST>", filter);
        assertNextToken("java", "EXCEPTION", filter);
        assertNextToken("lang", "EXCEPTION", filter);
        assertNextToken("nullpointerexcept", "EXCEPTION", filter);
        assertNextToken("sometim", "<ALPHANUM>", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testExceptionStringWithLeadingDot() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("Throws .java.lang.NullPointerException sometimes.");
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken("throw", "<ALPHANUM>", filter);
        // The Lead dot should be removed, and tehn act as per usual.
        assertNextToken("java.lang.nullpointerexcept", "<HOST>", filter);
        assertNextToken("java", "EXCEPTION", filter);
        assertNextToken("lang", "EXCEPTION", filter);
        assertNextToken("nullpointerexcept", "EXCEPTION", filter);
        assertNextToken("sometim", "<ALPHANUM>", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testExceptionStringWithTrailingDot() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("Throws java.lang.NullPointerException.");
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken("throw", "<ALPHANUM>", filter);

        // JRA-15484 Lucene used to get this wrong with a dot at the end. In order to get it right, you need to call the
        // StandardTokenizer constructor with replaceInvalidAcronym=true.
        // see http://issues.apache.org/jira/browse/LUCENE-1068
        assertNextToken("java.lang.nullpointerexcept", "<HOST>", filter);
        assertNextToken("java", "EXCEPTION", filter);
        assertNextToken("lang", "EXCEPTION", filter);
        assertNextToken("nullpointerexcept", "EXCEPTION", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testExceptionStringGreek() throws Exception
    {
        // Greek uses different characters to English, so the latin letters used in our java Exception will get type "word", instead of "<ALPHANUM>".

        final TokenStream tokenStream = new GreekAnalyzer(LuceneVersion.get()).tokenStream("TestField", new StringReader(
            "Throws java.lang.NullPointerException sometimes."));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        // No stemming for English (its all Greek to me).
        assertNextToken("throws", "<ALPHANUM>", filter);
        // Note that this comes through as "word" (the default type), not <ALPHANUM>.
        assertNextToken("java.lang.nullpointerexception", "<HOST>", filter);
        assertNextToken("java", "EXCEPTION", filter);
        assertNextToken("lang", "EXCEPTION", filter);
        assertNextToken("nullpointerexception", "EXCEPTION", filter);
        assertNextToken("sometimes", "<ALPHANUM>", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testExceptionStringWithTrailingDotInGreek() throws Exception
    {
        final TokenStream tokenStream = new GreekAnalyzer(LuceneVersion.get()).tokenStream("TestField", new StringReader("Throws java.lang.NullPointerException."));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken("throws", "<ALPHANUM>", filter);

        // For JIRA, we would still like to find the "NullPointerException".
        // This was found to not work out of the box for English (JRA-15484), but is fine in Greek.
        assertNextToken("java.lang.nullpointerexception", "<HOST>", filter);

        assertNextToken("java", "EXCEPTION", filter);
        assertNextToken("lang", "EXCEPTION", filter);
        assertNextToken("nullpointerexception", "EXCEPTION", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testExceptionStringWithoutTrailingDotInGreek() throws Exception
    {
        final TokenStream tokenStream = new GreekAnalyzer(LuceneVersion.get()).tokenStream("TestField", new StringReader("Throws java.lang.NullPointerException"));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken("throws", "<ALPHANUM>", filter);

        // For JIRA, we would still like to find the "NullPointerException".
        // This was found to not work out of the box for English (JRA-15484), but is fine in Greek.
        assertNextToken("java.lang.nullpointerexception", "<HOST>", filter);

        // @TODO fix this for 2.9

        assertNextToken("java", "EXCEPTION", filter);
        assertNextToken("lang", "EXCEPTION", filter);
        assertNextToken("nullpointerexception", "EXCEPTION", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testNumberList() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("2,500");
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken("2,500", "<NUM>", filter);
        assertNextToken("2", "<NUM>", filter);
        assertNextToken("500", "<NUM>", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testNumberListInGreek() throws Exception
    {
        final TokenStream tokenStream = new GreekAnalyzer(LuceneVersion.get()).tokenStream("TestField", new StringReader("2,500"));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        // note that the GreekAnalyzer did not tokenize "2,500" as a number in 2.3 and now it does!!!.
        assertNextToken("2,500", "<NUM>", filter);
        assertNextToken("2", "<NUM>", filter);
        assertNextToken("500", "<NUM>", filter);
        assertFalse(filter.incrementToken());
    }

    /**
     * The implementations of tokenizers that we use will not actually create such tokens currently, but our Filter should
     * be able to handle preceding dots, trailing dots, and multiple dots in a row.
     * @throws IOException IOException
     */
    @Test
    public void testTheoreticalEdgeCases() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list("..java..lang..NullPointerException.."));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken("..java..lang..NullPointerException..", "word", filter);
        assertNextToken("java", "EXCEPTION", filter);
        assertNextToken("lang", "EXCEPTION", filter);
        assertNextToken("NullPointerException", "EXCEPTION", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testSingleSubtokenWithDot() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list(".NullPointerException"));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken(".NullPointerException", "word", filter);
        // This is not ideal, but this is historically how it works, and won't happen with the real tokenizers.
        assertNextToken("NullPointerException", "EXCEPTION", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testSingleSubtokenWithTrailingDot() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list("NullPointerException."));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertNextToken("NullPointerException.", "word", filter);
        // This is not ideal, but this is historically how it works, and won't happen with the real tokenizers.
        assertNextToken("NullPointerException", "EXCEPTION", filter);
        assertFalse(filter.incrementToken());
    }

    private void assertNextToken(final String termText, final String type, TokenFilter filter) throws IOException
    {
        assertTrue(filter.incrementToken());
        CharTermAttribute termAttribute = filter.getAttribute(CharTermAttribute.class);
        assertEquals(termText, new String(termAttribute.buffer(), 0, termAttribute.length()));
        TypeAttribute typeAttribute = filter.getAttribute(TypeAttribute.class);
        assertEquals(type, typeAttribute.type());
    }

    private TokenStream getTokenStreamFromEnglishAnalyzer(final String text)
    {
        return new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()).tokenStream("TestField", new StringReader(text));
    }

    private class MockTokenStream extends TokenStream
    {
        private final Iterator<String> iterator;

        private CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
        private TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

        MockTokenStream(final List<String> tokens)
        {
            iterator = tokens.iterator();
        }

        @Override
        public boolean incrementToken() throws IOException
        {
            if (iterator.hasNext())
            {
                String token = iterator.next();
                charTermAttribute.setLength(0).append(token);
                return true;
            }
            else
            {
                // End Of Stream
                return false;
            }
        }
    }
}
