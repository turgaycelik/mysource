package com.atlassian.jira.issue.link;

import org.ofbiz.core.entity.GenericValue;

public interface IssueLinkCreator
{
    IssueLink createIssueLink(GenericValue issueLinkGV);
}
