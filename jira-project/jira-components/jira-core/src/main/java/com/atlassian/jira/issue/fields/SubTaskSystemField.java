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
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinksBeanBuilderFactory;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.SubTaskStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class SubTaskSystemField extends NavigableFieldImpl implements RestAwareField
{
    private final IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory;
    private final SubTaskStatisticsMapper subTaskStatisticsMapper;

    public SubTaskSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            SubTaskStatisticsMapper subTaskStatisticsMapper, IssueLinksBeanBuilderFactory issueLinkBeanBuilderFactory)
    {
        super(IssueFieldConstants.SUBTASKS, "issue.field.subtasks", "issue.column.heading.subtasks", NavigableField.ORDER_ASCENDING,
                templatingEngine, applicationProperties, authenticationContext);
        this.subTaskStatisticsMapper = subTaskStatisticsMapper;
        this.issueLinkBeanBuilderFactory = issueLinkBeanBuilderFactory;
    }

    public LuceneFieldSorter getSorter()
    {
        return subTaskStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("subtasks", issue.getSubTaskObjects());
        velocityParams.put("applicationProperties", getApplicationProperties());
        return renderTemplate("subtask-columnview.vm", velocityParams);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.systemArray(JsonType.ISSUELINKS_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        IssueLinksBeanBuilder issueLinkBeanBuilder = issueLinkBeanBuilderFactory.newIssueLinksBeanBuilder(issue);
        final List<IssueRefJsonBean> subtaskLinks = issueLinkBeanBuilder.buildSubtaskLinks();
        if (subtaskLinks != null)
        {
            return new FieldJsonRepresentation(new JsonData(subtaskLinks));
        }

        return null;
    }
}
