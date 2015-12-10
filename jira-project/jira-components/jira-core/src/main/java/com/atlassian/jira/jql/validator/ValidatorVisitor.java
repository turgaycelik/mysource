package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ListOrderedMessageSetImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operand.Operand;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Used to perform validation over a {@link com.atlassian.query.Query}. Uses
 * {@link com.atlassian.jira.jql.validator.ClauseValidator}'s to validate the individual clauses and
 * {@link com.atlassian.jira.jql.operand.OperandHandler#validate(User, com.atlassian.query.operand.Operand, com.atlassian.query.clause.TerminalClause)}
 * to validate the operands.
 *
 * @since v4.0
 */
public class ValidatorVisitor implements ClauseVisitor<MessageSet>
{
    private final ValidatorRegistry validatorRegistry;
    private final JqlOperandResolver operandResolver;
    private final OperatorUsageValidator operatorUsageValidator;
    private final User searcher;
    private final Long filterId;

    public ValidatorVisitor(ValidatorRegistry validatorRegistry, JqlOperandResolver operandResolver,
            OperatorUsageValidator operatorUsageValidator, User searcher, final Long filterId)
    {
        this.validatorRegistry = validatorRegistry;
        this.operandResolver = operandResolver;
        this.operatorUsageValidator = operatorUsageValidator;
        this.searcher = searcher;
        this.filterId = filterId;
    }

    public MessageSet visit(final AndClause andClause)
    {
        return getMessagesFromSubClauses(andClause.getClauses());
    }

    public MessageSet visit(final NotClause notClause)
    {
        return getMessagesFromSubClauses(Collections.singletonList(notClause.getSubClause()));
    }

    public MessageSet visit(final OrClause orClause)
    {
        return getMessagesFromSubClauses(orClause.getClauses());
    }

    public MessageSet visit(final TerminalClause clause)
    {
        final Collection<ClauseValidator> validators = validatorRegistry.getClauseValidator(searcher, clause);
        final MessageSet messages = validateOperatorAndOperand(clause, validators);
        if (messages.hasAnyErrors())
        {
            return messages;
        }
        // Now validate the clause itself
        validateClause(clause, validators, messages);

        return messages;
    }

    private MessageSet validateOperatorAndOperand(TerminalClause clause,  Collection<ClauseValidator> validators)
    {
        final MessageSet messages = new ListOrderedMessageSetImpl();
        // JRA-18554 It does not make sense to validate the operands or operators if the clause does not exist
        if (!validators.isEmpty())
        {
            // Validate the operator makes sense from a global perspective
            if (validateOperator(clause, messages))
            {
                // Short-circuit if there are any errors, we are cool if there are warnings
                return messages;
            }

            // Validate the terminal clauses operands
            if (validateOperands(clause, messages))
            {
                // Short-circuit if there are any errors, we are cool if there are warnings
                return messages;
            }
        }
        return messages;
    }

    @Override
    public MessageSet visit(WasClause clause)
    {
        final Collection<ClauseValidator> wasClauseValidators = validatorRegistry.getClauseValidator(searcher, clause);
        final MessageSet messages = validateOperatorAndOperand(clause, wasClauseValidators);
        if (messages.hasAnyMessages())
        {
            return messages;
        }
          // Now validate the clause itself
        validateClause(clause, wasClauseValidators, messages);
        return messages;
    }

    @Override
    public MessageSet visit(ChangedClause clause)
    {
        final ChangedClauseValidator changedClauseValidator = validatorRegistry.getClauseValidator(searcher, clause);
        return changedClauseValidator.validate(searcher, clause);
    }

    private boolean validateOperator(final TerminalClause clause, final MessageSet messages)
    {
        messages.addMessageSet(operatorUsageValidator.validate(searcher, clause));
        return messages.hasAnyErrors();
    }

    private void validateClause(final TerminalClause clause, final Collection<ClauseValidator> validators, final MessageSet messages)
    {
        if (!validators.isEmpty())
        {
            for (ClauseValidator validator : validators)
            {
                final MessageSet messageSet;
                if (validator instanceof SavedFilterClauseValidator)
                {
                    messageSet = ((SavedFilterClauseValidator) validator).validate(searcher, clause, filterId);
                }
                else
                {
                    messageSet = validator.validate(searcher, clause);
                }

                messages.addMessageSet(messageSet);
            }
        }
        else
        {
            I18nHelper i18n = getI18n();
            //JRADEV-2649: Provide slighly more feedback about logged in/logged out state, so that clients can
            //display more useful error messages
            if(searcher != null)
            {
                messages.addErrorMessage(i18n.getText("jira.jql.validation.no.such.field", clause.getName()));
            }
            else
            {
                messages.addErrorMessage(i18n.getText("jira.jql.validation.no.such.field.anonymous", clause.getName()));
            }
        }
    }

    ///CLOVER:OFF
    I18nHelper getI18n()
    {
        return new I18nBean(searcher);
    }
    ///CLOVER:ON

    private boolean validateOperands(final TerminalClause clause, final MessageSet messages)
    {
        final Operand operand = clause.getOperand();
        messages.addMessageSet(operandResolver.validate(searcher, operand, clause));
        return messages.hasAnyErrors();
    }

    private MessageSet getMessagesFromSubClauses(List<Clause> subClauses)
    {
        final MessageSet messages = new ListOrderedMessageSetImpl();

        for (Clause subClause : subClauses)
        {
            final MessageSet subMessages = subClause.accept(this);
            if (subMessages != null)
            {
                messages.addMessageSet(subMessages);
            }
        }

        return messages;
    }

    public static class ValidatorVisitorFactory
    {
        private final ValidatorRegistry validatorRegistry;
        private final JqlOperandResolver operandResolver;
        private final OperatorUsageValidator operatorUsageValidator;

        public ValidatorVisitorFactory(final ValidatorRegistry validatorRegistry, final JqlOperandResolver operandResolver, final OperatorUsageValidator operatorUsageValidator)
        {
            this.validatorRegistry = Assertions.notNull("validatorRegistry", validatorRegistry);
            this.operandResolver = Assertions.notNull("operandResolver", operandResolver);
            this.operatorUsageValidator = Assertions.notNull("operatorUsageValidator", operatorUsageValidator);
        }

        public ValidatorVisitor createVisitor(User searcher, Long filterId)
        {
            return new ValidatorVisitor(validatorRegistry, operandResolver, operatorUsageValidator, searcher, filterId);
        }
    }
}
