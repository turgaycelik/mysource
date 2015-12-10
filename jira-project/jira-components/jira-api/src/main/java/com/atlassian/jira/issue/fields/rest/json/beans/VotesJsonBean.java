package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;

import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

/**
 * @since v5.0
 */
public class VotesJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private long votes;

    private boolean hasVoted;

    // This will either be a Collection<UserBean> or an ErrorCollection explaining that you don't have permission
    // to view the voters for this issue.
    @JsonProperty
    private Collection<UserJsonBean> voters;

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public long getVotes()
    {
        return votes;
    }

    public void setVotes(long votes)
    {
        this.votes = votes;
    }

    public boolean isHasVoted()
    {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted)
    {
        this.hasVoted = hasVoted;
    }

    public Collection<UserJsonBean> getVoters()
    {
        return voters;
    }

    public void setVoters(Collection<UserJsonBean> voters)
    {
        this.voters = voters;
    }

    /**
     *
     * @return null if the input is null
     */
    public static VotesJsonBean shortBean(final String issueKey, final long votes, final boolean hasVoted, final JiraBaseUrls urls)
    {
        final VotesJsonBean bean = new VotesJsonBean();
        bean.self = urls.restApi2BaseUrl() + "issue/" + issueKey +  "/votes";
        bean.hasVoted = hasVoted;
        bean.votes = votes;

        return bean;
    }

    /**
     *
     * @return null if the input is null
     * @deprecated Use {@link #fullBean(String, long, boolean, java.util.Collection, JiraBaseUrls, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static VotesJsonBean fullBean(final String issueKey, final long votes, final boolean hasVoted, Collection<User> voters, final JiraBaseUrls urls)
    {
        return fullBean(issueKey, votes, hasVoted, voters, urls, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     *
     * @return null if the input is null
     */
    public static VotesJsonBean fullBean(final String issueKey, final long votes, final boolean hasVoted, Collection<User> voters, final JiraBaseUrls urls, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        final VotesJsonBean bean = shortBean(issueKey, votes, hasVoted, urls);

        Collection<UserJsonBean> result = Lists.newArrayListWithCapacity(voters.size());
        for (User from : voters)
        {
            result.add(UserJsonBean.shortBean(from, urls, loggedInUser, emailFormatter));
        }

        bean.voters = result;

        return bean;
    }

}
