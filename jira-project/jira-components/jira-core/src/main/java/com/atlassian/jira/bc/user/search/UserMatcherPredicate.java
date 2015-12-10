package com.atlassian.jira.bc.user.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;

/**
 * Matcher to compare User parts (username, Full Name and email) with a query string and return true any part matches.
 *
 * @since v5.0
 */
public class UserMatcherPredicate implements Predicate<User>
{
    private final String query;
    private final boolean canMatchAddresses;
    private final String emailQuery;

    /**
     * @param query The query to compare. Query can not be null.  Empty string will return true for all, so don't pass one in.
     * @param canMatchAddresses Whether email should be searched
     */
    public UserMatcherPredicate(String query, boolean canMatchAddresses)
    {
        this(query, "", canMatchAddresses);
    }

    /**
     * Search both nameQuery and emailQuery unless one is empty.
     *
     * If canMatchAddresses is false, email matching is ignored.
     *
     * <ul>
     * <li>If both nameQuery and emailQuery are not empty and canMatchAddresses is true, the user's user name/display name has to match nameQuery
     *  and email matches emailQuery before considered as a match. </li>
     *
     * <li>If only nameQuery is not empty or both nameQuery and emailQuery are specified but canMatchAddresses is false,
     *  nameQuery is used to match against username, display name, and email (when canMatchAddresses is true).
     *  But matching on any field is considered a match. This is the same behaviour as the original
     *  {@link UserMatcherPredicate#UserMatcherPredicate(String, boolean)} constructor </li>
     *
     * <li>If only emailQuery is not empty and canMatchAddresses is true, emailQuery is used to match against email only.</li>
     *
     * <li>if both are empty or only emailQuery is not empty but canMatchAddresses is false, it's always considered a match.
     *  Avoid it.</li>
     *
     * </ul>
     *
     * When emailQuery is not empty and canMatchAddresses is true, it corresponds to the search behaviour in the
     *  user picker popup browser where email search field is entered some value.
     *
     * @param nameQuery The query on user name
     * @param emailQuery The query on email
     * @param canMatchAddresses Whether email should be searched
     *
     * @since v6.2
     */
    public UserMatcherPredicate(String nameQuery, String emailQuery, boolean canMatchAddresses)
    {
        Assertions.notNull("query", nameQuery);
        Assertions.notNull("emailquery", emailQuery);
        this.query = nameQuery.toLowerCase();
        this.emailQuery = emailQuery==null?null:emailQuery.toLowerCase();
        this.canMatchAddresses = canMatchAddresses;
    }

    @Override
    /**
     * @param user  The user to test. Cannot be null.
     * @return true if any part matches the query string
     */
    public boolean apply(User user)
    {
        // NOTE - we don't test against blank or null strings here. Do that once before the code that calls this method.
        boolean separateEmailQuery = StringUtils.isNotBlank(emailQuery);

        boolean usernameMatched = false;
        // 1. Try the username
        String userPart = user.getName();
        if (StringUtils.isNotBlank(userPart) && startsWithCaseInsensitive(userPart, query))
        {
            if (separateEmailQuery && canMatchAddresses)
            {
                // still need to check emailQuery against email
                usernameMatched = true;
            }
            else
            {
                return true;
            }
        }

        // 2. If allowed, try the User's email address
        //    at this point, either username not matched, or username matched but we need to match email separately
        if (canMatchAddresses)
        {
            userPart = user.getEmailAddress();
            if (StringUtils.isNotBlank(userPart) && startsWithCaseInsensitive(userPart, separateEmailQuery?emailQuery:query))
            {
                if (!separateEmailQuery || usernameMatched)
                {
                    // email matched using name query or email matched using email query and username already matched
                    return true;
                }
                // email separately matched but username not match, we want to try display name
            }
            else
            {
                if (separateEmailQuery)
                {
                    return false; // when emailQuery is explicitly specified, it must match
                }
            }
        }

        // 3. at this point, email matching is not required, or matched, but username not matched, just need to check display name
        userPart = user.getDisplayName();
        if (StringUtils.isNotBlank(userPart))
        {
            // 3a. Go for a quick win with the start of the first name...
            if (startsWithCaseInsensitive(userPart, query))
            {
                return true;
            }

            // 3b. No? It was worth a try. Walk every word in the name, skip first token, we know it failed
            String[] tokens = StringUtils.split(userPart, ' ');
            for (int i = 1; i < tokens.length; i++)
            {
                String token = tokens[i];
                if (startsWithCaseInsensitive(token, query))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean startsWithCaseInsensitive(final String userPart, final String query)
    {
        // We try and delay any lower case conversion in this class to as little and as later as possible.
        if (query.length() == 0)
        {
            return true;
        }
        if (query.length() > userPart.length())
        {
            return false;
        }

        // > 90% of searches will typically miss on the first character.
        if (userPart.substring(0, 1).toLowerCase().charAt(0) == query.charAt(0))
        {
            return userPart.substring(0, query.length()).toLowerCase().equals(query);
        }
        return false;
    }
}
