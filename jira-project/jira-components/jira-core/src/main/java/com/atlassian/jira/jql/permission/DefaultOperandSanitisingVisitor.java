package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.OperandVisitor;
import com.atlassian.query.operand.SingleValueOperand;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p>The default strategy for sanitising an arbitrary {@link com.atlassian.query.operand.Operand} is:
 *
 * <ul>
 * <li>{@link com.atlassian.query.operand.EmptyOperand}s do not need sanitising;
 * <li>{@link com.atlassian.query.operand.FunctionOperand}s have their arguments sanitised by {@link com.atlassian.jira.jql.operand.JqlOperandResolver};
 * <li>{@link com.atlassian.query.operand.MultiValueOperand}s must have their children sanitised and potentially recombined
 * into a new MultiValueOperand instance.
 * <li>{@link com.atlassian.query.operand.SingleValueOperand}s should be sanitised depending on the context (what field's values
 * we are dealing with). But we don't know about that here, so we just return the operand back.
 * </ul>
 *
 * <p>In general, if no sanitisation is required, the input {@link com.atlassian.query.operand.Operand} should be returned.
 *
 * @since v4.0
 */
public class DefaultOperandSanitisingVisitor implements OperandVisitor<Operand>
{
    private final JqlOperandResolver jqlOperandResolver;
    private final User searcher;

    public DefaultOperandSanitisingVisitor(final JqlOperandResolver jqlOperandResolver, final User searcher)
    {
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.searcher = searcher;
    }

    public Operand visit(final EmptyOperand empty)
    {
        return empty;
    }

    public Operand visit(final FunctionOperand function)
    {
        return jqlOperandResolver.sanitiseFunctionOperand(searcher, function);
    }

    public Operand visit(final MultiValueOperand originalMulti)
    {
        boolean isModified = false;

        // keep a set of operands: if we're going to sanitise the operand, we may as well optimise and remove duplicates.
        Set<Operand> sanitisedOperands = new LinkedHashSet<Operand>();
        for (Operand childOperand : originalMulti.getValues())
        {
            Operand sanitisedChild = childOperand.accept(this);
            if (!sanitisedChild.equals(childOperand))
            {
                isModified = true;
            }
            sanitisedOperands.add(sanitisedChild);
        }

        return isModified ? new MultiValueOperand(sanitisedOperands) : originalMulti;
    }

    public Operand visit(final SingleValueOperand singleValueOperand)
    {
        // we are too dumb to know how to sanitise SingleValueOperands
        return singleValueOperand;
    }
}
