package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.converters.DateConverter;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverter;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherInputHelper;
import com.atlassian.jira.issue.search.searchers.util.DefaultDateSearcherInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.timezone.TimeZoneManager;
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

import static com.atlassian.jira.datetime.DateTimeStyle.DATE_PICKER;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A SearchInputTransformer for Dates.
 *
 * @since v4.0
 */
public class DateSearchInputTransformer implements SearchInputTransformer
{
    private static final Logger log = Logger.getLogger(DateSearchInputTransformer.class);

    private final DateSearcherConfig dateSearcherConfig;
    private final DateConverter dateConverter;
    private final JqlOperandResolver operandResolver;
    private final JqlDateSupport jqlDateSupport;
    private final DateTimeConverter dateTimeConverter;
    private boolean allowTimeComponent;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final TimeZoneManager timeZoneManager;
    private final CustomFieldInputHelper customFieldInputHelper;

    public DateSearchInputTransformer(
            final boolean allowTimeComponent,
            final DateSearcherConfig config,
            final DateConverter dateConverter,
            final DateTimeConverter dateTimeConverter,
            final JqlOperandResolver operandResolver,
            final JqlDateSupport jqlDateSupport,
            final CustomFieldInputHelper customFieldInputHelper,
            final TimeZoneManager timeZoneManager,
            final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.allowTimeComponent = allowTimeComponent;
        this.dateSearcherConfig = notNull("config", config);
        this.dateConverter = notNull("dateConverter", dateConverter);
        this.dateTimeConverter = notNull("dateTimeConverter", dateTimeConverter);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.jqlDateSupport = notNull("jqlDateSupport", jqlDateSupport);
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.timeZoneManager = notNull("timeZoneManager", timeZoneManager);
        this.dateTimeFormatterFactory = notNull("dateTimeFormatterFactory", dateTimeFormatterFactory);
    }

    public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);
        notNull("actionParams", actionParams);

        fieldValuesHolder.put(dateSearcherConfig.getBeforeField(), actionParams.getFirstValueForKey(dateSearcherConfig.getBeforeField()));
        fieldValuesHolder.put(dateSearcherConfig.getAfterField(), actionParams.getFirstValueForKey(dateSearcherConfig.getAfterField()));
        fieldValuesHolder.put(dateSearcherConfig.getPreviousField(), actionParams.getFirstValueForKey(dateSearcherConfig.getPreviousField()));
        fieldValuesHolder.put(dateSearcherConfig.getNextField(), actionParams.getFirstValueForKey(dateSearcherConfig.getNextField()));
    }

    public void validateParams(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
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
        final String[] dateParamNames = dateSearcherConfig.getAbsoluteFields();
        final Date[] dateParamValues = new Date[2];

        int i = 0;
        for (final String dateParamName : dateParamNames)
        {
            final String dateString = (String) fieldValuesHolder.get(dateParamName);
            if (StringUtils.isNotEmpty(dateString))
            {
                DateTimeFormatter formatter = dateTimeFormatterFactory.formatter().forLoggedInUser().withStyle(DATE_PICKER);
                try
                {
                    dateParamValues[i] = formatter.parse(dateString);
                }
                catch (final IllegalArgumentException e)
                {
                    errors.addError(dateParamName, i18nHelper.getText("fields.validation.data.format", formatter.getFormatHint()));
                }
            }
            i++;
        }

        // validate date format After and Before are not stupid
        final Date afterDate = dateParamValues[0];
        final Date beforeDate = dateParamValues[1];
        if (afterDate != null && beforeDate != null)
        {
            if (beforeDate.compareTo(afterDate) < 0)
            {
                errors.addError(dateSearcherConfig.getAfterField(), i18nHelper.getText("fields.validation.date.absolute.before.after"));
            }
        }
    }

    private void validateRelativeDates(final FieldValuesHolder fieldValuesHolder, final ErrorCollection errors, final I18nHelper i18nHelper)
    {
        // for each field, try to convert its value into a Duration
        final String[] periodParamNames = dateSearcherConfig.getRelativeFields();
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
        final String previousDateString = (String) fieldValuesHolder.get(dateSearcherConfig.getPreviousField());
        final String nextDateString = (String) fieldValuesHolder.get(dateSearcherConfig.getNextField());
        if (StringUtils.isNotEmpty(previousDateString) && StringUtils.isNotEmpty(nextDateString))
        {
            try
            {
                final long prevDateLong = DateUtils.getDurationWithNegative(previousDateString);
                final long nextDateLong = DateUtils.getDurationWithNegative(nextDateString);
                if (prevDateLong > nextDateLong)
                {
                    errors.addError(dateSearcherConfig.getPreviousField(), i18nHelper.getText("fields.validation.date.period.from.to"));
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

    public void populateFromQuery(final User user, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);
        notNull("query", query);

        if (query.getWhereClause() != null)
        {
            DateSearcherInputHelper helper = createDateSearcherInputHelper();
            final DateSearcherInputHelper.ConvertClauseResult clauseResult = helper.convertClause(query.getWhereClause(), user, allowTimeComponent);
            final Map<String, String> result = clauseResult.getFields();
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
            final Clause whereClause = query.getWhereClause();
            // check that it conforms to simple navigator structure, and that the right number of clauses appear
            // with the correct operators
            DateSearcherInputHelper inputHelper = createDateSearcherInputHelper();
            return inputHelper.convertClause(whereClause, user, allowTimeComponent).fitsFilterForm();
        }
        return true;
    }

    DateSearcherInputHelper createDateSearcherInputHelper()
    {
        return new DefaultDateSearcherInputHelper(dateSearcherConfig, operandResolver, jqlDateSupport, dateConverter, dateTimeConverter, timeZoneManager);
    }

    public Clause getSearchClause(final User user, final FieldValuesHolder fieldValuesHolder)
    {
        notNull("fieldValuesHolder", fieldValuesHolder);

        final String clauseName = getClauseName(user);
        final Clause relativeClause = createPeriodClause((String) fieldValuesHolder.get(dateSearcherConfig.getPreviousField()), (String) fieldValuesHolder.get(dateSearcherConfig.getNextField()), clauseName);
        final Clause absoluteClause = createDateClause((String) fieldValuesHolder.get(dateSearcherConfig.getAfterField()), (String) fieldValuesHolder.get(dateSearcherConfig.getBeforeField()), clauseName);
        return createCompoundClause(relativeClause, absoluteClause);
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
                Date parsedDate = dateTimeFormatterFactory.formatter().withStyle(DATE_PICKER).forLoggedInUser().parse(date);
                if (parsedDate != null)
                {
                    final String jqlDate = jqlDateSupport.getDateString(parsedDate);
                    if (jqlDate != null)
                    {
                        return new TerminalClauseImpl(clauseName, operator, jqlDate);
                    }
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
        final String primaryName = dateSearcherConfig.getClauseNames().getPrimaryName();
        final String fieldName = dateSearcherConfig.getFieldName();

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
