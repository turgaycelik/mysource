package com.atlassian.jira.bc.filter;

import java.util.Collection;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.FilterCronValidationErrorMappingUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.component.cron.parser.CronExpressionParser;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestFilterSubscriptionCronValidation
{
    private static final Matcher<Collection<String>> IS_EMPTY = Matchers.empty();

    private FilterSubscriptionService service;
    private JiraServiceContext context;


    @Before
    public void setUp() throws Exception
    {
        service = createService();
        context = createContext();
    }


    @After
    public void tearDown() throws Exception
    {
        service = null;
        context = null;
    }

    @Test
    public void testFilterCronNullExpr()
    {
        service.validateCronExpression(context, null);
        assertHasErrors("filter.subsription.cron.errormessage.mode.error");
    }

    @Test
    public void testFilterCronEmptyStringExpr()
    {
        service.validateCronExpression(context, "");
        assertHasErrors("filter.subsription.cron.errormessage.unexpected.end.of.expr");
    }

    @Test
    public void testFilterCronUnexpectedEnd()
    {
        service.validateCronExpression(context, "0 0 0 ? 1");
        assertHasErrors("filter.subsription.cron.errormessage.unexpected.end.of.expr");
    }

    @Test
    public void testFilterCronIllegalFormat()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 NO");
        assertHasErrors("filter.subsription.cron.errormessage.illegal.format:java.lang.StringIndexOutOfBoundsException: String index out of range: 3");
    }

    @Test
    public void testFilterCronInvalidMonthName()
    {
        service.validateCronExpression(context, "0 0 0 ? FEB MON");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? FEB-NOV MON");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? NOT MON");
        assertHasErrors("filter.subsription.cron.errormessage.invalid.month:NOT");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? FEB-NOT MON");
        assertHasErrors("filter.subsription.cron.errormessage.invalid.month:NOT");
    }

    @Test
    public void testFilterCronInvalidDayName()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 MON");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON-TUE");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 NOT");
        assertHasErrors("filter.subsription.cron.errormessage.invalid.day.of.week:NOT");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON-NOT");
        assertHasErrors("filter.subsription.cron.errormessage.invalid.day.of.week:NOT");
    }

    @Test
    public void testFilterCronNumericAfterHash()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 MON#3");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 1#3");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON#8");
        assertHasErrors("filter.subsription.cron.errormessage.numeric.value.between.after.hash");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON#three");
        assertHasErrors("filter.subsription.cron.errormessage.numeric.value.between.after.hash");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 1#8");
        assertHasErrors("filter.subsription.cron.errormessage.numeric.value.between.after.hash");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 1#three");
        assertHasErrors("filter.subsription.cron.errormessage.numeric.value.between.after.hash");

    }

    @Test
    public void testFilterCronInvalidChars()
    {
        service.validateCronExpression(context, "0 XXX 0 ? 1 MON-TUE");
        assertHasErrors("filter.subsription.cron.errormessage.illegal.characters.for.position:XXX");
    }

    @Test
    public void testFilterCronIllegalCharAfterQuestionMark()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 MON-TUE");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ?\t1 MON-TUE");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ?X 1 MON-TUE");

        // When QUARTZ-575 (http://jira.opensymphony.com/browse/QUARTZ-575) is fixed, replace the assertFalse below
        // with the commented-out test below it.
        assertNoErrors();
        //assertHasErrors("filter.subsription.cron.errormessage.illegal.character.after.question.mark:X");


        context = createContext();
        service.validateCronExpression(context, "0 0 0 ?XX 1 MON-TUE");
        // When QUARTZ-575 is fixed, add an extra 'X' to the message
        assertHasErrors("filter.subsription.cron.errormessage.illegal.character.after.question.mark:X");
    }

    @Test
    public void testFilterCronInvalidQuestionMark()
    {
        service.validateCronExpression(context, "0 ? 0 ? 1 1");
        assertHasErrors("filter.subsription.cron.errormessage.question.mark.invalid.position");
    }

    @Test
    public void testFilterCronInvalidQuestionMarkForBoth()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 ?");
        assertHasErrors("filter.subsription.cron.errormessage.question.mark.invalid.for.both");
    }

    @Test
    public void testFilterCronInvalidIncrements()
    {
        service.validateCronExpression(context, "0 /5 0 ? 1 MON");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 5/5 0 ? 1 MON");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 */5 0 ? 1 MON");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 / 0 ? 1 MON");
        assertHasErrors("filter.subsription.cron.errormessage.slash.must.be.followed.by.integer");

        context = createContext();
        service.validateCronExpression(context, "/65 0 0 ? 1 MON");
        assertHasErrors("filter.subsription.cron.errormessage.increment.greater.than.sixty:65");

        context = createContext();
        service.validateCronExpression(context, "0 /65 0 ? 1 MON");
        assertHasErrors("filter.subsription.cron.errormessage.increment.greater.than.sixty:65");

        context = createContext();
        service.validateCronExpression(context, "0 0 /26 ? 1 MON");
        assertHasErrors("filter.subsription.cron.errormessage.increment.greater.than.twentyfour:26");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? /15 MON");
        assertHasErrors("filter.subsription.cron.errormessage.increment.greater.than.twelve:15");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 /9");
        assertHasErrors("filter.subsription.cron.errormessage.increment.greater.than.seven:9");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 /36 1 ?");
        assertHasErrors("filter.subsription.cron.errormessage.increment.greater.than.thirtyone:36");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 0/6a 1 ?");
        assertHasErrors("filter.subsription.cron.errormessage.unexpected.character.after.slash:A");
    }

    @Test
    public void testFilterCronInvalidCharacter()
    {
        service.validateCronExpression(context, "0 @ 0 1 1 ?");
        assertHasErrors("filter.subsription.cron.errormessage.unexpected.character:@");
    }

    @Test
    public void testFilterCronInvalidLOption()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 L");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 L 1 ?");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "L 0 0 1 1 ?");

        // When QUARTZ-576 is fixed, change the assertHasErrors below to use the message from this assertion,
        // then delete this assertion.
        assertThat("Happy Fail!!  This will fail when QUARTZ-576 is fixed.",
                context.getErrorCollection().getErrorMessages(), not(contains("filter.subsription.cron.errormessage.l.not.valid")));
        assertHasErrors("filter.subsription.cron.errormessage.general.error:L 0 0 1 1 ?");

        context = createContext();
        service.validateCronExpression(context, "6L 0 0 1 1 ?");
        assertHasErrors("filter.subsription.cron.errormessage.l.not.valid:1");
    }

    @Test
    public void testFilterCronInvalidWOption()
    {
        service.validateCronExpression(context, "0 0 0 LW 1 ?");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 15W 1 ?");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "6W 0 0 1 1 ?");
        assertHasErrors("filter.subsription.cron.errormessage.w.not.valid:1");
    }

    @Test
    public void testFilterCronInvalidHashOption()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 1#3");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 MON#3");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "6#4 0 0 1 1 ?");
        assertHasErrors("filter.subsription.cron.errormessage.hash.not.valid:1");
    }


    @Test
    public void testFilterCronInvalidCOption()
    {
        service.validateCronExpression(context, "0 0 0 ? 1 5C");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "0 0 0 5C 1 ?");
        assertNoErrors();

        context = createContext();
        service.validateCronExpression(context, "6C 0 0 1 1 ?");

        // This passes quartz' checks although it really shouldn't.  If it starts failing, try
        // the commented-out assertHasErrors in its place.
        assertThat("Happy fail!", context.getErrorCollection().hasAnyErrors(), is(false));
        //assertHasErrors("filter.subsription.cron.errormessage.c.not.valid:1");
    }

    @Test
    public void testFilterCronInvalidValues()
    {
        service.validateCronExpression(context, "65 0 0 ? 1 MON");
        assertHasErrors("filter.subsription.cron.errormessage.minute.and.seconds.between.zero.fiftynine");

        context = createContext();
        service.validateCronExpression(context, "0 65 0 ? 1 MON");
        assertHasErrors("filter.subsription.cron.errormessage.minute.and.seconds.between.zero.fiftynine");

        context = createContext();
        service.validateCronExpression(context, "0 0 26 ? 1 MON");
        assertHasErrors("filter.subsription.cron.errormessage.hour.between.zero.twentythree");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 33 1 ?");
        assertHasErrors("filter.subsription.cron.errormessage.day.of.month.between.one.thirtyone");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 15 MON");
        assertHasErrors("filter.subsription.cron.errormessage.month.between.one.twelve");

        context = createContext();
        service.validateCronExpression(context, "0 0 0 ? 1 9");
        assertHasErrors("filter.subsription.cron.errormessage.day.of.week.between.one.seven");
    }

    /**
     * Check validity of default string used to seed the cron editor.
     */
    @Test
    public void testFilterWithDefaultValue()
    {
        service.validateCronExpression(context, CronExpressionParser.DEFAULT_CRONSTRING);
        assertNoErrors();
    }

    private DefaultFilterSubscriptionService createService()
    {
        FilterCronValidationErrorMappingUtil mapper = new FilterCronValidationErrorMappingUtil(null)
        {

            protected String getText(String key)
            {
                return key;
            }

            protected String getText(String key, Object param)
            {
                return key + ":" + param;
            }

        };

        return new DefaultFilterSubscriptionService(mapper, null, null)
        {

            @Override
            protected String getText(String key)
            {
                return key;
            }

            protected String getText(String key, Object param)
            {
                return key + ":" + param;
            }
        };
    }

    private JiraServiceContextImpl createContext()
    {
        return new JiraServiceContextImpl((ApplicationUser)null, new SimpleErrorCollection());
    }

    private void assertNoErrors()
    {
        assertThat(context.getErrorCollection().getErrorMessages(), IS_EMPTY);
        assertFalse(context.getErrorCollection().hasAnyErrors());
    }

    private void assertHasErrors(String... expectedErrors)
    {
        assertThat(context.getErrorCollection().getErrorMessages(), contains(expectedErrors));
        assertTrue(context.getErrorCollection().hasAnyErrors());
    }
}
