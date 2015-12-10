package com.atlassian.jira.config.util;

import javax.annotation.Nonnull;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.index.LuceneVersion;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TieredMergePolicy;

import static com.atlassian.jira.config.properties.PropertiesUtil.getBooleanProperty;
import static com.atlassian.jira.config.properties.PropertiesUtil.getIntProperty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Controls how the Lucene IndexWriter will be set up.
 *
 * @since v4.0
 */
public interface IndexWriterConfiguration
{
    static final class Default
    {
        // use the Lucene IndexWriter default for this, as the default inside ILuceneConnection.DEFAULT_CONFIGURATION is HUGE!!!
        static final int MAX_FIELD_LENGTH = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;

        public static final WriterSettings BATCH = new WriterSettings()
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
                return MAX_FIELD_LENGTH;
            }
        };

        public static final WriterSettings INTERACTIVE = new WriterSettings()
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
                return MAX_FIELD_LENGTH;
            }
        };
    }

    WriterSettings getInteractiveSettings();

    WriterSettings getBatchSettings();

    public abstract class WriterSettings
    {
        public IndexWriterConfig getWriterConfiguration(Analyzer analyser)
        {
            final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();

            final TieredMergePolicy mergePolicy = new TieredMergePolicy();
            mergePolicy.setExpungeDeletesPctAllowed(getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.MergePolicy.EXPUNGE_DELETES_PCT_ALLOWED, 10));
            mergePolicy.setFloorSegmentMB(getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.MergePolicy.FLOOR_SEGMENT_MB, 2));
            mergePolicy.setMaxMergedSegmentMB(getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.MergePolicy.MAX_MERGED_SEGMENT_MB, 512));
            mergePolicy.setMaxMergeAtOnce(getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.MergePolicy.MAX_MERGE_AT_ONCE, 10));
            mergePolicy.setMaxMergeAtOnceExplicit(getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.MergePolicy.MAX_MERGE_AT_ONCE_EXPLICIT, 30));
            mergePolicy.setNoCFSRatio(getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.MergePolicy.NO_CFS_PCT, 10) / 100.0);
            mergePolicy.setSegmentsPerTier(getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.MergePolicy.SEGMENTS_PER_TIER, 10));
            mergePolicy.setUseCompoundFile(getBooleanProperty(applicationProperties, APKeys.JiraIndexConfiguration.MergePolicy.USE_COMPOUND_FILE, true));

            final IndexWriterConfig luceneConfig = new IndexWriterConfig(LuceneVersion.get(), analyser);
            luceneConfig.setMergePolicy(mergePolicy);
            luceneConfig.setMaxBufferedDocs(getMaxBufferedDocs());
            return luceneConfig;
        }

        public abstract int getMaxBufferedDocs();

        /** @deprecated Only applies to LogMergePolicy. */
        public abstract int getMergeFactor();

        /** @deprecated Only applies to LogMergePolicy. */
        public abstract int getMaxMergeDocs();

        /** @deprecated Not really relevant for Lucene 3.2+ . */
        public abstract int getMaxFieldLength();
    }



    public static class PropertiesAdaptor implements IndexWriterConfiguration
    {
        private final ApplicationProperties properties;

        public PropertiesAdaptor(final @Nonnull ApplicationProperties properties)
        {
            this.properties = notNull("properties", properties);
        }

        private final WriterSettings batch = new WriterSettings()
        {
            public int getMaxBufferedDocs()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Batch.MAX_BUFFERED_DOCS, Default.BATCH.getMaxBufferedDocs());
            }

            public int getMergeFactor()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Batch.MERGE_FACTOR, Default.BATCH.getMergeFactor());
            }

            public int getMaxMergeDocs()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Batch.MAX_MERGE_DOCS, Default.BATCH.getMaxMergeDocs());
            }

            public int getMaxFieldLength()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.MAX_FIELD_LENGTH, Default.INTERACTIVE.getMaxFieldLength());
            }
        };

        private final WriterSettings interactive = new WriterSettings()
        {
            public int getMergeFactor()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Interactive.MERGE_FACTOR, Default.INTERACTIVE.getMergeFactor());
            }

            public int getMaxMergeDocs()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Interactive.MAX_MERGE_DOCS, Default.INTERACTIVE.getMaxMergeDocs());
            }

            public int getMaxBufferedDocs()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.Interactive.MAX_BUFFERED_DOCS,
                    Default.INTERACTIVE.getMaxBufferedDocs());
            }

            public int getMaxFieldLength()
            {
                return getIntProperty(properties, APKeys.JiraIndexConfiguration.MAX_FIELD_LENGTH, Default.INTERACTIVE.getMaxFieldLength());
            }

        };

        public WriterSettings getBatchSettings()
        {
            return batch;
        }

        public WriterSettings getInteractiveSettings()
        {
            return interactive;
        }
    }
}
