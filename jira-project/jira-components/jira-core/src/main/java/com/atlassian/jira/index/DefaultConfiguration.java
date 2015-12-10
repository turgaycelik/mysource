package com.atlassian.jira.index;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.util.IndexWriterConfiguration;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultConfiguration implements Configuration
{
    private static final class Default
    {
        /**
         * 1million (the lucene default is 10,000).
         * at (say) 10chars per token, that is a 10meg limit. Fair enough.
         * [from Kelvin Tan]
         */
        private static final int maxFieldLength = 1000000;

        private static final IndexWriterConfiguration.WriterSettings interactiveWriterSettings = new IndexWriterConfiguration.WriterSettings()
        {
            public int getMergeFactor()
            {
                return 4;
            }

            public int getMaxMergeDocs()
            {
                return 5000;
            }

            public int getMaxBufferedDocs()
            {
                return 300;
            }

            public int getMaxFieldLength()
            {
                return maxFieldLength;
            }
        };

        private static final IndexWriterConfiguration.WriterSettings batchWriterSettings = new IndexWriterConfiguration.WriterSettings()
        {
            public int getMergeFactor()
            {
                return 50;
            }

            public int getMaxMergeDocs()
            {
                return Integer.MAX_VALUE;
            }

            public int getMaxBufferedDocs()
            {
                return 300;
            }

            public int getMaxFieldLength()
            {
                return maxFieldLength;
            }
        };

        private static final IndexWriterConfiguration writerConfiguration = new IndexWriterConfiguration()
        {
            public WriterSettings getInteractiveSettings()
            {
                return interactiveWriterSettings;
            }

            public WriterSettings getBatchSettings()
            {
                return batchWriterSettings;
            }
        };
    }

    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriterConfiguration writerConfiguration;

    public DefaultConfiguration(final @Nonnull Directory directory, final @Nonnull Analyzer analyzer)
    {
        this(directory, analyzer, Default.writerConfiguration);
    }

    public DefaultConfiguration(final @Nonnull Directory directory, final @Nonnull Analyzer analyzer, final @Nonnull IndexWriterConfiguration writerConfiguration)
    {
        this.directory = notNull("directory", directory);
        this.analyzer = notNull("analyzer", analyzer);
        this.writerConfiguration = notNull("writerConfiguration", writerConfiguration);
    }

    public Directory getDirectory()
    {
        return directory;
    }

    public Analyzer getAnalyzer()
    {
        return analyzer;
    }

    public IndexWriterConfiguration.WriterSettings getWriterSettings(final Index.UpdateMode mode)
    {
        return mode.getWriterSettings(writerConfiguration);
    }
}
