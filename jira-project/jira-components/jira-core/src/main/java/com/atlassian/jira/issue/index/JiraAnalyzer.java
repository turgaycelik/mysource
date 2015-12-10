package com.atlassian.jira.issue.index;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ExecutionException;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.index.analyzer.BrazilianAnalyzer;
import com.atlassian.jira.issue.index.analyzer.BulgarianAnalyzer;
import com.atlassian.jira.issue.index.analyzer.CJKAnalyzer;
import com.atlassian.jira.issue.index.analyzer.CzechAnalyzer;
import com.atlassian.jira.issue.index.analyzer.EnglishAnalyzer;
import com.atlassian.jira.issue.index.analyzer.FrenchAnalyzer;
import com.atlassian.jira.issue.index.analyzer.GermanAnalyzer;
import com.atlassian.jira.issue.index.analyzer.GreekAnalyzer;
import com.atlassian.jira.issue.index.analyzer.ItalianAnalyzer;
import com.atlassian.jira.issue.index.analyzer.SimpleAnalyzer;
import com.atlassian.jira.issue.index.analyzer.StemmingAnalyzer;
import com.atlassian.jira.issue.index.analyzer.ThaiAnalyzer;
import com.atlassian.jira.issue.index.analyzer.TokenFilters;
import com.atlassian.jira.issue.index.indexers.phrase.PhraseQuerySupportField;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import static com.atlassian.jira.issue.index.indexers.phrase.PhraseQuerySupportField.isPhraseQuerySupportField;

public class JiraAnalyzer extends Analyzer
{

    private final boolean indexing;
    private final Stemming stemming;
    private final StopWordRemoval stopWordRemoval;

    public enum Stemming
    {
        ON, OFF
    }

    public enum StopWordRemoval
    {
        ON, OFF
    }

    private static final Logger log = Logger.getLogger(JiraAnalyzer.class);

    public static final Analyzer ANALYZER_FOR_INDEXING = new PerFieldIndexingAnalyzer();

    public static final Analyzer ANALYZER_FOR_SEARCHING = new JiraAnalyzer(false, JiraAnalyzer.Stemming.ON, StopWordRemoval.ON);

    public static final Analyzer ANALYZER_FOR_EXACT_SEARCHING = new JiraAnalyzer(false, JiraAnalyzer.Stemming.OFF, StopWordRemoval.OFF);

    private final Cache<String, Analyzer> analyzers = CacheBuilder.newBuilder().build(new CacheLoader<String, Analyzer>()
    {
        @Override
        public Analyzer load(final String key) throws Exception
        {
            return makeAnalyzer(key);
        }
    });

    private final Analyzer fallbackAnalyzer;


    public JiraAnalyzer(final boolean indexing, final Stemming stemming, final StopWordRemoval stopWordRemoval)
    {
        this.indexing = indexing;
        this.stemming = stemming;
        this.stopWordRemoval = stopWordRemoval;
        fallbackAnalyzer = new SimpleAnalyzer(LuceneVersion.get(), this.indexing);
    }
      
