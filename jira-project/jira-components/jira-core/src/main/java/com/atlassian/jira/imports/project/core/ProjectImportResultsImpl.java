package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since v3.13
 */
public class ProjectImportResultsImpl implements ProjectImportResults
{
    private static final long serialVersionUID = -564712258696084710L;

    // Transient as class implements serializable and is replicated across the cluster by the caching system
    // Restored in {@link #readResolve()}
    private transient Project importedProject;
    private transient I18nHelper i18n;

    private Long importedProjectId;

    private final List<String> errors;
    private final Map<String, AtomicInteger> roleUsersCreatedCountByRole;
    private final Map<String, AtomicInteger> roleGroupsCreatedCountByRole;
    private final Set<String> roles;
    private final AtomicInteger issuesCreatedCount;
    private final AtomicInteger usersCreatedCount;
    private final AtomicInteger attachmentsCreatedCount;
    private final AtomicInteger errorCount;

    private final long startTime;
    private final int expectedIssuesCreatedCount;
    private final int expectedUsersCreatedCount;
    private final int expectedAttachmentsCreatedCount;

    private long endTime;
    private boolean importCompleted;

    public ProjectImportResultsImpl(final long startTime, final int expectedIssuesCreatedCount, final int expectedUsersCreatedCount, final int expectedAttachmentsCreatedCount, final I18nHelper i18n)
    {
        this.startTime = startTime;
        this.expectedIssuesCreatedCount = expectedIssuesCreatedCount;
        this.expectedUsersCreatedCount = expectedUsersCreatedCount;
        this.expectedAttachmentsCreatedCount = expectedAttachmentsCreatedCount;
        this.i18n = i18n;
        issuesCreatedCount = new AtomicInteger(0);
        usersCreatedCount = new AtomicInteger(0);
        errorCount = new AtomicInteger(0);
        attachmentsCreatedCount = new AtomicInteger(0);
        errors = Collections.synchronizedList(new ArrayList<String>());
        roleUsersCreatedCountByRole = new HashMap<String, AtomicInteger>();
        roleGroupsCreatedCountByRole = new HashMap<String, AtomicInteger>();
        roles = Collections.synchronizedSet(new TreeSet<String>());
    }

    public long getImportDuration()
    {
        return endTime - startTime;
    }

    public void setEndTime(final long endTime)
    {
        this.endTime = endTime;
    }

    @ClusterSafe("SESSION AFFINITY")
    public synchronized void incrementRoleUserCreatedCount(final String roleName)
    {
        AtomicInteger roleUsersCreated = roleUsersCreatedCountByRole.get(roleName);
        if (roleUsersCreated == null)
        {
            roleUsersCreated = new AtomicInteger(0);
            roleUsersCreatedCountByRole.put(roleName, roleUsersCreated);
            roles.add(roleName);
        }
        roleUsersCreated.incrementAndGet();
    }

    @ClusterSafe("SESSION AFFINITY")
    public synchronized void incrementRoleGroupCreatedCount(final String roleName)
    {
        AtomicInteger roleGroupsCreated = roleGroupsCreatedCountByRole.get(roleName);
        if (roleGroupsCreated == null)
        {
            roleGroupsCreated = new AtomicInteger(0);
            roleGroupsCreatedCountByRole.put(roleName, roleGroupsCreated);
            roles.add(roleName);
        }
        roleGroupsCreated.incrementAndGet();
    }

    public Collection<String> getRoles()
    {
        return Collections.unmodifiableCollection(roles);
    }

    public int getGroupsCreatedCountForRole(final String roleName)
    {
        final AtomicInteger groupsCreatedForRole = roleGroupsCreatedCountByRole.get(roleName);
        if (groupsCreatedForRole != null)
        {
            return groupsCreatedForRole.get();
        }
        return 0;
    }

    public int getUsersCreatedCountForRole(final String roleName)
    {
        final AtomicInteger usersCreatedForRole = roleUsersCreatedCountByRole.get(roleName);
        if (usersCreatedForRole != null)
        {
            return usersCreatedForRole.get();
        }
        return 0;
    }

    public void addError(final String error)
    {
        errors.add(error);
        errorCount.incrementAndGet();
    }

    /**
     * Returns the maximum number of Errors that are allowed.
     * Once this limit is reached, the import is aborted.
     * @return the maximum number of Errors that are allowed.
     */
    int getErrorCountLimit()
    {
        return 10;
    }

    public List<String> getErrors()
    {
        return Collections.unmodifiableList(errors);
    }

    public boolean isImportCompleted()
    {
        return importCompleted;
    }

    public void incrementIssuesCreatedCount()
    {
        issuesCreatedCount.incrementAndGet();
    }

    public void incrementUsersCreatedCount()
    {
        usersCreatedCount.incrementAndGet();
    }

    public void incrementAttachmentsCreatedCount()
    {
        attachmentsCreatedCount.incrementAndGet();
    }

    public int getIssuesCreatedCount()
    {
        return issuesCreatedCount.get();
    }

    public int getUsersCreatedCount()
    {
        return usersCreatedCount.get();
    }

    public int getAttachmentsCreatedCount()
    {
        return attachmentsCreatedCount.get();
    }

    public int getExpectedIssuesCreatedCount()
    {
        return expectedIssuesCreatedCount;
    }

    public int getExpectedUsersCreatedCount()
    {
        return expectedUsersCreatedCount;
    }

    public int getExpectedAttachmentsCreatedCount()
    {
        return expectedAttachmentsCreatedCount;
    }

    public void setImportCompleted(final boolean importCompleted)
    {
        this.importCompleted = importCompleted;
    }

    public Project getImportedProject()
    {
        return importedProject;
    }

    public void setImportedProject(final Project importedProject)
    {
        this.importedProject = importedProject;
        this.importedProjectId = importedProject.getId();
    }

    public I18nHelper getI18n()
    {
        return i18n;
    }

    public boolean abortImport()
    {
        // Check if we should abort.
        return errorCount.get() >= getErrorCountLimit();
    }

    /**
     * Restore transient variable when deserialized.
     * @return deserialized object.
     */
    private Object readResolve()
    {
        i18n = ComponentAccessor.getComponent(I18nHelper.class);
        if (importedProjectId != null)
        {
            importedProject = ComponentAccessor.getComponent(ProjectManager.class).getProjectObj(importedProjectId);
        }
        return this;
    }
}