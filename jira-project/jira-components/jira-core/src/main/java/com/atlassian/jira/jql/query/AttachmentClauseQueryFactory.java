package com.atlassian.jira.jql.query;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import static com.atlassian.jira.jql.query.QueryFactoryResult.createFalseResult;

/**
 * Factory for producing clauses for the attachments.
 *
 * @since v6.2
 */
@InjectableComponent
public class AttachmentClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(WatcherClauseQueryFactory.class);

    public AttachmentClauseQueryFactory() {}

    @Nonnull
    public QueryFactoryResult getQuery(@Nonnull final QueryCreationContext queryCreationContext, @Nonnull final TerminalClause terminalClause)
    {
        if (SystemSearchConstants.forAttachments().getSupportedOperators().contains(terminalClause.getOperator()))
        {
            return new QueryFactoryResult(new TermQuery(new Term(DocumentConstants.ISSUE_ATTACHMENT, getTermText(terminalClause.getOperator()))));
        }
        else
        {
            log.debug("Attempt to search attachments when attachments are disabled.");
            return createFalseResult();
        }
    }

    private String getTermText(final Operator operator)
    {
        // Query for issues which contain attachment is "attachments IS NOT EMPTY", therefore we query lucene for
        // ISSUE_ATTACHMENT:true, for "attachments IS EMPTY" ISSUE_ATTACHMENT:false.
        return String.valueOf(operator != Operator.IS);
    }

}

