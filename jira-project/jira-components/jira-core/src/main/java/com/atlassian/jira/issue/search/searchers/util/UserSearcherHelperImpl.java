package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.comparator.UserCachingComparator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.GroupNameComparator;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import static com.google.common.base.Functions.compose;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;

public class UserSearcherHelperImpl implements UserSearcherHelper
{
    private final GroupManager groupManager;
    private final PermissionManager permissionManager;
    private final UserUtil userUtil;
    private final UserManager userManager;
    private final UserHistoryManager userHistoryManager;
    private final UserPickerSearchService userPickerSearchService;

    public UserSearcherHelperImpl(final GroupManager groupManager, final PermissionManager permissionManager,
            final UserUtil userUtil, final UserManager userManager, final UserHistoryManager userHistoryManager,
            final UserPickerSearchService userPickerSearchService)
    {
        this.groupManager = groupManager;
        this.permissionManager = permissionManager;
        this.userUtil = userUtil;
        this.userManager = userManager;
        this.userHistoryManager = userHistoryManager;
        this.userPickerSearchService = userPickerSearchService;
    }


    @Override
    public void addUserSuggestionParams(User user, List<String> selectedUsers, Map<String, Object> params)
    {
        params.put("hasPermissionToPickUsers", hasUserPickingPermission(user));
        params.put("suggestedUsers", getSuggestedUsers(user, selectedUsers, UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY));
        if (hasUserPickingPermission(user))
        {
            params.put("placeholderText", getI18n(user).getText("common.concepts.sparkler.placeholder.browse_user"));
        }
        else
        {
            params.put("placeholderText", getI18n(user).getText("common.concepts.sparkler.placeholder.no_browse_user"));
        }
    }

    @Override
    public void addUserGroupSuggestionParams(User user, List<String> selectedUsers, Map<String, Object> params)
    {
        addUserGroupSuggestionParams(user, selectedUsers, UserSearchParams.ACTIVE_USERS_ALLOW_EMPTY_QUERY, params);
    }

    @Override
    public void addUserGroupSuggestionParams(final User user, final List<String> selectedUsers, final UserSearchParams searchParams, final Map<String, Object> params)
    {
        params.put("hasPermissionToPickUsers", hasUserPickingPermission(user));
        if (searchParams != null)
        {
            // only add suggestedUsers when the searchParams is specified
            params.put("suggestedUsers", ApplicationUsers.toDirectoryUsers(getSuggestedUsers(user, selectedUsers, searchParams)));
        }
        params.put("suggestedGroups", getSuggestedGroups(user));
        if (hasUserPickingPermission(user))
        {
            params.put("placeholderText", getI18n(user).getText("common.concepts.sparkler.placeholder.browse_usergroup"));
        }
        else
        {
            params.put("placeholderText", getI18n(user).getText("common.concepts.sparkler.placeholder.no_browse_usergroup"));
        }
    }


    @Override
    public void addGroupSuggestionParams(User user, Map<String, Object> params)
    {
        params.put("hasPermissionToPickUsers", hasUserPickingPermission(user));
        params.put("suggestedGroups", getSuggestedGroups(user));
        if (hasUserPickingPermission(user))
        {
            params.put("placeholderText", getI18n(user).getText("common.concepts.sparkler.placeholder.browse_group"));
        }
        else
        {
            params.put("placeholderText", getI18n(user).getText("common.concepts.sparkler.placeholder.no_browse_group"));
        }
    }


    /**
     * @param user The user performing the search.
     * @return Groups that should be suggested.
     */
    List<Group> getSuggestedGroups(User user)
    {
        // very specific implementation, do not reuse com.atlassian.jira.bc.group.search.GroupPickerSearchService.findGroups()
        List<Group> groups = new ArrayList<Group>();
        if (user != null)
        {
            // Suggest groups the user is a member of.
            groups.addAll(groupManager.getGroupsForUser(user));
        }
        else if (hasUserPickingPermission(user))
        {
            // Suggest the first n groups in the system.
            groups.addAll(groupManager.getAllGroups());
        }
        else
        {
            return null;
        }

        Collections.sort(groups, new GroupNameComparator());
        return groups.subList(0, Math.min(5, groups.size()));
    }

