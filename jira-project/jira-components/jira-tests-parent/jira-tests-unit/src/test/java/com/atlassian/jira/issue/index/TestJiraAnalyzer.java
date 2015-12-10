package com.atlassian.jira.issue.index;

import java.io.IOException;
import java.io.StringReader;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.index.LuceneVersion;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestJiraAnalyzer
{
    @Test
    public void testNullPointerFound() throws IOException, ParseException
    {
        assertSearchFoundInAllLanguages("java.lang.NullPointerException", "NullPointerException");
    }

    @Test
    public void testSqlProcessorFound() throws IOException, ParseException
    {
        final String exceptionLine = "at org.ofbiz.core.entity.jdbc.SQLProcessor.executeQuery(SQLProcessor.java:311)";
        assertSearchFoundInAllLanguages(exceptionLine, "SQLProcessor");
        assertSearchFoundInAllLanguages(exceptionLine, "SQLProcessor.java");
        assertSearchFoundInAllLanguages(exceptionLine, "org.ofbiz.core.entity.jdbc.SQLProcessor.executeQuery");
        assertSearchFoundInAllLanguages(exceptionLine, "executeQuery");
        assertSearchFoundInAllLanguages(exceptionLine, exceptionLine);
    }

    /**
     * Ensures that tokens are created for each number present in a comma-separated string.
     *
     * This is a regression test for JRA-7774.
     */
    @Test
    public void shouldTokeniseNumbersGivenTheyAreInACommaSeparatedString() throws IOException, ParseException
    {
        assertSearchFoundInLanguagesWithOurNumberSystem("1602,1712,0000", "1602"); //JRA-7774
        assertSearchFoundInLanguagesWithOurNumberSystem("1602,1712,0000", "1712"); //JRA-7774
        assertSearchFoundInLanguagesWithOurNumberSystem("1602,1712,0000", "0000"); //JRA-7774
        assertSearchFoundInLanguagesWithOurNumberSystem("1602,1712,0000", "1602,1712,0000"); //JRA-7774
        assertSearchFoundInAllLanguages("abc,def,ghi", "def"); //JRA-7774
    }

    /**
     * Ensures that an exception will be tokenised when it end with a full-stop (.) character.
     *
     * <p> e.g. Given an issue description: <tt>throws java.lang.NullPointerException.</tt>, should be tokenised such
     * that a search for <tt>NullPointerException</tt> matches that issue.</p>
     *
     * See JRA-15484.
     */
    @Test
    public void shouldTokeniseExceptionsEndingWithAFullStop() throws IOException, ParseException
    {
        // TODO: Get these other languages working as expected, and add them into the test.
        final String[] fixedLanguages = new String[] {
        APKeys.Languages.BRAZILIAN,
        APKeys.Languages.CHINESE, APKeys.Languages.CJK,
        APKeys.Languages.CZECH,
        APKeys.Languages.DUTCH,
        APKeys.Languages.ENGLISH,
        APKeys.Languages.ENGLISH_MODERATE_STEMMING,
        APKeys.Languages.ENGLISH_MINIMAL_STEMMING,
        APKeys.Languages.FRENCH,
        APKeys.Languages.GERMAN,
        APKeys.Languages.GREEK,
        APKeys.Languages.RUSSIAN,
        APKeys.Languages.THAI,
        APKeys.Languages.OTHER };
        final String exceptionLine = "Throws java.lang.NullPointerException.";

        assertSearchFoundInGivenLanguages(exceptionLine, "NullPointerException", fixedLanguages);
        assertSearchFoundInGivenLanguages(exceptionLine, "java.lang.NullPointerException", fixedLanguages);
        assertSearchFoundInGivenLanguages(exceptionLine, "throws", fixedLanguages);
        assertSearchFoundInGivenLanguages(exceptionLine, "java", fixedLanguages);
        assertSearchFoundInGivenLanguages(exceptionLine, "lang", fixedLanguages);
        assertSearchFoundInGivenLanguages(exceptionLine, exceptionLine, fixedLanguages);
    }

    @Test
    public void testHandlingStopWords() throws IOException, ParseException
    {
        assertSearchFoundInAllLanguages("This is a bug", "This is a bug");
    }

    @Test
    public void testStopWords() throws IOException, ParseException
    {
        assertSearchNotFound(APKeys.Languages.ENGLISH, "The quick brown fox.", "the");
        assertSearchNotFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, "The quick brown fox.", "the");
        assertSearchNotFound(APKeys.Languages.ENGLISH_MINIMAL_STEMMING, "The quick brown fox.", "the");
        assertSearchFound(APKeys.Languages.FRENCH, "The quick brown fox.", "the");

        assertSearchFound(APKeys.Languages.ENGLISH, "Le quick brown fox.", "le");
        assertSearchFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, "Le quick brown fox.", "le");
        assertSearchFound(APKeys.Languages.ENGLISH_MINIMAL_STEMMING, "Le quick brown fox.", "le");
        assertSearchNotFound(APKeys.Languages.FRENCH, "Le quick brown fox.", "le");

        assertSearchFound(APKeys.Languages.ENGLISH, "Der quick brown fox.", "der");
        assertSearchFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, "Der quick brown fox.", "der");
        assertSearchFound(APKeys.Languages.ENGLISH_MINIMAL_STEMMING, "Der quick brown fox.", "der");
        assertSearchNotFound(APKeys.Languages.GERMAN, "Der quick brown fox.", "der");
    }

    @Test
    public void testStemming() throws IOException, ParseException
    {
        // This stemming should work in English using the agressive stemmer
        assertSearchFound(APKeys.Languages.ENGLISH, "The child walked.", "walk");
        assertSearchFound(APKeys.Languages.ENGLISH, "The child will walk.", "walked");

        // This stemming should also work in English using the moderate stemmer
        assertSearchFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, "The child walked.", "walk");
        assertSearchFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, "The child will walk.", "walked");

        // but not in French
        assertSearchNotFound(APKeys.Languages.FRENCH, "The child walked.", "walk");
        assertSearchNotFound(APKeys.Languages.FRENCH, "The child will walk.", "walked");

        // This stemming should work in French
        assertSearchFound(APKeys.Languages.FRENCH, "L'enfant a march\u00e9.", "marchera");
        assertSearchFound(APKeys.Languages.FRENCH, "l'enfant marchera.", "march\u00e9");

        // but not in English using the agressive stemmer
        assertSearchNotFound(APKeys.Languages.ENGLISH, "L'enfant a march\u00e9.", "marchera");
        assertSearchNotFound(APKeys.Languages.ENGLISH, "l'enfant marchera.", "march\u00e9");

        // and not in English using the moderate stemmer
        assertSearchNotFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, "L'enfant a march\u00e9.", "marchera");
        assertSearchNotFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, "l'enfant marchera.", "march\u00e9");
    }

    private void assertSearchFoundInAllLanguages(final String textToSearch, final String searchTerm) throws IOException, ParseException
    {
        assertSearchFound(APKeys.Languages.ARMENIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.BASQUE, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.BULGARIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.BRAZILIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.CATALAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.CHINESE, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.CJK, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.CZECH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.DANISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.DUTCH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ENGLISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ENGLISH_MINIMAL_STEMMING, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.FINNISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.FRENCH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.GERMAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.GREEK, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.HUNGARIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ITALIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.NORWEGIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.PORTUGUESE, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ROMANIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.RUSSIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.SPANISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.SWEDISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.THAI, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.OTHER, textToSearch, searchTerm);
        // unknown language
        assertSearchFound("Klingon", textToSearch, searchTerm);
    }

    private void assertSearchFoundInGivenLanguages(final String textToSearch, final String searchTerm, final String[] languages) throws IOException, ParseException
    {
        for (final String language : languages)
        {
            assertSearchFound(language, textToSearch, searchTerm);
        }
    }

    private void assertSearchFoundInLanguagesWithOurNumberSystem(final String textToSearch, final String searchTerm) throws IOException, ParseException
    {
        assertSearchFound(APKeys.Languages.ARMENIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.BASQUE, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.BULGARIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.BRAZILIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.CATALAN, textToSearch, searchTerm);
//        assertSearchFound(APKeys.Languages.CHINESE, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.CJK, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.CZECH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.DANISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.DUTCH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ENGLISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ENGLISH_MODERATE_STEMMING, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ENGLISH_MINIMAL_STEMMING, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.FINNISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.FRENCH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.GERMAN, textToSearch, searchTerm);
//        assertSearchFound(APKeys.Languages.GREEK, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.HUNGARIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ITALIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.NORWEGIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.PORTUGUESE, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.ROMANIAN, textToSearch, searchTerm);
//        assertSearchFound(APKeys.Languages.RUSSIAN, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.SPANISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.SWEDISH, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.THAI, textToSearch, searchTerm);
        assertSearchFound(APKeys.Languages.OTHER, textToSearch, searchTerm);
        // unknown language
        assertSearchFound("Atlaxian", textToSearch, searchTerm);
    }

    private void assertSearchFound(final String language, final String textToSearch, final String searchTerm) throws IOException, ParseException
    {
        final TopDocs hits = getHitsForSearch(language, textToSearch, searchTerm);
        assertEquals("Search Term '" + searchTerm + "' wasn't found in text '" + textToSearch + "' for the language " + language + ".", 1,
                hits.totalHits);
    }

    private void assertSearchNotFound(final String language, final String textToSearch, final String searchTerm) throws IOException, ParseException
    {
        final TopDocs hits = getHitsForSearch(language, textToSearch, searchTerm);
        assertEquals("Search Term '" + searchTerm + "' was found in '" + textToSearch + "'.", 0, hits.totalHits);
    }

    private TopDocs getHitsForSearch(final String language, final String textToSearch, final String searchTerm) throws IOException, ParseException
    {
        final Analyzer indexingAnalyzer = Mocks.analyser().indexing(true).language(language).build();
        final Analyzer searchingAnalyzer = Mocks.analyser().indexing(false).language(language).build();
        final Directory directory = new RAMDirectory();
        final IndexWriter writer = new IndexWriter(directory, indexingAnalyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);

        final Document doc = new Document();
        doc.add(new Field("term", textToSearch, Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        final IndexSearcher searcher = new IndexSearcher(directory);
        final QueryParser parser = new QueryParser(LuceneVersion.get(), "term", searchingAnalyzer);
        final Query query = parser.parse(searchTerm);

        // This is useful for debugging:
        //        AnalyzerUtils.displayTokensWithFullDetails(indexingAnalyzer, textToSearch);
        //        AnalyzerUtils.displayTokensWithFullDetails(searchingAnalyzer, searchTerm);

        return searcher.search(query, Integer.MAX_VALUE);
    }

    /**
     * This is a regression test for http://jira.atlassian.com/browse/JRA-16239
     */
    @Test
    public void shouldBeAbleToAnalyseIfFrenchIsTheSetToBeTheIndexingLanguage() throws Exception
    {
        final JiraAnalyzer analyzer = Mocks.analyser().indexing(false).language(APKeys.Languages.FRENCH).build();

        assertNotNull(analyzer.tokenStream(null, new StringReader("this is a token string")));
    }

    private static class Mocks
    {
        private static AnalyzerBuilder analyser()
        {
            return new AnalyzerBuilder();
        }

        private static class AnalyzerBuilder
        {
            private String language;
            private boolean indexing;

            private AnalyzerBuilder language(String language)
            {
                this.language = language;
                return this;
            }

            private AnalyzerBuilder indexing(boolean indexing)
            {
                this.indexing = indexing;
                return this;
            }

            private JiraAnalyzer build()
            {
                return new JiraAnalyzer(indexing, JiraAnalyzer.Stemming.ON, JiraAnalyzer.StopWordRemoval.ON)
                {
                    @Override
                    String getLanguage()
                    {
                        return language;
                    }
                };
            }
        }
    }
}
