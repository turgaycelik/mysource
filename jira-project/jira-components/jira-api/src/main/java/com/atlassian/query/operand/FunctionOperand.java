package com.atlassian.query.operand;

import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a function in the query tree.
 *
 * @since v4.0
 */
public class FunctionOperand implements Operand
{
    private final String name;
    private final List<String> args;

    //This is calculated on the fly. I don't care about memory affects. In the most likely case this will only be calculated
    //once, in the worst case this may be calculated multiple times (but all to the same value).
    private String caseInsensitiveName = null;

    public FunctionOperand(final String name)
    {
        this(name, Collections.<String>emptyList());
    }

    public FunctionOperand(final String name, final String...args)
    {
        this(name, Arrays.asList(Assertions.notNull("args", args)));
    }

    public FunctionOperand(String name, Collection<String> args)
    {
        this.name = Assertions.notNull("name", name);
        this.args = CollectionUtil.copyAsImmutableList(Assertions.notNull("args", args));
    }

    public String getName()
    {
        return name;
    }

    /**
     * Renders the function call with its name and arguments for display in the UI.
     *
     * @return something that looks like this: shear(sheep, goats).
     */
    public String getDisplayString()
    {
        if (args.isEmpty())
        {
            return name + "()";
        }
        else
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(name).append("(");
            boolean first = true;
            for (String arg : args)
            {
                if (!first)
                {
                    sb.append(", ");
                }
                sb.append(arg);
                first = false;
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public <R> R accept(final OperandVisitor<R> visitor)
    {
        return visitor.visit(this);
    }

    public List<String> getArgs()
    {
        return args;
    }

    private String getCaseInsensitiveName()
    {
        if (caseInsensitiveName == null)
        {
            caseInsensitiveName = name.toLowerCase();
        }
        return caseInsensitiveName;
    }

    ///CLOVER: OFF

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final FunctionOperand that = (FunctionOperand) o;

        if (!args.equals(that.args))
        {
            return false;
        }
        if (!getCaseInsensitiveName().equals(that.getCaseInsensitiveName()))
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    @Override
    public int hashCode()
    {
        int result = getCaseInsensitiveName().hashCode();
        result = 31 * result + args.hashCode();
        return result;
    }
    ///CLOVER:ON
}
