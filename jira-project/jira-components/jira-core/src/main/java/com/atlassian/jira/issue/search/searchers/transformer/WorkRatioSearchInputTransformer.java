package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.DefaultWorkRatioSearcherInputHelper;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherConfig;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Search input transformer for the {@link com.atlassian.jira.issue.search.searchers.impl.WorkRatioSearcher}.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class WorkRatioSearchInputTransformer implements SearchInputTransformer
{
    public static final String MIN_LIMIT_SUFFIX = ":min";
    public static final String MAX_LIMIT_SUFFIX = ":max";

    private final SimpleFieldSearchConstants constants;
    private final WorkRatioSearcherConfig config;
    private final JqlOperandResolver operandResolver;

    public WorkRatioSearchInputTransformer(final SimpleFieldSearchConstants constants, final WorkRatioSearcherConfig config, final JqlOperandResolver operandResolver)
    {
        this.constants = notNull("constants", constants);
        this.config = notNull("config", config);
        this.operandResolver = notNull("operandResolver", operandResolver);
    }

    public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);
        notNull("actionParams", actionParams);

        fieldValuesHolder.put(getMinField(), actionParams.getFirstValueForKey(getMinField()));
        fieldValuesHolder.put(getMaxField(), actionParams.getFirstValueForKey(getMaxField()));
    }

    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);

        Long minLimit = validateRatioField(fieldValuesHolder, getMinField(), errors, i18nHelper, "navigator.filter.workratio.min.error");
        Long maxLimit = validateRatioField(fieldValuesHolder, getMaxField(), errors, i18nHelper, "navigator.filter.workratio.max.error");

        // check that min <= max
        if (minLimit != null && maxLimit != null && (minLimit > maxLimit))
        {
            errors.addError(getMinField(), i18nHelper.getText("navigator.filter.workratio.limits.error"));
        }
    }

    public void populateFromQuery(final User user, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);
        notNull("query", query);

        if (query.getWhereClause() != null)
        {
            WorkRatioSearcherInputHelper helper = createWorkRatioSearcherInputHelper();
            final Map<String, String> result = helper.convertClause(query.getWhereClause(), user);
            if (result != null)
            {
                fieldValuesHolder.putAll(result);
            }
        }
    }

    public boolean doRelevantClausesFitFilterForm(final User user, final Query query, final SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            WorkRatioSearcherInputHelper helper = createWorkRatioSearcherInputHelper();
            if (helper.convertClause(query.getWhereClause(), user) == null)
            {
                return false;
            }
        }
        return true;
    }

    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);

        final String minValue = (String) fieldValuesHolder.get(config.getMinField());
        final String maxValue = (String) fieldValuesHolder.get(config.getMaxField());

        final JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
        final Clause result;
        if (!StringUtils.isBlank(minValue) && !StringUtils.isBlank(maxValue))
        {
            result = builder.workRatio().range(minValue, maxValue).buildClause();
        }
        else if (!StringUtils.isBlank(minValue))
        {
            result = builder.workRatio().gtEq(minValue).buildClause();
        }
        else if (!StringUtils.isBlank(maxValue))
        {
            result = builder.workRatio().ltEq(maxValue).buildClause();
        }
        else
        {
            result = null;
        }
        return result;
    }

    WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
    {
        return new DefaultWorkRatioSearcherInputHelper(constants, operandResolver);
    }

    /**
     * Check that the field's value (if specified) is a valid ratio limit
     *
     * @param fieldValuesHolder the field values holder
     * @param fieldId the field to retrieve
     * @param errors the error collection
     * @param i18nHelper the i18n helper
     * @param errorKey the error msg to add if the input was invalid
     * @return the ratio value as a Long; null if there was an error.
     */
    private Long validateRatioField(final FieldValuesHolder fieldValuesHolder, final String fieldId, final ErrorCollection errors, final I18nHelper i18nHelper, final String errorKey)
    {
        final String input = (String) fieldValuesHolder.get(fieldId);
        Long limitValue = null;
        if (StringUtils.isNotEmpty(input))
        {
            try
            {
                limitValue = new Long(input);
            }
            catch (NumberFormatException e)
            {
                errors.addError(fieldId, i18nHelper.getText(errorKey));
            }
        }
        return limitValue;
    }

    private String getMinField()
    {
        return config.getMinField();
    }

    private String getMaxField()
    {
        return config.getMaxField();
    }
}
