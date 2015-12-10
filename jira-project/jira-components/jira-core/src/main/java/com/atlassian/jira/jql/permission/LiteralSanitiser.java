package com.atlassian.jira.jql.permission;

import com.atlassian.jira.jql.operand.QueryLiteral;

import java.util.List;

/**
 * Defines how to sanitise a list of query literals.
 *
 * @since v4.0
 */
public interface LiteralSanitiser
{
    /**
     * Note: in general, it is possible that a literal can expand out into multiple id values. The strategy for handling
     * these should be that if ALL values are sanitised, then modification should occur, but if at least one is okay
     * then we should keep the original literal.
     * 
     * @param literals the literals to sanitise; must not be null.
     * @return the result object, which states if there was any modification, and also contains the resultant literals. Never null.
     */
    Result sanitiseLiterals(List<QueryLiteral> literals);

    /**
     * Dictates the result of sanitising a list of {@link com.atlassian.jira.jql.operand.QueryLiteral}s. If no modifications
     * were made on the input, then the value returned by {@link #getLiterals()} is not guaranteed to be meaningful.
     */
    public static class Result
    {
        private final boolean isModified;
        private final List<QueryLiteral> literals;

        public Result(final boolean modified, final List<QueryLiteral> literals)
        {
            this.isModified = modified;
            this.literals = literals;
        }

        public boolean isModified()
        {
            return isModified;
        }

        public List<QueryLiteral> getLiterals()
        {
            return literals;
        }
    }
}
