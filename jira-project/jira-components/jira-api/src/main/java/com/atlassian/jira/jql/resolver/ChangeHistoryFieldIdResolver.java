package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.changehistory.ChangeHistoryFieldConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * As both WasClauseQueryFactor and ChangedClauseQueryFactory need to resolve ids this is a helper class
 * to accomplish this.
 *
 * @since v5.0
 */
public class ChangeHistoryFieldIdResolver
{
    // -1 is the generic empty indicator
    private static final Collection<String> EMPTY_ID = Collections.singleton("-1");

    private final UserResolver userResolver;
    private final VersionResolver versionResolver;
    private final ConstantsManager constantsManager;

    /**
     * @deprecated Use the other constructor.  Since 6.0.
     */
    @Deprecated
    public ChangeHistoryFieldIdResolver(final ChangeHistoryFieldConstants changeHistoryFieldConstants,
            final VersionResolver versionResolver, final ConstantsManager constantsManager)
    {
        this(changeHistoryFieldConstants, ComponentAccessor.getComponentOfType(UserResolver.class),
                versionResolver, constantsManager);
    }

    /**
     * @deprecated Use the other constructor.  Since 6.2.
     */
    @Deprecated
    public ChangeHistoryFieldIdResolver(final ChangeHistoryFieldConstants changeHistoryFieldConstants,
            final UserResolver userResolver, final VersionResolver versionResolver,
            final ConstantsManager constantsManager)
    {
        this.userResolver = userResolver;
        this.versionResolver = versionResolver;
        this.constantsManager = constantsManager;
    }

    public ChangeHistoryFieldIdResolver(final UserResolver userResolver, final VersionResolver versionResolver,
            final ConstantsManager constantsManager)
    {
        this.userResolver = userResolver;
        this.versionResolver = versionResolver;
        this.constantsManager = constantsManager;
    }

    public Collection<String> resolveIdsForField(final String field, QueryLiteral literal, boolean emptyOperand)
    {
        final String value = (literal.getLongValue() != null) ? literal.getLongValue().toString() : literal.getStringValue();
        if (emptyOperand)
        {
            return (value != null) ? Collections.singleton(value) : EMPTY_ID;
        }

        // If passed an id, that is what we should return.
        if (literal.getLongValue() != null)
        {
            return ImmutableSet.of(literal.getLongValue().toString());
        }
        // Things that can be renamed (like issue constants, versions, and now users) need special treatment
        // during the history searches, because we need to search for IDs that had that value in the past,
        // not just the ones that currently have it.
        if (isConstantField(field))
        {
            return resolveIdsForConstantField(field, literal.getStringValue());
        }
        if (isAssigneeOrReporterField(field))
        {
            return resolveIdsForUserField(value);
        }
        if (isVersionField(field))
        {
            return resolveIdsForVersion(value);
        }
        return Collections.singleton(value);
    }

    private Collection<String> resolveIdsForConstantField(String field, String value)
    {
        // Handle magical empty value
        if (FieldIndexer.NO_VALUE_INDEX_VALUE.equals(value))
        {
            return ImmutableSet.of(value);
        }
        final IssueConstant issueConstant = constantsManager.getConstantByNameIgnoreCase(field, value);
        if (issueConstant == null)
        {
            return null;
        }
        return Collections.singletonList(issueConstant.getId());
    }

    private Collection<String> resolveIdsForUserField(String rawValue)
    {
        return userResolver.getIdsFromName(rawValue);
    }

    private Collection<String> resolveIdsForVersion(String rawValue)
    {
        // Handle magical empty value
        if (FieldIndexer.NO_VALUE_INDEX_VALUE.equals(rawValue))
        {
            return ImmutableSet.of(rawValue);
        }
        List<String> idsPerVersion = versionResolver.getIdsFromName(rawValue);
        if (idsPerVersion.isEmpty())
        {
            return null;
        }
        return Sets.newHashSet(idsPerVersion);
    }

    public boolean isConstantField(String field)
    {
        return constantsManager.getConstantObjects(field) != null;
    }

    public boolean isAssigneeOrReporterField(String field)
    {
        return SystemSearchConstants.forAssignee().getFieldUrlParameter().equalsIgnoreCase(field) ||
                SystemSearchConstants.forReporter().getFieldUrlParameter().equalsIgnoreCase(field);
    }

    public boolean isVersionField(String field)
    {
        return SystemSearchConstants.FIX_FOR_VERSION.equalsIgnoreCase(field);
    }
}
