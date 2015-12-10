package com.atlassian.query.clause;

import java.util.List;

import com.atlassian.annotations.PublicApi;

/**
 * Represents a node in the tree that gets generated for a {@link com.atlassian.query.Query}.
 * The tree of these will be used to generate an overall search.
 */
@PublicApi
public interface Clause
{
    /**
     * The name of the individual clause, this should be unique amongst the implementations otherwise
     * the clauses will be treated as the "same" type of clause.
     *
     * @return the name of the individual clause.
     */
    String getName();

    /**
     * @return child clauses if the clause has any, empty list if it has none.
     */
    List<Clause> getClauses();

    /**
     * Allows us to perform operations over the clauses based on the passed in visitor. This method calls the
     * visit method on the visitor with this reference.
     *
     * @param visitor the visitor to accept.
     *
     * @return the result of the visit operation who's type is specified by the incoming visitor.
     */
    <R> R accept (ClauseVisitor<R> visitor);

    /**
     * Return a string representation of the clause. This string representation should not be used to represent
     * the clause to the user as it may not be valid. For example, this method makes no attempt to escape invalid
     * names and strings.
     *
     * @return the string representation of the clause.
     */
    String toString();

}
