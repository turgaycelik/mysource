package com.atlassian.jira.imports.project.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.util.dbc.Null;

import org.apache.commons.collections.map.ListOrderedMap;

/**
 * @since v3.13
 */
public class BackupOverviewImpl implements BackupOverview
{
    private static final long serialVersionUID = -8149242078577537996L;

    private final transient Map fullProjectsByKey;
    private final BackupSystemInformation backupSystemInformation;

    public BackupOverviewImpl(final BackupSystemInformation backupSystemInformation, final List backupProjects)
    {
        Null.not("backupSystemInformation", backupSystemInformation);
        Null.not("backupProjects", backupProjects);

        this.backupSystemInformation = backupSystemInformation;

        fullProjectsByKey = new ListOrderedMap();

        // Sort the projects by name so they are ordered
        Collections.sort(backupProjects, new BackupProjectNameComparator());
        for (final Object backupProject1 : backupProjects)
        {
            final BackupProject backupProject = (BackupProject) backupProject1;
            fullProjectsByKey.put(backupProject.getProject().getKey(), backupProject);
        }
    }

    public BackupProject getProject(final String projectKey)
    {
        return (BackupProject) fullProjectsByKey.get(projectKey);
    }

    public List /*<BackupProject>*/getProjects()
    {
        return new ArrayList(fullProjectsByKey.values());
    }

    public BackupSystemInformation getBackupSystemInformation()
    {
        return backupSystemInformation;
    }

    ///CLOVER:OFF - this will be removed before we go into production, this is just for testing
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        for (final Object o : getProjects())
        {
            final BackupProject backupProject = (BackupProject) o;
            sb.append("--").append(backupProject.getProject().getKey()).append("--");
            sb.append(backupProject);
            sb.append("\n");
        }

        sb.append(backupSystemInformation);
        return sb.toString();
    }
    ///CLOVER:ON

}
