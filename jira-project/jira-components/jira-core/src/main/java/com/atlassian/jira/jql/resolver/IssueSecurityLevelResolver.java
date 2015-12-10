package com.atlassian.jira.jql.resolver;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.InjectableComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resolves Issue Security Levels for the specified user.
 *
 * @since v4.0
 */
@InjectableComponent
public class IssueSecurityLevelResolver
{
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public IssueSecurityLevelResolver(final IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.issueSecurityLevelManager = notNull("issueSecurityLevelManager", issueSecurityLevelManager);
    }

    /**
     * Returns the issue level objects that the passed user can see.
     *
     * @param searcher all the issues levels that the passed user can see.
     *
     * @return a list of all the issue levels that the passed user can see. The list returned may be edited by the caller.
     */
    public List<IssueSecurityLevel> getAllSecurityLevels(User searcher)
    {
        return new ArrayList<IssueSecurityLevel>(issueSecurityLevelManager.getAllSecurityLevelsForUser(searcher));
    }

    /**
     * Resolves {@link org.ofbiz.core.entity.GenericValue}s representing Issue Security Levels based on the
     * {@link com.atlassian.jira.jql.operand.QueryLiteral} provided. Will only return security levels the specified
     * user can see.
     *
     * Note: a null value in the returned list represents the "Empty" security level
     *
     * @param searcher the user performing the search.
     * @param rawValue the raw search input
     * @return the collection of security levels; never null.
     */
    public List<IssueSecurityLevel> getIssueSecurityLevels(User searcher, QueryLiteral rawValue)
    {
        return getIssueSecurityLevels(searcher, Collections.singletonList(rawValue));
    }

    /**
     * Resolves {@link org.ofbiz.core.entity.GenericValue}s representing Issue Security Levels based on the
     * {@link com.atlassian.jira.jql.operand.QueryLiteral}s provided. Will only return security levels the specified
     * user can see.
     *
     * Note: a null value in the returned list represents the "Empty" security level
     *
     * @param searcher the user performing the search.
     * @param rawValues the raw search inputs
     * @return the collection of security levels; never null.
     */
    public List<IssueSecurityLevel> getIssueSecurityLevels(User searcher, List<QueryLiteral> rawValues)
    {
        return _getIssueSecurityLevels(searcher, false, rawValues);
    }

    /**
     * Resolves {@link org.ofbiz.core.entity.GenericValue}s representing Issue Security Levels based on the
     * {@link com.atlassian.jira.jql.operand.QueryLiteral}s provided. Permissions are ignored.
     *
     * Note: a null value in the returned list represents the "Empty" security level
     *
     * @param rawValues the raw search inputs
     * @return the collection of security levels; never null.
     */
    public List<IssueSecurityLevel> getIssueSecurityLevelsOverrideSecurity(List<QueryLiteral> rawValues)
    {
        return _getIssueSecurityLevels(null, true, rawValues);
    }

    private List<IssueSecurityLevel> _getIssueSecurityLevels(User searcher, boolean overrideSecurity, List<QueryLiteral> rawValues)
    {
        notNull("rawValues", rawValues);
        List<IssueSecurityLevel> matchingLevels = new ArrayList<IssueSecurityLevel>();

        for (QueryLiteral rawValue : rawValues)
        {
            notNull("rawValue", rawValue);
            if (rawValue.getStringValue() != null)
            {
                matchingLevels.addAll(getIssueSecurityLevelsForString(searcher, overrideSecurity, rawValue.getStringValue()));
            }
            else if (rawValue.getLongValue() != null)
            {
                matchingLevels.addAll(getIssueSecurityLevelsForLong(searcher, overrideSecurity, rawValue.getLongValue()));
            }
            else if (rawValue.isEmpty())
            {
                // we somehow got an Empty literal - use null to represent this
                matchingLevels.add(null);
            }
        }

        return matchingLevels;
    }

    private Collection<IssueSecurityLevel> getIssueSecurityLevelsForString(final User searcher, final boolean overrideSecurity, final String rawValue)
    {
        Collection<IssueSecurityLevel> levels = getIssueSecurityLevelsByName(searcher, overrideSecurity, rawValue);
        if (levels == null)
        {
            levels = new LinkedHashSet<IssueSecurityLevel>();
        }

        if (levels.isEmpty())
        {
            final Long valueAsLong = getValueAsLong(rawValue);
            if (valueAsLong != null)
            {
                // Try to look up the filter by id
                final IssueSecurityLevel securityLevel = getIssueSecurityLevelById(searcher, overrideSecurity, valueAsLong);
                if (securityLevel != null)
                {
                    levels.add(securityLevel);
                }
            }
        }
        return levels;
    }

    private List<IssueSecurityLevel> getIssueSecurityLevelsForLong(final User searcher, final boolean overrideSecurity, final Long rawValue)
    {
        final List<IssueSecurityLevel> levels = new ArrayList<IssueSecurityLevel>();
        final IssueSecurityLevel level = getIssueSecurityLevelById(searcher, overrideSecurity, rawValue);
        if (level != null)
        {
            levels.add(level);
        }
        else
        {
            // Try to look up the filter by name
            final Collection<IssueSecurityLevel> securityLevelsByName = getIssueSecurityLevelsByName(searcher, overrideSecurity, rawValue.toString());
            if (securityLevelsByName != null)
            {
                levels.addAll(securityLevelsByName);
            }
        }
        return levels;
    }

    /**
     * @param searcher the user performing the search
     * @param overrideSecurity false if permissions should be checked
     * @param valueAsLong the id of the security level
     * @return the {@link org.ofbiz.core.entity.GenericValue} representing the level with the specified id; null if it
     * couldn't be found or if the user did not have permission to see it.
     */
    IssueSecurityLevel getIssueSecurityLevelById(final User searcher, final boolean overrideSecurity, final Long valueAsLong)
    {
        final Collection<IssueSecurityLevel> usersSecurityLevels = overrideSecurity ? issueSecurityLevelManager.getAllIssueSecurityLevels() : issueSecurityLevelManager.getAllSecurityLevelsForUser(searcher);
        for (IssueSecurityLevel level : usersSecurityLevels)
        {
            if (valueAsLong.equals(level.getId()))
            {
                return level;
            }
        }
        return null;
    }

    /**
     * @param searcher the user performing the search
     * @param overrideSecurity false if permissions should be checked
     * @param nameValue the name to search for
     * @return the security levels with the specified name that the user can see
     */
    Collection<IssueSecurityLevel> getIssueSecurityLevelsByName(final User searcher, final boolean overrideSecurity, final String nameValue)
    {
        return overrideSecurity ? issueSecurityLevelManager.getIssueSecurityLevelsByName(nameValue) : issueSecurityLevelManager.getSecurityLevelsForUserByName(searcher, nameValue);
    }


    private Long getValueAsLong(final String singleValueOperand)
    {
        try
        {
            return new Long(singleValueOperand);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
