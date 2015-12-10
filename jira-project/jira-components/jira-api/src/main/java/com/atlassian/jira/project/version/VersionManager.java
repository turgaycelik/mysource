/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.version;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Option;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Manager responsible for <a href="http://www.atlassian.com/software/jira/docs/latest/version_management.html">JIRA versions</a>.
 */
@PublicApi
public interface VersionManager
{
    /**
     * Used to represent empty version fields
     */
    public static final String NO_VERSIONS = "-1";

    /**
     * Used to retrieve all unreleased versions
     */
    public static final String ALL_UNRELEASED_VERSIONS = "-2";

    /**
     * Used to retrieve all released versions
     */
    public static final String ALL_RELEASED_VERSIONS = "-3";


    /**
     * Creates a new {@link Version} object.
     *
     * @param name the Name
     * @param releaseDate          date of release or null if not released.
     * @param description the Description
     * @param projectId            the id of the Project of the version.
     * @param scheduleAfterVersion id of the version after which this should be sequenced or null.
     * @return the new Version.
     * @throws CreateException        If there was a problem creating the version.
     */
    Version createVersion(String name, Date releaseDate, String description, Long projectId, Long scheduleAfterVersion) throws CreateException;

    /**
     * Creates a new {@link Version} object.
     *
     * @param name the Name
     * @param startDate            start date of the version or null
     * @param releaseDate          date of release or null if not released.
     * @param description the Description
     * @param projectId            the id of the Project of the version.
     * @param scheduleAfterVersion id of the version after which this should be sequenced or null.
     * @return the new Version.
     * @throws CreateException        If there was a problem creating the version.
     * @since v6.0
     */
    Version createVersion(final String name, final Date startDate, final Date releaseDate, final String description, final Long projectId, final Long scheduleAfterVersion) throws CreateException;

    // ---- Scheduling Methods ----

    /**
     * Move a version to the start of the version list
     *
     * @param version the Version to move
     */
    void moveToStartVersionSequence(Version version);

    /**
     * Move a version to have a lower sequence number - ie make it earlier
     *
     * @param version the Version
     */
    void increaseVersionSequence(Version version);

    /**
     * Move a version to have a higher sequence number - ie make it later
     *
     * @param version the Version
     */
    void decreaseVersionSequence(Version version);

    /**
     * Move a version to the end of the version sequence
     *
     * @param version the Version
     */
    void moveToEndVersionSequence(Version version);

    /**
     * Move a version after another version
     * @param version version to reschedule
     * @param scheduleAfterVersion id of the version to schedule after the given version object
     */
    void moveVersionAfter(Version version, Long scheduleAfterVersion);

    // ---- Delete Version Methods ----

    /**
     * Removes a specific version from the system.
     *
     * @param version              The version to be removed.
     */
    void deleteVersion(final Version version);

    // ---- Edit Version Name Methods ----

    /**
     * Updates details for an existing version.
     *
     * @param version     The version to update
     * @param name new name
     * @param description new description
     * @param project     Used to check for duplicate version names in a project.
     * @throws IllegalArgumentException If the name is not set, or already exists.
     * @deprecated use {@link #editVersionDetails(Version, String, String)} instead.  since 5.0
     */
    @Deprecated
    void editVersionDetails(Version version, String name, String description, GenericValue project);

    /**
     * Updates details of an existing version.
     *
     * @param version  The version to update
     * @param name     The new version name, cannot be empty.
     * @param description The description of this version.
     * @throws IllegalArgumentException If the name is not set, or already exists.
     */
    void editVersionDetails(Version version, String name, String description);

    /**
     * Check that the version name we are changing to is not a duplicate.
     *
     * @param version The version to update
     * @param name     The new name for the version
     * @return  true if there is already a version with that name for the project
     */
    public boolean isDuplicateName(Version version, final String name);

