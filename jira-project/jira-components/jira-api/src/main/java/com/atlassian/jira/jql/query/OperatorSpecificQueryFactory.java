package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.query.operator.Operator;

import java.util.List;

/**
 * An abstraction for the creation of a {@link org.apache.lucene.search.Query} from an expression and operator.
 * Because our Lucene index cannot support all operator-field combinations, we need to implement this in different
 * ways for different fields.
 *
 * @since v4.0
 */
public interface OperatorSpecificQueryFactory
{
    /**
     * Generates the Query for a single operand id.
     *
     * @param fieldName        the index field name the query should be generated for.
     * @param operator         operator which is handled by this implementation.
     *
     * @param rawValues        the raw values provided to the operand that need to be converted to index values.
     * @return the queryFactoryResult that contains the query and the metadata.
     *
     * @throws com.atlassian.query.operator.OperatorDoesNotSupportOperand if the method is passed an operator that it
     * can not handle. In this case the {@link #handlesOperator(com.atlassian.query.operator.Operator)} call will have
     * returned false.
     * @throws com.atlassian.query.operator.OperatorDoesNotSupportSingleOperand if the implementation does not support
     * the operator for single values.
     */
    QueryFactoryResult createQueryForSingleValue(String fieldName, Operator operator, List<QueryLiteral> rawValues);

    /**
     * Generates the Query for a list of operand ids.
     *
     * @param fieldName        the index field name the query should be generated for.
     * @param operator         operator which is handled by this implementation.
     * @param rawValues        the raw values provided to the operand that need to be converted to index values.
     * @return the queryFactoryResult that contains the query and the metadata.
     *
     * @throws com.atlassian.query.operator.OperatorDoesNotSupportOperand if the method is passed an operator that it
     * can not handle. In this case the {@link #handlesOperator(com.atlassian.query.operator.Operator)} call will have
     * returned false.
     * @throws com.atlassian.query.operator.OperatorDoesNotSupportMultiValueOperand if the implementation does not support
     * the operator for multiple values.
     */
    QueryFactoryResult createQueryForMultipleValues(String fieldName, Operator operator, List<QueryLiteral> rawValues);

    /**
     * Generates the query for an operand that has an {@link com.atlassian.jira.jql.operand.OperandHandler} that
     * returns true for the isEmpty method. This should generate a Lucene query that will perform the correct search
     * for issues where the field value is not set.
     *
     * @param fieldName        the index field name the query should be generated for.
     * @param operator         operator which is handled by this implementation.
     * @return the queryFactoryResult that contains the query and the metadata.
     */
    QueryFactoryResult createQueryForEmptyOperand(String fieldName, Operator operator);

    /**
     * @param operator the operator in question.
     * @return true if this implementation can handle the operator, false otherwise.
     */
    boolean handlesOperator(Operator operator);
}
