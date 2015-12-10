package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Can generate queries for issue constant clauses.
 *
 * @since v4.0
 */
public class GenericClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(GenericClauseQueryFactory.class);

    private final JqlOperandResolver operandResolver;
    private final List<OperatorSpecificQueryFactory> operatorQueryFactories;
    private final String documentFieldName;

    public GenericClauseQueryFactory(final String documentFieldName, List<OperatorSpecificQueryFactory> operatorQueryFactories, JqlOperandResolver operandResolver)
    {
        this.documentFieldName = notNull("documentFieldName", documentFieldName);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.operatorQueryFactories = CollectionUtil.copyAsImmutableList(notNull("operatorQueryFactories", operatorQueryFactories));
    }

    public GenericClauseQueryFactory(SimpleFieldSearchConstants constants, List<OperatorSpecificQueryFactory> operatorQueryFactories, JqlOperandResolver operandResolver)
    {
       this(constants.getIndexField(), operatorQueryFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(QueryCreationContext queryCreationContext, TerminalClause terminalClause)
    {
        final Operand operand = terminalClause.getOperand();
        Operator operator = terminalClause.getOperator();

        if (operandResolver.isValidOperand(operand))
        {
            // Run through all the registered operatorQueryFactories giving each one a chance to handle the current
            // Operator.
            for (OperatorSpecificQueryFactory operatorQueryFactory : operatorQueryFactories)
            {
                if (operatorQueryFactory.handlesOperator(operator))
                {
                    if (operandResolver.isEmptyOperand(operand))
                    {
                        return operatorQueryFactory.createQueryForEmptyOperand(documentFieldName, operator);
                    }
                    else
                    {
                        // Turn the raw input values from the Operand into values that we can query in the index.
                        final List<QueryLiteral> rawValues = getRawValues(queryCreationContext, terminalClause);

                        // We want to indicate to the OperatorQueryFactory whether these index values come from a
                        // single inputted value or a list of inputted values.
                        if (operandResolver.isListOperand(operand))
                        {
                            return operatorQueryFactory.createQueryForMultipleValues(documentFieldName, operator, rawValues);
                        }
                        else
                        {
                            return operatorQueryFactory.createQueryForSingleValue(documentFieldName, operator, rawValues);
                        }
                    }
                }
            }

            // If no one handled the operator then lets log it and return false
            log.debug(String.format("The '%s' clause does not support the %s operator.", terminalClause.getName(), operator.getDisplayString()));
            return QueryFactoryResult.createFalseResult();
        }
        // If there is no registered OperandHandler then lets log it and return false
        log.debug(String.format("There is no OperandHandler registered to handle the operand '%s' for operand '%s'.", operator.getDisplayString(), terminalClause.getOperand().getDisplayString()));
        return QueryFactoryResult.createFalseResult();
    }

    List<QueryLiteral> getRawValues(QueryCreationContext queryCreationContext, final TerminalClause clause)
    {
        return operandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
    }

}
