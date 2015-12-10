package com.atlassian.jira.jql.resolver;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resolves User objects and their names.
 *
 * @since v4.0
 */
public class UserResolverImpl implements UserResolver
{
    private static final Logger log = Logger.getLogger(UserResolverImpl.class);

    private final UserKeyService userKeyService;
    private final UserManager userManager;

    public UserResolverImpl(final UserKeyService userKeyService, final UserManager userManager)
    {
        this.userKeyService = userKeyService;
        this.userManager = userManager;
    }

    public List<String> getIdsFromName(final String name)
    {
        notNull("name", name);
        final String userkey = userKeyService.getKeyForUsername(name);
        if (userkey != null)
        {
            return Collections.singletonList(userkey);
        }
        return getUsersFromFullNameOrEmail(name);
    }

    public boolean nameExists(final String name)
    {
        notNull("name", name);
        if (userKeyService.getKeyForUsername(name) != null)
        {
            return true;
        }
        if (log.isDebugEnabled())
        {
            log.debug("Username '" + name + "' not found - searching as email or full name.");
        }
        return hasAnyFullNameOrEmailMatches(name);
    }

    public boolean idExists(final Long id)
    {
        return nameExists(notNull("id", id).toString());
    }

    /**
     * Picks between the matched full name and email matches.  If both full name and email matches are
     * available, then the email matches are used if the name looks like an email address; otherwise,
     * the full name matches are used.
     *
     * @param name the name to find matches for
     * @param fullNameMatches the names of users whose full names matched
     * @param emailMatches the names of users whose email addresses matched
     * @return a list of user keys for the users that best match the supplied <em>name</em>.
     */
    private List<String> pickEmailOrFullNameMatches(final String name, final List<String> fullNameMatches, final List<String> emailMatches)
    {
        if (emailMatches.isEmpty())
        {
            return fullNameMatches;
        }
        if (fullNameMatches.isEmpty() || isEmail(name))
        {
            return emailMatches;
        }
        return fullNameMatches;
    }

    // Users and UserUtil can't be mocked any way so these aren't testable
    ///CLOVER:OFF
    private List<String> getUsersFromFullNameOrEmail(String name)
    {
        final List<String> fullNameMatches = new ArrayList<String>();
        final List<String> emailMatches = new ArrayList<String>();
        for (User user : userManager.getUsers())
        {
            if (name.equalsIgnoreCase(user.getDisplayName()))
            {
                addUserKeyToList(fullNameMatches, user);
            }
            if (name.equalsIgnoreCase(user.getEmailAddress()))
            {
                addUserKeyToList(emailMatches, user);
            }
        }

        return pickEmailOrFullNameMatches(name, fullNameMatches, emailMatches);
    }

    private void addUserKeyToList(List<String> list, User user)
    {
        final String key = userKeyService.getKeyForUsername(user.getName());
        if (key != null)
        {
            list.add(key);
        }
    }

    private boolean hasAnyFullNameOrEmailMatches(String name)
    {
        for (User user : userManager.getUsers())
        {
            if (name.equalsIgnoreCase(user.getDisplayName()) || name.equalsIgnoreCase(user.getEmailAddress()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isEmail(final String name)
    {
        return TextUtils.verifyEmail(name);
    }

    public User get(final Long id)
    {
        final ApplicationUser user = getApplicationUser(id);
        return (user != null) ? user.getDirectoryUser() : null;
    }

    public ApplicationUser getApplicationUser(final Long id)
    {
        return getApplicationUser(id.toString());
    }

    @Nonnull
    public Collection<User> getAll()
    {
        return userManager.getUsers();
    }

    ApplicationUser getApplicationUser(String name)
    {
        return userManager.getUserByName(name);
    }
    ///CLOVER:ON
}
