package com.atlassian.jira.project.version;

import com.atlassian.fugue.Option;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.NullComparator;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class MockVersionManager implements VersionManager
{
    private final Map<Long, Version> versions = new HashMap<Long, Version>();

    @Override
    public Version createVersion(String name, Date releaseDate, String description, Long projectId, Long scheduleAfterVersion)
            throws CreateException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Version createVersion(String name, Date startDate, Date releaseDate, String description, Long projectId, Long scheduleAfterVersion)
            throws CreateException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void moveToStartVersionSequence(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void increaseVersionSequence(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void decreaseVersionSequence(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void moveToEndVersionSequence(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void moveVersionAfter(Version version, Long scheduleAfterVersion)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteVersion(Version version)
    {
        versions.remove(version.getId());
    }

    @Override
    public Version update(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void editVersionDetails(Version version, String name, String description, GenericValue project)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void editVersionDetails(Version version, String name, String description)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isDuplicateName(Version version, String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isDuplicateName(Version version, String name, GenericValue project)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void releaseVersion(Version version, boolean release)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void releaseVersions(Collection<Version> versions, boolean release)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void moveIssuesToNewVersion(List issues, Version currentVersion, Version swapToVersion) throws IndexException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void archiveVersions(String[] idsToArchive, String[] idsToUnarchive)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void archiveVersion(Version version, boolean archive)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getVersionsUnarchived(Long projectId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getVersionsArchived(GenericValue projectGV)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getVersionsArchived(Project project)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void editVersionStartDate(Version version, Date startDate)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void editVersionReleaseDate(Version version, Date duedate)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void editVersionStartReleaseDate(Version version, Date startDate, Date releaseDate)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isVersionOverDue(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Version> getVersions(GenericValue project)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Version> getVersions(Long projectId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Version> getVersions(Long projectId, boolean includeArchived)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Version> getVersions(Project project)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getVersionsByName(String versionName)
    {
        Collection<Version> versionList = new ArrayList<Version>();
        for (Version version : versions.values())
        {
            if (versionName.equals(version.getName()))
                versionList.add(version);
        }
        return versionList;
    }

    @Override
    public Collection<Version> getVersions(List<Long> ids)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Version getVersion(Long id)
    {
        return versions.get(id);
    }

    @Override
    public Version getVersion(Long projectId, String versionName)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getVersionsUnreleased(Long projectId, boolean includeArchived)
    {
        List<Version> v = Lists.newArrayList();
        for (Version version : versions.values())
        {
            if (projectId.equals(version.getProjectObject().getId()) && !version.isReleased())
            {
                if (!version.isArchived() || includeArchived)
                {
                    v.add(version);
                }
            }
        }
        return v;
    }

    @Override
    public List<Version> getVersionsReleased(Long projectId, boolean includeArchived)
    {
        List<Version> v = Lists.newArrayList();
        for (Version version : versions.values())
        {
            if (projectId.equals(version.getProjectObject().getId()) && version.isReleased())
            {
                if (!version.isArchived() || includeArchived)
                {
                    v.add(version);
                }
            }
        }
        return v;
    }

    @Override
    public Collection<Version> getVersionsReleasedDesc(Long projectId, boolean includeArchived)
    {
        List<Version> versions = getVersionsReleased(projectId, includeArchived);
        Collections.sort(versions, new Comparator<Version>()
        {
            @Override
            public int compare(Version a, Version b)
            {
                return new NullComparator().compare(
                        a == null ? null : a.getReleaseDate(),
                        b == null ? null : b.getReleaseDate()
                );
            }
        });
        return versions;
    }

    @Override
    public Collection<Version> getOtherVersions(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getOtherUnarchivedVersions(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<GenericValue> getAllAffectedIssues(Collection<Version> versions)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<GenericValue> getFixIssues(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Issue> getIssuesWithFixVersion(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<GenericValue> getAffectsIssues(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Issue> getIssuesWithAffectsVersion(Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Long> getIssueIdsWithAffectsVersion(@Nonnull final Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Long> getIssueIdsWithFixVersion(@Nonnull final Version version)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getAffectedVersionsByIssue(GenericValue issue)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getAffectedVersionsFor(Issue issue)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getFixVersionsByIssue(GenericValue issue)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getFixVersionsFor(Issue issue)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getAllVersions()
    {
        return versions.values();
    }

    @Override
    public Collection<Version> getAllVersionsForProjects(Collection<Project> projects, boolean includeArchived)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getAllVersionsReleased(boolean includeArchived)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Version> getAllVersionsUnreleased(boolean includeArchived)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void swapVersionForRelatedIssues(final ApplicationUser user, final Version version, final Option<Version> affectsSwapVersion, final Option<Version> fixSwapVersion)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void add(MockVersion version)
    {
        versions.put(version.getId(), version);
    }
}
