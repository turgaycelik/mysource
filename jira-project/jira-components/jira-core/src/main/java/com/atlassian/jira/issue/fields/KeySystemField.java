/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.statistics.IssueKeyStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import org.apache.lucene.search.SortField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeySystemField extends NavigableFieldImpl
{

    public KeySystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        super(IssueFieldConstants.ISSUE_KEY, "issue.field.issuekey", "issue.column.heading.issuekey", ORDER_ASCENDING, templatingEngine, applicationProperties, authenticationContext);
    }

    public LuceneFieldSorter getSorter()
    {
        return IssueKeyStatisticsMapper.MAPPER;
    }

    @Override
    public List<SortField> getSortFields(final boolean sortOrder)
    {
        List<SortField> sortFields = new ArrayList<SortField>();
        sortFields.add(new SortField(DocumentConstants.PROJECT_KEY, new StringSortComparator(), sortOrder));
        sortFields.add(new SortField(DocumentConstants.ISSUE_KEY_NUM_PART_RANGE, new StringSortComparator(), sortOrder));

        return sortFields;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("applicationProperties", getApplicationProperties());
        return renderTemplate("key-columnview.vm", velocityParams);
    }
}
