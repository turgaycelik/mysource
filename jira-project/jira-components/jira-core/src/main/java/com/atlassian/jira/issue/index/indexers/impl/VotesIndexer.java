package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;

import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forVotes;

public class VotesIndexer extends BaseFieldIndexer
{
    private final VoteManager voteManager;

    public VotesIndexer(final FieldVisibilityManager fieldVisibilityManager, final VoteManager voteManager)
    {
        super(fieldVisibilityManager);
        this.voteManager = voteManager;
    }

    public String getId()
    {
        return forVotes().getFieldId();
    }

    public String getDocumentFieldId()
    {
        return forVotes().getIndexField();
    }

    @Override
    public boolean isFieldVisibleAndInScope(final Issue issue)
    {
        // You can not hide the Votes via the FieldConfiguration so we only need to check if it is enabled or disabled
        return voteManager.isVotingEnabled();
    }

    public void addIndex(final Document doc, final Issue issue)
    {
        indexLongAsPaddedKeywordWithDefault(doc, getDocumentFieldId(), issue.getVotes(), 0L, issue);
    }
}
