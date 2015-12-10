package com.atlassian.jira.util.index;

import com.atlassian.jira.task.context.Context;

import java.util.Collection;
import java.util.Collections;

public class MockIndexLifecycleManager implements IndexLifecycleManager
{

    public long activate(final Context ctx)
    {
        return 0;
    }

    @Override
    public long activate(final Context context, final boolean reindex)
    {
        return 0;
    }

    public void deactivate()
    {}

    public Collection<String> getAllIndexPaths()
    {
        return Collections.emptyList();
    }

    public boolean isIndexingEnabled()
    {
        return true;
    }

    @Override
    public boolean isIndexAvailable()
    {
        return true;
    }

    public long optimize()
    {
        return 0;
    }

    public long reIndexAll(final Context ctx)
    {
        return 0;
    }

    @Override
    public long reIndexAllIssuesInBackground(Context context)
    {
        return 0;
    }

    @Override
    public long reIndexAllIssuesInBackground(final Context context, final boolean reIndexComments, final boolean reIndexChangeHistory)
    {
        return 0;
    }

    public int size()
    {
        return 0;
    }

    public boolean isEmpty()
    {
        return true;
    }

    public boolean isIndexConsistent()
    {
        return true;
    }

    public void shutdown()
    {}
}
