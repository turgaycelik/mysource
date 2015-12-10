package com.atlassian.jira.issue.search.searchers.transformer;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.customfields.converters.DateConverter;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverter;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.ErrorCollectionAssert.assert1FieldError;
import static com.atlassian.jira.util.ErrorCollectionAssert.assertNoErrors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.issue.search.searchers.transformer.DateSearchInputTransformer}.
 *
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "ThrowableInstanceNeverThrown", "ResultOfObjectAllocationIgnored" })
public class TestDateSearchInputTransformer
{
    private static final User ANONYMOUS = null;
    private static final String FIELD_NAME = "aa";

    @Mock private SearchContext searchContext;
    @Mock private DateConverter dateConverter;
    @Mock private DateTimeConverter dateTimeConverter;
    @Mock private CustomFieldInputHelper customFieldInputHelper;
    @Mock private DateTimeFormatter formatter;
    @Mock private DateTimeFormatterFactory formatterFactory;
    @Mock private JqlDateSupport dateSupport;
    private JqlOperandResolver operandResolver = MockJqlOperandResolver.createSimpleSupport();

    @Mock
    @AvailableInContainer
    private TimeZoneManager timeZoneManager;

    @Rule
    public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);

    @Before
    public void setUp() throws Exception
    {
        final TimeZone zone = TimeZone.getDefault();
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(zone);
        when(timeZoneManager.getTimeZoneforUser(any(User.class))).thenReturn(zone);
        when(timeZoneManager.getDefaultTimezone()).thenReturn(zone);
        when(formatterFactory.formatter()).thenReturn(formatter);
        when(formatter.forLoggedInUser()).thenReturn(formatter);
        when(formatter.withStyle(any(DateTimeStyle.class))).thenReturn(formatter);
    }

    @After
    public void tearDown()
    {
        searchContext = null;
        dateConverter = null;
        dateTimeConverter = null;
        customFieldInputHelper = null;
        formatter = null;
        formatterFactory = null;
        timeZoneManager = null;
        operandResolver = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSearcherConfig()
    {
        new DateSearchInputTransformer(false, null, dateConverter, dateTimeConverter, operandResolver,
                dateSupport, customFieldInputHelper, timeZoneManager, formatterFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullDateConverter()
    {
        final DateSearcherConfig searcherConfig = new DateSearcherConfig("aa", new ClauseNames("aa"), FIELD_NAME);
        new DateSearchInputTransformer(false, searcherConfig, null, dateTimeConverter, operandResolver,
                dateSupport, customFieldInputHelper, timeZoneManager, formatterFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullJqlOperandResolver()
    {
        final DateSearcherConfig searcherConfig = new DateSearcherConfig("aa", new ClauseNames("aa"), FIELD_NAME);
        new DateSearchInputTransformer(false, searcherConfig, dateConverter, dateTimeConverter, null,
                dateSupport, customFieldInputHelper, timeZoneManager, formatterFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullJqlDateSupport()
    {
        final DateSearcherConfig searcherConfig = new DateSearcherConfig("aa", new ClauseNames("aa"), FIELD_NAME);
        new DateSearchInputTransformer(false, searcherConfig, dateConverter, dateTimeConverter, operandResolver,
                null, customFieldInputHelper, timeZoneManager, formatterFactory);
    }

    @Test
    public void testValidForNavigatorNullQuery() throws Exception
    {
        final String id = "fieldName";
        DateSearchInputTransformer transformer = createTransformer(id);
        assertTrue(transformer.doRelevantClausesFitFilterForm(ANONYMOUS, null, searchContext));
    }

    @Test
    public void testValidForNavigatorNoWhereClause() throws Exception
    {
        final String id = "fieldName";
        DateSearchInputTransformer transformer = createTransformer(id);
        assertTrue(transformer.doRelevantClausesFitFilterForm(ANONYMOUS, new QueryImpl(), searchContext));
    }

    @Test
    public void testValidForNavigatorFailed() throws Exception
    {
        final String id = "fieldName";
        TerminalClause clause = new TerminalClauseImpl(id, Operator.EQUALS, "value");
        final DateSearcherInputHelper helper = mock(DateSearcherInputHelper.class);
        when(helper.convertClause(clause, null, false)).thenReturn(
                new DateSearcherInputHelper.ConvertClauseResult(null, false));
        DateSearchInputTransformer transformer = createTransformerForValidateForNavigatorTests(id, helper);

        assertFalse(transformer.doRelevantClausesFitFilterForm(ANONYMOUS, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testValidForNavigatorHappyPath() throws Exception
    {
        final String id = "fieldName";
        TerminalClause clause = new TerminalClauseImpl(id, Operator.EQUALS, "value");
        final DateSearcherInputHelper helper = mock(DateSearcherInputHelper.class);
        when(helper.convertClause(clause, null, false)).thenReturn(
                new DateSearcherInputHelper.ConvertClauseResult(ImmutableMap.<String,String>of(), true));
        DateSearchInputTransformer transformer = createTransformerForValidateForNavigatorTests(id, helper);

        assertTrue(transformer.doRelevantClausesFitFilterForm(ANONYMOUS, new QueryImpl(clause), searchContext));
    }

    private DateSearchInputTransformer createTransformerForValidateForNavigatorTests(final String id, final DateSearcherInputHelper helper)
    {
        return new DateSearchInputTransformer(false, new DateSearcherConfig(id, new ClauseNames(id), FIELD_NAME),
                dateConverter, dateTimeConverter, operandResolver, dateSupport, customFieldInputHelper,
                timeZoneManager, formatterFactory)
        {
            @Override
            DateSearcherInputHelper createDateSearcherInputHelper()
            {
                return helper;
            }
        };
    }

    @Test
    public void testPopulateFromParams()
    {
        final String id = "fieldName";
        final DateSearchInputTransformer transformer = createTransformer(id);
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();

        final ActionParams params = mock(ActionParams.class);
        when(params.getFirstValueForKey(id + ":before")).thenReturn("beforeValue1");
        when(params.getFirstValueForKey(id + ":after")).thenReturn("afterValue1");
        when(params.getFirstValueForKey(id + ":nonsense")).thenReturn("nonsenseValue1");
        when(params.getFirstValueForKey(id + ":next")).thenReturn("nextValue1");
        transformer.populateFromParams(ANONYMOUS, fvh, params);

        // a few legit values
        assertEquals("beforeValue1", fvh.get(id + ":before"));
        assertEquals("afterValue1", fvh.get(id + ":after"));
        assertEquals("nextValue1", fvh.get(id + ":next"));

        assertFalse(fvh.containsKey(id + ":previous"));
        assertFalse(fvh.containsKey(id + ":nonsense"));
    }

    @Test
    public void testGetSearchClauseBlank() throws Exception
    {
        final DateSearchInputTransformer transformer = createTransformer("fieldName");
        final Clause clause = transformer.getSearchClause(ANONYMOUS, new FieldValuesHolderImpl());
        assertNull(clause);
    }

    @Test
    public void testGetSearchClauseAbsoluteOnlyFrom() throws Exception
    {
        final String input = "FromValue";
        final String output = "OutputValue";
        final String id = "fieldName";
        final Timestamp time = new Timestamp(1000);

        when(formatter.parse(input)).thenReturn(time);
        when(dateSupport.getDateString(time)).thenReturn(output);

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, input, null, null, null));
        final Clause expected = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, output);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseAbsoluteOnlyTo() throws Exception
    {
        final String id = "fieldName";
        final String input = "ToInput";
        final String output = "ToInput";
        final Timestamp time = new Timestamp(2131738712L);

        when(formatter.parse(input)).thenReturn(time);
        when(dateSupport.getDateString(time)).thenReturn(output);

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, null, input, null, null));
        final Clause expected = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, output);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClausePrimaryNameAndFieldNameDifferent() throws Exception
    {
        final String id = "fieldName";
        final String input = "ToInput";
        final String output = "ToInput";
        final Timestamp time = new Timestamp(2131738712L);

        when(formatter.parse(input)).thenReturn(time);
        when(dateSupport.getDateString(time)).thenReturn(output);
        when(customFieldInputHelper.getUniqueClauseName(ANONYMOUS, id, FIELD_NAME)).thenReturn(id);

        final DateSearchInputTransformer transformer = createTransformer(id, FIELD_NAME);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, null, input, null, null));
        final Clause expected = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, output);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseAbsoluteFromTo() throws Exception
    {
        final String id = "fieldName";
        final String beforeInput = "ToValue";
        final Timestamp beforeTime = new Timestamp(2743613748223L);
        final String beforeOutput = "ToValue output";
        final String afterInput = "FromValue";
        final Timestamp afterTime = new Timestamp(472381473982L);
        final String afterOutput = "FromValue output";

        when(formatter.parse(afterInput)).thenReturn(afterTime);
        when(formatter.parse(beforeInput)).thenReturn(beforeTime);
        when(dateSupport.getDateString(afterTime)).thenReturn(afterOutput);
        when(dateSupport.getDateString(beforeTime)).thenReturn(beforeOutput);

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, afterInput, beforeInput, null, null));
        final Clause expectedBefore = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeOutput);
        final Clause expectedAfter = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterOutput);
        final Clause expected = new AndClause(expectedAfter, expectedBefore);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseRelativeOnlyFrom() throws Exception
    {
        final String id = "why";
        final String input = "-4d";

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, null, null, input, null));
        final Clause expected = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, input);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseRelativeOnlyTo() throws Exception
    {
        final String id = "why";
        final String input = "-78h";

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, null, null, null, input));
        final Clause expected = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, input);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseRelativeFromTo() throws Exception
    {
        final String id = "testGetSearchClauseRelativeFromTo";
        final String beforeInput = "-3h";
        final String afterInput = "-3w";

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, null, null, afterInput, beforeInput));
        final Clause expectedPrevious = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterInput);
        final Clause expectedNext = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeInput);
        final Clause expected = new AndClause(expectedPrevious, expectedNext);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseAbsoluteFromRelativeTo() throws Exception
    {
        final String id = "testGetSearchClauseRelativeFromTo";
        final String beforeInput = "-17d";
        final String afterInput = "FromValueTestGetSearchClauseRelativeFromTo";
        final Timestamp afterTime = new Timestamp(7676);
        final String afterOutput = "FromValueTestGetSearchClauseRelativeFromTo output";

        when(formatter.parse(afterInput)).thenReturn(afterTime);
        when(dateSupport.getDateString(afterTime)).thenReturn(afterOutput);

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, afterInput, null, null, beforeInput));
        final Clause expectedAfter = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterOutput);
        final Clause expectedNext = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeInput);
        final Clause expected = new AndClause(expectedNext, expectedAfter);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseAbsoluteFromToAndRelativeTo() throws Exception
    {
        final String id = "testGetSearchClauseAbsoluteFromToAndRelativeTo";
        final String beforeRelInput = "-6h";
        final String beforeAbsInput = "beforeAbsInput";
        final Timestamp beforeAbsTime = new Timestamp(1111);
        final String beforeAbsOutput = "beforeAbsOutput";
        final String afterAbsInput = "afterAbsInput";
        final Timestamp afterAbsTime = new Timestamp(2222);
        final String afterAbsOutput = "afterAbsOutput";

        when(formatter.parse(afterAbsInput)).thenReturn(afterAbsTime);
        when(formatter.parse(beforeAbsInput)).thenReturn(beforeAbsTime);
        when(dateSupport.getDateString(beforeAbsTime)).thenReturn(beforeAbsOutput);
        when(dateSupport.getDateString(afterAbsTime)).thenReturn(afterAbsOutput);

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, afterAbsInput, beforeAbsInput, null, beforeRelInput));
        final Clause expectedAfter = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterAbsOutput);
        final Clause expectedBefore = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeAbsOutput);
        final Clause expectedNext = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeRelInput);
        final Clause expectedAbsolute = new AndClause(expectedAfter, expectedBefore);
        final Clause expected = new AndClause(expectedNext, expectedAbsolute);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseAbsoluteAndRelative() throws Exception
    {
        final String id = "testGetSearchClauseAbsoluteAndRelative";
        final String beforeRelInput = "-2w";
        final String afterRelInput = "-5w";
        final String beforeAbsInput = "beforeAbsInput";
        final Timestamp beforeAbsTime = new Timestamp(111);
        final String beforeAbsOutput = "beforeAbsOutput";
        final String afterAbsInput = "afterAbsInput";
        final Timestamp afterAbsTime = new Timestamp(222);
        final String afterAbsOutput = "afterAbsOutput";

        when(formatter.parse(afterAbsInput)).thenReturn(afterAbsTime);
        when(formatter.parse(beforeAbsInput)).thenReturn(beforeAbsTime);
        when(dateSupport.getDateString(beforeAbsTime)).thenReturn(beforeAbsOutput);
        when(dateSupport.getDateString(afterAbsTime)).thenReturn(afterAbsOutput);

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, afterAbsInput, beforeAbsInput, afterRelInput, beforeRelInput));
        final Clause expectedAfter = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterAbsOutput);
        final Clause expectedBefore = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeAbsOutput);
        final Clause expectedPrevious = new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterRelInput);
        final Clause expectedNext = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeRelInput);
        final Clause expectedAbsolute = new AndClause(expectedAfter, expectedBefore);
        final Clause expectedRelative = new AndClause(expectedPrevious, expectedNext);
        final Clause expected = new AndClause(expectedRelative, expectedAbsolute);
        assertEquals(expected, clause);
    }

    @Test
    public void testGetSearchClauseInvalidDate() throws Exception
    {
        final String id = "no...seriously....why";
        final String input = "myinput";

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause result = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, null, null, null, input));
        TerminalClause expectedResult = new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, input);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetSearchClauseInvalidDateWithValidPeriod() throws Exception
    {
        final String id = "try me please";
        final String afterInput = "invalidInput";
        final Timestamp afterTime = new Timestamp(30557);
        final String nextInput = "6d";

        when(formatter.parse(afterInput)).thenReturn(afterTime);

        final DateSearchInputTransformer transformer = createTransformer(id);
        Clause expectedClause = new AndClause(new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, nextInput), new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterInput));
        assertEquals(expectedClause, transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, afterInput, null, null, nextInput)));
    }

    @Test
    public void testGetSearchClauseValidDateWithInvalidPeriod() throws Exception
    {
        final String id = "try me please";
        final String afterInput = "validInput";
        final Timestamp afterTime = new Timestamp(46321);
        final String afterOutput = "validOutput";
        final String beforeInput = "invalidInput";

        when(formatter.parse(afterInput)).thenReturn(afterTime);
        when(dateSupport.getDateString(afterTime)).thenReturn(afterOutput);

        final DateSearchInputTransformer transformer = createTransformer(id);
        final Clause clause = transformer.getSearchClause(ANONYMOUS, createFieldValuesHolder(id, afterInput, null, null, beforeInput));
        Clause expected = new AndClause(new TerminalClauseImpl(id, Operator.LESS_THAN_EQUALS, beforeInput), new TerminalClauseImpl(id, Operator.GREATER_THAN_EQUALS, afterOutput));
        assertEquals(expected, clause);
    }

    @Test
    public void testValidateParamsEmpty() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, new FieldValuesHolderImpl(), i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsAbsoluteFromOnlyHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("After")).thenReturn(new Date());

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder("fieldName", "After", null, null, null), i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsAbsoluteFromOnlySad() throws Exception
    {
        final String id = "fieldName";
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("After")).thenThrow(new IllegalArgumentException("Message!"));
        when(formatter.getFormatHint()).thenReturn("Format invalid");

        DateSearchInputTransformer transformer = createTransformer(id);
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder(id, "After", null, null, null), i18n, errors);
        assert1FieldError(errors, "fieldName:after", "fields.validation.data.format [Format invalid]");
    }

    @Test
    public void testValidateParamsAbsoluteToOnlyHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("Before")).thenReturn(new Date());

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder("fieldName", null, "Before", null, null), i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsAbsoluteToOnlySad() throws Exception
    {
        final String id = "fieldName";
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("Before")).thenThrow(new IllegalArgumentException("Message!"));
        when(formatter.getFormatHint()).thenReturn("Format invalid");

        DateSearchInputTransformer transformer = createTransformer(id);
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder(id, null, "Before", null, null), i18n, errors);
        assert1FieldError(errors, "fieldName:before", "fields.validation.data.format [Format invalid]");
    }

    @Test
    public void testValidateParamsRelativeFromOnlyHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder("fieldName", null, null, "4d", null), i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsRelativeFromOnlySad() throws Exception
    {
        final String id = "fieldName";
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer(id);
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder(id, null, null, "invalidDuration", null), i18n, errors);
        assert1FieldError(errors, "fieldName:previous", "fields.validation.date.period.format.single.field [navigator.filter.constants.duedate.from]");
    }

    @Test
    public void testValidateParamsRelativeToOnlyHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder("fieldName", null, null, null, "4d"), i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsRelativeToOnlySad() throws Exception
    {
        final String id = "fieldName";
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer(id);
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder(id, null, null, null, "invalidDuration"), i18n, errors);
        assert1FieldError(errors, "fieldName:next", "fields.validation.date.period.format.single.field [navigator.filter.constants.duedate.to]");
    }

    @Test
    public void testValidateParamsAbsoluteFromToHappy() throws Exception
    {
        final Timestamp now = new Timestamp(new Date().getTime());
        final Timestamp aBitLater = new Timestamp(new Date().getTime() + 5000L);
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("After")).thenReturn(now);
        when(formatter.parse("Before")).thenReturn(aBitLater);

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder("fieldName", "After", "Before", null, null), i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsAbsoluteFromToSad() throws Exception
    {
        final Timestamp now = new Timestamp(new Date().getTime());
        final Timestamp aBitLater = new Timestamp(new Date().getTime() + 5000L);
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        when(formatter.parse("After")).thenReturn(aBitLater);
        when(formatter.parse("Before")).thenReturn(now);

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder("fieldName", "After", "Before", null, null), i18n, errors);
        assert1FieldError(errors, "fieldName:after", "fields.validation.date.absolute.before.after");
    }

    @Test
    public void testValidateParamsRelativeFromToHappy() throws Exception
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder("fieldName", null, null, "-4d", "4d"), i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidateParamsRelativeFromToSad()
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();

        DateSearchInputTransformer transformer = createTransformer("fieldName");
        transformer.validateParams(ANONYMOUS, null, createFieldValuesHolder("fieldName", null, null, "4d", "-4d"), i18n, errors);
        assert1FieldError(errors, "fieldName:previous", "fields.validation.date.period.from.to");
    }

    /**
     * Test for when there is no search in the query.
     */
    @Test
    public void testPopulateFromSearchRequestNoSearchQuery()
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        DateSearchInputTransformer transformer = createTransformer("testName");
        transformer.populateFromQuery(ANONYMOUS, holder, new QueryImpl(), searchContext);

        assertEquals(new FieldValuesHolderImpl(), holder);
    }

    /**
     * Test for when there is no clause in the query.
     */
    @Test
    public void testPopulateFromSearchRequestNoClause()
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        DateSearchInputTransformer transformer = createTransformer("testName");
        transformer.populateFromQuery(ANONYMOUS, holder, new QueryImpl(), searchContext);

        assertEquals(new FieldValuesHolderImpl(), holder);
    }

    /**
     * Test for when there is no date clauses.
     */
    @Test
    public void testPopulateFromSearchRequestNoDate()
    {
        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        DateSearchInputTransformer transformer = createTransformer("testName");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, new QueryImpl(new TerminalClauseImpl("notTestName", Operator.EQUALS, "something")), searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Test what happens when the operand returns multiple dates. We don't support this.
     */
    @Test
    public void testPopulateFromSearchRequestTooManyDates()
    {
        final FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        final FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        final DateSearchInputTransformer transformer = createTransformer("created2");
        transformer.populateFromQuery(ANONYMOUS, actualHolder,
                new QueryImpl(new TerminalClauseImpl("created2", Operator.LESS_THAN_EQUALS, new MultiValueOperand("something", "more"))),
                searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created <= "-3w"' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeTo()
    {
        final String beforeDate = "-3w";

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:next", beforeDate);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder,
                new QueryImpl(new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, beforeDate)),
                searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created >= "-2w"' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeFrom()
    {
        final String afterDate = "-2h";

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:previous", afterDate);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder,
                new QueryImpl(new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, afterDate)),
                searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created >= "-2w" and created <= -2h' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeFromAndTo()
    {
        final String afterDate = "-2w";
        final String beforeDate = "-2h";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, afterDate),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, beforeDate)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:previous", afterDate);
        expectedHolder.put("created:next", beforeDate);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created <= "-2w" and created <= -2h' is not converted.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeTooManyTo()
    {
        final String beforeDate2 = "-2w";
        final String beforeDate = "-2h";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, beforeDate2),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, beforeDate)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created >= "-2w" and created >= -2h and created<=-5h' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestRelativeTooManyFrom()
    {
        final String afterDate = "-2w";
        final String afterDate2 = "-2h";
        final String beforeDate = "-5h";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, afterDate),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, afterDate2),
                new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, beforeDate)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created <= "10/10/2009"' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsoluteTo()
    {
        final String toInput = "10/10/2009";
        final Date toDate = createDate(2009, 11, 10, 0, 0, 0, 0);
        final String toOutput = "10/Nov/2009";

        final Query query = new QueryImpl(new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, toInput));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:before", toOutput);

        when(dateSupport.convertToDate(toInput, TimeZone.getDefault())).thenReturn(toDate);
        when(dateConverter.getString(toDate)).thenReturn(toOutput);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created >= 1014873287389' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsoluteFrom()
    {
        final Long fromInput = 1014873287389L;
        final Date fromDate = createDate(1981, 1, 12, 0, 0, 0, 0);
        final String fromOutput = "12/Jan/1981";
        final Query query = new QueryImpl(new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, fromInput));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:after", fromOutput);

        when(dateSupport.convertToDate(fromInput)).thenReturn(fromDate);
        when(dateConverter.getString(fromDate)).thenReturn(fromOutput);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created <= 1000 and created >= "25/12/2008"' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsoluteFromAndTo()
    {
        final Long toInput = 1000L;
        final Date toDate = createDate(1945, 7, 3, 0, 0, 0, 0);
        final String toOutput = "3/July/1945";
        final String fromInput = "25/12/2008";
        final Date fromDate = createDate(2008, 12, 25, 0, 0, 0, 0);
        final String fromOutput = "someArbitraryString";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, toInput),
                new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, fromInput)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random222", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);
        expectedHolder.put("created:after", fromOutput);
        expectedHolder.put("created:before", toOutput);

        when(dateSupport.convertToDate(toInput)).thenReturn(toDate);
        when(dateSupport.convertToDate(fromInput, TimeZone.getDefault())).thenReturn(fromDate);
        when(dateConverter.getString(toDate)).thenReturn(toOutput);
        when(dateConverter.getString(fromDate)).thenReturn(fromOutput);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created <= 10001 and created <= 20002' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsolutTooManyTo()
    {
        final Long toInput = 10001L;
        final Date toDate = createDate(1945, 7, 3, 0, 0, 0, 0);
        final String toOutput = "3/July/1945";

        final Long toInput2 = 20002L;
        final Date toDate2 = createDate(2008, 12, 25, 0, 0, 0, 0);
        final String toOutput2 = "someArbitraryString";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, toInput),
                new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, toInput2)
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random222", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        when(dateSupport.convertToDate(toInput)).thenReturn(toDate);
        when(dateSupport.convertToDate(toInput2)).thenReturn(toDate2);
        when(dateConverter.getString(toDate)).thenReturn(toOutput);
        when(dateConverter.getString(toDate2)).thenReturn(toOutput2);

        DateSearchInputTransformer transformer = createTransformer("created");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query 'created >= 10 and created >= 20 and created <= -3w' is converted correctly.
     */
    @Test
    public void testPopulateFromSearchRequestAbsolutTooManyFrom()
    {
        final Long fromInput = 10L;
        final Date fromDate = createDate(1945, 7, 4, 0, 0, 0, 0);
        final String fromOutput = "4/July/1945";

        final Long fromInput2 = 20L;
        final Date fromDate2 = createDate(2008, 12, 25, 0, 0, 0, 0);
        final String fromOutput2 = "someArbitraryString";

        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("notCreated", Operator.GREATER_THAN_EQUALS, fromInput),
                new TerminalClauseImpl("notCreated", Operator.GREATER_THAN_EQUALS, fromInput2),
                new TerminalClauseImpl("notCreated", Operator.LESS_THAN_EQUALS, "-3w")
        ));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random222", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        when(dateSupport.convertToDate(fromInput)).thenReturn(fromDate);
        when(dateSupport.convertToDate(fromInput2)).thenReturn(fromDate2);
        when(dateConverter.getString(fromDate)).thenReturn(fromOutput);
        when(dateConverter.getString(fromDate2)).thenReturn(fromOutput2);

        DateSearchInputTransformer transformer = createTransformer("notCreated");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query with a bad operand (i.e. no value) does not compute.
     */
    @Test
    public void testPopulateFromSearchRequestBadLiteral()
    {
        final Operand operand = new SingleValueOperand("-67w");
        final TerminalClauseImpl clause = new TerminalClauseImpl("field1", Operator.LESS_THAN_EQUALS, operand);
        final Query query = new QueryImpl(clause);

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        actualHolder.put("random222", "word2");

        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(actualHolder);

        operandResolver = mock(JqlOperandResolver.class);
        when(operandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(
                new QueryLiteral(new SingleValueOperand("blarg"), (String)null) ));

        DateSearchInputTransformer transformer = createTransformer("field1");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query with a bad date does not compute.
     */
    @Test
    public void testPopulateFromQueryInvalidDate()
    {
        final String badDate = "13/13/2008";
        final Query query = new QueryImpl(new TerminalClauseImpl("field1", Operator.LESS_THAN_EQUALS, badDate));

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(MapBuilder.newBuilder("field1:before", badDate).toMap());

        DateSearchInputTransformer transformer = createTransformer("field1");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    /**
     * Check that the query where the navigator cannot display the date without loss cannot be handled.
     */
    @Test
    public void testPopulateFromQueryNotLossy()
    {
        final String lossyInput = "2008/12/25 15:00";
        final Date lossyDate = createDate(2008, 12, 25, 15, 0, 0, 0);
        final String lossyDateOutput = "lossyDateOutput";
        final String okDateString = "6/10/2008";
        final Date okDate = createDate(2008, 10, 6, 0, 0, 0, 0);
        final String okDateOutput = "6/10/2008";
        final Query query = new QueryImpl(new AndClause(
                new TerminalClauseImpl("field1", Operator.LESS_THAN_EQUALS, lossyInput),
                new TerminalClauseImpl("field1", Operator.GREATER_THAN_EQUALS, okDateString))
        );

        FieldValuesHolder actualHolder = new FieldValuesHolderImpl();
        FieldValuesHolder expectedHolder = new FieldValuesHolderImpl(
                MapBuilder.newBuilder("field1:before", lossyDateOutput)
                        .add("field1:after", okDateOutput)
                        .toMap());

        when(dateSupport.convertToDate(lossyInput, TimeZone.getDefault())).thenReturn(lossyDate);
        when(dateSupport.convertToDate(okDateString, TimeZone.getDefault())).thenReturn(okDate);
        when(dateTimeConverter.getString(lossyDate)).thenReturn(lossyDateOutput);
        when(dateConverter.getString(okDate)).thenReturn(okDateOutput);

        DateSearchInputTransformer transformer = createTransformer("field1");
        transformer.populateFromQuery(ANONYMOUS, actualHolder, query, searchContext);

        assertEquals(expectedHolder, actualHolder);
    }

    private FieldValuesHolder createFieldValuesHolder(String id, String after, String before, String previous, String next)
    {
        return new FieldValuesHolderImpl(MapBuilder.newBuilder(
                id + ":before", before,
                id + ":after", after,
                id + ":previous", previous,
                id + ":next", next).toMap());
    }

    private DateSearchInputTransformer createTransformer(String id)
    {
        return createTransformer(id, id);
    }

    private DateSearchInputTransformer createTransformer(String id, String fieldName)
    {
        return new DateSearchInputTransformer(false, new DateSearcherConfig(id, new ClauseNames(id), fieldName),
                dateConverter, dateTimeConverter, operandResolver, dateSupport, customFieldInputHelper,
                timeZoneManager, formatterFactory);
    }

    private static Date createDate(int year, int month, int day, int hour, int minute, int second, int millisecond)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, millisecond);
        return cal.getTime();
    }
}
