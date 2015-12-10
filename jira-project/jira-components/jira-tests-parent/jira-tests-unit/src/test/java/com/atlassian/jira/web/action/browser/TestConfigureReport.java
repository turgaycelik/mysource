package com.atlassian.jira.web.action.browser;

import java.util.LinkedHashMap;
import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.plugin.PluginAccessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import webwork.action.ActionContext;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class TestConfigureReport
{
    @Mock private EventPublisher mockEventPublisher;
    @Mock private PermissionManager mockPermissionManager;
    @Mock private PluginAccessor mockPluginAccessor;
    @Mock private ProjectManager mockProjectManager;

    private ConfigureReport configureReport;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init().getMockApplicationProperties().setEncoding("UTF-8");
        configureReport =
                new ConfigureReport(mockProjectManager, mockPermissionManager, mockPluginAccessor, mockEventPublisher);
    }

    @After
    public void tearDown()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    // TODO Someone who knows the difference can rename these two tests...
    @Test
    public void testGetQueryString1()
    {
        final Map<String, String[]> parameters = new LinkedHashMap<String, String[]>();
        parameters.put("a", toArray("b"));
        parameters.put("encode", toArray("yes sir"));
        parameters.put("funny", toArray("ch(ara=cter&s"));
        parameters.put("done", toArray("ok"));

        assertQueryString(parameters, "a=b&encode=yes+sir&funny=ch%28ara%3Dcter%26s&done=ok");
    }

    @Test
    public void testGetQueryString2()
    {
        final Map<String, String[]> parameters = singletonMap("one", toArray("value"));
        assertQueryString(parameters, "one=value");
    }

    private void assertQueryString(final Map<String, String[]> parameters, final String expectedQueryString)
    {
        // Set up
        ActionContext.setParameters(parameters);

        // Invoke and check
        assertEquals(expectedQueryString, configureReport.getQueryString());
    }

    private String[] toArray(final String val)
    {
        return new String[] { val };
    }
}
