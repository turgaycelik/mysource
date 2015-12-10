package com.atlassian.jira.web.action.filter;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.server.MailServerManager;

import com.opensymphony.util.TextUtils;

/**
 * Action class for Managing Subscriptions.
 * Was previously a command of ManageFilters, but has been extracted for increase security.  Action now is protected by
 * the user role.
 */
public class ManageSubscriptions extends AbstractFilterAction implements FilterOperationsAction
{
    private final FilterSubscriptionService filterSubscriptionService;
    private final MailServerManager mailServerManager;
    private final UserManager userManager;
    private Collection<com.atlassian.jira.issue.subscription.FilterSubscription> subscriptions;

    public ManageSubscriptions(final IssueSearcherManager issueSearcherManager,
            final FilterSubscriptionService filterSubscriptionService, final MailServerManager mailServerManager,
            final SearchService searchService, final SearchSortUtil searchSortUtil, UserManager userManager)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.filterSubscriptionService = filterSubscriptionService;
        this.mailServerManager = mailServerManager;
        this.userManager = userManager;
    }

    public String doDefault() throws Exception
    {
        // If filter doesn't exist, go to Manage Filters
        return (getFilterId() == null) ? getRedirect("ManageFilters.jspa") : super.doDefault();
    }

    public int getSubscriptionCount()
    {

        return getSubscriptions().size();
    }

    public Collection getSubscriptions()
    {
        if (subscriptions == null)
        {
            subscriptions = filterSubscriptionService.getVisibleFilterSubscriptions(getLoggedInApplicationUser(), getFilter());
        }

        return subscriptions;
    }

    public boolean isMailConfigured()
    {
        final List smtpServers = mailServerManager.getSmtpMailServers();
        return !smtpServers.isEmpty();
    }

    public String doView()
    {
        return SUCCESS;
    }

    public String getSubscriber(com.atlassian.jira.issue.subscription.FilterSubscription subscription)
    {
        final String userKey = subscription.getUserKey();
        final ApplicationUser user = userManager.getUserByKey(userKey);
        if (user == null)
            return userKey;
        return user.getDisplayName();
    }

    public boolean loggedInUserIsOwner(com.atlassian.jira.issue.subscription.FilterSubscription subscription)
    {
        final ApplicationUser loggedInUser = getLoggedInApplicationUser();
        return loggedInUser != null && loggedInUser.getKey().equals(subscription.getUserKey());
    }

    /**
     * Get the tooltip for the for a subscription.
     *
     * @param sub The subscrion to get the tooltip for
     * @return The tooltip
     */
    public String getCronTooltip(com.atlassian.jira.issue.subscription.FilterSubscription sub)
    {
        String cronExpression = filterSubscriptionService.getCronExpression(getJiraServiceContext(), sub);

        return cronExpression == null ? "" : getText("cron.editor.cronstring") + " '" + cronExpression + "'";
    }

    /**
     * Get a pretty version of the cron trigger.  E.g. Every day at 12
     *
     * @param sub The subscription to get the value for.
     * @return A description of the cron trigger id pretty format
     */
    public String getPrettySchedule(com.atlassian.jira.issue.subscription.FilterSubscription sub)
    {
        String cronExpression = filterSubscriptionService.getCronExpression(getJiraServiceContext(), sub);

        return (cronExpression == null) ? "" : filterSubscriptionService.getPrettySchedule(getJiraServiceContext(), cronExpression);
    }

    /**
     * Get the las sent date for a subscription
     *
     * @param sub The subscription to get last send for
     * @return A date suitable for displaying
     */
    public String getLastSent(com.atlassian.jira.issue.subscription.FilterSubscription sub)
    {
        final Date ts = sub.getLastRunTime();

        return (ts == null) ? "Never" : getOutlookDate().formatDMYHMS(ts);

    }

    /**
     * Get the next sent date for a subscription
     *
     * @param sub The subscription to get next send for
     * @return A date suitable for displaying
     */
    public String getNextSend(com.atlassian.jira.issue.subscription.FilterSubscription sub)
    {
        Date nextSendTime = filterSubscriptionService.getNextSendTime(sub);
        return nextSendTime == null ? "" : getDateTimeFormatter().format(nextSendTime);
    }

    public boolean isGroupValid(com.atlassian.jira.issue.subscription.FilterSubscription sub)
    {
        String groupName = sub.getGroupName();
        return TextUtils.stringSet(groupName) && userManager.getGroup(groupName) != null;
    }

}
