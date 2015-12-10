package com.atlassian.jira.rest.v2.issue.version;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.issue.VersionResource;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Simple boilerplate implementation of {@link VersionUnresolvedIssueCountsBeanFactory}
 * that provides URL information for the {@link Version} to the created {@link VersionUnresolvedIssueCountsBean}.
 *
 * @since v4.4
 */
public class VersionUnresolvedIssueCountsBeanFactoryImpl implements VersionUnresolvedIssueCountsBeanFactory
{
    private final UriInfo info;

    public VersionUnresolvedIssueCountsBeanFactoryImpl(UriInfo info)
    {
        //these two are proxied to objects from the current request. Be careful we are hunting AOP.
        this.info = info;
    }

    public VersionUnresolvedIssueCountsBean createVersionBean(Version version, long unresolvedIssueCount)
    {
        return new VersionUnresolvedIssueCountsBean(unresolvedIssueCount, createSelfURI(version));
    }

    private URI createSelfURI(Version version)
    {
        return info.getBaseUriBuilder().path(VersionResource.class).path(version.getId().toString()).build();
    }
}
