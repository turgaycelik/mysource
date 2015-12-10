package com.atlassian.jira.issue.link;

import com.atlassian.jira.issue.IssueManager;
import org.ofbiz.core.entity.GenericValue;

public class DefaultIssueLinkCreator implements IssueLinkCreator
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueManager issueManager;
    
    public DefaultIssueLinkCreator(IssueLinkTypeManager issueLinkTypeManager, IssueManager issueManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueManager = issueManager;
    }

    public IssueLink createIssueLink(GenericValue issueLinkGV)
    {
         return new IssueLinkImpl(issueLinkGV, issueLinkTypeManager, issueManager);
    }
}
