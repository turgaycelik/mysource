package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.rest.api.expand.PagedListWrapper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Ordering;

import java.util.List;

/**
 * Wraps a list of Users and pages over them
 *
 * @since v6.0
 */
public class UserJsonBeanListWrapper extends PagedListWrapper<UserJsonBean, User>
{
    private final Ordering<User> userOrdering = Ordering.from(new UserBestNameComparator());
    private final JiraBaseUrls jiraBaseUrls;
    // We can't keep just list there, as this will cause EntityCrawler to fail due to existance of two collections in
    // class (see PagedListWrapper.items)
    private final Supplier<List<User>> usersSupplier;
    private final ApplicationUser loggedInUser;
    private final EmailFormatter emailFormatter;

    public UserJsonBeanListWrapper(final JiraBaseUrls jiraBaseUrls, final List<User> users, final int maxResults, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        super(users.size(), maxResults);
        this.jiraBaseUrls = jiraBaseUrls;
        this.usersSupplier = Suppliers.ofInstance(users);
        this.loggedInUser = loggedInUser;
        this.emailFormatter = emailFormatter;
    }

    public UserJsonBean fromBackedObject(final User user)
    {
        return UserJsonBean.shortBean(user, jiraBaseUrls, loggedInUser, emailFormatter);
    }

    @Override
    public int getBackingListSize()
    {
        return usersSupplier.get().size();
    }

    @Override
    public List<User> getOrderedList(final int startIndex, final int endIndex)
    {
        final List<User> sortedUsers = userOrdering.leastOf(usersSupplier.get(), endIndex + 1);
        return sortedUsers.subList(startIndex, endIndex + 1);
    }
}
