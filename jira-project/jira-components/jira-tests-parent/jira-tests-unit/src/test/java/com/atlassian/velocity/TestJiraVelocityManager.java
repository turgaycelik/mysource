package com.atlassian.velocity;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryStub;
import com.atlassian.jira.template.velocity.VelocityEngineFactory;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;
import static com.atlassian.velocity.JiraVelocityManager.Key.FORMATTER;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestJiraVelocityManager
{
    private DateTimeFormatter dateTimeFormatter;


    @Test
    public void testGetBaseUrl() throws Exception
    {
        final JiraVelocityManager manager = newJiraVelocityManager();
        final Map<String, ?> contextParameters = manager.createContextParams("someurl", Collections.emptyMap());
        assertNotNull(contextParameters.get("baseurl"));
        assertEquals("someurl", contextParameters.get("baseurl"));
    }

    @Test
    public void testBaseUrlIsOverridenByMapValue() throws Exception
    {
        final JiraVelocityManager manager = newJiraVelocityManager();
        final Map<String, ?> contextParameters = manager.createContextParams("someurl", Collections.singletonMap("baseurl", "differenturl"));
        assertNotNull(contextParameters.get("baseurl"));
        assertEquals("differenturl", contextParameters.get("baseurl"));
    }

    @Test
    public void contextShouldContainFormatter() throws Exception
    {
        JiraVelocityManager manager = newJiraVelocityManager();
        Map<String, ?> contextParameters = manager.createContextParams("someurl", Collections.emptyMap());

        assertThat(contextParameters.keySet(), hasItem(FORMATTER));
        assertFalse(((DateFormat) contextParameters.get(FORMATTER)).format(new Date()).isEmpty());
    }

    @Before
    public void setUp() throws Exception
    {
        dateTimeFormatter = new DateTimeFormatterFactoryStub().formatter().forLoggedInUser().withStyle(COMPLETE);
    }

    protected JiraVelocityManager newJiraVelocityManager()
    {
        return new JiraVelocityManager(dateTimeFormatter, new VelocityEngineFactory.Default());
    }
}
