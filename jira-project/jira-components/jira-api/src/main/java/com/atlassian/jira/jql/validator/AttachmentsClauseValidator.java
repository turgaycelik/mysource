package com.atlassian.jira.jql.validator;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.EmptyOperand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Clause validator for the attachments clause.
 *
 * @since v6.2
 */
public class AttachmentsClauseValidator implements ClauseValidator
{
    private final SupportedOperatorsValidator supportedOperatorsValidator;

    public AttachmentsClauseValidator()
    {
        this.supportedOperatorsValidator = getSupportedOperatorsValidator();
    }


    @Nonnull
    @Override
    public MessageSet validate(final User searcher, @Nonnull final TerminalClause terminalClause)
    {
        notNull("terminalClause", terminalClause);
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!errors.hasAnyErrors())
        {
            if (!(terminalClause.getOperand() instanceof EmptyOperand))
            {
                errors.addErrorMessage(getI18n(searcher).getText("jira.jql.clause.no.value.for.name", terminalClause.getName(), terminalClause.getOperand().getDisplayString()));
            }
        }
        return errors;
    }

    private SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(SystemSearchConstants.forAttachments().getSupportedOperators());
    }

    I18nHelper getI18n(User user)
    {
        return ComponentAccessor.getComponent(I18nHelper.BeanFactory.class).getInstance(user);
    }

}
