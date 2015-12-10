/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import org.apache.log4j.Logger;

import java.util.Map;

public class ThumbnailSystemField extends NavigableFieldImpl
{
    private static final Logger log = Logger.getLogger(ThumbnailSystemField.class);
    private final ThumbnailManager thumbnailManager;

    public ThumbnailSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, ThumbnailManager thumbnailManager, JiraAuthenticationContext authenticationContext)
    {
        super(IssueFieldConstants.THUMBNAIL, "issue.field.thumbnail", "issue.column.heading.thumbnail", templatingEngine, applicationProperties, authenticationContext);
        this.thumbnailManager = thumbnailManager;
    }

    public LuceneFieldSorter getSorter()
    {
        return null;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("charset", getApplicationProperties().getEncoding());
        try
        {
            velocityParams.put("thumbnails", thumbnailManager.getThumbnails(issue, getAuthenticationContext().getLoggedInUser()));
            velocityParams.put("applicationProperties", getApplicationProperties());
        }
        catch (Exception e)
        {
            log.error("Error occurred while generating thumbnails for issue with id '" + issue.getId() + "'.", e);
        }
        return renderTemplate("thumbnail-columnview.vm", velocityParams);
    }
}
