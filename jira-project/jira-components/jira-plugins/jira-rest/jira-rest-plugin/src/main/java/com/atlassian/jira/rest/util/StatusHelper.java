package com.atlassian.jira.rest.util;

import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.StatusCategoryResource;

import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @since v6.0
 */
public class StatusHelper
{

    private final JiraBaseUrls jiraBaseUrls;
    private final ResourceUriBuilder uriBuilder;
    private final StatusCategoryHelper statusCategoryHelper;

    public StatusHelper(final JiraBaseUrls jiraBaseUrls, final ResourceUriBuilder uriBuilder, StatusCategoryHelper statusCategoryHelper)
    {
        this.jiraBaseUrls = jiraBaseUrls;
        this.uriBuilder = uriBuilder;
        this.statusCategoryHelper = statusCategoryHelper;
    }

    public StatusJsonBean createStatusBean(final Status status, final UriInfo uriInfo, final Class resourceClass)
    {
        String absoluteIconUrl;
        try
        {
            absoluteIconUrl = new URL(status.getIconUrl()).toString();
        }
        catch (MalformedURLException e)
        {
            absoluteIconUrl = jiraBaseUrls.baseUrl() + status.getIconUrl();
        }

        return StatusJsonBean.bean(
                status.getId(),
                status.getNameTranslation(),
                uriBuilder.build(uriInfo, resourceClass, status.getId()).toString(),
                absoluteIconUrl,
                status.getDescTranslation(),
                statusCategoryHelper.createStatusCategoryBean(status.getStatusCategory(), uriInfo, StatusCategoryResource.class)
        );
    }
}
