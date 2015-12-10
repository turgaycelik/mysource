package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.api.expand.PagedListWrapper;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.Ordering;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * Wraps a list of FilterSubscriptions and pages over them
 *
 * @since v6.0
 */
public class FilterSubscriptionBeanListWrapper extends PagedListWrapper<FilterSubscriptionBean, GenericValue>
{

    private FilterSubscriptionService filterSubscriptionService;
    private SearchRequest filter;
    private ApplicationUser user;
    private UserManager userManager;
    private JiraBaseUrls jiraBaseUrls;

    private LazyReference<Collection <GenericValue>> visibleSubscriptionsReference = new LazyReference<Collection<GenericValue>>()
    {
        @Override
        protected Collection<GenericValue> create() throws Exception
        {
            return filterSubscriptionService.getVisibleSubscriptions(user, filter);
        }
    };

    public FilterSubscriptionBeanListWrapper(FilterSubscriptionService filterSubscriptionService,
            UserManager userManager, ApplicationUser user, SearchRequest filter, JiraBaseUrls jiraBaseUrls)
    {
        super(0, FilterBean.MAX_USER_LIMIT);
        this.filter = filter;
        this.filterSubscriptionService = filterSubscriptionService;
        this.user = user;
        this.userManager = userManager;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    private FilterSubscriptionBeanListWrapper()
    {
        super(0,0);
    }

    /**
     * Returns an empty FilterSubscriptionBeanListWrapper.
     *
     *
     * @return an empty FilterSubscriptionBeanListWrapper
     */
    public static  FilterSubscriptionBeanListWrapper empty()
    {
        return new FilterSubscriptionBeanListWrapper(null, null, null, null, null);
    }

    @Override
    public FilterSubscriptionBean fromBackedObject(GenericValue filterSubscription)
    {
        return new FilterSubscriptionBeanBuilder().subscription(filterSubscription).build();
    }

    @Override
    public int getBackingListSize()
    {
        return visibleSubscriptionsReference.get().size();
    }

    @Override
    public List<GenericValue> getOrderedList(int startIndex, int endIndex)
    {
        List<GenericValue> subscriptions = Ordering.natural().immutableSortedCopy(visibleSubscriptionsReference.get());
        return subscriptions.subList(startIndex,endIndex+1);
    }


    class FilterSubscriptionBeanBuilder
    {
        private GenericValue subscription;

        public FilterSubscriptionBeanBuilder subscription(GenericValue subscription)
        {
            this.subscription = subscription;
            return this;
        }

        private UserBean buildUserBean()
        {
            // Mismatch due to legacy column names from before users were renameable
            final String userKey = subscription.getString("username");
            return new UserBeanBuilder(jiraBaseUrls).user(userManager.getUserByKey(userKey)).buildShort();
        }

        private GroupJsonBean buildGroupBean()
        {
            final String groupName = subscription.getString("group");
            return validGroupName(groupName) ? new GroupJsonBeanBuilder(jiraBaseUrls).name(groupName).build() : null;
        }

        private boolean validGroupName(String groupName)
        {
            return TextUtils.stringSet(groupName) && userManager.getGroup(groupName) != null;
        }

        public FilterSubscriptionBean build()
        {
            return new FilterSubscriptionBean(subscription.getLong("id"), buildUserBean(), buildGroupBean());
        }
    }

}