    /**
     * Check that the version name we are changing to is not a duplicate.
     * @param version     The version to update
     * @param name The new name for the version
     * @param project Used to check for duplicate version names in a project.
     * @return true if there is already a version with that name for the project
     *
     * @deprecated use {@link #isDuplicateName(Version, String)} instead. since 5.0
     */
    @Deprecated
    boolean isDuplicateName(Version version, final String name, final GenericValue project);


    // ---- Release Version Methods ----
    /**
     * Used to release or unrelease a version, depending on the release flag.
     *
     * @param version Version to be released (or unreleased)
     * @param release  True to release a version. False to 'unrelease' a version
     */
    void releaseVersion(Version version, boolean release);

    /**
     * Used to release versions depending on the release flag.
     *
     * @param versions Collection of {@link Version}s
     * @param release  True to release a version. False to 'unrelease' a version
     */
    void releaseVersions(Collection<Version> versions, boolean release);

    /**
     * Swaps the list of issues supplied from one Fix version to another.
     *
     * @param issues the Issues
     * @param currentVersion Current fix version they will be swapped from
     * @param swapToVersion New fix version they will be swapped to.
     * @throws IndexException if an error occurs will indexing these new issue values.
     */
    void moveIssuesToNewVersion(List<GenericValue> issues, Version currentVersion, Version swapToVersion) throws IndexException;

    // ---- Archive Version Methods ----

    /**
     * Method used to archive and un-archive a number of versions.
     *
     * @param idsToArchive Archive all these Versions
     * @param idsToUnarchive Unarchive these Versions
     */
    void archiveVersions(String[] idsToArchive, String[] idsToUnarchive);

    /**
     * Archive/Un-archive a single version depending on the archive flag.
     *
     * @param version the Version to archive or unarchive
     * @param archive new archive value
     */
    void archiveVersion(Version version, boolean archive);

    /**
     * Return all un-archived versions for a particular project
     * @param projectId id of the project.
     * @return A collection of {@link Version}s
     * @since v3.10
     */
    Collection<Version> getVersionsUnarchived(Long projectId);

    /**
     * Return all archived versions for a particular project.
     *
     * @param projectGV the Project
     * @return A collections of {@link Version}s
     *
     * @deprecated Use {@link #getVersionsArchived(Project)} instead. Since v5.0.
     */
    Collection<Version> getVersionsArchived(GenericValue projectGV);

    /**
     * Return all archived versions for a particular project.
     *
     * @param project the Project
     * @return Archived versions for this project.
     */
    Collection<Version> getVersionsArchived(final Project project);

    /**
     * Persists updates to the specified version object.
     *
     * @param version the version
     * @return the updated version
     *
     * @since 6.0
     */
    Version update(Version version);

    // ---- Version Release Date Mthods ----
    /**
     * Update the release date of a version.
     *
     * @param version the Version to edit
     * @param duedate new release date
     */
    void editVersionReleaseDate(Version version, Date duedate);

    /**
     * Updates the start date of a version
     *
     * @param version the version to edit
     * @param startDate new start date
     *
     * @since v6.0
     */
    void editVersionStartDate(Version version, Date startDate);

    /**
     * Updates the start and release date of a version
     *
     * @param version the version to edit
     * @param startDate new start date
     * @param releaseDate new release date
     *
     * @since v6.0
     */
    void editVersionStartReleaseDate(Version version, Date startDate, Date releaseDate);

    /**
     * Checks to see if a version is overdue.  Note: This method checks if the due date
     * set for a version is previous to last midnight. (not now()).
     *
     * @param version the Version
     * @return True if the version is overdue. (i.e. releaseDate is before last midnight)
     */
    public boolean isVersionOverDue(Version version);

    /**
     * Gets all the versions for a project.
     *
     * @param project the Project
     * @return a List of Version objects.
     * @deprecated Use {@link #getVersions(Long)} or {@link #getVersions(Project)} instead. Since 2006.
     */
    List<Version> getVersions(GenericValue project);

