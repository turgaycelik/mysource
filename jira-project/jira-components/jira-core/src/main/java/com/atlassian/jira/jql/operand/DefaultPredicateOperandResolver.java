package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.operand.Operand;
import org.apache.log4j.Logger;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link PredicateOperandResolver}
 *
 * @since v4.3
 */
public class DefaultPredicateOperandResolver  implements PredicateOperandResolver
{


    private static final Logger log = Logger.getLogger(DefaultPredicateOperandResolver.class);
    private final PredicateOperandHandlerRegistry predicateOperandHandlerRegistry;

    public DefaultPredicateOperandResolver(PredicateOperandHandlerRegistry handlerRegistry)
    {
          this.predicateOperandHandlerRegistry = handlerRegistry;
    }

    public List<QueryLiteral> getValues(User searcher, String field, final Operand operand)
    {
        notNull("operand", operand);
        return predicateOperandHandlerRegistry.getHandler(searcher, field, operand).getValues();
    }

    @Override
    public boolean isEmptyOperand(User searcher, String field, Operand operand)
    {
        return predicateOperandHandlerRegistry.getHandler(searcher, field, operand).isEmpty();
    }

    @Override
    public boolean isFunctionOperand(User searcher, String field, Operand operand)
    {
        return predicateOperandHandlerRegistry.getHandler(searcher, field, operand).isFunction();
    }

    @Override
    public boolean isListOperand(User searcher, String field, Operand operand)
    {
        return predicateOperandHandlerRegistry.getHandler(searcher, field, operand).isList();
    }
}
