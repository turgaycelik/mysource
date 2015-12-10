package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class CurrentEstimateIndexer extends BaseFieldIndexer
{
    private final FieldVisibilityManager fieldVisibilityManager;
    private final ApplicationProperties applicationProperties;

    public CurrentEstimateIndexer(final FieldVisibilityManager fieldVisibilityManager, final ApplicationProperties applicationProperties)
    {
        super(fieldVisibilityManager);
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.applicationProperties = applicationProperties;
    }

    public String getId()
    {
        return SystemSearchConstants.forCurrentEstimate().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forCurrentEstimate().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING) && fieldVisibilityManager.isFieldVisible(IssueFieldConstants.TIMETRACKING, issue);
    }

    public void addIndex(Document doc, Issue issue)
    {
        indexLongAsPaddedKeywordWithDefault(doc, getDocumentFieldId(), issue.getEstimate(), NO_VALUE_INDEX_VALUE, issue);
        indexLongAsPaddedKeywordWithDefault(doc, DocumentConstants.ISSUE_TIME_SPENT, issue.getTimeSpent(), NO_VALUE_INDEX_VALUE, issue);
    }
}
