package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.AbstractProgressBarSystemField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * This class indexes issue's progress (based on time spent and remaining).
 *
 * @since v3.11
 */
public class ProgressIndexer implements FieldIndexer
{
    private static final Logger log = Logger.getLogger(ProgressIndexer.class);

    private static final String PROGRESS_UNKNOWN = "-1";
    private final FieldVisibilityManager fieldVisibilityManager;
    private final ApplicationProperties applicationProperties;

    public ProgressIndexer(final FieldVisibilityManager fieldVisibilityManager, final ApplicationProperties applicationProperties)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.applicationProperties = applicationProperties;
    }

    public String getId()
    {
        return DocumentConstants.ISSUE_PROGRESS;
    }

    public String getDocumentFieldId()
    {
        return DocumentConstants.ISSUE_PROGRESS;
    }

    // Since this field depends on time tracking we need to delegate and see if time tracking is hidden.
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING) && fieldVisibilityManager.isFieldVisible(IssueFieldConstants.TIMETRACKING, issue);
    }

    public void addIndex(Document doc, Issue issue)
    {
        try
        {
            final Long percentage = AbstractProgressBarSystemField.calculateProgressPercentage(issue.getTimeSpent(), issue.getEstimate());
            if (isFieldVisibleAndInScope(issue))
            {
                doc.add(new Field(
                        getDocumentFieldId(),
                        percentage == null ? PROGRESS_UNKNOWN : percentage.toString(),
                        Field.Store.YES,
                        Field.Index.NOT_ANALYZED_NO_NORMS));
            }
            else
            {
                doc.add(new Field(
                        getDocumentFieldId(),
                        percentage == null ? PROGRESS_UNKNOWN : percentage.toString(),
                        Field.Store.YES,
                        Field.Index.NO));                
            }
        }
        catch (IllegalArgumentException mustBeNegative)
        {
            log.warn("Issue: '" + issue.getKey() + "' has an uncalculable percentage", mustBeNegative);
        }
    }
}
