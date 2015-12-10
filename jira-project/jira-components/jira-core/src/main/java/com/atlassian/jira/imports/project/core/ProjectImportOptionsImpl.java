package com.atlassian.jira.imports.project.core;

/**
 * @since v3.13
 */
public class ProjectImportOptionsImpl implements ProjectImportOptions
{
    private final String pathToBackupXml;
    private final String attachmentPath;
    private String selectedProjectKey;
    private boolean overwriteProjectDetails;

    public ProjectImportOptionsImpl(final String pathToBackupXml, final String attachmentPath)
    {
        this.pathToBackupXml = pathToBackupXml;
        this.attachmentPath = attachmentPath;
    }

    public ProjectImportOptionsImpl(final String pathToBackupXml, final String attachmentPath, final boolean overwriteProjectDetails)
    {
        this.pathToBackupXml = pathToBackupXml;
        this.attachmentPath = attachmentPath;
        this.overwriteProjectDetails = overwriteProjectDetails;
    }

    public String getPathToBackupXml()
    {
        return pathToBackupXml;
    }

    public String getAttachmentPath()
    {
        return attachmentPath;
    }

    public boolean overwriteProjectDetails()
    {
        return overwriteProjectDetails;
    }

    public void setOverwriteProjectDetails(final boolean overwriteProjectDetails)
    {
        this.overwriteProjectDetails = overwriteProjectDetails;
    }

    public String getSelectedProjectKey()
    {
        return selectedProjectKey;
    }

    public void setSelectedProjectKey(final String selectedProjectKey)
    {
        this.selectedProjectKey = selectedProjectKey;
    }
}
