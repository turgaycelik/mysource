package com.atlassian.jira.jql.validator;

import java.util.Date;
import java.util.TimeZone;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.MockOperandHandler;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.util.DateRange;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertErrorMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoWarnings;

/**
 * Simple test for {@link DateValueValidator}.
 *
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDateValueValidator
{
    private static final String FIELD_NAME = "FieldName";

    @Mock TimeZoneManager timeZoneManager;

    @After
    public void tearDown()
    {
        timeZoneManager = null;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testNullArgsInConstructor() throws Exception
    {
        new DateValueValidator(null, timeZoneManager);
    }
      
    @Test
    public void testNoHandlerForOperand() throws Exception
    {
        final DateValueValidator val = new DateValueValidator(new MockJqlOperandResolver(), timeZoneManager);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, ""));
        assertNoMessages(messageSet);
    }

    @Test
    public void testEmptyValues() throws Exception
    {
        final OperandHandler<?> handler = new MockOperandHandler();
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, ""));
        assertNoMessages(messageSet);
    }

    @Test
    public void testEmptyOperand() throws Exception
    {
        JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        final DateValueValidator val = createValidator(jqlOperandResolver, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, EmptyOperand.EMPTY));
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidLongValue() throws Exception
    {
        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add(-123456789L);
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidPeriodFormat() throws Exception
    {
        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add("4w2d");
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidOutlookDateFormat() throws Exception
    {
        final String period = "4J2d";
        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add(period);
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidDdMmYyyy() throws Exception
    {
        final String date = "1/1/2008";
        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add(date);
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidYyyyMmDd1() throws Exception
    {
        final String date = "2008/01/01";
        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add(date);
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidYyyyMmDd2() throws Exception
    {
        final String date = "2008-01-21";

        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add(date);
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidYyyyMmDdHhMm1() throws Exception
    {
        final String date = "2008/01/01 13:22";

        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add(date);
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidYyyyMmDdHhMm2() throws Exception
    {
        final String date = "2008-01-21 15:22";

        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add(date);
        final DateValueValidator val = createValidator(handler, true);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertNoMessages(messageSet);
    }

    @Test
    public void testInvalidFormat() throws Exception
    {
        final String date = "NOTPARSABLE";

        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add(date);
        final DateValueValidator val = createValidator(handler, false);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.date.format.invalid [NOTPARSABLE] [FieldName]");
    }

    @Test
    public void testListOfValuesOneInvalid() throws Exception
    {
        final String date = "NOTPARSABLE";

        final MockOperandHandler<?> handler = new MockOperandHandler();
        handler.add("4w", date);

        final DateValueValidator val = createValidator(handler, false);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assertErrorMessages(messageSet,
                "jira.jql.clause.date.format.invalid [4w] [FieldName]",
                "jira.jql.clause.date.format.invalid [NOTPARSABLE] [FieldName]");
        assertNoWarnings(messageSet);
    }

    @Test
    public void testListOfValuesOneInvalidFromFunction() throws Exception
    {
        final String date = "NOTPARSABLE";

        final MockOperandHandler<?> handler = new MockOperandHandler(false, false, true);
        handler.add("4w", date);

        final DateValueValidator val = createValidator(handler, false);
        final MessageSet messageSet = val.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.date.format.invalid.from.func [FieldName] [SingleValueOperand]");
    }

    private static DateValueValidator createValidator(final OperandHandler<?> handler, final boolean isValid)
    {
        final MockJqlOperandResolver jqlOperandResolver = new MockJqlOperandResolver();
        jqlOperandResolver.addHandler(SingleValueOperand.OPERAND_NAME, handler);
        jqlOperandResolver.addHandler(MultiValueOperand.OPERAND_NAME, handler);
        return createValidator(jqlOperandResolver, isValid);
    }

    private static DateValueValidator createValidator(final JqlOperandResolver jqlOperandResolver, final boolean isValid)
    {
        final JqlDateSupport dateSupport = new JqlDateSupport()
        {
            public boolean validate(final String dateString)
            {
                return isValid;
            }

            public String getDateString(final Date date)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getDateString(Date date, TimeZone timeZone)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isDuration(String dateString)
            {
                throw new UnsupportedOperationException();
            }

            public Date convertToDate(final String dateString)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public Date convertToDate(String dateString, TimeZone timeZone)
            {
                throw new UnsupportedOperationException();
            }

            public Date convertToDate(final Long dateLong)
            {
                throw new UnsupportedOperationException();
            }

            public String getIndexedValue(final Date date)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public DateRange convertToDateRangeWithImpliedPrecision(String dateString)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public DateRange convertToDateRange(Long dateLong)
            {
                throw new UnsupportedOperationException();
            }
        };

        return new DateValueValidator(jqlOperandResolver, dateSupport)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };
    }
}