    Analyzer makeAnalyzer(String language)
    {
        if (language.equals(APKeys.Languages.ARMENIAN))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Armenian.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Armenian.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals( APKeys.Languages.BASQUE))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Basque.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Basque.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.BULGARIAN))
        {
            return new BulgarianAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Bulgarian.Stemming.standard()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Bulgarian.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.BRAZILIAN))
        {
            return new BrazilianAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Portuguese.Brazil.Stemming.standard()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Portuguese.Brazil.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.CATALAN))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Catalan.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Catalan.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.CHINESE))
        {
            return new SimpleAnalyzer(LuceneVersion.get(), indexing);
        }
        if (language.equals(APKeys.Languages.CJK))
        {
            return new CJKAnalyzer
                    (
                            indexing,
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.CJK.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.CZECH))
        {
            return new CzechAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Czech.Stemming.standard()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Czech.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.DANISH))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Danish.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Danish.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.DUTCH))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Dutch.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Dutch.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.ENGLISH))
        {
            return new EnglishAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.English.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.English.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.ENGLISH_MODERATE_STEMMING))
        {
            return new EnglishAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.English.Stemming.moderate()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.English.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.ENGLISH_MINIMAL_STEMMING))
        {
            return new EnglishAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.English.Stemming.minimal()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.English.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.FINNISH))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Finnish.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Finnish.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.FRENCH))
        {
            return new FrenchAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.French.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.French.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.GERMAN))
        {
            return new GermanAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.German.Stemming.standard()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.German.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.GREEK))
        {
            return new GreekAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Greek.Stemming.standard()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Greek.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.HUNGARIAN))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Hungarian.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Hungarian.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.ITALIAN))
        {
            return new ItalianAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Italian.Stemming.agressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Italian.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.NORWEGIAN))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Norwegian.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Norwegian.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.PORTUGUESE))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Portuguese.Portugal.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Portuguese.Portugal.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.ROMANIAN))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Romanian.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Romanian.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.RUSSIAN))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Russian.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Russian.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.SPANISH))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Spanish.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Spanish.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.SWEDISH))
        {
            return new StemmingAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stemming == Stemming.ON
                                    ? TokenFilters.Swedish.Stemming.aggressive()
                                    : TokenFilters.General.Stemming.none(),
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Swedish.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        if (language.equals(APKeys.Languages.THAI))
        {
            return new ThaiAnalyzer
                    (
                            LuceneVersion.get(), indexing,
                            stopWordRemoval == StopWordRemoval.ON
                                    ? TokenFilters.Thai.StopWordRemoval.defaultSet()
                                    : TokenFilters.General.StopWordRemoval.none()
                    );
        }
        // special case
        if (language.equals(APKeys.Languages.OTHER))
        {
            return fallbackAnalyzer;
        }
        // Deep fallback
        return fallbackAnalyzer;
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public final TokenStream tokenStream(String fieldname, final Reader reader)
    {
        // workaround for https://issues.apache.org/jira/browse/LUCENE-1359
        // reported here: http://jira.atlassian.com/browse/JRA-16239
        if (fieldname == null)
        {
            fieldname = "";
        }
        // end workaround
        return findAnalyzer().tokenStream(fieldname, reader);
    }

    /*
     * We do this because Lucene insists we subclass this and make it final.
     */
    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
    {
        return super.reusableTokenStream(fieldName, reader);
    }

    private Analyzer findAnalyzer()
    {
        final String language = getLanguage();
        if (language == null)
        {
            return fallbackAnalyzer;
        }
        Analyzer analyzer = null;
        try
        {
            analyzer = analyzers.get(language);
        }
        catch (ExecutionException e)
        {
            log.error("Invalid indexing language: '" + language + "', defaulting to '" + APKeys.Languages.OTHER + "'.");
            analyzer = fallbackAnalyzer;
        }
        if (analyzer == null)
        {
            log.error("Invalid indexing language: '" + language + "', defaulting to '" + APKeys.Languages.OTHER + "'.");
            analyzer = fallbackAnalyzer;
        }
        return analyzer;
    }

    String getLanguage()
    {
        return ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_I18N_LANGUAGE_INPUT);
    }

    /**
     * An {@link Analyzer analyzer} that delegates analysis tasks to the appropriate {@link JiraAnalyzer jira analyzer}
     * instance depending on whether it is a standard text field or a phrase query support text field.
     *
     * @see PhraseQuerySupportField
     */
    private static class PerFieldIndexingAnalyzer extends Analyzer
    {
        private final Analyzer PHRASE_QUERY_SUPPORT_TEXT_FIELD_ANALYZER = new JiraAnalyzer(true, Stemming.OFF, StopWordRemoval.OFF);
        private final Analyzer TEXT_FIELD_INDEXING_ANALYZER = new JiraAnalyzer(true, Stemming.ON, StopWordRemoval.ON);

        @Override
        public final TokenStream tokenStream(final String fieldName, final Reader reader)
        {
            if (isPhraseQuerySupportField(fieldName))
            {
                return PHRASE_QUERY_SUPPORT_TEXT_FIELD_ANALYZER.tokenStream(fieldName, reader);
            }
            else
            {
                return TEXT_FIELD_INDEXING_ANALYZER.tokenStream(fieldName, reader);
            }
        }

        @Override
        public final TokenStream reusableTokenStream(final String fieldName, final Reader reader) throws IOException
        {
            if (isPhraseQuerySupportField(fieldName))
            {
                return PHRASE_QUERY_SUPPORT_TEXT_FIELD_ANALYZER.reusableTokenStream(fieldName, reader);
            }
            else
            {
                return TEXT_FIELD_INDEXING_ANALYZER.reusableTokenStream(fieldName, reader);
            }
        }
    }
}
