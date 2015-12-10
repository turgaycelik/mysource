package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.project.version.Version;

/**
 * Simple factory used to create version issue counts bean from versions and count data.
 *
 * @since v4.4
 */
public interface VersionIssueCountsBeanFactory
{
    /**
     * Create a VersionBean given the passed Version.
     *
     * @param version the version to convert.
     * @param fixIssueCount the version to convert.
     * @param affectsIssueCount the version to convert.
     * @return the VersionIssueCountsBean from the passed Version.
     */
    VersionIssueCountsBean createVersionBean(Version version, long fixIssueCount, long affectsIssueCount);
}
