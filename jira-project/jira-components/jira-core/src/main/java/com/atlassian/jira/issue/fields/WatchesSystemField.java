package com.atlassian.jira.issue.fields;

import com.atlassian.jira.component.ComponentAccessor;
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
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.WatchersJsonBean;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.WatchesStatisticsMapper;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

import java.util.Map;

/**
 * @since 4.4
 */
public class WatchesSystemField extends NavigableFieldImpl implements RestAwareField
{
    private final JiraBaseUrls jiraBaseUrls;

    public WatchesSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, JiraBaseUrls jiraBaseUrls)
    {
        super(IssueFieldConstants.WATCHES, "issue.field.watch", "issue.column.heading.watch", NavigableField.ORDER_DESCENDING, templatingEngine, applicationProperties, authenticationContext);
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public LuceneFieldSorter getSorter()
    {
        return WatchesStatisticsMapper.MAPPER;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put(getId(), issue.getWatches());
        return renderTemplate("watches-columnview.vm", velocityParams);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.systemArray(JsonType.WATCHES_TYPE, IssueFieldConstants.WATCHES);
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        WatcherManager watcherManager = ComponentAccessor.getWatcherManager();
        if (watcherManager.isWatchingEnabled())
        {
            return new FieldJsonRepresentation(new JsonData(WatchersJsonBean.shortBean(issue.getKey(), issue.getWatches(), watcherManager.isWatching(authenticationContext.getLoggedInUser(), issue), jiraBaseUrls)));
        }
        return null;
    }

}
