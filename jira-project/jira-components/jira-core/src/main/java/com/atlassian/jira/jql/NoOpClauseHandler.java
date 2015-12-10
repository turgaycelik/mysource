package com.atlassian.jira.jql;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.DefaultClauseInformation;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import javax.annotation.Nonnull;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.search.BooleanQuery;

import java.util.Collections;

/**
 * A clause handler that does nothing and will show the passed in error message when validate is invoked. This will
 * return a false query if the query factory is invoked, it will also generate an All context if asked for its
 * context. The permission handler is the one that is passed in.
 *
 * @since v4.0
 */
public class NoOpClauseHandler implements ClauseHandler
{
    private final ClauseInformation clauseInformation;
    private final ClauseQueryFactory clauseQueryFactory;
    private final ClauseValidator clauseValidator;
    private final ClausePermissionHandler clausePermissionHandler;
    private final ClauseContextFactory clauseContextFactory;

    public NoOpClauseHandler(final ClausePermissionHandler clausePermissionHandler, final String fieldId, final ClauseNames clauseNames, final String validationI18nKey)
    {
        this.clausePermissionHandler = clausePermissionHandler;
        this.clauseInformation = new DefaultClauseInformation(fieldId, clauseNames, fieldId, Collections.<Operator>emptySet(), JiraDataTypes.ALL);
        this.clauseQueryFactory = new ClauseQueryFactory()
        {
            public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
            {
                return new QueryFactoryResult(new BooleanQuery());
            }
        };
        this.clauseValidator = new ClauseValidator()
        {
            @Override
            public MessageSet validate(User searcher, @Nonnull TerminalClause terminalClause)
            {
                MessageSet messages = new MessageSetImpl();
                I18nHelper i18n = getI18n(searcher);
                messages.addErrorMessage(i18n.getText(validationI18nKey, terminalClause.getName()));

                return messages;
            }
        };
        this.clauseContextFactory =  new SimpleClauseContextFactory();
    }

    public ClauseInformation getInformation()
    {
        return this.clauseInformation;
    }

    public ClauseQueryFactory getFactory()
    {
        return this.clauseQueryFactory;
    }

    public ClauseValidator getValidator()
    {
        return this.clauseValidator;
    }

    public ClausePermissionHandler getPermissionHandler()
    {
        return this.clausePermissionHandler;
    }

    public ClauseContextFactory getClauseContextFactory()
    {
        return this.clauseContextFactory;
    }

    I18nHelper getI18n(final User user)
    {
        return new I18nBean(user);
    }

    @Override
    public String toString()
    {
        return "NoOpClauseHandler";
    }
}
