package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.renderer.links.Link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

/** Defines a link for a link to another jira issue */
public class JiraIssueLink extends Link
{
    private static final Logger log = LoggerFactory.getLogger(JiraIssueLink.class);
    private String issueKey;

    public JiraIssueLink(String issueKey) throws ParseException
    {
        this(issueKey, null);
    }

    public JiraIssueLink(String issueKey, String linkTitle) throws ParseException
    {
        super(issueKey);
        setupIssueLink(issueKey, linkTitle);
    }

    private void setupIssueLink(String key, String linkTitle) throws ParseException
    {
        Issue issue;
        try
        {
            issue = ComponentAccessor.getIssueManager().getIssueObject(key);

            if (issue != null)
            {
                if (canCurrentUserSeeIssue(issue))
                {
                    title = issue.getSummary();
                }
                else
                {
                    // JRA-14893: if a user cannot see an issue, do not linkify
                    throw new ParseException("Current user cannot see issue with key " + key, 0);
                }
                issueKey = issue.getKey();
                url = getBaseUrl() + "/browse/" + issueKey;

                if (issue.getResolutionObject() != null)
                {
                    linkBody = "-" + (linkTitle == null ? key : linkTitle) + "-";
                }
                else
                {
                    linkBody = linkTitle == null ? key : linkTitle;
                }
            }
        }
        catch (Exception e)
        {
            log.debug("Unable to resolve an issue with key: {}", key);
            throw new IllegalArgumentException("No issue found with key " + key);
        }

        if (issue == null)
        {
            throw new ParseException("Unable to resolve the JIRA issue for key: " + key, 0);
        }

    }

    boolean canCurrentUserSeeIssue(Issue issue)
    {
        final ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getUser();
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.BROWSE, issue, user);
    }

    /**
     * Returns the base URL from VelocityRequestContext
     *
     * @return the base URL
     * @see VelocityRequestContext
     */
    private String getBaseUrl()
    {
        final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        return velocityRequestContext.getCanonicalBaseUrl();
    }

    @Override
    public String getLinkAttributes()
    {
        return String.format("%s class=\"issue-link\" data-issue-key=\"%s\"", super.getLinkAttributes(), issueKey);
    }
}
