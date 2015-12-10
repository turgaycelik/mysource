package com.atlassian.jira.jql.clause;

import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;

import java.util.LinkedList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Compares two operands for equivalence, which is defined as having the same values, but
 * for {@link com.atlassian.query.operand.MultiValueOperand}s the ordering of values is
 * not relevant.
 *
 * @since v4.0
 */
class SimpleOperandComparator
{
    public boolean isEquivalent(Operand operand, Operand operand1)
    {
        notNull("operand", operand);
        notNull("operand1", operand1);

        if (operand.equals(operand1))
        {
            return true;
        }
        else
        {
            if (operand instanceof SingleValueOperand && operand1 instanceof SingleValueOperand)
            {                 
                return operand.equals(operand1);
            }
            else if (operand instanceof FunctionOperand && operand1 instanceof FunctionOperand)
            {
                return operand.equals(operand1);
            }
            else if (operand instanceof MultiValueOperand && operand1 instanceof MultiValueOperand)
            {
                return compareMulti((MultiValueOperand) operand, (MultiValueOperand) operand1);
            }
        }

        // if they're not the same operand type, then they aint equal!
        return false;
    }
          
    private boolean compareMulti(final MultiValueOperand operand, final MultiValueOperand operand1)
    {
        List<Operand> children = operand.getValues();
        List<Operand> children1 = new LinkedList<Operand>(operand1.getValues());

        if (children.size() != children1.size())
        {
            return false;
        }

        for (Operand child : children)
        {
            boolean found = false;
            for (Operand child1 : children1)
            {
                if (isEquivalent(child, child1))
                {
                    found = true;
                    children1.remove(child1);
                    break;
                }
            }

            if (!found)
            {
                return false;
            }
        }

        return true;
    }

}
