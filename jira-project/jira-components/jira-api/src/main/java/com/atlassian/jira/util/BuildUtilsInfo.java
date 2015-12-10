package com.atlassian.jira.util;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * Provides version information about the currently running build of JIRA.
 *
 * @since v4.0
 */
@PublicApi
public interface BuildUtilsInfo
{
    /**
     * Gets the current version of JIRA
     *
     * @return the current version of JIRA
     */
    String getVersion();

    /**
     * Return the version used for JIRA documentation.
     *
     * This is the version that JIRA uses when generating links to its externally hosted documentation.
     *
     * @return the version used for JIRA documentation.
     * @since 6.2.3.
     */
    String getDocVersion();

    /**
     * Gets the numerical components of the version.
     *
     *  <li>
     *      <ul><code>"5.0"</code> gives [5, 0, 0]</ul>
     *      <ul><code>"5.0.1"</code> gives [5, 0, 1]</ul>
     *      <ul><code>"5.0-beta1"</code> gives [5, 0, 0]</ul>
     *      <ul><code>"5.0.1-SNAPSHOT"</code> gives [5, 0, 1]</ul>
     *  </li>
     *
     * The returned array is guaranteed to have at least 3 elements. Any non-numeric suffix in getVersion() is ignored.
     * <p>
     *     A simple way to use this information is with Guava's {@link com.google.common.primitives.Ints#lexicographicalComparator()}:
     * </p>
     * <pre>
     *  int[] v510 = {5, 1, 0};
     *  if (Ints.lexicographicalComparator().compare(buildUtils.getVersionNumbers(), v510) >= 0 ) { ...
     * </pre>
     *
     * @return the leading numerical components of getVersion()
     */
    int[] getVersionNumbers();

    /**
     * Gets the current build number of JIRA.
     * <p>
     * This is the same value as {@link #getApplicationBuildNumber()} except it is returned as a String.
     *
     * @return the current build number of JIRA
     *
     * @see #getDatabaseBuildNumber()
     * @see #getApplicationBuildNumber()
     */
    String getCurrentBuildNumber();

    /**
     * Gets the build number of this JIRA install.
     * <p>
     * That is, the build number of the currently running installation files.
     * The DB also stores a build number.
     *
     * @return the build number of this JIRA install.
     *
     * @see #getDatabaseBuildNumber()
     */
    int getApplicationBuildNumber();

    /**
     * Gets the build number of the database that this JIRA instance points to.
     * <p>
     * Under normal circumstances this will be the same as the build number of the JIRA installation.
     * However, when you first upgrade a JIRA database or import data exported from an older JIRA, the existing database
     * will start on the previous build number. JIRA will then run "Upgrade Tasks" to update the data in the DB.
     *
     * @return the build number of the database that JIRA points to.
     *
     * @see #getCurrentBuildNumber()
     */
    int getDatabaseBuildNumber();

    /**
     * Gets the minimal build number that JIRA can upgrade from
     *
     * @return the minimal build number that JIRA can upgrade from
     */
    String getMinimumUpgradableBuildNumber();

    /**
     * Gets the date this version of JIRA was built on.
     *
     * @return the date this version of JIRA was built on
     */
    Date getCurrentBuildDate();

    /**
     * Gets the partner name of this JIRA build
     *
     * @return the partner name of this JIRA build.
     */
    String getBuildPartnerName();

    /**
     * Gets a build information summary as a String.
     *
     * @return a build information summary
     */
    String getBuildInformation();

    /**
     * Returns the SCM commit id that was used to make this build of JIRA. This used to be an SVN revision, but is now a
     * Git commit id (aka a SHA1). This method returns an empty string if JIRA was built from the source distribution.
     *
     * @return a String containing the SCM commit id, or an empty String.
     * @deprecated Use {@link #getCommitId()} instead. Since v5.1.
     */
    @Deprecated
    String getSvnRevision();

    /**
     * Returns the id of the SCM commit that was used to make this build of JIRA. This method returns an empty string if
     * JIRA was built from the source distribution.
     *
     * @return the SCM commit id that was used to make this build of JIRA.
     */
    String getCommitId();

    /**
     * Get the minimum version of JIRA that can be upgraded to this instance version.
     *
     * @return the minimum version that can be upgraded.
     */
    String getMinimumUpgradableVersion();

    Collection<Locale> getUnavailableLocales();

    /**
     * Returns the version of Atlassian SAL that JIRA exports into OSGI-land.
     *
     * @return the version of Atlassian SAL that JIRA exports
     */
    String getSalVersion();

    /**
     * Returns the version of AppLinks that JIRA ships with.
     *
     * @return the version of AppLinks that JIRA ships with
     */
    String getApplinksVersion();

    /**
     * Returns the version of Apache Lucene included in JIRA.
     *
     * <p>This will be the version used to export the lucene packages to the OSGI container.</p>
     *
     * @return the version of Apache Lucene included in JIRA.
     *
     */
    String getLuceneVersion();

    /**
     * @return the osgi version that Guava (com.google.common) shold be exported as
     */
    String getGuavaOsgiVersion();
    String getBuildProperty(String key);

    /**
     * Returns whether the current version is a beta (i.e. has -beta after its version numbers)
     * @return true if the version string has -beta after its version numbers, otherwise false (case-insensitive)
     */
    boolean isBeta();

    /**
     * Returns whether the current version is a release candidate (i.e. has -rc after its version numbers)
     * @return true if the version string has -rc after its version numbers, otherwise false (case-insensitive)
     */
    boolean isRc();

    /**
     * Returns whether the current version is a snapshot (i.e. has -SNAPSHOT after its version numbers)
     * @return true if the version string has -SNAPSHOT after its version numbers, otherwise false (case-insensitive)
     */
    boolean isSnapshot();

    /**
     * Returns whether the current version is a milestone (i.e. has -mXX after its version numbers)
     * @return true if the version string has -m after its version numbers, otherwise false (case-insensitive)
     */
    boolean isMilestone();
}
