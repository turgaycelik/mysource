package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.util.dbc.Null;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v3.13
 */
public class BackupProjectImpl implements BackupProject
{
    private final ExternalProject project;
    private final List<ExternalVersion> projectVersions;
    private final List<ExternalComponent> projectComponents;
    private final Set<Long> issueIds;
    private final Map<String, ExternalCustomFieldConfiguration> customFieldConfigurations;

    public BackupProjectImpl(final ExternalProject project, final List<ExternalVersion> versions, final List<ExternalComponent> components, final List<ExternalCustomFieldConfiguration> customFieldConfigurations, final List<Long> issueIds)
    {
        Null.not("project", project);
        Null.not("versions", versions);
        Null.not("components", components);
        Null.not("customFieldConfigurations", customFieldConfigurations);
        Null.not("issueIds", issueIds);

        this.project = project;
        projectVersions = Collections.unmodifiableList(versions);
        projectComponents = Collections.unmodifiableList(components);
        this.customFieldConfigurations = new HashMap<String, ExternalCustomFieldConfiguration>();
        for (final ExternalCustomFieldConfiguration configuration : customFieldConfigurations)
        {
            this.customFieldConfigurations.put(configuration.getCustomField().getId(), configuration);
        }
        this.issueIds = Collections.unmodifiableSet(new HashSet<Long>(issueIds));
    }

    public ExternalProject getProject()
    {
        return project;
    }

    public Collection<ExternalVersion> getProjectVersions()
    {
        return projectVersions;
    }

    public Collection<ExternalComponent> getProjectComponents()
    {
        return projectComponents;
    }

    public Collection<Long> getIssueIds()
    {
        return issueIds;
    }

    public Collection<ExternalCustomFieldConfiguration> getCustomFields()
    {
        return Collections.unmodifiableCollection(customFieldConfigurations.values());
    }

    public ExternalCustomFieldConfiguration getCustomFieldConfiguration(final String customFieldId)
    {
        return customFieldConfigurations.get(customFieldId);
    }

    public boolean containsIssue(final String id)
    {
        if (id == null)
        {
            return false;
        }
        try
        {
            return getIssueIds().contains(new Long(id));
        }
        catch (final NumberFormatException e)
        {
            return false;
        }
    }

    ///CLOVER:OFF - this will be removed before we go into production, this is just for testing
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("project: ").append(project.getKey());
        sb.append(" issueCount: ").append(issueIds.size());
        sb.append("\n");
        sb.append("versions: ");
        for (final ExternalVersion externalVersion : projectVersions)
        {
            sb.append("\n");
            sb.append("     ");
            sb.append(externalVersion);
        }
        sb.append("\n");
        sb.append("comoponents: ");
        for (final ExternalComponent externalComponent : projectComponents)
        {
            sb.append("\n");
            sb.append("     ");
            sb.append(externalComponent);
        }
        sb.append("\n");
        sb.append("customFieldConfigs: ");
        for (final ExternalCustomFieldConfiguration externalCustomFieldConfiguration : customFieldConfigurations.values())
        {
            sb.append("\n");
            sb.append("     ");
            sb.append(externalCustomFieldConfiguration);
        }
        return sb.toString();
    }

    ///CLOVER:ON

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

        final BackupProjectImpl that = (BackupProjectImpl) o;

        if (!customFieldConfigurations.equals(that.customFieldConfigurations))
        {
            return false;
        }
        if (!issueIds.equals(that.issueIds))
        {
            return false;
        }
        if (!project.equals(that.project))
        {
            return false;
        }
        if (!projectComponents.equals(that.projectComponents))
        {
            return false;
        }
        if (!projectVersions.equals(that.projectVersions))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = project.hashCode();
        result = 31 * result + projectVersions.hashCode();
        result = 31 * result + projectComponents.hashCode();
        result = 31 * result + issueIds.hashCode();
        result = 31 * result + customFieldConfigurations.hashCode();
        return result;
    }

}
