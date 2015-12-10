package com.atlassian.jira.jql.validator;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;

/**
 * A clause validator for the comment system field.
 *
 * @since v4.0
 */
public class CommentValidator extends FreeTextFieldValidator
{
    private final JqlOperandResolver jqlOperandResolver;
    ///CLOVER:OFF
    public CommentValidator(final JqlOperandResolver jqlOperandResolver)
    {
        super(SystemSearchConstants.forComments().getIndexField(), jqlOperandResolver);
        this.jqlOperandResolver = jqlOperandResolver;
    }
    ///CLOVER:ON

    @Nonnull
    @Override
    public MessageSet validate(final User searcher, @Nonnull final TerminalClause terminalClause)
    {
        // Comments are funny, they can not support empty searching so lets check that first
        if (jqlOperandResolver.isEmptyOperand(terminalClause.getOperand()))
        {
            final MessageSetImpl messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.field.does.not.support.empty", terminalClause.getName()));
            return messageSet;
        }
        return super.validate(searcher, terminalClause);
    }
}
