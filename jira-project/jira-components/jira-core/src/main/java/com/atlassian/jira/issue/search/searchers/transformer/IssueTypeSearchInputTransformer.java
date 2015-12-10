package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;

/**
 * A search input transformer for issue type
 *
 * @since v4.0
 */
public class IssueTypeSearchInputTransformer extends IssueConstantSearchInputTransformer<IssueType>
{
    public IssueTypeSearchInputTransformer(IndexInfoResolver<IssueType> indexInfoResolver, JqlOperandResolver operandResolver,
            FieldFlagOperandRegistry fieldFlagOperandRegistry, final NameResolver<IssueType> nameResolver)
    {
        super(SystemSearchConstants.forIssueType().getJqlClauseNames(),
                SystemSearchConstants.forIssueType().getUrlParameter(),
                indexInfoResolver,
                operandResolver,
                fieldFlagOperandRegistry,
                new NavigatorStructureChecker<IssueType>(SystemSearchConstants.forIssueType().getJqlClauseNames(), true,
                        fieldFlagOperandRegistry, operandResolver),
                nameResolver);
    }
}
