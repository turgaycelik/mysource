package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.vote.IssueVoterAccessor;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

public class VoterIndexer extends UserFieldIndexer
{
    private final IssueVoterAccessor voterAccessor;

    public VoterIndexer(final FieldVisibilityManager fieldVisibilityManager, final IssueVoterAccessor voterAccessor)
    {
        super(fieldVisibilityManager);
        this.voterAccessor = voterAccessor;
    }

    public String getId()
    {
        return SystemSearchConstants.forVoters().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return SystemSearchConstants.forVoters().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        // You can not hide the Votes via the FieldConfiguration so we only need to check if it is enabled or disabled
        return voterAccessor.isVotingEnabled();
    }

    public void addIndex(final Document doc, final Issue issue)
    {
        for (final String userkey : voterAccessor.getVoterUserkeys(issue))
        {
            indexUserKey(doc, getDocumentFieldId(), userkey, issue);
        }
    }
}
