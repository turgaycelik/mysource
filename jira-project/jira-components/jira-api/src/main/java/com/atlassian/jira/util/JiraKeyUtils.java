package com.atlassian.jira.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.util.UrlUtils;
import org.apache.log4j.Logger;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.oro.text.regex.MatchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.component.ComponentAccessor.getJiraAuthenticationContext;
import static com.atlassian.jira.component.ComponentAccessor.getPermissionManager;
import static com.opensymphony.util.TextUtils.htmlEncode;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class JiraKeyUtils
{
    private static final Logger log = Logger.getLogger(JiraKeyUtils.class);

    @SuppressWarnings("unused")
    public static final String STRIKE_THROUGH_CLOSED_KEYS = "strikeThroughClosedKeys";

    private static final String WINDOWS = "windows";

    private static volatile KeyMatcher keyMatcher;

    private static KeyMatcher getKeyMatcher()
    {
        if (keyMatcher == null)
        {
            // Cluster-safe because it's guarding component instantiation
            synchronized (KeyMatcher.class)
            {
                if (keyMatcher == null)
                {
                    keyMatcher = new ProductionKeyMatcher();
                }
            }
        }
        return keyMatcher;
    }

    @VisibleForTesting
    static KeyMatcher getCurrentKeyMatcher()
    {
        return keyMatcher;
    }

    /**
     * Resets the configuration of the key matcher so that it will re-read the
     * ApplicationProperty that specifies how to find an issue key in a string.
     * This must be called if the property for representing the key matching
     * regular expression is changed at runtime.
     */
    public static void resetKeyMatcher()
    {
        // Cluster-safe because it's only guarding component resetting.
        synchronized (KeyMatcher.class)
        {
            keyMatcher = null;
        }
    }

    /**
     * Validates the given project key and returns true if valid, false otherwise.
     *
     * @param key project key to validate
     * @return True if the supplied project key is valid
     */
    public static boolean validProjectKey(final String key)
    {
        return getKeyMatcher().isValidProjectKey(key);
    }

    /**
     * Check if the Project Key matches a list of reserved keywords - JRA-8051. For example, a folder named 'CON' cannot
     * be created in WINDOWS as it is a reserved word.
     * <p/>
     * This check is only enabled for WINDOWS machines at present as this issue has only been reported in the WINDOWS
     * environment.
     *
     * @param key project key
     * @return true if reserved word, false otherwise
     */
    public static boolean isReservedKeyword(final String key)
    {
        if (ComponentAccessor.getComponentOfType(JiraProperties.class).getProperty("os.name").compareToIgnoreCase(WINDOWS) >= 0)
        {
            final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
            final String reservedKeywords = applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECTKEY_RESERVEDWORDS_LIST);

            final StringTokenizer st = new StringTokenizer(reservedKeywords, ", ");
            while (st.hasMoreTokens())
            {
                if (key.equals(st.nextToken()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a project key of the project given issue belongs to.
     *
     * @param key issue key
     * @return The project key from an issue key, or null if the key is invalid
     * @deprecated Use {@link com.atlassian.jira.issue.IssueKey#from(String)} instead. Since v6.1.
     */
    @Deprecated
    public static String getProjectKeyFromIssueKey(final String key)
    {
        if (!validIssueKey(key))
        {
            return null;
        }

        return key.substring(0, key.lastIndexOf("-"));
    }

    /**
     * Same as {@link #getProjectKeyFromIssueKey(String)} except that it does not check key validity
     *
     * @param key issue key
     * @return The project key from an issue key, or null if the key is invalid
     * @deprecated Use {@link com.atlassian.jira.issue.IssueKey#from(String)} instead. Since v6.1.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static String getFastProjectKeyFromIssueKey(final String key)
    {
        try
        {
            return key.substring(0, key.lastIndexOf("-"));
        }
        catch (final Exception e)
        {
            return null;
        }
    }

    /**
     * Same as {@link #getCountFromKey(String)} except that is does not check for key validity
     *
     * @param key issue key
     * @return The issue count from an issue key, or -1 if the key is invalid
     * @deprecated Use {@link com.atlassian.jira.issue.IssueKey#from(String)} instead. Since v6.1.
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static long getFastCountFromKey(final String key)
    {
        try
        {
            return Long.parseLong(key.substring(key.lastIndexOf("-") + 1));
        }
        catch (final Exception e)
        {
            return -1;
        }
    }

    /**
     * @param key issue key
     * @return The issue count from an issue key, or -1 if the key is invalid
     * @deprecated Use {@link com.atlassian.jira.issue.IssueKey#from(String)} instead. Since v6.1.
     */
    @Deprecated
    public static long getCountFromKey(final String key)
    {
        long count = -1;

        if (!validIssueKey(key))
        {
            return count;
        }

        final String countInKey = key.substring(key.lastIndexOf("-") + 1);
        try
        {
            count = Long.parseLong(countInKey);
        }
        catch (final NumberFormatException nfe)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Count part of the key is not a number: '" + countInKey + "'");
            }
        }
        return count;
    }

    /**
     * Validates the given issue key.
     *
     * @param key issue key
     * @return True if the supplied issue key starts with a valid project key, and ends with a number
     */
    public static boolean validIssueKey(final String key)
    {
        return getKeyMatcher().isValidIssueKey(key);
    }

    /**
     * Determines whether the provided key is part of the provided string
     *
     * @param issueKey issue key
     * @param body     string to check
     * @return true if the given key is in the body, false otherwise
     */
    public static boolean isKeyInString(final String issueKey, final String body)
    {
        // Check that we have a non-empty key
        if (isBlank(issueKey))
        {
            throw new IllegalArgumentException("A valid key must be passed.");
        }

        // If the body is null or empty we cannot find the key
        if (isBlank(body))
        {
            return false;
        }

        //JRA-12354: Fixed key detection to use regular expressions and find exact matches in those regular expressions.
        //this should avoid the problem where body contains something like 'This is a commit for AJRA-1' and JRA-1
        // matches AJRA-1.  In order to stay backwards compatible, we've added the
        // {@link APKeys.JIRA_OPTION_KEY_DETECTION_BACKWARDS_COMPATIBLE} flag to go back to the old way.
        if (getKeyMatcher().isKeyDetectionBackwardsCompatible())
        {
            return isKeyInStringBackwardsCompatible(issueKey, body);
        }
        else
        {
            return isKeyInStringMatchingRegex(issueKey, body);
        }
    }

    private static boolean isKeyInStringBackwardsCompatible(final String issueKey, final String body)
    {
        // Go through the whole body and try to find the given issueKey
        int startSearchIndex = 0;
        while (startSearchIndex < body.length())
        {
            final int index = body.indexOf(issueKey, startSearchIndex);

            // Check that we have the key in the body
            if (index > -1)
            {
                // If we do ensure that the key is the exact match. That is we do not want to match, for example, 'JRA-11' when searching for
                // 'JRA-1'. We should check that the key is at the end of body, or that the next character after the key
                // is NOT a digit. As if it is a digit then the matched string is another (wider) key.
                final int positionAfterKey = index + issueKey.length();
                if (positionAfterKey == body.length())
                {
                    // The issueKey is at the end of body. This is a match.
                    return true;
                }
                else
                {
                    // The issueKey is not at the end body. Check the next character.
                    final char character = body.charAt(positionAfterKey);

                    // If the character is NOT a digit, we have a match.
                    if (!Character.isDigit(character))
                    {
                        return true;
                    }
                    else
                    {
                        // Otherwise continue with the loop to check the rest of body
                        startSearchIndex += positionAfterKey;
                    }
                }
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    private static boolean isKeyInStringMatchingRegex(final String issueKey, String body)
    {
        final Perl5Util util = new Perl5Util(); //note that we have to create a new match, as MatchResult is not Threadsafe

        while (util.match(getKeyMatcher().getIssueKeyRegex(), body))
        {
            final MatchResult match = util.getMatch();

            // The issue key is composed of the all sub-pattern match
            // groups after the initial sub pattern match group
            final int matchGroups = match.groups();
            final String key = createKeyFromMatchingGroups(matchGroups, match);

            if (issueKey.equals(key))
            {
                // Check backs from the key to see if it part of the url & URL is ignored
                if (!getKeyMatcher().isIgnoreUrlWithKey() || !isPartOfUrl(body, match.beginOffset(2)))
                {
                    return true;
                }
            }
            body = body.substring(match.endOffset(matchGroups - 1));
        }
        return false;
    }

    /**
     * Determines whether any JIRA issue key is in the given string
     *
     * @param s string to check
     * @return true if a key was found, false otherwise
     */
    public static boolean isKeyInString(final String s)
    {
        return getKeyMatcher().isKeyInString(s);
    }

    public static String linkBugKeys(String body)
    {
        if (isBlank(body))
        {
            return "";
        }

        final Perl5Util util = new Perl5Util(); //note that we have to create a new match, as MatchResult is not Threadsafe

        final StringBuilder buff = new StringBuilder(body.length());

        while (util.match(getKeyMatcher().getIssueKeyRegex(), body))
        {
            MatchResult match = util.getMatch();

            // The issue key is composed of the all sub-pattern match
            // groups after the initial sub pattern match group
            final int matchGroups = match.groups();
            final String key = createKeyFromMatchingGroups(matchGroups, match);

            // Check backs from the key to see if it part of the url
            final int keyStart = match.beginOffset(2);
            if (isPartOfUrl(body, keyStart))
            {
                // JRA-16002: if inside URL, skip the containing anchor or the entire URL string, incase there are
                // multiple keys present. Be careful to only match the current key and not a key further along.
                int endIndex;
                boolean isInsideAnchor = util.match("/<a[^>]*>(.*?" + key + ".*?)</a>/", body) && (util.getMatch().beginOffset(0) < keyStart);
                if (isInsideAnchor)
                {
                    endIndex = util.getMatch().endOffset(0);
                    if (endIndex <= keyStart)
                    {
                        // double check to see that the anchor tag we matched with the key actually includes the key that
                        // we found before. if somehow the anchor tag completes before the key starts, then something
                        // is screwed up majorly
                        log.error("Matched an anchor tag containing key '" + key + "' which ended before the key started!");
                    }
                }
                else
                {
                    endIndex = getUrlEnd(body, match.beginOffset(2));
                }
                buff.append(body.substring(0, endIndex));
                body = body.substring(endIndex);
            }
            else
            {
                // add everything before the key
                buff.append(body.substring(0, match.beginOffset(2)));

                // add the linked key
                buff.append(getKeyMatcher().getLink(key));

                // clear the added strings
                body = body.substring(match.endOffset(matchGroups - 1));
            }
        }

        // append any remaining body (or the whole thing if no matches occurred)
        buff.append(body);
        return buff.toString();
    }

    public static List<String> getIssueKeysFromString(String body)
    {
        if (isBlank(body))
        {
            return Collections.emptyList();
        }

        final Perl5Util util = new Perl5Util(); //note that we have to create a new match, as MatchResult is not Threadsafe
        final List<String> result = new ArrayList<String>();

        while (util.match(getKeyMatcher().getIssueKeyRegex(), body))
        {
            final MatchResult match = util.getMatch();

            // The issue key is composed of the all sub-pattern match
            // groups after the initial sub pattern match group
            final int matchGroups = match.groups();

            // Check backs from the key to see if it part of the url & URL is ignored
            if (!getKeyMatcher().isIgnoreUrlWithKey() || !isPartOfUrl(body, match.beginOffset(2)))
            {
                result.add(createKeyFromMatchingGroups(matchGroups, match));
            }
            body = body.substring(match.endOffset(matchGroups - 1));
        }
        return Collections.unmodifiableList(result);
    }

    private static String createKeyFromMatchingGroups(final int matchGroups, final MatchResult match)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 2; i < matchGroups; i++)
        {
            sb.append(match.group(i));
        }
        return sb.toString();
    }

    public static boolean isPartOfUrl(final String body, final int start)
    {
        // Loop backwards until the begin or a space
        for (int i = start; i > 2; i--)
        {
            final char currentChar = body.charAt(i);
            if (!UrlUtils.isValidURLChar(currentChar))
            {
                return false;
            }
            else if (currentChar == '/')
            {
                // check if the two characters before are :/
                final String s = body.substring(i - 2, i);
                if (s.equals(":/"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the index of the first character after the URL ends. This makes it easy to select the URL portion of a
     * string using the substring method (which takes the end offset as exclusive)
     *
     * @param body  the string to parse
     * @param start the starting offset
     * @return the index of the first character after the URL ends
     */
    static int getUrlEnd(final String body, final int start)
    {
        int i;
        for (i = start; i < body.length(); i++)
        {
            if (!UrlUtils.isValidURLChar(body.charAt(i)))
            {
                break;
            }
        }
        return i;
    }

    public static String getIssueKeyRegex()
    {
        return getKeyMatcher().getIssueKeyRegex();
    }

    @VisibleForTesting
    static void setKeyMatcher(final KeyMatcher keyMatcher)
    {
        JiraKeyUtils.keyMatcher = keyMatcher;
    }

    @Nullable
    public static String fastFormatIssueKey(@Nonnull final String projectKey, @Nonnull final Long issueNumber) {
        if (issueNumber<0 || projectKey.isEmpty())
        {
            return null; // don't return a broken key
        }
        return String.format("%s-%d", projectKey, issueNumber);
    }

    public static String slowFormatIssueKey(@Nonnull final String projectKey, @Nonnull final Long issueNumber) {
        String issueKey = String.format("%s-%d", projectKey, issueNumber);
        if (issueNumber<0 || projectKey.isEmpty())
        {
            throw new IllegalArgumentException(String.format("Could not format correct issue key for project key '%s' "
                    + "and issue number %d. Resulting issue key is incorrect: '%s'", projectKey, issueNumber, issueKey));
        }
        return issueKey;
    }

    public interface KeyMatcher
    {
        @SuppressWarnings("unused")
        String getProjectKeyRegex();

        String getIssueKeyRegex();

        boolean isKeyInString(String s);

        boolean isValidProjectKey(String projectKey);

        boolean isValidIssueKey(String issueKey);

        boolean isIgnoreUrlWithKey();

        boolean isKeyDetectionBackwardsCompatible();

        String getLink(String key);
    }

    static class ProductionKeyMatcher extends DefaultKeyMatcher
    {
        ProductionKeyMatcher()
        {
            // Retrieve project key regex from application properties file
            super(ComponentAccessor.getApplicationProperties().getDefaultBackedString(APKeys.JIRA_PROJECTKEY_PATTERN));
        }

        ProductionKeyMatcher(final String projectRegexp)
        {
            super(projectRegexp);
        }

        @Override
        public boolean isIgnoreUrlWithKey()
        {
            return Boolean.valueOf(getApplicationProperties().getDefaultBackedString(APKeys.JIRA_OPTION_IGNORE_URL_WITH_KEY));
        }

        @Override
        public boolean isKeyDetectionBackwardsCompatible()
        {
            return Boolean.valueOf(getApplicationProperties().getDefaultBackedString(APKeys.JIRA_OPTION_KEY_DETECTION_BACKWARDS_COMPATIBLE));
        }

        @Override
        public String getLink(final String key)
        {
            final StringBuilder buff = new StringBuilder();
            try
            {
                final Issue issue = getIssueManager().getIssueObject(key);
                if ((issue != null) && canCurrentUserSeeIssue(issue))
                {
                    final String title = htmlEncode(issue.getSummary());
                    final VelocityRequestContext velocityRequestContext = ComponentAccessor.getComponent(VelocityRequestContextFactory.class).getJiraVelocityRequestContext();

                    buff.append("<a href=\"")
                            .append(velocityRequestContext.getBaseUrl())
                            .append("/browse/")
                            .append(issue.getKey())
                            .append("\" title=\"")
                            .append(title)
                            .append("\"")
                            .append(" class=\"issue-link\" data-issue-key=\"")
                            .append(issue.getKey())
                            .append("\">");
                    if (issue.getResolutionObject() != null)
                    {
                        buff.append("<strike>");
                        buff.append(key);
                        buff.append("</strike>");
                    }
                    else
                    {
                        buff.append(key);
                    }

                    buff.append("</a>");
                }
                else
                {
                    buff.append(key);
                }
            }
            catch (final Exception e)
            {
                buff.append(key);
            }
            return buff.toString();
        }

        boolean canCurrentUserSeeIssue(Issue issue)
        {
            ApplicationUser loggedInUser = getJiraAuthenticationContext().getUser();
            return getPermissionManager().hasPermission(Permissions.BROWSE, issue, loggedInUser);
        }

        ApplicationProperties getApplicationProperties()
        {
            return ComponentAccessor.getApplicationProperties();
        }

        IssueManager getIssueManager()
        {
            return ComponentAccessor.getIssueManager();
        }
    }

    static class DefaultKeyMatcher implements KeyMatcher
    {
        private final Perl5Util perlUtil = new Perl5Util();
        private final String projectKeyRegex;
        private final String issueKeyRegex;

        DefaultKeyMatcher(final String projectRegexp)
        {
            if (isNotBlank(projectRegexp))
            {
                projectKeyRegex = "/^" + projectRegexp + "$/";
                issueKeyRegex = "/(^|[^a-zA-Z]|\n)" + projectRegexp + "(-[0-9]+)/";
            }
            else
            {
                // Default setting if not specified in properties file
                projectKeyRegex = "/^[A-Z][A-Z]+$/";
                issueKeyRegex = "/(^|[^a-zA-Z]|\n)([A-Z][A-Z]+-[0-9]+)/";
            }
        }

        public boolean isKeyInString(final String s)
        {
            return perlUtil.match(getIssueKeyRegex(), s);
        }

        public String getProjectKeyRegex()
        {
            return projectKeyRegex;
        }

        public String getIssueKeyRegex()
        {
            return issueKeyRegex;
        }

        public boolean isValidIssueKey(final String issueKey)
        {
            if (issueKey == null)
            {
                return false;
            }

            final int hyphenLocation = issueKey.lastIndexOf('-');
            if (hyphenLocation > 0)
            {
                final String projectKey = issueKey.substring(0, hyphenLocation);
                final String issueNumber = issueKey.substring(hyphenLocation + 1);
                if (validProjectKey(projectKey))
                {
                    if (issueNumber.length() == 0)
                    {
                        return false;
                    }

                    final char[] chars = issueNumber.toCharArray();
                    for (final char c : chars)
                    {
                        if (!Character.isDigit(c))
                        {
                            return false;
                        }
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
            return false;
        }

        /**
         * Validates the given project key
         *
         * @param projectKey project key
         * @return true if valid, false otherwise
         */
        public boolean isValidProjectKey(final String projectKey)
        {
            if (isBlank(projectKey))
            {
                return false;
            }
            // Fixes JRA-6091
            return perlUtil.match(getProjectKeyRegex(), projectKey);
        }

        public boolean isIgnoreUrlWithKey()
        {
            return true;
        }

        public boolean isKeyDetectionBackwardsCompatible()
        {
            return false;
        }

        public String getLink(final String key)
        {
            return key;
        }
    }
}
