package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkType;

import java.net.URI;

/**
 * @since v5.0
 */
public class IssueLinkBeanBuilder
{
    private final JiraBaseUrls jiraBaseUrls;

    public IssueLinkBeanBuilder(JiraBaseUrls jiraBaseUrls)
    {
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public IssueLinkJsonBean buildIssueLinkBean(IssueLinkType issueLinkType, String id)
    {
        URI linkTypeURI = URI.create(jiraBaseUrls.restApi2BaseUrl() + "issueLinkType/" + issueLinkType.getId());
        URI selfURI = URI.create(jiraBaseUrls.restApi2BaseUrl() + "issueLink/" + id);
        IssueLinkJsonBean issueLink = new IssueLinkJsonBean().type(IssueLinkTypeJsonBean.create(issueLinkType, linkTypeURI));
        issueLink.self(selfURI);
        issueLink.id(id);
        return issueLink;
    }

    public  IssueRefJsonBean createIssueRefJsonBean(final Issue issue)
    {
        return new IssueRefJsonBean()
                .id(String.valueOf(issue.getId()))
                .key(issue.getKey())
                .self(URI.create(jiraBaseUrls.restApi2BaseUrl() + "issue/" + issue.getId()))
                .fields(new IssueRefJsonBean.Fields()
                        .summary(issue.getSummary())
                        .status(StatusJsonBean.bean(issue.getStatusObject(), jiraBaseUrls))
                        .issueType(IssueTypeJsonBean.shortBean(issue.getIssueTypeObject(), jiraBaseUrls))
                        .priority(PriorityJsonBean.shortBean(issue.getPriorityObject(), jiraBaseUrls))
                );
    }
}


