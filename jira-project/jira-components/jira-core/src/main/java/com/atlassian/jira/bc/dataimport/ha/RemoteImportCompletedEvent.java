package com.atlassian.jira.bc.dataimport.ha;

import org.apache.commons.lang.StringUtils;

/**
 * Raised after a Remote Export has completed
 *
 * @since v6.1
 */
public class RemoteImportCompletedEvent
{
    private final String indexBackupFileName;

    public RemoteImportCompletedEvent(final String indexBackupFileName)
    {
        this.indexBackupFileName = indexBackupFileName;
    }

    public boolean isImportSuccessful()
    {
        return StringUtils.isNotEmpty(indexBackupFileName);
    }

    public String getIndexBackupFileName()
    {
        return indexBackupFileName;
    }
}
