package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.query.operand.SingleValueOperand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Extension of {@link DefaultIndexedInputHelper} that knows how to create {@link SingleValueOperand}s by resolving
 * ids to Version names.
 *
 * @since v4.0
 */
public class VersionIndexedInputHelper extends DefaultIndexedInputHelper<Version>
{
    private final NameResolver<Version> versionResolver;

    public VersionIndexedInputHelper(IndexInfoResolver<Version> versionIndexInfoResolver, JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final NameResolver<Version> versionResolver)
    {
        super(versionIndexInfoResolver, operandResolver, fieldFlagOperandRegistry);
        this.versionResolver = notNull("versionResolver", versionResolver);
    }

    @Deprecated
    public VersionIndexedInputHelper(IndexInfoResolver<Version> versionIndexInfoResolver, JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry, final SearchContextVisibilityChecker searchContextVisibilityChecker,
            final NameResolver<Version> versionResolver)
    {
        this(versionIndexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver);
    }


    @Override
    protected SingleValueOperand createSingleValueOperandFromId(final String stringValue)
    {
        final long versionId;
        try
        {
            versionId = Long.parseLong(stringValue);
        }
        catch (NumberFormatException e)
        {
            return new SingleValueOperand(stringValue);
        }

        final Version version = versionResolver.get(versionId);

        if(version != null)
        {
            return new SingleValueOperand(version.getName());
        }
        return new SingleValueOperand(versionId);
    }
}
