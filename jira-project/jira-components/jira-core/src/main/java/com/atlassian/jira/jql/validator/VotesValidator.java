package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.VotesIndexValueConverter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.TerminalClause;

/**
 * Votes clause validator
 *
 * @since v4.0
 */
@InjectableComponent
public class VotesValidator implements ClauseValidator
{
    private final IndexValuesValidator indexValuesValidator;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlOperandResolver jqlOperandResolver;
    private final VoteManager voteManager;

    public VotesValidator(final JqlOperandResolver jqlOperandResolver, final VotesIndexValueConverter votesIndexValueConverter, final VoteManager voteManager)
    {
        this.jqlOperandResolver = jqlOperandResolver;
        this.voteManager = voteManager;
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
        this.indexValuesValidator = getIndexValuesValidator(votesIndexValueConverter);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        if (voteManager.isVotingEnabled())
        {
            MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
            if (!errors.hasAnyErrors())
            {
                errors = indexValuesValidator.validate(searcher, terminalClause);
            }
            return errors;
        }
        else
        {
            MessageSet messageSet = new MessageSetImpl();
            messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.votes.disabled", terminalClause.getName()));
            return messageSet;
        }
    }

    IndexValuesValidator getIndexValuesValidator(final VotesIndexValueConverter votesIndexValueConverter)
    {
        return new IndexValuesValidator(jqlOperandResolver, votesIndexValueConverter, false)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, TerminalClause terminalClause, final QueryLiteral literal)
            {
                String fieldName = terminalClause.getName();
                final String literalString = literal.isEmpty() ? "EMPTY" : literal.asString();
                if (jqlOperandResolver.isFunctionOperand(literal.getSourceOperand()))
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.votes.value.function", literal.getSourceOperand().getName(), fieldName));
                }
                else
                {
                    messageSet.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.invalid.votes.value", fieldName, literalString));
                }
            }
        };
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS, OperatorClasses.RELATIONAL_ONLY_OPERATORS);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON
}
