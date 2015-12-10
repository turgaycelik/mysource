package com.atlassian.jira.rest.v2.issue.users;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.rest.v2.issue.UserPickerResultsBean;
import com.atlassian.jira.rest.v2.issue.UserPickerUser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.DelimeterInserter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.Math.min;

/**
 * A helper for finding users
 * @since v5.2
 */
@Component
public class UserPickerResourceHelperImpl implements UserPickerResourceHelper
{

    private static final int DEFAULT_USERS_RETURNED = 50;
    private static final int MAX_USERS_RETURNED = 1000;
    private final UserPickerSearchService userPickerSearchService;
    private final I18nHelper i18n;
    private final AvatarService avatarService;
    private final JiraAuthenticationContext authContext;
    private final PermissionManager permissionManager;

    @Autowired
    public UserPickerResourceHelperImpl(UserPickerSearchService userPickerSearchService, I18nHelper i18n, AvatarService avatarService,
            JiraAuthenticationContext authContext, PermissionManager permissionManager)
    {
        this.userPickerSearchService = userPickerSearchService;
        this.i18n = i18n;
        this.avatarService = avatarService;
        this.authContext = authContext;
        this.permissionManager = permissionManager;
    }

    /**
     * Returns a list of users matching query with highlighting. This resource cannot be accessed anonymously.
     *
     *
     * @param query A string used to search username, Name or e-mail address
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param showAvatar
     * @param excludeUsers
     * @return An object containing list of matched user objects, with html representing highlighting.
     */
    @Override
    public UserPickerResultsBean findUsersAsBean(String query, Integer maxResults, Boolean showAvatar, List<String> excludeUsers)
    {
        return findUsersAsBean(query, maxResults, showAvatar, excludeUsers, UserSearchParams.ACTIVE_USERS_IGNORE_EMPTY_QUERY);
    }

    @Override
    public UserPickerResultsBean findUsersAsBean(final String query, final Integer maxResults, final Boolean showAvatar, final List<String> excludeUsers, final UserSearchParams userSearchParams)
    {
        List<User> users = Lists.newArrayList();
        List<User> page;

        if (permissionManager.hasPermission(Permissions.USER_PICKER, authContext.getUser())) {
            users = findUsers(query, userSearchParams);
            page = limitUserSearch(0, maxResults, users, excludeUsers);
        } else {
            final User user = getUserByName(query);
            if (user != null) {
                users.add(user);
            }
            page = limitUserSearch(0, 1, users, excludeUsers);
        }

        return makeUserPickerBean(users, page, query, showAvatar);
    }

    @Override
    public List<User> limitUserSearch(Integer startAt, Integer maxResults, Iterable<User> users, Iterable<String> excludeUsers)
    {
        final int skip = startAt != null && startAt >= 0 ? startAt : 0;
        final int limit = maxResults != null && maxResults >= 0 ? min(MAX_USERS_RETURNED, maxResults) : DEFAULT_USERS_RETURNED;

        final Iterable<User> filteredUsers;
        if (excludeUsers == null)
        {
            filteredUsers = users;
        }
        else
        {
            final ImmutableSet<String> filter = ImmutableSet.copyOf(excludeUsers);
            filteredUsers = Iterables.filter(users, new Predicate<User>()
            {
                @Override
                public boolean apply(User user)
                {
                    return !filter.contains(user.getName());
                }
            });
        }

        return ImmutableList.copyOf(Iterables.limit(Iterables.skip(filteredUsers, skip), limit));
    }

    @Override
    public List<User> findActiveUsers(String searchString)
    {
        return findUsers(searchString, true, false);
    }

    @Override
    public List<User> findUsers(String searchString, Boolean includeActive, Boolean includeInactive)
    {
        return findUsers(searchString, includeActive, includeInactive, false);
    }

    @Override
    public List<User> findUsers(String searchString, Boolean includeActive, Boolean includeInactive, boolean allowEmptySearchString)
    {
        if (!allowEmptySearchString && searchString == null)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.user.error.no.username.param")));
        }

        final UserSearchParams userSearchParams = UserSearchParams.builder()
                .includeActive(includeActive == null ? true : includeActive.booleanValue())
                .includeInactive(includeInactive == null ? false : includeInactive.booleanValue())
                .allowEmptyQuery(allowEmptySearchString)
                .build();

        return findUsers(searchString, userSearchParams);
    }

    @Override
    public List<User> findUsers(final String searchString, final UserSearchParams userSearchParams)
    {
        if (searchString == null)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.user.error.no.username.param")));
        }

        return userPickerSearchService.findUsers(getContext(), searchString, userSearchParams);
    }

    @Override
    public User getUserByName(final String userName)
    {
        if (userName == null)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.user.error.no.username.param")));
        }

        return userPickerSearchService.getUserByName(getContext(), userName);
    }

    private UserPickerResultsBean makeUserPickerBean(Collection<User> users, Collection<User> page, String query, Boolean showAvatar)
    {
        int total = users.size();
        int showing = page.size();
        final boolean canShowEmailAddresses = userPickerSearchService.canShowEmailAddresses(getContext());
        if (showAvatar == null) {
            showAvatar = false;
        }
        List<UserPickerUser> beans = new ArrayList<UserPickerUser>();
        for (User user : page)
        {
            final String html = formatUser(user, query, canShowEmailAddresses);
            beans.add(new UserPickerUser(user.getName(), ApplicationUsers.getKeyFor(user), user.getDisplayName(), html, showAvatar ? avatarService.getAvatarURL(user, user.getName(), Avatar.Size.SMALL) : null));
        }
        return new UserPickerResultsBean(beans, i18n.getText("jira.ajax.autocomplete.user.more.results", String.valueOf(showing), String.valueOf(total)), total);
    }

    /*
    * We use direct html instead of velocity to ensure the AJAX lookup is as fast as possible
    */
    private String formatUser(User user, String query, boolean canShoweEmailAddresses)
    {

        DelimeterInserter delimeterInserter = new DelimeterInserter("<strong>", "</strong>");

        String[] terms = { query };

        String userFullName = delimeterInserter.insert(TextUtils.htmlEncode(user.getDisplayName()), terms);
        String userName = delimeterInserter.insert(TextUtils.htmlEncode(user.getName()), terms);


        StringBuilder sb = new StringBuilder();
        sb.append(userFullName);
        if (canShoweEmailAddresses)
        {
            String userEmail = delimeterInserter.insert(TextUtils.htmlEncode(user.getEmailAddress()), terms);
            /*
             We dont mask the email address by design.  We dont think the email bots will be able to easily
             get email addresses from YUI generated divs and also its only an issue if "browse user" is given to group
             anyone.  So here is where we would change this if we change our mind in the future.
             */
            sb.append(" - ");
            sb.append(userEmail);
        }
        sb.append(" (");
        sb.append(userName);
        sb.append(")");

        return sb.toString();
    }

    JiraServiceContext getContext()
    {
        User user = authContext.getLoggedInUser();
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        return new JiraServiceContextImpl(user, errorCollection);
    }

}
