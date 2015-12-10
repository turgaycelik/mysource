package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.searchers.transformer.TextQueryValidator;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.lucene.parsing.LuceneQueryParserFactory;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.QueryParser;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A generic validator for text fields
 *
 * @since v4.0
 */
public class FreeTextFieldValidator implements ClauseValidator
{
    private static final Logger log = Logger.getLogger(FreeTextFieldValidator.class);
    /**
     * This field is not used anymore and will be removed in next releases.
     */
    @Deprecated
    public static final List<String> INVALID_FIRST_CHAR_LIST = ImmutableList.of("?", "*", "~", ":", ";", "!", "]", "[", "^", "{", "}", "(", ")");

    private final String indexField;
    private final JqlOperandResolver operandResolver;
    private final TextQueryValidator textQueryValidator;

    public FreeTextFieldValidator(final String indexField, final JqlOperandResolver operandResolver)
    {
        this.indexField = notBlank("indexField", indexField);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.textQueryValidator = new TextQueryValidator();
    }

    @Nonnull
    @Override
    public MessageSet validate(final User searcher, @Nonnull final TerminalClause terminalClause)
    {
        final MessageSet messageSet = new MessageSetImpl();
        final I18nHelper i18n = getI18n(searcher);
        final Operator operator = terminalClause.getOperator();
        final String fieldName = terminalClause.getName();
        if (!handlesOperator(operator))
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.does.not.support.operator", operator.getDisplayString(), fieldName));
            return messageSet;
        }

        final Operand operand = terminalClause.getOperand();

        final List<QueryLiteral> values = operandResolver.getValues(searcher, operand, terminalClause);
        if (values != null)
        {
            for (final QueryLiteral literal : values)
            {
                // empty literals are always okay
                if (!literal.isEmpty())
                {
                    final String query = literal.asString();
                    if (StringUtils.isNotBlank(query))
                    {
                        final String functionName = operandResolver.isFunctionOperand(literal.getSourceOperand())
                                ? literal.getSourceOperand().getName() : null;

                        final MessageSet validationResult = textQueryValidator.validate(
                                getQueryParser(indexField), query, fieldName, functionName, false, i18n);
                        messageSet.addMessageSet(validationResult);
                    }
                    else
                    {
                        messageSet.addErrorMessage(i18n.getText("jira.jql.text.clause.does.not.support.empty", fieldName));
                    }
                }
            }
        }
        else
        {
            // This should never be allowed to happen since we do not allow list operands with '~' so lets log it
            log.error("Text field validation was provided an operand handler that gave us back more than one value when validating '" + fieldName + "'.");
        }

        return messageSet;
    }

    private boolean handlesOperator(final Operator operator)
    {
        return OperatorClasses.TEXT_OPERATORS.contains(operator);
    }

    ///CLOVER:OFF
    I18nHelper getI18n(final User user)
    {
        final I18nHelper.BeanFactory beanFactory = ComponentAccessor.getComponent(I18nHelper.BeanFactory.class);
        return beanFactory.getInstance(user);
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    QueryParser getQueryParser(final String indexField)
    {
        return ComponentAccessor.getComponent(LuceneQueryParserFactory.class).createParserFor(indexField);
    }
    ///CLOVER:ON
}
