package com.atlassian.jira.rest.v2.issue.component;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.rest.v2.issue.ComponentResource;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Implementation of {@link com.atlassian.jira.rest.v2.issue.component.ComponentIssueCountsBeanFactory}.
 *
 * @since v4.4
 */
public class ComponentIssueCountsBeanFactoryImpl implements ComponentIssueCountsBeanFactory
{
    private final UriInfo info;

    public ComponentIssueCountsBeanFactoryImpl(UriInfo info)
    {
        //these two are proxied to objects from the current request. Be careful we are hunting AOP.
        this.info = info;
    }

    public ComponentIssueCountsBean createComponentBean(ProjectComponent component, long issueCount)
    {
        return new ComponentIssueCountsBean(issueCount, createSelfURI(component));
    }

    private URI createSelfURI(ProjectComponent component)
    {
        return info.getBaseUriBuilder().path(ComponentResource.class).path(component.getId().toString()).build();
    }
}
