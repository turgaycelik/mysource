package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.Clause;

/**
 * Represents a mutable JQL clause. Used interally by JQL builers to contruct a JQL clause incrementally.
 *
 * @since v4.0
 */
interface MutableClause
{
    /**
     * Combines the passed clause with the current using the passed operator. A new MutableClause may be returned
     * if necessary.
     *
     * @param logicalOperator the operator to use in the combination.
     * @param otherClause the clause to combine.
     *
     * @return the combined clause. A new clause may be returned.
     */
    MutableClause combine(BuilderOperator logicalOperator, MutableClause otherClause);

    /**
     * Turn the current MutableClause into a JQL clause.
     *
     * @return a new equilavent JQL clause.
     */
    Clause asClause();

    /**
     * Copy the clause so that is may be used safely. May return null to indicate that there is no clause.
     *
     * @return a copy of the clause so that it may be used safely. May return null to indicate that there is no clause.
     */
    MutableClause copy();
}
