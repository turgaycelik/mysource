package com.atlassian.query.operand;

/**
 * Represents the right hand side value of a clause.
 *
 * @since v4.0
 */
public interface Operand
{
    /**
     * The name that represents this Operand.
     *
     * @return the name of the operand, null if the operand is unnamed. If an operand is unnamed then it likely represents
     *         literal values (such as Strings or Longs).
     */
    String getName();

    /**
     * Produces the unexpanded representation of the Operand. In the case of a function operand this would be the
     * function as represented in the Query (i.e. group(jira-users)).
     *
     * @return a string that represents this operand as represented in the JQL query string.
     */
    String getDisplayString();

    /**
     * Allows us to perform operations over the operand based on the passed in visitor. This method calls the
     * visit method on the visitor with this reference.
     *
     * @param visitor the visitor to accept.
     * @param <R> the return type for the visitor.
     * @return the result of the visit operation who's type is specified by the incomming visitor.
     */
     <R> R accept(OperandVisitor<R> visitor);    
}
