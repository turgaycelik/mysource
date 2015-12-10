package com.atlassian.jira.charts.util;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class TestChartUtils
{
    private static final ApplicationUser ANONYMOUS = null;

    @Mock JiraAuthenticationContext jiraAuthenticationContext;
    @Mock SearchRequest searchRequest;
    @Mock SearchRequestService searchRequestService;
    @Mock SearchService searchService;
    @Mock ProjectManager projectManager;
    @Mock JiraHome jiraHome;

    @After
    public void tearDown()
    {
        jiraAuthenticationContext = null;
        searchRequest = null;
        searchRequestService = null;
        projectManager = null;
        jiraHome = null;
    }

    @Test
    public void testRetrieveOrMakeFilter()
    {
        when(searchRequestService.getFilter(new JiraServiceContextImpl(ANONYMOUS, new SimpleErrorCollection()), 10000L))
                .thenReturn(searchRequest);

        final ChartUtils chartUtils = new Fixture();

        final Map<String, Object> params = new HashMap<String, Object>(4);
        final SearchRequest request = chartUtils.retrieveOrMakeSearchRequest("filter-10000", params);
        assertThat(request, sameInstance(searchRequest));
        assertThat(params, hasEntry("searchRequest", searchRequest));
        assertThat(params, not(hasKey("project")));

        //cant test the project case since that crates a ProjectClause, which initialises the world via the
        //ComponentManager.getInstance.
    }

    /**
     * Test for {@link ChartUtils#renderBase64Chart(BufferedImage, String)}.
     * It checks the image is correctly transformed to data uri base 64 string.
     */
    @Test
    public void testRenderBase64Image()
    {
        BufferedImage bImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = (Graphics2D) bImage.getGraphics();

        graphics2D.setBackground(Color.WHITE);
        graphics2D.clearRect(0, 0, 100, 100);

        graphics2D.setColor(Color.BLACK);
        graphics2D.drawString("Hello", 20, 20);

        final ChartUtils chartUtils = new Fixture();
        assertThat(chartUtils.renderBase64Chart(bImage, "Some Chart"), startsWith("data:image/png;base64,"));
    }

    class Fixture extends ChartUtilsImpl
    {
        Fixture()
        {
            super(searchRequestService, jiraAuthenticationContext, projectManager, searchService, jiraHome);
        }
    }

    // To fix generics bounding
    static Matcher<Map<? extends String, ?>> hasEntry(String key, Object value)
    {
        return Matchers.hasEntry(key, value);
    }
}
