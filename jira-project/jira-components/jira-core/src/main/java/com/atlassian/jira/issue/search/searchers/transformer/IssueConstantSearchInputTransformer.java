package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IssueConstantIndexedInputHelper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A issue-constant-specific {@link IdIndexedSearchInputTransformer}.
 *
 * @since v4.0
 */
public class IssueConstantSearchInputTransformer<T extends IssueConstant> extends IdIndexedSearchInputTransformer<T>
{
    private final NameResolver<T> issueConstantResolver;

    public IssueConstantSearchInputTransformer(ClauseNames clauseNames, IndexInfoResolver<T> indexInfoResolver,
            JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final NameResolver<T> issueConstantResolver)
    {
        super(clauseNames, indexInfoResolver, operandResolver, fieldFlagOperandRegistry);
        this.issueConstantResolver = notNull("issueConstantResolver", issueConstantResolver);
    }

    public IssueConstantSearchInputTransformer(final ClauseNames jqlClauseNames, final String urlParameter, final IndexInfoResolver<T> indexInfoResolver,
            final JqlOperandResolver operandResolver, final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final NavigatorStructureChecker<IssueType> navigatorStructureChecker,
            final NameResolver<T> issueConstantResolver)
    {
        super(jqlClauseNames, urlParameter, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, navigatorStructureChecker);
        this.issueConstantResolver = notNull("issueConstantResolver", issueConstantResolver);

    }

    @Override
    IndexedInputHelper createIndexedInputHelper()
    {
        return new IssueConstantIndexedInputHelper<T>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, issueConstantResolver);
    }
}
