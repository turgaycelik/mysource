package com.atlassian.query.clause;

import com.atlassian.annotations.Internal;

/**
 * Used to determine the logical precedence of the clauses that can be contained in a SearchQuery.
 */
@Internal
public enum ClausePrecedence
{
    /*
     * NOTE: The order of these enums is important. They should be ordered in order from lowest to highest precedence
     * so that that compareTo method can be used to compare precedence in a logical way.
     */

    // Has the lowest logical precedence
    OR(700),
    // Has the second highest logical precedence
    AND(1000),
    // Has the highest logical precedence
    NOT(2000),
    // This really has not precedence, but give it a large value to make things easuer.
    TERMINAL(Integer.MAX_VALUE);

    private final int value;

    public static ClausePrecedence getPrecedence(Clause clause)
    {
        if (clause instanceof AndClause)
        {
            return AND;
        }
        else if (clause instanceof OrClause)
        {
            return OR;
        }
        else if (clause instanceof NotClause)
        {
            return NOT;
        }
        else if (clause instanceof TerminalClause || clause instanceof ChangedClause)
        {
            return TERMINAL;
        }

        throw new IllegalArgumentException("Attempt to get precedence for an unsupported clause.");
    }

    private ClausePrecedence(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
