package com.atlassian.jira.jql.validator;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.operand.FunctionOperand;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Simple Function Operand Validator that validates the number of arguments in the
 * {@link com.atlassian.query.operand.FunctionOperand}.
 *
 * @since v4.0
 */
public class NumberOfArgumentsValidator
{
    private final int minExpected;
    private final int maxExpected;
    private final I18nHelper i18n;

    /**
     * @param expected the number of arguments expected. Input operands must have this exact number of arguments. Cannot be negative.
     * @param i18nHelper used for printing errors.
     */
    public NumberOfArgumentsValidator(final int expected, final I18nHelper i18nHelper)
    {
        this(expected, expected, i18nHelper);
    }

    /**
     * @param minExpected the minimum number of arguments expected (inclusive). Cannot be negative.
     * @param maxExpected the maximum number of arguments expected (inclusive). Cannot be negative.
     * @param i18nHelper used for printing errors.
     */
    public NumberOfArgumentsValidator(final int minExpected, final int maxExpected, final I18nHelper i18nHelper)
    {
        if (minExpected < 0 || maxExpected < 0)
        {
            throw new IllegalArgumentException("expected args must not be negative");
        }
        if (minExpected > maxExpected)
        {
            throw new IllegalArgumentException("Minimum number of args must be <= maximum number of args");
        }
        this.minExpected = minExpected;
        this.maxExpected = maxExpected;
        this.i18n = notNull("i18nHelper", i18nHelper);
    }

    /**
     * @param operand the function operand to validate
     * @return a message set with errors if the number of arguments was not as expected, otherwise an empty message set. Never null.
     */
    public MessageSet validate(FunctionOperand operand)
    {
        final List<String> args = operand.getArgs();
        final String name = operand.getName();
        MessageSet messages = new MessageSetImpl();
        if (minExpected == maxExpected && args.size() != minExpected)
        {
            messages.addErrorMessage(i18n.getText("jira.jql.function.arg.incorrect.exact", name, minExpected + "", args.size() + ""));
        }
        else if (minExpected > args.size() || maxExpected < args.size())
        {
            messages.addErrorMessage(i18n.getText("jira.jql.function.arg.incorrect.range", name, minExpected + "", maxExpected + "", args.size() + ""));
        }
        return messages;
    }
}
