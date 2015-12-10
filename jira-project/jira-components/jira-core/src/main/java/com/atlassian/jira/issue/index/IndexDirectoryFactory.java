package com.atlassian.jira.issue.index;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesUtil;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexWriterConfiguration;
import com.atlassian.jira.index.Configuration;
import com.atlassian.jira.index.DefaultConfiguration;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.Manager;
import com.atlassian.jira.index.Indexes;
import com.atlassian.jira.util.LuceneDirectoryUtils;
import com.atlassian.jira.util.Supplier;

import org.apache.lucene.store.Directory;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Responsible for creating the {@link Directory directories} required for issue and comment indexing.
 *
 * @since v4.0
 */
public interface IndexDirectoryFactory extends Supplier<Map<IndexDirectoryFactory.Name, Index.Manager>>
{
    enum Mode
    {
        DIRECT
        {
            @Override
            Manager createIndexManager(final String name, final Configuration configuration, final ApplicationProperties applicationProperties)
            {
                return Indexes.createSimpleIndexManager(configuration);
            }
        },
        QUEUED
        {
            @Override
            Manager createIndexManager(final String name, final Configuration configuration, final ApplicationProperties applicationProperties)
            {
                int maxQueueSize = PropertiesUtil.getIntProperty(applicationProperties, APKeys.JiraIndexConfiguration.Issue.MAX_QUEUE_SIZE, 1000);
                return Indexes.createQueuedIndexManager(name, configuration, maxQueueSize);
            }
        };

        abstract Index.Manager createIndexManager(String name, Configuration configuration, ApplicationProperties applicationProperties);
    }

    enum Name
    {
        COMMENT
        {
            @Override
            @Nonnull
            String getPath(final IndexPathManager indexPathManager)
            {
                return verify(indexPathManager, indexPathManager.getCommentIndexPath());
            }
        },

        ISSUE
        {
            @Override
            @Nonnull
            String getPath(final IndexPathManager indexPathManager)
            {
                return verify(indexPathManager, indexPathManager.getIssueIndexPath());
            }
        },

        CHANGE_HISTORY
        {
            @Override
            @Nonnull
            String getPath(final IndexPathManager indexPathManager)
            {
                return verify(indexPathManager, indexPathManager.getChangeHistoryIndexPath());
            }
        };

        final @Nonnull
        Directory directory(@Nonnull final IndexPathManager indexPathManager)
        {
            LuceneDirectoryUtils luceneDirectoryUtils = ComponentAccessor.getComponent(LuceneDirectoryUtils.class);
            return luceneDirectoryUtils.getDirectory(new File(getPath(indexPathManager)));
        }

        final @Nonnull
        String verify(final IndexPathManager indexPathManager, final String path) throws IllegalStateException
        {
            if (indexPathManager.getMode() == IndexPathManager.Mode.DISABLED)
            {
                throw new IllegalStateException("Indexing is disabled.");
            }
            return notNull("Index path is null: " + this, path);
        }

        abstract @Nonnull
        String getPath(@Nonnull IndexPathManager indexPathManager);
    }

    String getIndexRootPath();

    List<String> getIndexPaths();

    /**
     * Sets the Indexing Mode - one of either DIRECT or QUEUED.
     *
     * @param mode the indexing mode.
     */
    void setIndexingMode(@Nonnull Mode mode);

    class IndexPathAdapter implements IndexDirectoryFactory
    {
        private final IndexPathManager indexPathManager;
        private final IndexWriterConfiguration writerConfiguration;
        private final ApplicationProperties applicationProperties;
        private volatile Mode strategy = Mode.QUEUED;

        public IndexPathAdapter(final @Nonnull IndexPathManager indexPathManager, final IndexWriterConfiguration writerConfiguration, final ApplicationProperties applicationProperties)
        {
            this.applicationProperties = applicationProperties;
            this.indexPathManager = notNull("indexPathManager", indexPathManager);
            this.writerConfiguration = notNull("writerConfiguration", writerConfiguration);
        }

        public Map<Name, Index.Manager> get()
        {
            final Mode strategy = this.strategy;
            final EnumMap<Name, Index.Manager> indexes = new EnumMap<Name, Index.Manager>(Name.class);
            for (final Name type : Name.values())
            {
                indexes.put(type, strategy.createIndexManager(type.name(), new DefaultConfiguration(type.directory(indexPathManager),
                    IssueIndexer.Analyzers.INDEXING, writerConfiguration), applicationProperties));
            }
            return Collections.unmodifiableMap(indexes);
        }

        public String getIndexRootPath()
        {
            return indexPathManager.getIndexRootPath();
        }

        public List<String> getIndexPaths()
        {
            final List<String> result = new ArrayList<String>(Name.values().length);
            for (final Name indexType : Name.values())
            {
                try
                {
                    result.add(indexType.getPath(indexPathManager));
                }
                catch (final RuntimeException ignore)
                {
                    //probable not setup
                }
            }
            return Collections.unmodifiableList(result);
        }

        public void setIndexingMode(final Mode strategy)
        {
            this.strategy = strategy;
        }
    }
}
