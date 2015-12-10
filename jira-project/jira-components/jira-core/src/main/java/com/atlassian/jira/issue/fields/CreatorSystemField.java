package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.CreatorSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.statistics.CreatorStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.EmailFormatter;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class CreatorSystemField extends NavigableFieldImpl implements SearchableField, RestAwareField
{
    private static final Logger log = Logger.getLogger(ReporterSystemField.class);

    private final SearchHandlerFactory searchHandlerFactory;
    private final JiraBaseUrls jiraBaseUrls;
    private final CreatorStatisticsMapper creatorStatisticsMapper;
    private final EmailFormatter emailFormatter;

    public CreatorSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            CreatorSearchHandlerFactory searchHandlerFactory, final JiraBaseUrls jiraBaseUrls, final CreatorStatisticsMapper creatorStatisticsMapper, final EmailFormatter emailFormatter)
    {
        super(IssueFieldConstants.CREATOR, "issue.field.creator", "issue.column.heading.creator", ORDER_DESCENDING, templatingEngine, applicationProperties, authenticationContext);
        this.searchHandlerFactory = searchHandlerFactory;
        this.jiraBaseUrls = jiraBaseUrls;
        this.creatorStatisticsMapper = creatorStatisticsMapper;
        this.emailFormatter = emailFormatter;
    }

    public LuceneFieldSorter getSorter()
    {
        return creatorStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        final Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        try
        {
            final String creatorUserId = issue.getCreatorId();
            if (creatorUserId != null)
            {
                velocityParams.put("creatorUserkey", creatorUserId);
            }
        }
        catch (DataAccessException e)
        {
            log.debug("Error occurred retrieving creator", e);
        }
        return renderTemplate("creator-columnview.vm", velocityParams);
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searchHandlerFactory.createHandler(this);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        String autoCompleteUrl = String.format("%s/rest/api/latest/user/search?username=", jiraBaseUrls.baseUrl());
        return new FieldTypeInfo(null, autoCompleteUrl);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.USER_TYPE, IssueFieldConstants.CREATOR);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(UserJsonBean.shortBean(issue.getCreator(), jiraBaseUrls, authenticationContext.getUser(), emailFormatter)));
    }
}
