package com.atlassian.jira.rest.v2.issue.watcher;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bc.issue.watcher.WatchingDisabledException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.v2.issue.IssueResource;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.rest.v2.issue.WatchersBean;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

/**
 * Implementation of the WatcherOps interface.
 *
 * @since v4.2
 */
class WatcherOpsImpl implements WatcherOps
{
    /**
     * Logger for this WatcherOpsImpl instance.
     */
    private final Logger log = LoggerFactory.getLogger(WatcherOpsImpl.class);

    /**
     * The WatcherManager instance.
     */
    private final WatcherService watcherService;

    /**
     * A ContextUriInfo.
     */
    private final ContextUriInfo uriInfo;
    private final JiraBaseUrls jiraBaseUrls;

    /**
     * Constructs a new WatcherOpsImpl with the required dependencies.
     *
     * @param watcherService a WatcherManager
     * @param uriInfo a ContextUriInfo
     * @param jiraBaseUrls a JiraBaseUrls
     */
    WatcherOpsImpl(WatcherService watcherService, ContextUriInfo uriInfo, JiraBaseUrls jiraBaseUrls)
    {
        this.watcherService = watcherService;
        this.uriInfo = uriInfo;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public WatchersBean getWatchers(Issue issue, User callingUser)
    {
        return buildBean(issue, callingUser, true);
    }

    private WatchersBean buildBean(final Issue issue, final User callingUser, final boolean includeUserList)
    {
        try
        {
            final WatchersBean.Builder builder = WatchersBean.Builder.create();
            URI watchersUri = uriInfo.getBaseUriBuilder().path(IssueResource.class).path(IssueResource.class, "getIssueWatchers").build(issue.getKey());
            builder.self(watchersUri.toString());

            ServiceOutcome<Pair<Integer, List<User>>> outcome = watcherService.getWatchers(issue, callingUser);

            builder.watchCount(outcome.getReturnedValue().first());

            List<User> watcherUsers = outcome.getReturnedValue().second();
            builder.isWatching(watcherUsers.contains(callingUser));

            if (includeUserList)
            {
                List<UserBean> watcherUserBeans = Lists.newArrayList(Lists.transform(watcherUsers, new ToUserBean(jiraBaseUrls)));
                builder.watchers(watcherUserBeans);
                log.trace("Visible watchers on issue '{}': {}", issue.getKey(), watcherUserBeans);
            }

            // the 'size' always contains the actual number of watchers, regardless of permissions. this is to remain
            // consistent with the web UI.
            return builder.build();
        }
        catch (WatchingDisabledException e)
        {
            // don't report any watcher info
            return null;
        }

    }

    public WatchersBean getWatcherCount(final Issue issue, final User callingUser)
    {
        return buildBean(issue, callingUser, false);
    }

    /**
     * Function object that converts username to UserBean.
     */
    class ToUserBean implements Function<User, UserBean>
    {
        private final JiraBaseUrls jiraBaseUrls;

        ToUserBean(JiraBaseUrls jiraBaseUrls)
        {
            this.jiraBaseUrls = jiraBaseUrls;
        }

        public UserBean apply(@Nullable User user)
        {
            return user != null ? new UserBeanBuilder(jiraBaseUrls).user(user).buildShort() : null;
        }
    }
}
