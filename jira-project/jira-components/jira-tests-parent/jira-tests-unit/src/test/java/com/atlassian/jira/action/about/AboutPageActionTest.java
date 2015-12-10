package com.atlassian.jira.action.about;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.HttpServletVariables;
import com.atlassian.jira.web.action.util.AboutPage;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */

public class AboutPageActionTest
{
    @Mock
    private BuildUtilsInfo buildUtilsInfo;

    @Mock(answer = Answers.RETURNS_MOCKS)
    @AvailableInContainer
    private HttpServletVariables httpServletVariables;

    @Mock
    private PluginAccessor pluginAccessor;

    @Mock
    private EncodingConfiguration encodingConfiguration;

    @Mock
    private Plugin plugin;

    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);


    @Test
    public void shouldFindBuildYear()
    {
        when(encodingConfiguration.getEncoding()).thenReturn("utf-8");
        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(makeDateWithYear(2015));

        final Map<String, Object> data = new AboutPage(buildUtilsInfo, pluginAccessor).getData();
        assertThat((String) data.get(AboutPage.KEY_COPYRIGHT_UNTIL), Matchers.is(Integer.toString(2015)));
    }

    @Test
    public void shouldProvideJiraVersion()
    {
        when(encodingConfiguration.getEncoding()).thenReturn("utf-8");
        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(new Date());
        when(buildUtilsInfo.getVersion()).thenReturn("4.3.2.1");

        final Map<String, Object> data = new AboutPage(buildUtilsInfo, pluginAccessor).getData();
        assertThat((String) data.get(AboutPage.KEY_BUILD_VERSION), Matchers.is("4.3.2.1"));
    }

    @Test
    public void shouldProvideJiraContext()
    {
        when(encodingConfiguration.getEncoding()).thenReturn("utf-8");
        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(new Date());
        final String requestContest = "myjira";

        final HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletVariables.getHttpRequest()).thenReturn(httpRequest);
        when(httpRequest.getContextPath()).thenReturn(requestContest);

        final Map<String, Object> data = new AboutPage(buildUtilsInfo, pluginAccessor).getData();
        assertThat((String) data.get(AboutPage.KEY_REQUEST_CONTEXT), Matchers.is(requestContest));
    }

    private Date makeDateWithYear(final int year)
    {
        return new GregorianCalendar(year, 10, 10).getTime();
    }


}
