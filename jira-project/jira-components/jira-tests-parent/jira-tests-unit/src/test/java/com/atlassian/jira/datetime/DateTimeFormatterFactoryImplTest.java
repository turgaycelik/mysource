package com.atlassian.jira.datetime;

import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneResolver;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.event.HasEventListenerFor.hasEventListenerFor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

/**
 * Tests for DateTimeFormatterFactoryImpl.
 *
 * @since 4.4
 */
public class DateTimeFormatterFactoryImplTest
{
    @Mock
    private TimeZoneResolver timeZoneService;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private I18nHelper.BeanFactory beanFactory;

    @Test
    public void factoryShouldSupportAllStyles() throws Exception
    {
        DateTimeFormatterFactoryImpl factory = newDateTimeFormatterImpl();

        // make sure that all possible styles are handled
        assertThat(factory.formatters.keySet(), hasItems(DateTimeStyle.values()));
        for (Map.Entry<DateTimeStyle, DateTimeFormatStrategy> entry : factory.formatters.entrySet())
        {
            assertThat(entry.getValue().style(), equalTo(entry.getKey()));
        }
    }

    @Test
    public void factoryShouldHandleOnClearCacheEvent() throws Exception
    {
        assertThat(DateTimeFormatterFactoryImpl.class, hasEventListenerFor(ClearCacheEvent.class));
    }

    @Test
    public void formatterDefaultsToRelative() throws Exception
    {
        final DateTimeFormatter formatter = newDateTimeFormatterImpl().formatter();
        assertThat(formatter.getZone(), nullValue());
        assertThat(formatter.getLocale(), nullValue());
        assertThat(formatter.getStyle(), equalTo(DateTimeStyle.RELATIVE));
    }

    @Before
    public void setUp() throws Exception
    {
        EasyMockAnnotations.initMocks(this);
    }

    protected DateTimeFormatterFactoryImpl newDateTimeFormatterImpl()
    {
        return new DateTimeFormatterFactoryImpl(timeZoneService, jiraAuthenticationContext, applicationProperties, beanFactory);
    }
}
