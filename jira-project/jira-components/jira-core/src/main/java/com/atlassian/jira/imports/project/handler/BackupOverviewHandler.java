package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl;

import java.util.Map;

/**
 * Performs the first pass on the XML backup for a ProjectImport. This will populate a {@link com.atlassian.jira.imports.project.core.BackupOverview}
 * object that can be used to prompt the user with which project to import.
 *
 * @since v3.13
 */
public class BackupOverviewHandler implements ImportEntityHandler
{
    private BackupOverviewBuilder backupOverviewBuilder;

    // Call-through to super
    public BackupOverviewHandler()
    {
        super();
    }

    public BackupOverview getBackupOverview()
    {
        return getBackupOverviewBuilder().getBackupOverview();
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        getBackupOverviewBuilder().populateInformationFromElement(entityName, attributes);
    }

    public void startDocument()
    {
    // No op
    }

    public void endDocument()
    {
    // No op
    }

    BackupOverviewBuilder getBackupOverviewBuilder()
    {
        if (backupOverviewBuilder == null)
        {
            backupOverviewBuilder = new BackupOverviewBuilderImpl();
        }
        return backupOverviewBuilder;
    }
}