    /**
     * @param user The user performing the search.
     * @param selectedUserNames the values that are already selected in this search
     */
    List<ApplicationUser> getSuggestedUsers(User user, List<String> selectedUserNames, UserSearchParams searchParams)
    {
        ApplicationUser appUser = ApplicationUsers.from(user);
        if (hasUserPickingPermission(appUser))
        {
            int limit = 5;
            LinkedHashSet<ApplicationUser> suggestedUsers = new LinkedHashSet<ApplicationUser>();
            Collection<ApplicationUser> recentlyUsedUsers = getRecentlyUsedUsers(appUser);
            addToSuggestList(limit, suggestedUsers, recentlyUsedUsers, selectedUserNames, searchParams);

            // JRADEV-15375 With a significant number of users in the system, a full sort is very slow and pointless to
            // suggest random users anyway. On the other hand, we do like putting_something_in here for evaluators...
            if (suggestedUsers.size() < limit && userUtil.getActiveUserCount() <= 10)
            {
                // Suggest the first 'limit' users in the system (if they don't already appear).
                final List<User> allUsers = userPickerSearchService.findUsers("", searchParams);
                if (!recentlyUsedUsers.isEmpty())
                {
                    // if there are recently used users, then we expand selectedUserNames to be more aggressively filtering users
                    selectedUserNames = newArrayList(selectedUserNames);
                    selectedUserNames.addAll(Collections2.transform(recentlyUsedUsers, new Function<ApplicationUser, String>()
                    {
                        @Override
                        public String apply(@Nullable final ApplicationUser input)
                        {
                            return input.getName();
                        }
                    }));
                }
                Collections.sort(allUsers, new UserCachingComparator());
                List<ApplicationUser> allAppUsers = Lists.transform(allUsers, new Function<User, ApplicationUser>() {
                    @Override
                    public ApplicationUser apply(@Nullable User input)
                    {
                        return ApplicationUsers.from(input);
                    }
                });
                addToSuggestList(limit, suggestedUsers, allAppUsers, selectedUserNames, searchParams);
            }

            return ImmutableList.copyOf(suggestedUsers);
        }
        else
        {
            return null;
        }
    }

    /**
     * Add to 'suggestedUsers' all users from 'users' (except users already in 'alreadySelected'),
     * until 'suggestedUsers' is 'limit' big.
     */
    void addToSuggestList(int limit, LinkedHashSet<ApplicationUser> suggestedUsers, Iterable<ApplicationUser> users, List<String> alreadySelected, UserSearchParams searchParams)
    {
        for (ApplicationUser user : users)
        {
            if (suggestedUsers.size() >= limit) {
                break;
            }
            if (!suggestedUsers.contains(user) && !alreadySelected.contains(user.getName())
                    && userPickerSearchService.userMatches(user, searchParams))
            {
                suggestedUsers.add(user);
            }
        }
    }

    private Collection<ApplicationUser> getRecentlyUsedUsers(ApplicationUser user)
    {
        Function<String, ApplicationUser> toUser = new Function<String, ApplicationUser>()
        {
            @Override
            public ApplicationUser apply(@Nullable String input)
            {
                return input == null ? null : userManager.getUserByName(input);
            }
        };

        Function<UserHistoryItem, ApplicationUser> historyItemToUser = compose(toUser, UserHistoryItem.GET_ENTITY_ID);
        return new LinkedHashSet<ApplicationUser>(filter(Lists.transform(userHistoryManager.getHistory(UserHistoryItem.USED_USER, user), historyItemToUser), notNull()));
    }

    @Override
    public boolean hasUserPickingPermission(User user)
    {
        return hasUserPickingPermission(ApplicationUsers.from(user));
    }

    @Override
    public boolean hasUserPickingPermission(final ApplicationUser user)
    {
        return permissionManager.hasPermission(Permissions.USER_PICKER, user);
    }

    private I18nHelper getI18n(User user)
    {
        return ComponentAccessor.getComponent(I18nHelper.BeanFactory.class).getInstance(user);
    }


}
