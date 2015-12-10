/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gzipfilter.org.apache.commons.lang.math.NumberUtils;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.issue.comparator.UserNameComparator;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class UserPickerFilter extends PagerFilter
{
    private UserFilter filter;

    private String nameFilter;
    private String emailFilter;
    private String group;

    private final FieldConfigManager fieldConfigManager;
    private final JiraServiceContext jiraServiceContext;
    private final PermissionManager permissionManager;
    private final UserFilterManager userFilterManager;
    private final UserPickerSearchService userPickerSearchService;


    // custom fields
    /**
     * id of custom field
     */
    private String element;

    /**
     * field configuration id of custom field in the current context
     */
    private Long fieldConfigId;
    /**
     * project ids of the projects in the current context, to help infer project role related info for custom field
     */
    private Collection<Long> projectIds;

    public UserPickerFilter(final FieldConfigManager fieldConfigManager,
            final JiraServiceContext jiraServiceContext,
            final PermissionManager permissionManager,
            final UserFilterManager userFilterManager,
            final UserPickerSearchService userPickerSearchService)
    {
        this.fieldConfigManager = fieldConfigManager;
        this.jiraServiceContext = jiraServiceContext;
        this.permissionManager = permissionManager;
        this.userFilterManager = userFilterManager;
        this.userPickerSearchService = userPickerSearchService;
    }

    @SuppressWarnings ("UnusedDeclaration")
    public String getNameFilter()
    {
        return nameFilter;
    }

    @SuppressWarnings ("UnusedDeclaration")
    public void setNameFilter(final String nameFilter)
    {
        this.nameFilter = FilterUtils.verifyString(nameFilter);
    }

    @SuppressWarnings ("UnusedDeclaration")
    public String getEmailFilter()
    {
        return emailFilter;
    }

    @SuppressWarnings ("UnusedDeclaration")
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

    public String getElement()
    {
        return element;
    }

    public void setElement(final String element)
    {
        this.element = element;
    }

    public Long getFieldConfigId()
    {
        return fieldConfigId;
    }

    public void setFieldConfigId(final Long fieldConfigId)
    {
        this.fieldConfigId = fieldConfigId;
    }

    public Collection<Long> getProjectIds()
    {
        return projectIds;
    }

    /**
     * Setter method for BeanUtils to inject projectIds.
     * The name of the multi-valued url query parameter is projectId, so it has to be named as setProjectId().
     * @param projectIds the list of project id's
     */
    public void setProjectId(final String[] projectIds)
    {
        this.projectIds = Sets.newHashSetWithExpectedSize(projectIds.length);
        for (String projectIdStr : projectIds)
        {
            long projectId = NumberUtils.toLong(projectIdStr, -1);
            if (projectId != -1)
            {
                this.projectIds.add(projectId);
            }
        }
    }

    /**
     * Get a list of users based on the parameters of the filter
     **/
    public List<User> getFilteredUsers() throws Exception
    {
        final UserFilter filter = getFilter();
        final Collection<Long> projectIdSet = CustomFieldUtils.getProjectIdsForUser(
                jiraServiceContext.getLoggedInApplicationUser(), projectIds, permissionManager, filter);
        final UserNameComparator userNameComparator = new UserNameComparator(jiraServiceContext.getI18nBean().getLocale());
        final UserSearchParams userSearchParams = UserSearchParams.builder()
                                                        .allowEmptyQuery(true)
                                                        .canMatchEmail(true)
                                                        .filter(filter)
                                                        .filterByProjectIds(projectIdSet)
                                                        .build();
        final List<User> usersByFilter = userPickerSearchService.findUsers(nameFilter, emailFilter, userSearchParams);
        if (group == null)
        {
            return usersByFilter;
        }
        else
        {
            // Further filter by group. UserFilter supports OR but not AND of the groups/roles filtering.
            // We might add that support in the future if there are more usage of such AND-ed filtering.
            // For now, we do the AND separately here.
            final UserFilter filterByGroup = new UserFilter(true, null, ImmutableSet.of(group));
            final UserSearchParams userSearchParamsWithGroup = UserSearchParams.builder()
                    .allowEmptyQuery(true)
                    .canMatchEmail(true)
                    .filter(filterByGroup)
                    .build();
            final List<User> usersByGroup = userPickerSearchService.findUsers(nameFilter, emailFilter, userSearchParamsWithGroup);

            return intersectLists(userNameComparator, usersByFilter, usersByGroup);
        }
    }

    /**
     * Intersect the two lists and return the intersection ordered based on {@code userNameComparator}.
     */
    @VisibleForTesting
    List<User> intersectLists(final Comparator<User> userNameComparator, final List<User> list1, final List<User> list2)
    {
        if (list1 == null || list2 == null)
        {
            return ImmutableList.of();
        }
        // Not really taking an advantage of the fact that the two lists are already sorted.
        Set<User> intersection = new TreeSet(userNameComparator);
        intersection.addAll(list1);
        intersection.retainAll(list2);

        return ImmutableList.copyOf(intersection);
    }

    private UserFilter getFilter()
    {
        if (filter == null)
        {
            if (fieldConfigId == null)
            {
                filter = UserFilter.DISABLED;
            }
            else
            {
                FieldConfig fieldConfig = fieldConfigManager.getFieldConfig(fieldConfigId);
                if (fieldConfig == null)
                {
                    filter = UserFilter.DISABLED;
                }
                else
                {
                    filter = userFilterManager.getFilter(fieldConfig);
                }
            }
        }
        return filter;
    }
}
