package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.util.dbc.Assertions;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndNotClauses;
import static com.atlassian.query.operator.Operator.EQUALS;

/**
 * This is an implementation of SearchRequestAppender suitable for use with any IssueConstant implementation. This is
 * typically used by subclasses of AbstractConstantStatisticsMapper which also implement SearchRequestAppender.Factory.
 *
 * @see SearchRequestAppender
 * @see AbstractConstantStatisticsMapper
 * @see SearchRequestAppender.Factory
 * @see IssueConstant
 * @since v6.0
 */
class IssueConstantSearchRequestAppender
        implements SearchRequestAddendumBuilder.AddendumCallback<IssueConstant>, SearchRequestAppender<IssueConstant>
{
    private final String issueFieldConstant;

    public IssueConstantSearchRequestAppender(String issueFieldConstant)
    {
        this.issueFieldConstant = Assertions.notNull(issueFieldConstant);
    }

    @Override
    public void appendNonNullItem(IssueConstant value, JqlClauseBuilder clauseBuilder)
    {
        clauseBuilder.addStringCondition(issueFieldConstant, EQUALS, value.getName());
    }

    @Override
    public void appendNullItem(JqlClauseBuilder clauseBuilder)
    {
        clauseBuilder.addEmptyCondition(issueFieldConstant);
    }

    @Override
    public SearchRequest appendInclusiveSingleValueClause(IssueConstant value, SearchRequest searchRequest)
    {
        return appendAndClause(value, searchRequest, this);
    }

    @Override
    public SearchRequest appendExclusiveMultiValueClause(Iterable<? extends IssueConstant> values, SearchRequest searchRequest)
    {
        return appendAndNotClauses(values, searchRequest, this);
    }
}