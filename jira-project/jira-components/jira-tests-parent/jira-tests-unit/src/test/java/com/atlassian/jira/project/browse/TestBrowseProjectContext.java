package com.atlassian.jira.project.browse;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.ProjectHelper;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.Query;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;

@RunWith (MockitoJUnitRunner.class)
public class TestBrowseProjectContext
{
    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Test
    public void testCreateSearchQuery() throws Exception
    {
        BrowseProjectContext ctx = new BrowseProjectContext(null, new MockProject(100L, "STVO"));
        Query initialQuery = ctx.createQuery();
        assertEquals(JqlQueryBuilder.newBuilder().where().project("STVO").buildQuery(), initialQuery);
    }

    @Test
    public void testCreateParameters() throws Exception
    {
        final MockHttpServletRequest currentHttpRequest = new MockHttpServletRequest();
        final Project proj100 = new MockProject(100L, "JQL");
        final User admin = new MockUser("admin");

        final BrowseProjectContext ctx = new BrowseProjectContext(admin, proj100)
        {
            @Override
            protected HttpServletRequest getExecutingHttpRequest()
            {
                return currentHttpRequest;
            }
        };

        final JiraHelper helper = new ProjectHelper(currentHttpRequest, ctx);

        final Map<String,Object> params = ctx.createParameterMap();

        assertEquals(4, params.size());
        assertEquals(admin, params.get("user"));
        assertEquals(proj100, params.get("project"));
        assertEquals(helper, params.get("helper"));
        assertEquals("project", params.get("contextType"));
    }
}
