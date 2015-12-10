package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.DefaultClauseInformation;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Unit test for @link{com.atlassian.jira.issue.search.searchers.transformer.TextQuerySearchInputTransformer}.
 *
 * @since v5.2
 */
public class TestTextQuerySearchInputTransformer
{
    @Test
    public void emptyQuery() {
        final DefaultClauseInformation information = new DefaultClauseInformation("indexfield", "text", "fieldid", OperatorClasses.TEXT_OPERATORS, JiraDataTypes.TEXT);
        TextQuerySearchInputTransformer transformer = new TextQuerySearchInputTransformer("t", information, new MockJqlOperandResolver());
        assertFalse("expected no fit for empty query", transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), null));
    }

    @Test
    public void simpleAnonTextQuery() {
        final DefaultClauseInformation information = new DefaultClauseInformation("indexfield", "text", "fieldid", OperatorClasses.TEXT_OPERATORS, JiraDataTypes.TEXT);
        final JqlOperandResolver operandResolver = MockJqlOperandResolver.createSimpleSupport();
        TextQuerySearchInputTransformer transformer = new TextQuerySearchInputTransformer("t", information, operandResolver);

        final Query simpleQuery = new QueryImpl(new TerminalClauseImpl("text", Operator.LIKE, new SingleValueOperand("double rainbow")));
        final SearchContext searchContext = null;
        assertTrue("Expected simple text query for anonymous to fit filter form", transformer.doRelevantClausesFitFilterForm(null, simpleQuery, searchContext));
    }

    @Test
    public void notLike() {
        final DefaultClauseInformation information = new DefaultClauseInformation("indexfield", "text", "fieldid", OperatorClasses.TEXT_OPERATORS, JiraDataTypes.TEXT);
        final JqlOperandResolver operandResolver = MockJqlOperandResolver.createSimpleSupport();
        TextQuerySearchInputTransformer transformer = new TextQuerySearchInputTransformer("t", information, operandResolver);

        final Query simpleQuery = new QueryImpl(new TerminalClauseImpl("text", Operator.NOT_LIKE, new SingleValueOperand("double rainbow")));
        final SearchContext searchContext = null;
        assertFalse("Expected simple text !~ query to not fit filter form", transformer.doRelevantClausesFitFilterForm(null, simpleQuery, searchContext));
    }
}
