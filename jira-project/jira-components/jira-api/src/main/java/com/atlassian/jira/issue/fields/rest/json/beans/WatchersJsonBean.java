package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;

import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

/**
 * @since v5.0
 */
public class WatchersJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private long watchCount;

    @JsonProperty("isWatching")
    private boolean watching;

    // This will either be a Collection<UserBean> or an ErrorCollection explaining that you don't have permission
    // to view the watcherrs for this issue.
    @JsonProperty
    private Collection<UserJsonBean> watchers;

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public long getWatchCount()
    {
        return watchCount;
    }

    public void setWatchCount(long watchCount)
    {
        this.watchCount = watchCount;
    }

    @JsonIgnore
    public boolean isWatching()
    {
        return watching;
    }

    public void setWatching(boolean watching)
    {
        this.watching = watching;
    }

    public Collection<UserJsonBean> getWatchers()
    {
        return watchers;
    }

    public void setWatchers(Collection<UserJsonBean> watchers)
    {
        this.watchers = watchers;
    }

    /**
     *
     * @return null if the input is null
     */
    public static WatchersJsonBean shortBean(final String issueKey, final long watchers, final boolean isWatching, final JiraBaseUrls urls)
    {
        final WatchersJsonBean bean = new WatchersJsonBean();
        bean.self = urls.restApi2BaseUrl() + "issue/" + issueKey +  "/watchers";
        bean.watching = isWatching;
        bean.watchCount = watchers;

        return bean;
    }

    /**
     *
     * @return null if the input is null
     * @deprecated Use {@link #fullBean(String, long, boolean, java.util.Collection, JiraBaseUrls, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.util.EmailFormatter)}
     */
    @Deprecated
    public static WatchersJsonBean fullBean(final String issueKey, final long watchers, final boolean isWatching, Collection<User> watcherrs, final JiraBaseUrls urls)
    {
        return fullBean(issueKey, watchers, isWatching, watcherrs, urls, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    /**
     *
     * @return null if the input is null
     */
    public static WatchersJsonBean fullBean(final String issueKey, final long watchers, final boolean isWatching, Collection<User> watcherrs, final JiraBaseUrls urls, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        final WatchersJsonBean bean = shortBean(issueKey, watchers, isWatching, urls);

        Collection<UserJsonBean> result = Lists.newArrayListWithCapacity(watcherrs.size());
        for (User from : watcherrs)
        {
            result.add(UserJsonBean.shortBean(from, urls, loggedInUser, emailFormatter));
        }

        bean.watchers = result;
        
        return bean;
    }

}
