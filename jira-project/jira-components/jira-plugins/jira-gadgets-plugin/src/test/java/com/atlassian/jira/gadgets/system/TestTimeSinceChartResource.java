package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.gadgets.system.util.ResourceDateValidator;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * Unit test for {@link com.atlassian.jira.gadgets.system.TimeSinceChartResource}.
 *
 * @since v4.0
 */
public class TestTimeSinceChartResource extends TestCase
{
    private ApplicationProperties applicationProperties;

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

    public void testValidate()
    {
        final String expectedQueryString = "some query";
        final String expectedDateField = "the date field";
        final String days = "440";
        final String period = "someperiod";
        Collection<ValidationError> errors = Collections.emptyList();

        final ResourceDateValidator mockValidator = EasyMock.createMock(ResourceDateValidator.class);
        EasyMock.expect(mockValidator.validatePeriod(TimeSinceChartResource.PERIOD_NAME, period, errors)).andReturn(null);
        EasyMock.expect(mockValidator.validateDaysPrevious(TimeSinceChartResource.DAYS, null, days, errors)).andReturn(-800);
        EasyMock.replay(mockValidator);
        final AtomicBoolean getSearchRequestAndValidateWasCalled = new AtomicBoolean(false);
        final AtomicBoolean validateDateFieldWasCalled = new AtomicBoolean(false);

        TimeSinceChartResource tscr = new TimeSinceChartResource(null, null, null, null, null, null, mockValidator, null)
        {
            @Override
            protected SearchRequest getSearchRequestAndValidate(final String queryString, final Collection<ValidationError> errors, final Map<String, Object> params)
            {
                getSearchRequestAndValidateWasCalled.set(true);
                assertEquals(expectedQueryString, queryString);
                assertTrue(errors.isEmpty());
                return null;
            }

            @Override
            Field validateDateField(final String dateField, final Collection<ValidationError> errors)
            {
                assertEquals(expectedDateField, dateField);
                assertTrue(errors.isEmpty());
                validateDateFieldWasCalled.set(true);
                return null;
            }
        };
        tscr.validateChart(expectedQueryString, expectedDateField, days, period);

        EasyMock.verify(mockValidator);
        assertTrue(getSearchRequestAndValidateWasCalled.get());
        assertTrue(validateDateFieldWasCalled.get());
    }

    public void testValidateDateFieldHappy()
    {
        FieldManager mockFieldManager = EasyMock.createMock(FieldManager.class);
        TimeSinceChartResource tscr = new TimeSinceChartResource(null, null, null, null, null, mockFieldManager, null, applicationProperties)
        {
            @Override
            boolean isDateTypeField(final Field field)
            {
                return true;
            }
        };
        final List<ValidationError> errors = Collections.emptyList();
        Field mockField = EasyMock.createMock(Field.class);
        final String fieldName = "someDateField";
        EasyMock.expect(mockFieldManager.getField(fieldName)).andReturn(mockField);
        EasyMock.replay(mockFieldManager, mockField);

        final Field field = tscr.validateDateField(fieldName, errors);
        assertTrue(mockField == field);

        EasyMock.verify(mockFieldManager, mockField);
    }

    public void testValidateDateFieldSad()
    {
        FieldManager mockFieldManager = EasyMock.createMock(FieldManager.class);
        TimeSinceChartResource tscr = new TimeSinceChartResource(null, null, null, null, null, mockFieldManager, null, applicationProperties)
        {
            @Override
            boolean isDateTypeField(final Field field)
            {
                return false;
            }
        };
        final List<ValidationError> errors = new ArrayList<ValidationError>();
        Field mockField = EasyMock.createMock(Field.class);
        final String fieldId = "customfield_123";
        final String fieldName = "theFieldName";
        EasyMock.expect(mockField.getName()).andReturn(fieldName);
        EasyMock.expect(mockFieldManager.getField(fieldId)).andReturn(mockField);
        EasyMock.replay(mockFieldManager, mockField);

        tscr.validateDateField(fieldId, errors);
        assertEquals(1, errors.size());
        assertEquals(TimeSinceChartResource.DAYS, errors.get(0).getField());
        assertEquals("gadget.time.since.not.date.field", errors.get(0).getError());
        assertEquals(Arrays.asList(fieldId, fieldName), errors.get(0).getParams());

        EasyMock.verify(mockFieldManager, mockField);
    }

    public void testValidateDateFieldDoesntExist()
    {
        FieldManager mockFieldManager = EasyMock.createMock(FieldManager.class);
        TimeSinceChartResource tscr = new TimeSinceChartResource(null, null, null, null, null, mockFieldManager, null, applicationProperties);
        final List<ValidationError> errors = new ArrayList<ValidationError>();
        Field mockField = EasyMock.createMock(Field.class);
        final String fieldId = "customfield_123";
        EasyMock.expect(mockFieldManager.getField(fieldId)).andReturn(null);
        EasyMock.replay(mockFieldManager, mockField);

        assertNull(tscr.validateDateField(fieldId, errors));
        assertEquals(1, errors.size());
        assertEquals(TimeSinceChartResource.DAYS, errors.get(0).getField());
        assertEquals("gadget.time.since.invalid.date.field", errors.get(0).getError());

        EasyMock.verify(mockFieldManager, mockField);
    }

    public void testGenerateChartInvalid()
    {
        JiraAuthenticationContext mockJiraAuthenticationContext = EasyMock.createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney", "Barney Google", "testing@other.peoples.code.com");
        EasyMock.expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney);

        EasyMock.replay(mockJiraAuthenticationContext);
        final ValidationError daysError = new ValidationError("daysField", "daysValidationError");
        final ValidationError periodError = new ValidationError("periodField", "periodValidationError");
        final ResourceDateValidator failingDateValidator = new ResourceDateValidator(applicationProperties)
        {
            @Override
            public int validateDaysPrevious(final String fieldName, final ChartFactory.PeriodName period, final String days, final Collection<ValidationError> errors)
            {
                errors.add(daysError);
                return -1;
            }

            @Override
            public ChartFactory.PeriodName validatePeriod(final String fieldName, final String periodName, final Collection<ValidationError> errors)
            {
                errors.add(periodError);
                return null;
            }
        };
        TimeSinceChartResource tscr = new TimeSinceChartResource(null, mockJiraAuthenticationContext, null, null, null, null, failingDateValidator, null)
        {
            @Override
            Field validateDateField(final String fieldId, final Collection<ValidationError> errors)
            {
                return null;
            }

            @Override
            protected SearchRequest getSearchRequestAndValidate(final String queryString, final Collection<ValidationError> errors, final Map<String, Object> params)
            {
                return null;
            }
        };
        Response response = tscr.generateChart(null, null, null, null, null, true, 100, 101, true);
        assertEquals(400, response.getStatus());

        final ArrayList<ValidationError> expectedErrors = new ArrayList<ValidationError>();
        expectedErrors.add(periodError);
        expectedErrors.add(daysError);

        final ErrorCollection errors = (ErrorCollection) response.getEntity();
        assertEquals(expectedErrors, errors.getErrors());
        EasyMock.verify(mockJiraAuthenticationContext);
    }
}
