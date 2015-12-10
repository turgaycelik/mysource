package com.atlassian.jira.jelly.tag;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import electric.xml.Document;
import electric.xml.Element;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collections;

public class TestRunSearchRequest extends AbstractJellyTestCase
{
    private User user;
    private Group group;
    private GenericValue project;
    private SearchRequest searchRequest;

    public TestRunSearchRequest(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Create user and place in the action context
        user = createMockUser("logged-in-user");
        group = createMockGroup("admin-group");
        addUserToGroup(user, group);
        JiraTestUtil.loginUser(user);

        // Create the administer permission for that group
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, "admin-group");

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10240), "key", "ABC", "name", "A Project", "lead", user.getName(),
            "counter", new Long(2)));

        UtilsForTests.getTestEntity(
            "SearchRequest",
            EasyMap.build(
                "id", new Long(10241),
                "name", "test",
                "author","logged-in-user",
                "user", "logged-in-user",
                "group", "admin-group",
                "project", project.get("id"),
                "request", "project = " + project.get("id")));

        final JiraServiceContext ctx = new JiraServiceContextImpl(user);
        searchRequest = ComponentManager.getInstance().getSearchRequestService().getFilter(ctx, 10241L);
    }

    public void testRunSearchRequestWithFilterId() throws Exception
    {

        final SearchProvider oldSearchProvider = ComponentManager.getComponentInstanceOfType(SearchProvider.class);
        final Mock mockSearchProvider = new Mock(SearchProvider.class);
        mockSearchProvider.setStrict(true);
        ManagerFactory.addService(SearchProvider.class, (SearchProvider) mockSearchProvider.proxy());

        mockSearchProvider.expectAndReturn("search", P.args(new IsEqual(searchRequest.getQuery()), new IsEqual(user), new IsAnything()), new SearchResults(Collections.<Issue>emptyList(), PagerFilter.getUnlimitedFilter()));

        final String scriptFilename = "runsearchrequest.test.runsearchrequest-with-filterid.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        mockSearchProvider.verify();

        ManagerFactory.addService(SearchProvider.class, oldSearchProvider);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        user = null;
        group = null;
        project = null;
        searchRequest = null;
    }

    @Override
    protected String getRelativePath()
    {
        return "tag" + FS;
    }
}
