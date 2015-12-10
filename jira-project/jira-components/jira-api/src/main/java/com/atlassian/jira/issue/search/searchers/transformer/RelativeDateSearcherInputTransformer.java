package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.annotations.Internal;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherInputHelper;
import com.atlassian.jira.issue.search.searchers.util.RelativeDateSearcherInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.4
 */
@Internal
public class RelativeDateSearcherInputTransformer implements SearchInputTransformer
{
    private static final Logger log = Logger.getLogger(RelativeDateSearcherInputTransformer.class);

    private final DateSearcherConfig config;
    private final JqlOperandResolver operandResolver;
    private final JqlLocalDateSupport jqlLocalDateSupport;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    public RelativeDateSearcherInputTransformer
            (
                    final DateSearcherConfig config,
                    final JqlOperandResolver operandResolver,
                    final JqlLocalDateSupport jqlLocalDateSupport,
                    final CustomFieldInputHelper customFieldInputHelper,
                    final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.config = notNull("config", config);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.jqlLocalDateSupport = notNull("jqlLocalDateSupport", jqlLocalDateSupport);
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.dateTimeFormatterFactory =  notNull("dateTimeFormatterFactory", dateTimeFormatterFactory);
    }

    @Override
    public void populateFromParams(User user, FieldValuesHolder fieldValuesHolder, ActionParams actionParams)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);
        notNull("actionParams", actionParams);

        fieldValuesHolder.put(config.getBeforeField(), actionParams.getFirstValueForKey(config.getBeforeField()));
        fieldValuesHolder.put(config.getAfterField(), actionParams.getFirstValueForKey(config.getAfterField()));
        fieldValuesHolder.put(config.getPreviousField(), actionParams.getFirstValueForKey(config.getPreviousField()));
        fieldValuesHolder.put(config.getNextField(), actionParams.getFirstValueForKey(config.getNextField()));
        fieldValuesHolder.put(config.getEqualsField(), actionParams.getFirstValueForKey(config.getEqualsField()));
    }

    @Override
    public void validateParams(User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, I18nHelper i18nHelper, ErrorCollection errors)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);
        notNull("i18nHelper", i18nHelper);
        notNull("errors", errors);

        validateAbsoluteDates(fieldValuesHolder, errors, i18nHelper);
        validateRelativeDates(fieldValuesHolder, errors, i18nHelper);
    }

    private void validateAbsoluteDates(final FieldValuesHolder fieldValuesHolder, final ErrorCollection errors, final I18nHelper i18nHelper)
    {
        // for each field, try to convert its value into a Timestamp
        final String[] dateParamNames = config.getAbsoluteFields();
        final LocalDate[] dateParamValues = new LocalDate[2];

        int i = 0;
        for (final String dateParamName : dateParamNames)
        {
            final String dateString = (String) fieldValuesHolder.get(dateParamName);
            if (StringUtils.isNotEmpty(dateString))
            {
                DateTimeFormatter dateTimeFormatter = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.DATE_PICKER);
                try
                {
                    //We have to parse the value in the user's time zone, because when the jqlLocalDateSupport
                    //converts it to a LocalDate we are using the user's time zone.
                    Date date = dateTimeFormatter.forLoggedInUser().parse(dateString);
                    dateParamValues[i] = jqlLocalDateSupport.convertToLocalDate(date.getTime());
                }
                catch (final IllegalArgumentException e)
                {
                    errors.addError(dateParamName, i18nHelper.getText("fields.validation.data.format", dateTimeFormatter.getFormatHint()));
                }
            }
            i++;
        }

        // validate date format After and Before are not stupid
        final LocalDate afterDate = dateParamValues[0];
        final LocalDate beforeDate = dateParamValues[1];
        if (afterDate != null && beforeDate != null)
        {
            if (beforeDate.compareTo(afterDate) < 0)
            {
                errors.addError(config.getAfterField(), i18nHelper.getText("fields.validation.date.absolute.before.after"));
            }
        }
    }

    private void validateRelativeDates(final FieldValuesHolder fieldValuesHolder, final ErrorCollection errors, final I18nHelper i18nHelper)
    {
        // for each field, try to convert its value into a Duration
        final String[] periodParamNames = config.getRelativeFields();
        final String[] periodParamLabels = { i18nHelper.getText("navigator.filter.constants.duedate.from"),
                i18nHelper.getText("navigator.filter.constants.duedate.to") };
        for (int i = 0; i < periodParamNames.length; i++)
        {
            final String periodParam = (String) fieldValuesHolder.get(periodParamNames[i]);
            if (StringUtils.isNotEmpty(periodParam))
            {
                try
                {
                    DateUtils.getDurationWithNegative(periodParam);
                }
                catch (final InvalidDurationException e)
                {
                    String validationKey = (fieldValuesHolder.size() > 1)? "fields.validation.date.period.format":"fields.validation.date.period.format.single.field";
                    errors.addError(periodParamNames[i], i18nHelper.getText(validationKey, periodParamLabels[i]));
                }
                catch (final NumberFormatException e)
                {
                    String validationKey = (fieldValuesHolder.size() > 1)? "fields.validation.date.period.format":"fields.validation.date.period.format.single.field";
                    errors.addError(periodParamNames[i], i18nHelper.getText(validationKey, periodParamLabels[i]));
                }
            }
        }

        // Validate that 'from' is not after 'to'
        final String previousDateString = (String) fieldValuesHolder.get(config.getPreviousField());
        final String nextDateString = (String) fieldValuesHolder.get(config.getNextField());
        if (StringUtils.isNotEmpty(previousDateString) && StringUtils.isNotEmpty(nextDateString))
        {
            try
            {
                final long prevDateLong = DateUtils.getDurationWithNegative(previousDateString);
                final long nextDateLong = DateUtils.getDurationWithNegative(nextDateString);
                if (prevDateLong > nextDateLong)
                {
                    errors.addError(config.getPreviousField(), i18nHelper.getText("fields.validation.date.period.from.to"));
                }
            }
            catch (final InvalidDurationException e)
            {
                // Errors logged previously
            }
            catch (final NumberFormatException e)
            {
                // Errors logged previously
            }
        }
    }

    @Override
    public void populateFromQuery(User user, FieldValuesHolder fieldValuesHolder, Query query, SearchContext searchContext)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);
        notNull("query", query);

        if (query.getWhereClause() != null)
        {
            DateSearcherInputHelper helper = createDateSearcherInputHelper();
            final DateSearcherInputHelper.ConvertClauseResult clauseResult = helper.convertClause(query.getWhereClause(), user, false);
            final Map<String, String> result = clauseResult.getFields();
            if (result != null)
            {
                fieldValuesHolder.putAll(result);
            }
        }
    }

    private DateSearcherInputHelper createDateSearcherInputHelper()
    {
        return new RelativeDateSearcherInputHelper(config, operandResolver, jqlLocalDateSupport, dateTimeFormatterFactory);
    }

    @Override
    public boolean doRelevantClausesFitFilterForm(User user, Query query, SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            final Clause whereClause = query.getWhereClause();
            // check that it conforms to simple navigator structure, and that the right number of clauses appear
            // with the correct operators
            DateSearcherInputHelper inputHelper = createDateSearcherInputHelper();
            return inputHelper.convertClause(whereClause, user, false).fitsFilterForm();
        }
        return true;
    }

    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);

        final String clauseName = getClauseName(user);
        final Clause relativeClause = createPeriodClause((String) fieldValuesHolder.get(config.getPreviousField()), (String) fieldValuesHolder.get(config.getNextField()), clauseName);

        final Clause absoluteClause = createDateClause((String) fieldValuesHolder.get(config.getAfterField()), (String) fieldValuesHolder.get(config.getBeforeField()), clauseName);
        final Clause equalsClause = createDateClause((String) fieldValuesHolder.get(config.getEqualsField()), Operator.EQUALS, clauseName);
        final Clause compoundAbsoluteClause = createCompoundClause(absoluteClause, equalsClause);

        return createCompoundClause(relativeClause, compoundAbsoluteClause);
    }

    private Clause createPeriodClause(final String lower, final String upper, final String clauseName)
    {
        return createCompoundClause(parsePeriodClause(lower, Operator.GREATER_THAN_EQUALS, clauseName), parsePeriodClause(upper, Operator.LESS_THAN_EQUALS, clauseName));
    }

    private Clause parsePeriodClause(final String period, final Operator operator, final String clauseName)
    {
        if (StringUtils.isBlank(period))
        {
            return null;
        }
        return new TerminalClauseImpl(clauseName, operator, period);
    }

    private Clause createDateClause(final String lower, final String upper, final String clauseName)
    {
        final Clause fromClause = createDateClause(lower, Operator.GREATER_THAN_EQUALS, clauseName);
        final Clause toClause = createDateClause(upper, Operator.LESS_THAN_EQUALS, clauseName);
        return createCompoundClause(fromClause, toClause);
    }

    private Clause createDateClause(final String date, final Operator operator, final String clauseName)
    {
        if (StringUtils.isNotBlank(date))
        {
            try
            {
                //We have to parse the value in the user's time zone, because when the jqlLocalDateSupport
                //converts it to a LocalDate we are using the user's time zone.
                Date parse = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.DATE_PICKER).forLoggedInUser().parse(date);
                LocalDate localDate = jqlLocalDateSupport.convertToLocalDate(parse.getTime());
                if (localDate != null)
                {
                    return new TerminalClauseImpl(clauseName, operator, jqlLocalDateSupport.getLocalDateString(localDate));
                }
            }
            catch (IllegalArgumentException e)
            {
                log.info(String.format("Unable to parse date '%s'.", date));
            }
            // If the parsing of the user date failed just put in the original input.
            return new TerminalClauseImpl(clauseName, operator, date);
        }
        else
        {
            return null;
        }
    }

    private static Clause createCompoundClause(final Clause left, final Clause right)
    {
        if (left == null)
        {
            return right;
        }
        else if (right != null)
        {
            return new AndClause(left, right);
        }
        else
        {
            return left;
        }
    }

    private String getClauseName(User searcher)
    {
        final String primaryName = config.getClauseNames().getPrimaryName();
        final String fieldName = config.getFieldName();

        if (primaryName.equalsIgnoreCase(fieldName))
        {
            return fieldName;
        }
        else
        {
            return customFieldInputHelper.getUniqueClauseName(searcher, primaryName, fieldName);
        }
    }
}
