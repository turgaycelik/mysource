package com.atlassian.jira.issue.index.analyzer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

import static com.atlassian.jira.util.collect.CollectionBuilder.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.3
 */
public class TestWildcardFilter
{

    @Test
    public void testSingleToken() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list("Bugs"));
        final WildcardFilter filter = new WildcardFilter(tokenStream);
        assertNextToken("Bugs*", "word", filter);
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testMultipleTokens() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list("Bugs","Features"));
        final WildcardFilter filter = new WildcardFilter(tokenStream);
        assertNextToken("Bugs*", "word", filter);
        assertNextToken("Features*", "word", filter);
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
