package com.atlassian.jira.rest.v2.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Suppliers;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.api.expand.PagedListWrapper;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.google.common.base.Supplier;
import com.google.common.collect.Ordering;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Wraps a list of Users and pages over them
 *
 * @since v6.0
 */
public class UserBeanListWrapper extends PagedListWrapper<UserBean, User>
{
    private final Ordering<User> userOrdering = Ordering.from(new UserBestNameComparator());
    private final JiraBaseUrls jiraBaseUrls;
    // We can't keep just list there, as this will cause EntityCrawler to fail due to existance of two collections in
    // class (see PagedListWrapper.items)
    private final Supplier<Collection<User>> sharedUsersSupplier;

    @SuppressWarnings ("UnusedDeclaration") // necessary for JAXB
    private UserBeanListWrapper()
    {
        this(null, Collections.<User>emptyList(), 0);
    }

    public UserBeanListWrapper(final JiraBaseUrls jiraBaseUrls, final Collection<User> sharedUsers, final int maxResults)
    {
        super(sharedUsers.size(), maxResults);
        this.jiraBaseUrls = jiraBaseUrls;
        this.sharedUsersSupplier = Suppliers.ofInstance(sharedUsers);
    }

    public UserBean fromBackedObject(final User user)
    {
        return new UserBeanBuilder(jiraBaseUrls)
                .user(user)
                .buildShort();
    }

    @Override
    public int getBackingListSize()
    {
        return sharedUsersSupplier.get().size();
    }

    @Override
    public List<User> getOrderedList(final int startIndex, final int endIndex)
    {
        final List<User> sortedUsers = userOrdering.leastOf(sharedUsersSupplier.get(), endIndex + 1);
        return sortedUsers.subList(startIndex, endIndex + 1);
    }


}
