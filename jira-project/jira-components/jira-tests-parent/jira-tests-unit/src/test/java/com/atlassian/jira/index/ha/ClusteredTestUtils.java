package com.atlassian.jira.index.ha;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.index.analyzer.EnglishAnalyzer;
import com.atlassian.jira.issue.index.analyzer.TokenFilters;

import com.google.common.collect.Lists;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Utility methods to help test index copy/move/clear operations
 *
 * @since v5.2
 */
public class ClusteredTestUtils
{
    public static final Analyzer ENGLISH_ANALYZER = new EnglishAnalyzer
            (
                    LuceneVersion.get(), true, TokenFilters.English.Stemming.moderate(),
                    TokenFilters.English.StopWordRemoval.defaultSet()
            );

    public static void populateIndex(Analyzer analyzerForIndexing, Directory indexDir, List<Document> documents) throws Exception
    {
        final IndexWriterConfig indexWriterConfig = getIndexWriterConfig(analyzerForIndexing);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter writer = null;
        try
        {
            writer = new IndexWriter(indexDir, indexWriterConfig);
            for (Document doc : documents)
            {
                writer.addDocument(doc);
            }
        }
        finally
        {
            if (writer != null)
            {
                writer.close();
            }
        }
    }

    public static Document buildDocument(final Map<String, String> fieldValues)
    {
        final Document doc = new Document();
        for (Map.Entry<String, String> entry : fieldValues.entrySet())
        {
            doc.add(new Field(entry.getKey(), entry.getValue(), Field.Store.YES, Field.Index.ANALYZED));
        }
        return doc;
    }

    public static Directory createIndexDirectory(File indexDirectory) throws Exception
    {
        IndexWriterConfig conf = getIndexWriterConfig(ENGLISH_ANALYZER);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        final FSDirectory fsDirectory = FSDirectory.open(indexDirectory);
        new IndexWriter(fsDirectory, conf).close();
        return fsDirectory;
    }

    private static IndexWriterConfig getIndexWriterConfig(Analyzer analyzer)
    {
        return new IndexWriterConfig(IssueIndexManager.LUCENE_VERSION, analyzer);
    }

    public static List<Document> getDocuments(File indexDirectory) throws Exception
    {
        final List<Document> documents = Lists.newArrayList();
        final Directory indexDir = createIndexDirectory(indexDirectory);
        IndexReader reader = null;
        try
        {
            reader = IndexReader.open(indexDir);
            for (int i=0; i<reader.maxDoc(); i++)
            {
                documents.add(reader.document(i));
            }
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
        return documents;
    }

    public static void clearDirectory(File directory) throws Exception
    {
        final Directory indexDir = createIndexDirectory(directory);
        new IndexWriter(indexDir, getIndexWriterConfig(ENGLISH_ANALYZER)).deleteAll();
    }
}
