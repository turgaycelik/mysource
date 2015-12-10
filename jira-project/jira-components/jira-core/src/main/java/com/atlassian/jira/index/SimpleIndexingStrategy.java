package com.atlassian.jira.index;

import com.atlassian.jira.util.Supplier;

public class SimpleIndexingStrategy implements IndexingStrategy
{
    public Index.Result get(final Supplier<Index.Result> input)
    {
        return input.get();
    }

    public void close()
    {}
}
