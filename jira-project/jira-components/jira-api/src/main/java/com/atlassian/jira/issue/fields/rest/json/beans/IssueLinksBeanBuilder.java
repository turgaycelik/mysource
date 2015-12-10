package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Builder for {@link IssueLinkJsonBean} instances.
 *
 * @since v5.0
 */
public class IssueLinksBeanBuilder
{
    private final ApplicationProperties applicationProperties;
    private final IssueLinkManager issueLinkManager;
    private final JiraAuthenticationContext authContext;
    private final JiraBaseUrls jiraBaseUrls;
    private final Issue issue;

    public IssueLinksBeanBuilder(final ApplicationProperties applicationProperties, final IssueLinkManager issueLinkManager,
            final JiraAuthenticationContext authContext,
            JiraBaseUrls jiraBaseUrls, final Issue issue)
    {
        this.applicationProperties = applicationProperties;
        this.issueLinkManager = issueLinkManager;
        this.authContext = authContext;
        this.jiraBaseUrls = jiraBaseUrls;
        this.issue = issue;
    }

    /**
     * Build a List of IssueLinkJsonBean objects representing the issue links for the current Issue object.
     *
     * @return a List of IssueLinkJsonBean objects, or null if issue linking is disabled, or an empty List if no issue links
     *         exist
     */
    public List<IssueLinkJsonBean> buildIssueLinks()
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            // issue linking disabled
            return null;
        }

        List<IssueLinkJsonBean> linkBeans = Lists.newArrayList();
        LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, authContext.getLoggedInUser());
        Set<IssueLinkType> linkTypes = linkCollection.getLinkTypes();
        if (linkTypes != null)
        {
            for (IssueLinkType issueLinkType : linkTypes)
            {
                List<Issue> outwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
                if (outwardIssues != null)
                {
                    for (Issue issue : outwardIssues)
                    {
                        IssueLink issueLink = issueLinkManager.getIssueLink(this.issue.getId(), issue.getId(), issueLinkType.getId());
                        linkBeans.add(buildLink(issueLinkType, issue, true, issueLink.getId().toString()));
                    }
                }

                List<Issue> inwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
                if (inwardIssues != null)
                {
                    for (Issue issue : inwardIssues)
                    {
                        IssueLink issueLink = issueLinkManager.getIssueLink(issue.getId(), this.issue.getId(), issueLinkType.getId());
                        linkBeans.add(buildLink(issueLinkType, issue, false, issueLink.getId().toString()));
                    }
                }
            }
        }

        return linkBeans;
    }

    /**
     * Builds an IssueLinkJsonBean from an IssueLink.
     *
     * @param issueLinkType an IssueLinkType instance
     * @param issue an Issue that is linked to this.issue
     * @param isOutbound a boolean indicating whether it's an outbound link
     * @param id issue link id
     * @return an IssueLinkJsonBean
     */
    public IssueLinkJsonBean buildLink(IssueLinkType issueLinkType, Issue issue, boolean isOutbound, String id)
    {
        IssueLinkBeanBuilder issueLinkBeanBuilder = new IssueLinkBeanBuilder(jiraBaseUrls);
        IssueLinkJsonBean issueLinkJsonBean = issueLinkBeanBuilder.buildIssueLinkBean(issueLinkType, id);
        return isOutbound ? issueLinkJsonBean.outwardIssue(issueLinkBeanBuilder.createIssueRefJsonBean(issue)) : issueLinkJsonBean.inwardIssue(issueLinkBeanBuilder.createIssueRefJsonBean(issue));
    }

    /**
     * Build an IssueLinkJsonBean object representing the parent link for the current Issue object.
     *
     * @return an IssueLinkJsonBean object, or null if no parent link exists
     */
    public IssueRefJsonBean buildParentLink()
    {
        Issue parent = issue.getParentObject();
        if (parent == null)
        {
            return null;
        }
        IssueLinkBeanBuilder issueLinkBeanBuilder = new IssueLinkBeanBuilder(jiraBaseUrls);
        return issueLinkBeanBuilder.createIssueRefJsonBean(parent);
    }

    /**
     * Build a List of IssueLinkJsonBean objects representing the sub-task links for the current Issue object.
     *
     * @return a List of IssueLinkJsonBean objects, or an empty List if no sub-task links exist
     */
    public List<IssueRefJsonBean> buildSubtaskLinks()
    {
        Collection<Issue> subtasks = issue.getSubTaskObjects();
        if (subtasks == null)
        {
            return Collections.emptyList();
        }
        IssueLinkBeanBuilder issueLinkBeanBuilder = new IssueLinkBeanBuilder(jiraBaseUrls);

        List<IssueRefJsonBean> subtaskLinks = Lists.newArrayListWithCapacity(subtasks.size());
        for (Issue subtask : subtasks)
        {
            subtaskLinks.add(issueLinkBeanBuilder.createIssueRefJsonBean(subtask));
        }

        return subtaskLinks;
    }


}
