package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;

import java.util.Collections;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Validation for the "all text fields" clause. Since this clause does not support searching on EMPTY, we can just
 * reuse the {@link CommentValidator}.
 * <p>
 * "All text" clause only supports the LIKE operator - NOT LIKE is too hard due to field visibility calculations, and we
 * couldn't decide whether or not aggregate results should be ORed or ANDed together.
 * <p>
 * All free text fields ultimately validate in the same way, using {@link FreeTextFieldValidator}, so we only do one
 * validation as opposed to going through each field and validating.
 *
 * @since v4.0
 */
public class AllTextValidator implements ClauseValidator
{
    private final CommentValidator delegate;
    private final SupportedOperatorsValidator supportedOperatorsValidator;

    public AllTextValidator(final CommentValidator delegate)
    {
        this.delegate = notNull("delegate", delegate);
        supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        final MessageSet messageSet = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (messageSet.hasAnyErrors())
        {
            return messageSet;
        }
        return delegate.validate(searcher, terminalClause);
    }

    SupportedOperatorsValidator getSupportedOperatorsValidator()
    {
        return new SupportedOperatorsValidator(Collections.singleton(Operator.LIKE));
    }
}
