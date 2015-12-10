package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.query.operand.SingleValueOperand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Extension of {@link com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper} that knows how to create {@link com.atlassian.query.operand.SingleValueOperand}s by resolving
 * ids to Issue Constant names.
 *
 * @since v4.0
 */
public class IssueConstantIndexedInputHelper<T extends IssueConstant> extends DefaultIndexedInputHelper<T>
{
    private final NameResolver<T> issueConstantResolver;

    public IssueConstantIndexedInputHelper(IndexInfoResolver<T> indexInfoResolver, JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry, final NameResolver<T> issueConstantResolver)
    {
        super(indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
        this.issueConstantResolver = notNull("issueConstantResolver", issueConstantResolver);
    }


    @Override
    protected SingleValueOperand createSingleValueOperandFromId(final String stringValue)
    {
        final long issueConstantId;
        try
        {
            issueConstantId = Long.parseLong(stringValue);
        }
        catch (NumberFormatException e)
        {
            return new SingleValueOperand(stringValue);
        }

        final IssueConstant issueConstant = issueConstantResolver.get(issueConstantId);

        if (issueConstant != null)
        {
            return new SingleValueOperand(issueConstant.getName());
        }
        return new SingleValueOperand(issueConstantId);
    }
}
