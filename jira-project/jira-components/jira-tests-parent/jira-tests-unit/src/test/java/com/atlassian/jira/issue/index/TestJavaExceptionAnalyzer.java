package com.atlassian.jira.issue.index;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJavaExceptionAnalyzer
{
    @Test
    public void testTokenStream() throws Exception
    {
        final AtomicBoolean tokenStreamCalled = new AtomicBoolean(false);
        final Analyzer mockAnalyzer = new Analyzer()
        {
            @Override
            public TokenStream tokenStream(final String fieldName, final Reader reader)
            {
                tokenStreamCalled.set(true);
                return new MockTokenStream(Collections.singletonList("java.lang.NullPointerException"));
            }
        };
        final JavaExceptionAnalyzer analyzer = new JavaExceptionAnalyzer(mockAnalyzer);

        final TokenStream tokenStream = analyzer.tokenStream(null, null);
        assertNextToken("java.lang.NullPointerException", tokenStream);
        assertNextToken("java", tokenStream);
        assertNextToken("lang", tokenStream);
        assertNextToken("NullPointerException", tokenStream);
        assertFalse(tokenStream.incrementToken());

    }

    private void assertNextToken(final String termText, TokenStream stream) throws IOException
    {
        assertTrue(stream.incrementToken());
        CharTermAttribute termAttribute = stream.getAttribute(CharTermAttribute.class);
        assertEquals(termText, new String(termAttribute.buffer(), 0, termAttribute.length()));
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

    @Test
    public void testGetPositionIncrementGap() throws Exception
    {
        final AtomicBoolean getPositionIncrementGapCalled = new AtomicBoolean(false);
        final Analyzer mockAnalyzer = new Analyzer()
        {
            @Override
            public TokenStream tokenStream(final String fieldName, final Reader reader)
            {
                return null;
            }

            @Override
            public int getPositionIncrementGap(final String fieldName)
            {
                getPositionIncrementGapCalled.set(true);
                return 123;
            }
        };
        final JavaExceptionAnalyzer analyzer = new JavaExceptionAnalyzer(mockAnalyzer);

        assertEquals(123, analyzer.getPositionIncrementGap(null));
        assertTrue(getPositionIncrementGapCalled.get());

    }

}
