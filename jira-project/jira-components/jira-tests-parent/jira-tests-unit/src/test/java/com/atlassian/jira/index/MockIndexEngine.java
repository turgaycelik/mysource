package com.atlassian.jira.index;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.Index.Operation;

import org.apache.lucene.search.IndexSearcher;

///CLOVER:OFF
class MockIndexEngine implements DefaultIndex.Engine
{
    public void clean()
    {
        throw new UnsupportedOperationException();
    }

    public void close()
    {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public IndexSearcher getSearcher()
    {
        throw new UnsupportedOperationException();
    }

    public void write(@Nonnull final Operation operation) throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
///CLOVER:ON

