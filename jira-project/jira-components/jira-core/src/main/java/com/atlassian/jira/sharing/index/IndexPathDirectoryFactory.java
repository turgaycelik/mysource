package com.atlassian.jira.sharing.index;

import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.util.LuceneDirectoryUtils;
import org.apache.lucene.store.Directory;

import java.io.File;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation that uses the {@link com.atlassian.jira.config.util.IndexPathManager} as its way of getting a
 * Directory
 */
public class IndexPathDirectoryFactory implements DirectoryFactory
{
    private final IndexPathManager pathManager;
    private final LuceneDirectoryUtils luceneDirectoryUtils;

    public IndexPathDirectoryFactory(final IndexPathManager pathManager, final LuceneDirectoryUtils luceneDirectoryUtils)
    {
        this.luceneDirectoryUtils = notNull("luceneDirectoryUtils", luceneDirectoryUtils);
        this.pathManager = notNull("path", pathManager);
    }

    public Directory get(final SharedEntity.TypeDescriptor<?> type)
    {
        return luceneDirectoryUtils.getDirectory(new File(getIndexPath(type)));
    }

    String getIndexPath(final SharedEntity.TypeDescriptor<?> type)
    {
        return pathManager.getSharedEntityIndexPath() + "/" + type.getName().toLowerCase();
    }
}
