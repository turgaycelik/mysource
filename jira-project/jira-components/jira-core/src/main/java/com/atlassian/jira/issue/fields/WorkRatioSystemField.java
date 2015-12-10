/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
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
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.WorkRatioSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.LongFieldStatisticsMapper;
import com.atlassian.jira.issue.worklog.WorkRatio;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

import javax.annotation.Nullable;
import java.util.Map;

public class WorkRatioSystemField extends NavigableFieldImpl implements SearchableField, RestAwareField
{
    private final SearchHandlerFactory searcherHandlerFactory;

    public WorkRatioSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, WorkRatioSearchHandlerFactory handlerFactory)
    {
        super(IssueFieldConstants.WORKRATIO, "issue.field.workratio", "issue.column.heading.workratio", ORDER_ASCENDING, templatingEngine, applicationProperties, authenticationContext);
        this.searcherHandlerFactory = handlerFactory;
    }

    public LuceneFieldSorter getSorter()
    {
        return LongFieldStatisticsMapper.WORK_RATIO;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        if (isWorkEstimateExists(issue))
        {
            velocityParams.put(getId(), getWorkRatio(issue));
        }
        return renderTemplate("workratio-columnview.vm", velocityParams);
    }

    private boolean isWorkEstimateExists(Issue issue)
    {
        return issue.getOriginalEstimate() != null;
    }

    public long getWorkRatio(Issue issue)
    {
        return WorkRatio.getWorkRatio(issue);
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searcherHandlerFactory.createHandler(this);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.system(JsonType.NUMBER_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(WorkRatio.getWorkRatio(issue)));
    }
}
