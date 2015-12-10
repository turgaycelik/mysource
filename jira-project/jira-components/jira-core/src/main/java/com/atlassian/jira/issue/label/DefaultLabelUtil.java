package com.atlassian.jira.issue.label;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DefaultLabelUtil implements LabelUtil
{
    private final SearchService searchService;
    private final CustomFieldManager customFieldManager;

    public DefaultLabelUtil(final SearchService searchService, final CustomFieldManager customFieldManager)
    {
        this.searchService = searchService;
        this.customFieldManager = customFieldManager;
    }

    @Override
    public String getLabelJql(final User user, final String label)
    {
        final JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
        JqlClauseBuilder jqlClauseBuilder = jqlQueryBuilder.where();
        jqlClauseBuilder.labels(label);
        return searchService.getQueryString(user, jqlQueryBuilder.buildQuery());
    }

    @Override
    public String getLabelJql(final User user, final Long customFieldId, final String label)
    {
        Assertions.notNull("customFieldId", customFieldId);

        final CustomField labelField = customFieldManager.getCustomFieldObject(customFieldId);
        final Set<Long> projectIds = new HashSet<Long>();
        @SuppressWarnings ("unchecked")
        final List<GenericValue> associatedProjects = labelField.getAssociatedProjects();
        if (null != associatedProjects)
        {
            for (GenericValue projectGv : associatedProjects)
            {
                if (null != projectGv)
                {
                    // Sometimes JIRA gives a collection of nulls
                    projectIds.add(projectGv.getLong("id"));
                }
            }
        }

        return getLabelJql(user, labelField, projectIds, getAssociatedIssueTypeIds(labelField), label);
    }

    @Override
    public String getLabelJqlForProject(final User user, final Long projectId, final String label)
    {
        final JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
        JqlClauseBuilder jqlClauseBuilder = jqlQueryBuilder.where();
        jqlClauseBuilder.project(projectId).and();
        if(label == null)
        {
            jqlClauseBuilder.labelsIsEmpty();
        }
        else
        {
            jqlClauseBuilder.labels(label);
        }
        return searchService.getQueryString(user, jqlQueryBuilder.buildQuery());
    }

    @Override
    public String getLabelJqlForProject(final User user, final Long projectId, final Long customFieldId, final String label)
    {
        Assertions.notNull("customFieldId", customFieldId);

        final CustomField labelField = customFieldManager.getCustomFieldObject(customFieldId);
        return getLabelJql(user, labelField, CollectionBuilder.newBuilder(projectId).asSet(), getAssociatedIssueTypeIds(labelField), label);
    }

    private String getLabelJql(User user, CustomField labelField, Set<Long> associatedProjectIds, Set<String> associatedIssueTypeIds, String label)
    {
        final JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
        JqlClauseBuilder jqlClauseBuilder = jqlQueryBuilder.where();

        boolean hasAssociatedProjectIds = null != associatedProjectIds && !associatedProjectIds.isEmpty();
        boolean hasAssociatedIssueTypeIds = null != associatedIssueTypeIds && !associatedIssueTypeIds.isEmpty();

        if (hasAssociatedProjectIds || hasAssociatedIssueTypeIds)
        {
            jqlClauseBuilder = jqlClauseBuilder.sub().defaultAnd();

            if (hasAssociatedProjectIds)
            {
                jqlClauseBuilder = jqlClauseBuilder.project(associatedProjectIds.toArray(new Long[associatedProjectIds.size()]));
            }

            if (hasAssociatedIssueTypeIds)
            {
                jqlClauseBuilder = jqlClauseBuilder.issueType(associatedIssueTypeIds.toArray(new String[associatedIssueTypeIds.size()]));
            }

            jqlClauseBuilder.endsub().and().customField(labelField.getIdAsLong()).eq().string(label);
        }
        else
        {
            jqlClauseBuilder.customField(labelField.getIdAsLong()).eq().string(label);
        }

        return searchService.getQueryString(user, jqlQueryBuilder.buildQuery());
    }

    private Set<String> getAssociatedIssueTypeIds(CustomField labelField)
    {
        Set<String> issueTypeIds = new TreeSet<String>();
        @SuppressWarnings ("unchecked")
        List<GenericValue> associatedIssueTypes = labelField.getAssociatedIssueTypes();
        if (null != associatedIssueTypes)
        {
            for (GenericValue issueTypeGv : associatedIssueTypes)
            {
                if (null != issueTypeGv) // Sometimes JIRA gives a collection of nulls
                {
                    issueTypeIds.add(issueTypeGv.getString("id"));
                }
            }
        }
        return issueTypeIds;
    }
}
