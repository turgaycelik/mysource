package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forWatches;

public class WatchesIndexer extends BaseFieldIndexer
{
    private final WatcherManager watcherManager;

    public WatchesIndexer(final FieldVisibilityManager fieldVisibilityManager, final WatcherManager watcherManager)
    {
        super(fieldVisibilityManager);
        this.watcherManager = watcherManager;
    }

    public String getId()
    {
        return forWatches().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return forWatches().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        // You can not hide the Watches via the FieldConfiguration so we only need to check if it is enabled or disabled
        return watcherManager.isWatchingEnabled();
    }

    public void addIndex(final Document doc, final Issue issue)
    {
        indexLongAsPaddedKeywordWithDefault(doc, getDocumentFieldId(), issue.getWatches(), 0L, issue);
    }
}
