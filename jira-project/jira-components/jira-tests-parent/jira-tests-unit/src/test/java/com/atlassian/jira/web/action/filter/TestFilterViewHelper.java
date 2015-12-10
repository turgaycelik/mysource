package com.atlassian.jira.web.action.filter;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.MockUser;

import org.easymock.MockControl;
import org.easymock.internal.AlwaysMatcher;
import org.junit.Before;
import org.junit.Test;

/**
 * Some tests for {@link com.atlassian.jira.web.action.filter.TestFilterViewHelper}.
 *
 * @since v3.13
 */
public class TestFilterViewHelper
{
    private MockControl factoryControl;
    private ShareTypeFactory factory;
    private MockControl searchRequestServiceControl;
    private SearchRequestService searchRequestService;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        factoryControl = MockControl.createControl(ShareTypeFactory.class);
        factory = (ShareTypeFactory) factoryControl.getMock();
        
        searchRequestServiceControl = MockControl.createControl(SearchRequestService.class);
        searchRequestService = (SearchRequestService) searchRequestServiceControl.getMock();

        user = new MockUser("TestFilterViewHelper");
    }

    private FilterViewHelper createHelper()
    {
        factoryControl.replay();
        searchRequestServiceControl.replay();

        return new FilterViewHelper(factory, new MockSimpleAuthenticationContext(user), "applicationContext", "actionUrl", searchRequestService);
    }

    private void verifyMocks()
    {
        factoryControl.verify();
        searchRequestServiceControl.verify();
    }

    /**
     * Test to ensure that the SearchRequest service is actually called.
     */
    @Test
    public void testServiceIsCalled()
    {
        searchRequestService.validateForSearch(null, null);
        searchRequestServiceControl.setMatcher(new AlwaysMatcher());

        searchRequestService.search(null, null, -1, -1);
        searchRequestServiceControl.setMatcher(new AlwaysMatcher());
        searchRequestServiceControl.setReturnValue(null);

        FilterViewHelper helper = createHelper();
        helper.search(new MockJiraServiceContext(user));

        verifyMocks();
    }
}
