package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.lucene.parsing.LuceneQueryParserFactory;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.QueryParser;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for free text custom fields.
 *
 * @since v4.0
 */
public class FreeTextCustomFieldSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private static final Logger log = Logger.getLogger(FreeTextCustomFieldSearchInputTransformer.class);
    private final CustomField customField;
    private final ClauseNames clauseNames;
    private final TextQueryValidator textQueryValidator;

    public FreeTextCustomFieldSearchInputTransformer(CustomField customField, ClauseNames clauseNames, String urlParameterName,
            final CustomFieldInputHelper customFieldInputHelper)
    {
        super(customField, clauseNames, urlParameterName, customFieldInputHelper);
        this.customField = customField;
        this.clauseNames = clauseNames;
        this.textQueryValidator = new TextQueryValidator();
    }

    @Override
    Clause createSearchClause(final User user, final String value)
    {
        return new TerminalClauseImpl(getClauseName(user, clauseNames), Operator.LIKE, value);
    }
    
    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        return convertForNavigator(query).fitsNavigator();
    }

    @Override
    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        if (fieldValuesHolder.containsKey(customField.getId()))
        {
            final CustomFieldParams customFieldParams = (CustomFieldParams) fieldValuesHolder.get(customField.getId());

            final String paramValue = getFieldValueAsString(customField.getCustomFieldType(), customFieldParams, errors);
            if (paramValue != null)
            {
                final MessageSet validationResult = textQueryValidator.validate(
                        getQueryParser(customField), paramValue, customField.getFieldName(), null, true, i18nHelper);

                for (final String errorMessage : validationResult.getErrorMessages())
                {
                    errors.addError(customField.getId(), errorMessage);
                }
            }
        }
    }

    /**
     * We know the field value will be a String, except if some other part of the system is completely broken.
     *
     * @param customFieldType   the custom field type
     * @param customFieldParams the field params, should be single value String.
     * @param errors            errors to add any problems to.
     * @return the String value, possibly null if there were errors.
     */
    private String getFieldValueAsString(CustomFieldType customFieldType, CustomFieldParams customFieldParams, ErrorCollection errors)
    {
        String paramValue = null;
        Object paramValueObject = new Object();
        try
        {
            paramValueObject = customFieldType.getValueFromCustomFieldParams(customFieldParams);
            paramValue = (String) paramValueObject;
        }
        catch (FieldValidationException e)
        {
            // this should never happen because we should always just be getting a string
            errors.addError(getCustomField().getId(), e.getMessage());
        }
        catch (ClassCastException e)
        {
            // shouldn't happen
            errors.addError(getCustomField().getId(), "Internal error attempting to validate the search term.");
            log.error("Expected to be able to get String value out of custom customField that has a " +
                "text searcher, actual value type is " + paramValueObject.getClass());
        }
        return paramValue;
    }

    ///CLOVER:OFF
    QueryParser getQueryParser(final CustomField customField)
    {
        return ComponentAccessor.getComponent(LuceneQueryParserFactory.class).createParserFor(customField.getId());
    }
    ///CLOVER:ON

}