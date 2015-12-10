package com.atlassian.jira.gadgets.system.util;

import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link ResourceDateValidator}.
 *
 * @since v4.0
 */
public class TestResourceDateValidator extends TestCase
{
    private ApplicationProperties applicationProperties;
    private static final String DAYS_FIELD = "daysField";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        applicationProperties = createMock(ApplicationProperties.class);
        replay(applicationProperties);
    }

    @Override
    protected void tearDown() throws Exception
    {
        verify(applicationProperties);
        super.tearDown();
    }

    public void testValidateDaysPreviousNoApplicationProperties()
    {
        reset(applicationProperties);

        expect(applicationProperties.getDefaultBackedString("jira.chart.days.previous.limit.hourly"))
                .andReturn(null);

        replay(applicationProperties);

        assertNoValidateDaysAgainstPeriodErrors("10", ChartFactory.PeriodName.hourly);
    }

    public void testValidateDaysPreviousAgainstPeriod() throws Exception
    {
        reset(applicationProperties);

        expect(applicationProperties.getDefaultBackedString("jira.chart.days.previous.limit.hourly"))
                .andReturn("10").times(2);
        expect(applicationProperties.getDefaultBackedString("jira.chart.days.previous.limit.daily"))
                .andReturn("10").times(2);
        expect(applicationProperties.getDefaultBackedString("jira.chart.days.previous.limit.weekly"))
                .andReturn("10").times(2);
        expect(applicationProperties.getDefaultBackedString("jira.chart.days.previous.limit.monthly"))
                .andReturn("10").times(2);
        expect(applicationProperties.getDefaultBackedString("jira.chart.days.previous.limit.quarterly"))
                .andReturn("10").times(2);
        expect(applicationProperties.getDefaultBackedString("jira.chart.days.previous.limit.yearly"))
                .andReturn("10").times(2);

        replay(applicationProperties);

        assertNoValidateDaysAgainstPeriodErrors("10", ChartFactory.PeriodName.hourly);
        assertValidateDaysAgainstPeriodErrors("11", ChartFactory.PeriodName.hourly);

        assertNoValidateDaysAgainstPeriodErrors("10", ChartFactory.PeriodName.daily);
        assertValidateDaysAgainstPeriodErrors("11", ChartFactory.PeriodName.daily);

        assertNoValidateDaysAgainstPeriodErrors("10", ChartFactory.PeriodName.weekly);
        assertValidateDaysAgainstPeriodErrors("11", ChartFactory.PeriodName.weekly);

        assertNoValidateDaysAgainstPeriodErrors("10", ChartFactory.PeriodName.monthly);
        assertValidateDaysAgainstPeriodErrors("11", ChartFactory.PeriodName.monthly);

        assertNoValidateDaysAgainstPeriodErrors("10", ChartFactory.PeriodName.quarterly);
        assertValidateDaysAgainstPeriodErrors("11", ChartFactory.PeriodName.quarterly);

        assertNoValidateDaysAgainstPeriodErrors("10", ChartFactory.PeriodName.yearly);
        assertValidateDaysAgainstPeriodErrors("11", ChartFactory.PeriodName.yearly);
    }
    
    public void testValidateDaysPreviousHappy()
    {
        assertNoValidateDaysPreviousErrors("3033");
        assertNoValidateDaysPreviousErrors("1");
        assertNoValidateDaysPreviousErrors("0");
        assertNoValidateDaysPreviousErrors("30");
    }

    public void testValidateDaysPreviousSad()
    {
        assertValidateDaysPreviousError("999999999999999999999999999999999999999999999999999999999999999999999999");
        assertValidateDaysPreviousError("a");
        assertValidateDaysPreviousError("-44");
        assertValidateDaysPreviousError("3.3");
    }

    public void testValidatePeriodHappy()
    {
        assertValidatePeriod(com.atlassian.jira.charts.ChartFactory.PeriodName.hourly);
        assertValidatePeriod(com.atlassian.jira.charts.ChartFactory.PeriodName.daily);
        assertValidatePeriod(com.atlassian.jira.charts.ChartFactory.PeriodName.weekly);
        assertValidatePeriod(com.atlassian.jira.charts.ChartFactory.PeriodName.monthly);
        assertValidatePeriod(com.atlassian.jira.charts.ChartFactory.PeriodName.quarterly);
        assertValidatePeriod(com.atlassian.jira.charts.ChartFactory.PeriodName.yearly);
    }

    public void testValidatePeriodSad()
    {
        assertValidatePeriodError("foobar");
        assertValidatePeriodError("hour");
        assertValidatePeriodError("day");
        assertValidatePeriodError("Daily");
    }

    private void assertValidatePeriodError(String periodName)
    {
        ResourceDateValidator rdv = new ResourceDateValidator(applicationProperties);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        ChartFactory.PeriodName period = rdv.validatePeriod("periodField", periodName, errors);
        assertEquals(null, period);
        assertEquals(1, errors.size());
        assertEquals("periodField", errors.get(0).getField());
    }

    public void assertValidatePeriod(ChartFactory.PeriodName period)
    {
        ResourceDateValidator rdv = new ResourceDateValidator(applicationProperties);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        ChartFactory.PeriodName p = rdv.validatePeriod("aPeriodField", period.name(), errors);
        assertEquals(p, period);
        assertTrue(errors.isEmpty());
    }

    private void assertNoValidateDaysAgainstPeriodErrors(String days, ChartFactory.PeriodName period)
    {
        ResourceDateValidator rdv = new ResourceDateValidator(applicationProperties);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        rdv.validateDaysPrevious(DAYS_FIELD, period, days, errors);
        assertTrue(errors.isEmpty());
    }

    private void assertValidateDaysAgainstPeriodErrors(String days, ChartFactory.PeriodName period)
    {
        ResourceDateValidator rdv = new ResourceDateValidator(applicationProperties);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        rdv.validateDaysPrevious(DAYS_FIELD, period, days, errors);
        assertEquals(1, errors.size());
        assertEquals(DAYS_FIELD, errors.get(0).getField());
    }

    private void assertNoValidateDaysPreviousErrors(String days)
    {
        reset(applicationProperties);
        expect(applicationProperties.getDefaultBackedString("jira.chart.days.previous.limit.yearly"))
                .andReturn("9999999");
        replay(applicationProperties);

        ResourceDateValidator rdv = new ResourceDateValidator(applicationProperties);
        List<ValidationError> errors = new ArrayList<ValidationError>();
        rdv.validateDaysPrevious(DAYS_FIELD, ChartFactory.PeriodName.yearly, days, errors);
        assertTrue(errors.isEmpty());

        verify(applicationProperties);
    }

    private void assertValidateDaysPreviousError(String days)
    {
        ResourceDateValidator rdv = new ResourceDateValidator(applicationProperties);
        final List<ValidationError> errors;
        errors = new ArrayList<ValidationError>();
        assertTrue(rdv.validateDaysPrevious(DAYS_FIELD, ChartFactory.PeriodName.daily, days, errors) <= -1);
        assertEquals(1, errors.size());
        assertEquals(DAYS_FIELD, errors.get(0).getField());
    }
}
