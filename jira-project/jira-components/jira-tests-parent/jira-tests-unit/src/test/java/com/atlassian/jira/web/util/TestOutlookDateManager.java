package com.atlassian.jira.web.util;

import java.util.Calendar;
import java.util.Locale;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestOutlookDateManager
{
    @Mock private EventPublisher eventPublisher;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private DateTimeFormatterFactory dateTimeFormatterFactory;
    @Mock private DateTimeFormatter defaultFormatter;
    @Mock private DateTimeFormatter userFormatter;

    private OutlookDateManagerImpl outlookDateManager;

    public TestOutlookDateManager()
    {
        super();
    }

    @Before
    public void setUp() throws Exception
    {
        when(dateTimeFormatterFactory.formatter()).thenReturn(defaultFormatter);
        when(defaultFormatter.forLoggedInUser()).thenReturn(userFormatter);
    }

    @After
    public void tearDown()
    {
        eventPublisher = null;
        applicationProperties = null;
        dateTimeFormatterFactory = null;
        defaultFormatter = null;
        userFormatter = null;
    }

    @Test
    public void testRefresh()
    {
        outlookDateManager = fixture();

        OutlookDate englishDate = outlookDateManager.getOutlookDate(Locale.ENGLISH);
        outlookDateManager.refresh();

        OutlookDate newEnglishDate = outlookDateManager.getOutlookDate(Locale.ENGLISH);
        assertNotSame(englishDate, newEnglishDate);
    }

    @Test
    public void testGetOutlookDate()
    {
        Calendar calendarInstance = Calendar.getInstance();
        calendarInstance.set(2001, Calendar.AUGUST, 1);

        DateTimeFormatter datePickerFormatter = mock(DateTimeFormatter.class);
        when(userFormatter.withStyle(DateTimeStyle.DATE_PICKER)).thenReturn(datePickerFormatter);
        when(datePickerFormatter.format(calendarInstance.getTime())).thenReturn("February 13, 2005");

        outlookDateManager = fixture();

        final OutlookDate englishDate = outlookDateManager.getOutlookDate(Locale.ENGLISH);
        final String englishFormattedDate = englishDate.formatDatePicker(calendarInstance.getTime());
        assertEquals("February 13, 2005", englishFormattedDate);
    }

    private OutlookDateManagerImpl fixture()
    {
        return new OutlookDateManagerImpl(applicationProperties, new MockI18nBean.MockI18nBeanFactory(), dateTimeFormatterFactory);
    }
}
