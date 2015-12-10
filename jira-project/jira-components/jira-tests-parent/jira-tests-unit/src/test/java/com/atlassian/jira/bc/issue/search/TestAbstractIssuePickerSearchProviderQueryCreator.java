package com.atlassian.jira.bc.issue.search;

import java.util.Collection;

import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.index.analyzer.EnglishAnalyzer;
import com.atlassian.jira.issue.index.analyzer.TokenFilters;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestAbstractIssuePickerSearchProviderQueryCreator
{
    Analyzer searchAnalyzer;
    Analyzer keywordAnalyzer;

    @Before
    public void setUp() throws Exception
    {
        keywordAnalyzer = new WhitespaceAnalyzer(LuceneVersion.get());
        searchAnalyzer =
                new EnglishAnalyzer
                        (
                                LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(),
                                TokenFilters.English.StopWordRemoval.defaultSet()
                        ); // DefaultIndexManager.ANALYZER_FOR_SEARCHING cant be used as its DB dependant
    }

    @After
    public void tearDown() throws Exception
    {
        keywordAnalyzer = null;
        searchAnalyzer = null;
    }

    @Test
    public void testWildCardQueryCreationEmptyTerm()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getWildCardQueryCreator("", "key");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(0, terms.size());

    }

    @Test
    public void testWildCardQueryCreationNullTerm()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getWildCardQueryCreator(null, "key");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(0, terms.size());

    }

    @Test
    public void testWildCardQueryCreationSingleTerm()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getWildCardQueryCreator("web", "key");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("key:*web*", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(1, terms.size());
        final String term = terms.iterator().next();

        assertEquals("web", term);

    }

    @Test
    public void testWildCardQueryCreationSingleNumberTerm()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getWildCardQueryCreator("123", "key");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("key:*-123^1.5", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(1, terms.size());
        final String term = terms.iterator().next();

        assertEquals("123", term);

    }

    @Test
    public void testWildCardQueryCreationMultiNumberTerms()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getWildCardQueryCreator("123 1234", "key");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("key:*-123^1.5 key:*-1234^1.5", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(2, terms.size());

        assertTrue(terms.contains("123"));
        assertTrue(terms.contains("1234"));

    }

    @Test
    public void testWildCardQueryCreationMixedTerms()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getWildCardQueryCreator("123 abc 1234", "key");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("key:*-123^1.5 key:*abc* key:*-1234^1.5", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(3, terms.size());

        assertTrue(terms.contains("123"));
        assertTrue(terms.contains("1234"));
        assertTrue(terms.contains("abc"));

    }

    @Test
    public void testWildCardQueryCreation()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getWildCardQueryCreator("web search", "key");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("key:*web* key:*search*", query.toString());
    }

    @Test
    public void testPrefixQueryCreation()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getPrefixQueryCreator("web search", "summary");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("summary:web* summary:search*", query.toString());
    }

    @Test
    public void testPrefixCardQueryCreationNullTerm()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getPrefixQueryCreator(null, "summary");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(0, terms.size());

    }

    @Test
    public void testPrefixQueryCreationSingleTerm()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getPrefixQueryCreator("web", "summary");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("summary:web*", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(1, terms.size());
        final String term = terms.iterator().next();

        assertEquals("web", term);

    }

    @Test
    public void testPrefixQueryCreationSingleNumberTerm()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getPrefixQueryCreator("123", "summary");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("summary:123*", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(1, terms.size());
        final String term = terms.iterator().next();

        assertEquals("123", term);

    }

    @Test
    public void testPrefixQueryCreationMultiNumberTerms()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getPrefixQueryCreator("123 1234", "summary");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("summary:123* summary:1234*", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(2, terms.size());

        assertTrue(terms.contains("123"));
        assertTrue(terms.contains("1234"));

    }

    @Test
    public void testPrefixQueryCreationMixedTerms()
    {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getPrefixQueryCreator("123 abc 1234", "blarg");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("blarg:123* blarg:abc* blarg:1234*", query.toString());

        final Collection<String> terms = queryCreator.getTokens();

        assertNotNull(terms);
        assertEquals(3, terms.size());

        assertTrue(terms.contains("123"));
        assertTrue(terms.contains("1234"));
        assertTrue(terms.contains("abc"));

    }

    @Test
    public void testPrefixQueryNullField()
    {
        try
        {
            getPrefixQueryCreator("123 abc 1234", null);
            fail("A null field name should throw an IllegalArgumentException");
        }
        catch (final Exception ignore)
        {}
    }

    @Test
    public void testConstantScorePrefixSubQueryCreation() {
        final AbstractIssuePickerSearchProvider.QueryCreator queryCreator = getConstantScorePrefixQueryCreator("web search", "summary");
        final Query query = queryCreator.getQuery();

        assertNotNull(query);
        assertEquals("summary:web* summary:search*", query.toString());
    }

    private AbstractIssuePickerSearchProvider.QueryCreator getWildCardQueryCreator(final String queryString, final String fieldName)
    {
        return new AbstractIssuePickerSearchProvider.QueryCreator(queryString, fieldName, keywordAnalyzer, new AbstractIssuePickerSearchProvider.WildCardSubQuery());
    }

    private AbstractIssuePickerSearchProvider.QueryCreator getPrefixQueryCreator(final String queryString, final String fieldName)
    {
        return new AbstractIssuePickerSearchProvider.QueryCreator(queryString, fieldName, searchAnalyzer, new AbstractIssuePickerSearchProvider.PrefixSubQuery());
    }

    private AbstractIssuePickerSearchProvider.QueryCreator getConstantScorePrefixQueryCreator(final String queryString, final String fieldName)
    {
        return new AbstractIssuePickerSearchProvider.QueryCreator(queryString,fieldName,keywordAnalyzer, new AbstractIssuePickerSearchProvider.ConstantScorePrefixSubQuery());
    }
}
