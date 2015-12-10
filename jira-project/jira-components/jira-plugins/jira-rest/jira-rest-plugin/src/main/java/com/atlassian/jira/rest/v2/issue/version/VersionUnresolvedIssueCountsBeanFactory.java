package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.project.version.Version;

/**
 * Simple factory used to create unresolved issue counts bean from version and count data.
 *
 * @since v4.4
 */
public interface VersionUnresolvedIssueCountsBeanFactory
{
    /**
         * Creates a {@link VersionUnresolvedIssueCountsBean} based on the data provided
         *
         * @param version version containing the unresolved issues
         * @param unresolvedIssueCount the number of unresolved issues in the version
         * @return the {@link VersionUnresolvedIssueCountsBean} for the passed in version
         */
    VersionUnresolvedIssueCountsBean createVersionBean(Version version, long unresolvedIssueCount);
}