    /**
     * Return a list of Versions for the given project.
     *
     * @param projectId the Project
     * @return a list of Versions for the given project.
     *
     * @see VersionManager#getVersions(Project)
     */
    List<Version> getVersions(Long projectId);

    /**
     * Return a list of Versions for the given project.
     *
     * @param projectId the Project
     * @param includeArchived whether or not to include archived versions
     * @return a list of Versions for the given project.
     *
     * @see VersionManager#getVersions(Project)
     */
    List<Version> getVersions(Long projectId, boolean includeArchived);

    /**
     * Return a list of Versions for the given project.
     *
     * @param project the Project
     * @return a list of Versions for the given project.
     *
     * @see VersionManager#getVersions(Long)
     */
    List<Version> getVersions(Project project);

    /**
     * Return a collection of {@link Version}s that have the specified name.
     *
     * @param versionName the name of the version (case-insensitive)
     * @return a Collection of Version objects. Never null.
     */
    Collection<Version> getVersionsByName(String versionName);

    /**
     * Return a collection of {@link Version}s matching the ids passed in.
     *
     * @param ids Version IDs
     * @return a collection of {@link Version}s matching the ids passed in.
     */
    Collection<Version> getVersions(List<Long> ids);

    /**
     * Returns a single version.
     *
     * @param id the Version ID
     * @return A {@link Version} object.
     */
    Version getVersion(Long id);

    /**
     * Search for a version by projectID and name.
     *
     * @param projectId the Project
     * @param versionName the Version name
     * @return A {@link Version} object.
     */
    Version getVersion(Long projectId, String versionName);

    /**
     * Gets a list of un-released versions for a particular project.
     *
     * @param projectId       The id of the project for which to return versions
     * @param includeArchived True if archived versions should be included
     * @return A Collection of {@link com.atlassian.jira.project.version.Version}s, never null
     * @since v3.10
     */
    Collection<Version> getVersionsUnreleased(Long projectId, boolean includeArchived);

    /**
     * Gets a list of released versions for a project. This list will include
     * archived versions if the 'includeArchived' flag is set to true.
     *
     * @param projectId       project id
     * @param includeArchived flag to indicate whether to include archived versions in the result.
     * @return A collection of {@link Version} objects
     */
    Collection<Version> getVersionsReleased(Long projectId, boolean includeArchived);

    /**
     * Gets a list of released versions for a project in reverse order.
     * This list will include archived versions if the 'includeArchived' flag
     * is set to true.
     *
     * @param projectId         project id
     * @param includeArchived flag to indicate whether to include archived versions in the result.
     * @return A collection of {@link Version} objects
     */
    Collection<Version> getVersionsReleasedDesc(Long projectId, boolean includeArchived);

    /**
     * Return all other versions in the project except this one
     *
     * @param version the Version
     * @return all other versions in the project except this one
     */
    public Collection<Version> getOtherVersions(Version version);

    /**
     * Return all unarchived versions except this one
     *
     * @param version the Version
     * @return all unarchived versions except this one
     */
    public Collection<Version> getOtherUnarchivedVersions(Version version);

    /**
     * Return all Issues that are associated with the specified versions
     *
     * @param versions a collection of {@link Version} objects
     * @return A collection of issue {@link GenericValue}s
     * @deprecated use {@link #getIssueIdsWithAffectsVersion(Version)} and {@link #getIssueIdsWithFixVersion(Version)} instead. since 5.0
     */
    @Deprecated
    public Collection<GenericValue> getAllAffectedIssues(Collection<Version> versions);

    /**
     * Return Fix Issues
     *
     * @param version the Version
     * @return A collection of issue {@link GenericValue}s
     * @deprecated use {@link #getIssuesWithFixVersion(Version)} instead. since 5.0
     */
    @Deprecated
    public Collection<GenericValue> getFixIssues(Version version);

    /**
     * Return all the issues in which the fix for version matches the specified version.
     *
     * @param version the fixed for version.
     *
     * @return all the issues in which the fix for version matches the specified version.
     *
     * @since v5.0
     */
     Collection<Issue> getIssuesWithFixVersion(Version version);

