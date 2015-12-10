package com.atlassian.jira.issue.fields;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryStub;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.collect.CompositeMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ColumnViewDateTimeHelperTest
{
    @Mock private NavigableFieldImpl field;
    @Mock private FieldLayoutItem fieldLayoutItem;
    @Mock private Issue issue;

    @Mock DateTimeFormatterFactory dateTimeFormatterFactory;
    @Mock JiraAuthenticationContext authenticationContext;

    private Date date;
    private Map<String, Object> displayParams;
    private I18nHelper i18nHelper;
    private Map<String, Object> legacyParams;
    private Map<String, Object> expectedParams;
    private DateTime dateTime;


    @Before
    public void setUp() throws Exception
    {
        dateTime = new DateTime(24666, 8, 7, 23, 58);
        date = dateTime.toDate();
        dateTimeFormatterFactory = new DateTimeFormatterFactoryStub();
        i18nHelper = new NoopI18nHelper();
        displayParams = ImmutableMap.of("key1", new Object());
        legacyParams = ImmutableMap.of("key2", new Object());
        expectedParams = CompositeMap.of(displayParams, legacyParams);

        when(authenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(field.getVelocityParams(eq(fieldLayoutItem), eq(i18nHelper), any(Map.class), eq(issue))).thenAnswer(new GetVelocityParamsAnswer());
    }

    @After
    public void tearDown()
    {
        field = null;
        fieldLayoutItem = null;
        issue = null;
        authenticationContext = null;
        date = null;
        dateTimeFormatterFactory = null;
        displayParams = null;
        i18nHelper = null;
        legacyParams = null;
        expectedParams = null;
        dateTime = null;
    }

    @Test
    public void renderUsesDateColumnViewVelocityTemplateByDefault() throws Exception
    {
        when(field.renderTemplate(eq("date-columnview.vm"), any(Map.class))).thenReturn("default formatted date");

        final String rendered = new Fixture().render(field, fieldLayoutItem, displayParams, issue, date);
        assertThat(rendered, equalTo("default formatted date"));
    }

    @Test
    public void renderUsesDateExcelViewVelocityTemplateForExcelView() throws Exception
    {
        when(field.renderTemplate(eq("date-excelview.vm"), any(Map.class))).thenReturn("excel formatted date");
        final Map<String, Object> excelDisplayParams = CompositeMap.of(Collections.singletonMap("excel_view", (Object) Boolean.TRUE), displayParams);

        final String rendered = new Fixture().render(field, fieldLayoutItem, excelDisplayParams, issue, date);
        assertThat(rendered, equalTo("excel formatted date"));
    }

    @Test
    public void velocityParamsShouldContainLegacyParams() throws Exception
    {
        new Fixture().render(field, fieldLayoutItem, displayParams, issue, date);

        verify(field).renderTemplate(anyString(), argThat(hasEntry("field-specific-key", "field-specific-value")));
    }

    @Test
    public void velocityParamsShouldContainTitle() throws Exception
    {
        new Fixture().render(field, fieldLayoutItem, displayParams, issue, date);

        verify(field).renderTemplate(anyString(), argThat(hasEntry("title", "07/Aug/66 11:58 PM")));
    }

    @Test
    public void velocityParamsShouldContainIso8601() throws Exception
    {
        final String timeZone = DateTimeFormat.forPattern("Z").print(dateTime);

        new Fixture().render(field, fieldLayoutItem, displayParams, issue, date);

        verify(field).renderTemplate(anyString(), argThat(hasEntry("iso8601", "24666-08-07T23:58:00" + timeZone)));
    }

    @Test
    public void velocityParamsShouldContainValue() throws Exception
    {
        new Fixture().render(field, fieldLayoutItem, displayParams, issue, date);

        verify(field).renderTemplate(anyString(), argThat(hasEntry("value", "07/Aug/66")));
    }

    /**
     * Adds the entry ("field-specific-key", "field-specific-value") to the given map.
     */
    static class GetVelocityParamsAnswer implements Answer<Map<String, Object>>
    {
        @Override
        public Map<String,Object> answer(final InvocationOnMock invocation) throws Throwable
        {
            // add a field-specific key and value
            Map<String, Object> displayParams = Maps.newHashMap((Map<String, Object>)invocation.getArguments()[2]);
            displayParams.put("field-specific-key", "field-specific-value");
            return displayParams;
        }
    }

    class Fixture extends ColumnViewDateTimeHelper
    {
        Fixture()
        {
            super(dateTimeFormatterFactory, authenticationContext);
        }
    }

}
