package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.util.ProjectImportTemporaryFiles;

/**
 * @since v3.13
 */
public class ProjectImportDataImpl implements ProjectImportData
{
    private final ProjectImportTemporaryFiles temporaryFiles;
    private final int issueCount;
    private final int fileAttachmentCount;
    private final int changeItemEntityCount;
    private int validAttachmentCount;
    private final int issueRelatedEntitiesCount;
    private final int customFieldValuesCount;
    private final ProjectImportMapper projectImportMapper;

    public ProjectImportDataImpl(final ProjectImportMapper projectImportMapper, final ProjectImportTemporaryFiles temporaryFiles, final int issueCount, final int customFieldValuesCount, final int issueRelatedEntitiesCount, final int fileAttachmentCount, final int changeItemEntityCount)
    {
        this.temporaryFiles = temporaryFiles;
        this.issueCount = issueCount;
        this.customFieldValuesCount = customFieldValuesCount;
        this.issueRelatedEntitiesCount = issueRelatedEntitiesCount;
        this.projectImportMapper = projectImportMapper;
        this.fileAttachmentCount = fileAttachmentCount;
        this.changeItemEntityCount = changeItemEntityCount;
    }

    public ProjectImportTemporaryFiles getTemporaryFiles()
    {
        return temporaryFiles;
    }

    public String getPathToIssuesXml()
    {
        return temporaryFiles.getIssuesXmlFile().getAbsolutePath();
    }

    public int getIssueEntityCount()
    {
        return issueCount;
    }

    public String getPathToFileAttachmentXml()
    {
        return temporaryFiles.getFileAttachmentEntitiesXmlFile().getAbsolutePath();
    }

    public int getFileAttachmentEntityCount()
    {
        return fileAttachmentCount;
    }

    public String getPathToIssueRelatedEntitiesXml()
    {
        return temporaryFiles.getIssueRelatedEntitiesXmlFile().getAbsolutePath();
    }

    public int getIssueRelatedEntityCount()
    {
        return issueRelatedEntitiesCount;
    }

    public String getPathToCustomFieldValuesXml()
    {
        return temporaryFiles.getCustomFieldValuesXmlFile().getAbsolutePath();
    }

    public int getCustomFieldValuesEntityCount()
    {
        return customFieldValuesCount;
    }

    public int getChangeItemEntityCount()
    {
        return changeItemEntityCount;
    }

    public ProjectImportMapper getProjectImportMapper()
    {
        return projectImportMapper;
    }

    public void setValidAttachmentsCount(final int validAttachmentCount)
    {
        this.validAttachmentCount = validAttachmentCount;
    }

    public int getValidAttachmentsCount()
    {
        return validAttachmentCount;
    }

    public String getPathToChangeItemXml()
    {
        return temporaryFiles.getChangeItemEntitiesXmlFile().getAbsolutePath();
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final ProjectImportDataImpl that = (ProjectImportDataImpl) o;

        if (customFieldValuesCount != that.customFieldValuesCount)
        {
            return false;
        }
        if (fileAttachmentCount != that.fileAttachmentCount)
        {
            return false;
        }
        if (issueCount != that.issueCount)
        {
            return false;
        }
        if (issueRelatedEntitiesCount != that.issueRelatedEntitiesCount)
        {
            return false;
        }
        if (validAttachmentCount != that.validAttachmentCount)
        {
            return false;
        }
        if (projectImportMapper != null ? !projectImportMapper.equals(that.projectImportMapper) : that.projectImportMapper != null)
        {
            return false;
        }
        if (temporaryFiles != null ? !temporaryFiles.equals(that.temporaryFiles) : that.temporaryFiles != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (temporaryFiles != null ? temporaryFiles.hashCode() : 0);
        result = 31 * result + issueCount;
        result = 31 * result + fileAttachmentCount;
        result = 31 * result + validAttachmentCount;
        result = 31 * result + issueRelatedEntitiesCount;
        result = 31 * result + customFieldValuesCount;
        result = 31 * result + (projectImportMapper != null ? projectImportMapper.hashCode() : 0);
        return result;
    }
}