    /**
     * Return 'Affects' Issues
     *
     * @param version the Version
     * @return A collection of issue {@link GenericValue}s
     * @deprecated use {@link #getIssuesWithAffectsVersion(Version)} instead. since 5.0
     */
    @Deprecated
    public Collection<GenericValue> getAffectsIssues(Version version);

    /**
     * Return all the issues in which the affected version matches the specified version.
     *
     * @param version the affected version.
     *
     * @return all the issues in which the affected version matches the specified version.
     *
     * @since v5.0
     */
    Collection<Issue> getIssuesWithAffectsVersion(Version version);

    /**
     * Return all the issues in which the affects version matches the specified version.
     *
     * @param version the affects version.
     *
     * @return all the issues in which the affects version matches the specified version.
     *
     * @since v6.1
     */
    Collection<Long> getIssueIdsWithAffectsVersion(@Nonnull final Version version);

    /**
     * Return all the issues in which the fix version matches the specified version.
     *
     * @param version the fix version.
     *
     * @return all the issues in which the fix version matches the specified version.
     *
     * @since v6.1
     */
    Collection<Long> getIssueIdsWithFixVersion(@Nonnull final Version version);

    /**
     * @param issue  the Issue
     * @return A collection of 'affects' {@link Version}s for an issue.
     * @deprecated use {@link #getAffectedVersionsFor(com.atlassian.jira.issue.Issue)} instead. since 5.0
     */
    @Deprecated
    Collection<Version> getAffectedVersionsByIssue(GenericValue issue);

    /**
     * Get all affected versions of the specified issue.
     *
     * @param issue the issue
     *
     * @return all affected versions of the specified issue.
     */
    Collection<Version> getAffectedVersionsFor(Issue issue);

    /**
     * @param issue the Issue
     * @return A collection of 'fix for' {@link Version}s for an issue.
     * @deprecated use {@link #getFixVersionsFor(com.atlassian.jira.issue.Issue)} instead. since 5.0
     */
    @Deprecated
    Collection<Version> getFixVersionsByIssue(GenericValue issue);

    /**
     * Get all fix for versions of the specified issue.
     *
     * @param issue the Issue
     * @return all fix for versions of the specified issue.
     */
    Collection<Version> getFixVersionsFor(Issue issue);

    /**
     * @return all versions in JIRA. Never null.
     */
    Collection<Version> getAllVersions();

    /**
     * Returns all versions that belong to the passed projects.
     *
     * @param projects projects to search in
     * @param includeArchived whether or not to include archived versions
     * @return all versions that belong to passed projects. Never null.
     */
    Collection<Version> getAllVersionsForProjects(Collection<Project> projects, boolean includeArchived);

    /**
     * @param includeArchived whether or not to include archived versions
     * @return all released versions in JIRA. Never null.
     */
    Collection<Version> getAllVersionsReleased(boolean includeArchived);

    /**
     * @param includeArchived whether or not to include archived versions
     * @return all released versions in JIRA. Never null.
     */
    Collection<Version> getAllVersionsUnreleased(boolean includeArchived);

    /**
     * This method will update all issues that currently have {@code version} set as either affects or fix version to
     * the new {@code affectsSwapVersion} or {@code fixSwapVersion}
     * <p/>
     * Both {@code affectsSwapVersion} or {@code fixSwapVersion} may be undefined in which case the {@code version} will
     * simply be removed from the issue.
     *
     * @param user The user that will be used to update related issues
     * @param version The version to remove from issues
     * @param affectsSwapVersion Affects version to replace version with. May be undefined to simply remove the
     * version.
     * @param fixSwapVersion Fix version to replace version with. May be undefined to simply remove the version.
     */
    void swapVersionForRelatedIssues(ApplicationUser user, Version version, Option<Version> affectsSwapVersion, Option<Version> fixSwapVersion);
}
