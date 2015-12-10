package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.issue.VersionResource;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Implementation of {@link com.atlassian.jira.rest.v2.issue.version.VersionIssueCountsBeanFactory}.
 *
 * @since v4.4
 */
public class VersionIssueCountsBeanFactoryImpl implements VersionIssueCountsBeanFactory
{
    private final UriInfo info;

    public VersionIssueCountsBeanFactoryImpl(UriInfo info)
    {
        //these two are proxied to objects from the current request. Be careful we are hunting AOP.
        this.info = info;
    }

    public VersionIssueCountsBean createVersionBean(Version version, long fixIssueCount, long affectsIssueCount)
    {
        return new VersionIssueCountsBean(fixIssueCount, affectsIssueCount, createSelfURI(version));
    }

    private URI createSelfURI(Version version)
    {
        return info.getBaseUriBuilder().path(VersionResource.class).path(version.getId().toString()).build();
    }
}
