package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.worklog.WorkRatio;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class WorkRatioIndexer extends BaseFieldIndexer
{
    private final FieldVisibilityManager fieldVisibilityManager;
    private final ApplicationProperties applicationProperties;

    public WorkRatioIndexer(final FieldVisibilityManager fieldVisibilityManager, final ApplicationProperties applicationProperties)
    {
        super(fieldVisibilityManager);
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.applicationProperties = applicationProperties;
    }

    public String getId()
    {
        return SystemSearchConstants.forWorkRatio().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forWorkRatio().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING) && fieldVisibilityManager.isFieldVisible(IssueFieldConstants.TIMETRACKING, issue);
    }

    public void addIndex(Document doc, Issue issue)
    {
        // Add actual vs estimated work ratio to index for range query - only add work ratio if a time estimate for issue exists
        if (issue.getOriginalEstimate() != null)
        {
            doc.add(new Field(getDocumentFieldId(), WorkRatio.getPaddedWorkRatio(issue.getGenericValue()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
        }
    }
}
