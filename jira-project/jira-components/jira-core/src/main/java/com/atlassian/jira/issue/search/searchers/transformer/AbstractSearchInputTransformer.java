package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.searchers.transformer.TextQueryValidator;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.lucene.parsing.LuceneQueryParserFactory;
import com.atlassian.query.operand.Operand;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Common capabilities for SearchInputTransformer implementations.
 *
 * @since v5.2
 */
public abstract class AbstractSearchInputTransformer implements SearchInputTransformer
{
    protected JqlOperandResolver operandResolver;
    protected final String fieldsKey;
    protected final String id;
    private final TextQueryValidator textQueryValidator;

    public AbstractSearchInputTransformer(JqlOperandResolver operandResolver, String id, String fieldsKey)
    {
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.id = id;
        this.fieldsKey = fieldsKey;
        this.textQueryValidator = new TextQueryValidator();
    }

    protected boolean hasDuplicates(final List<TerminalClause> foundChildren)
    {
        Set<String> containsSet = new HashSet<String>();
        for (TerminalClause foundChild : foundChildren)
        {
            if (!containsSet.add(foundChild.getName()))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean hasEmpties(final List<TerminalClause> foundChildren)
    {
        for (TerminalClause foundChild : foundChildren)
        {
            final Operand operand = foundChild.getOperand();
            if (operandResolver.isEmptyOperand(operand))
            {
                return true;
            }
        }
        return false;
    }

    protected String getValueForField(final List<TerminalClause> terminalClauses, final User user, String... jqlClauseNames)
    {
        return getValueForField(terminalClauses, user, Arrays.asList(jqlClauseNames));
    }

    protected String getValueForField(final List<TerminalClause> terminalClauses, final User user, Collection<String> jqlClauseNames)
    {
        TerminalClause theClause = null;
        for (TerminalClause terminalClause : terminalClauses)
        {
            if (jqlClauseNames.contains(terminalClause.getName()))
            {
                // if there was already a clause with the same name, then return null
                if (theClause != null)
                {
                    return null;
                }
                else
                {
                    theClause = terminalClause;
                }
            }
        }

        if (theClause != null)
        {
            final Operand operand = theClause.getOperand();
            final QueryLiteral rawValue = operandResolver.getSingleValue(user, operand, theClause);
            if (rawValue != null && !rawValue.isEmpty())
            {
                return rawValue.asString();
            }
        }
        return null;
    }

    public void validateParams(final User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, I18nHelper i18nHelper, ErrorCollection errors)
    {
        final String query = (String) fieldValuesHolder.get(id);

        if (StringUtils.isNotBlank(query))
        {

            final MessageSet validationResult = textQueryValidator.validate(createQueryParser(), query, id, null, true, i18nHelper);
            for (final String errorMessage : validationResult.getErrorMessages())
            {
                errors.addError(id, errorMessage);
            }
        }
    }

    QueryParser createQueryParser()
    {
        // We pass in the summary index field here, because we dont actually care about the lhs of the query, only that
        // user input can be parsed.
        return ComponentAccessor.getComponent(LuceneQueryParserFactory.class).
                createParserFor(SystemSearchConstants.forSummary().getIndexField());
    }
}
