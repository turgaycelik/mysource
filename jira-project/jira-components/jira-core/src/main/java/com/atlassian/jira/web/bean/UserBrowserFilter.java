/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.comparator.UserNameComparator;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UserBrowserFilter extends PagerFilter<User>
{
    public String emailFilter = null;
    public String userNameFilter = null;
    public String fullNameFilter = null;
    public String group = null;
    private final Locale userLocale;

    public UserBrowserFilter(final Locale userLocale)
    {
        this.userLocale = userLocale;
    }

    public String getEmailFilter()
    {
        return emailFilter;
    }

    public void setEmailFilter(final String emailFilter)
    {
        this.emailFilter = FilterUtils.verifyString(emailFilter);
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(final String group)
    {
        this.group = FilterUtils.verifyString(group);
    }

    public String getUserNameFilter()
    {
        return userNameFilter;
    }

    public void setUserNameFilter(final String userNameFilter)
    {
        this.userNameFilter = userNameFilter;
    }

    public String getFullNameFilter()
    {
        return fullNameFilter;
    }

    public void setFullNameFilter(final String fullNameFilter)
    {
        this.fullNameFilter = fullNameFilter;
    }

    public List<User> getFilteredUsers() throws Exception
    {
        // get list of filtered users
        final Iterable<User> unfilteredUsers = getUsersFilteredByGroup();
        // If there is no filter to apply just return the users.
        if (userNameFilter == null && fullNameFilter == null && emailFilter == null)
        {
            return Lists.newArrayList(unfilteredUsers);
        }

        final Iterable<User> users = Iterables.filter(unfilteredUsers, new Predicate<User>() {
            public boolean apply(final User user) {
                return isUserIncluded(user);
            }
        });

        // JRA-15309 - sort the users based on a Locale sensitive comparator
        final List<User> filteredUsers = Lists.newArrayList(users);
        Collections.sort(filteredUsers, new UserNameComparator(userLocale));
        return filteredUsers;
    }

    private Iterable<User> getUsersFilteredByGroup()
    {
        final CrowdService crowdService = ComponentAccessor.getComponentOfType(CrowdService.class);
        if (group != null)
        {
            final MembershipQuery<User> membershipQuery =
                    QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(group).returningAtMost(EntityQuery.ALL_RESULTS);
            return crowdService.search(membershipQuery);
        }
        final UserQuery<User> query = new UserQuery<User>(User.class, NullRestrictionImpl.INSTANCE, 0, EntityQuery.ALL_RESULTS);

        return crowdService.search(query);
    }

    private boolean isUserIncluded(final User user)
    {
        boolean included = true;

        // order matters here.
        included = includeBasedOnUserName(user, included);
        included &= includeBasedOnFullName(user, included);
        included &= includeBasedOnEmail(user, included);

        return included;
    }

    private boolean includeBasedOnUserName(final User user, final boolean included)
    {
        return (userNameFilter == null) ? included : includeBasedOnUserString(included, userNameFilter, user.getName());
    }

    private boolean includeBasedOnFullName(final User user, final boolean included)
    {
        return (fullNameFilter == null) ? included : includeBasedOnUserString(included, fullNameFilter, user.getDisplayName());
    }

    private boolean includeBasedOnEmail(final User user, final boolean included)
    {
        return (emailFilter == null) ? included : includeBasedOnUserString(included, emailFilter, user.getEmailAddress());
    }

    private boolean includeBasedOnUserString(boolean included, final String filterValue, final String userValue)
    {
        if (included && (filterValue != null))
        {
            included = (userValue != null) && (userValue.toLowerCase().indexOf(filterValue.toLowerCase()) >= 0);
        }
        return included;
    }
}
