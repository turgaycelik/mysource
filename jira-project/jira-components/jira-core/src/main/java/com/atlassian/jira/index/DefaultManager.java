package com.atlassian.jira.index;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.Index.Manager;
import com.atlassian.jira.util.RuntimeIOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.compress.utils.IOUtils.closeQuietly;

class DefaultManager implements Manager
{
    private final Configuration configuration;
    private final DefaultIndex.Engine actor;
    private final CloseableIndex index;

    DefaultManager(final @Nonnull Configuration configuration, final @Nonnull DefaultIndex.Engine actor, final @Nonnull CloseableIndex index)
    {
        this.configuration = notNull("configuration", configuration);
        this.actor = notNull("actor", actor);
        this.index = notNull("index", index);
    }

    @Nonnull
    public Index getIndex()
    {
        return index;
    }

    public int getNumDocs()
    {
        final IndexSearcher searcher = openSearcher();
        try
        {
            return searcher.getIndexReader().numDocs();
        }
        finally
        {
            closeQuietly(searcher);
        }
    }

    @Nonnull
    public IndexSearcher openSearcher()
    {
        return actor.getSearcher();
    }

    public void deleteIndexDirectory()
    {
        actor.clean();
    }

    public void close()
    {
        index.close();
    }

    public boolean isIndexCreated()
    {
        try
        {
            return IndexReader.indexExists(configuration.getDirectory());
        }
        catch (final IOException e)
        {
            ///CLOVER:OFF
            throw new RuntimeIOException(e);
            ///CLOVER:ON
        }
    }
}
