package com.atlassian.jira.rest.util;

import com.atlassian.jira.issue.fields.rest.json.beans.StatusCategoryJsonBean;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;

import javax.ws.rs.core.UriInfo;

/**
 * @since v6.1
 */
public class StatusCategoryHelper
{
    private final ResourceUriBuilder uriBuilder;

    public StatusCategoryHelper(final ResourceUriBuilder uriBuilder)
    {
        this.uriBuilder = uriBuilder;
    }

    public StatusCategoryJsonBean createStatusCategoryBean(final StatusCategory statusCategory, final UriInfo uriInfo, final Class resourceClass)
    {
        if (null == statusCategory)
        {
            return null;
        }

        return StatusCategoryJsonBean.bean(
                uriBuilder.build(uriInfo, resourceClass, String.valueOf(statusCategory.getId())).toString(),
                statusCategory.getId(),
                statusCategory.getKey(),
                statusCategory.getColorName(),
                statusCategory.getTranslatedName()
        );
    }
}
