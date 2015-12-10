package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestHasLastSearchRequestCondition extends MockControllerTestCase
{

    private VelocityRequestContextFactory requestContextFactory;
    private SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private SessionSearchRequestManager sessionSearchRequestManager;
    private VelocityRequestContext requestContext;

    private VelocityRequestSession requestSession;
    private HasLastSearchRequestCondition condition;

    @Before
    public void setUp() throws Exception
    {

        requestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        requestContext = mockController.getMock(VelocityRequestContext.class);
        requestSession = mockController.getMock(VelocityRequestSession.class);
        sessionSearchObjectManagerFactory = mockController.getMock(SessionSearchObjectManagerFactory.class);
        sessionSearchRequestManager = mockController.getMock(SessionSearchRequestManager.class);

        condition = new HasLastSearchRequestCondition(requestContextFactory, sessionSearchObjectManagerFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        requestContextFactory = null;
        requestContext = null;
        requestSession = null;

        condition = null;

    }

    @Test
    public void testNullcontext()
    {
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(null);

        mockController.replay();

        assertFalse(condition.shouldDisplay(null, null));

        mockController.verify();
    }

    @Test
    public void testNullSession()
    {
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(null);

        mockController.replay();

        assertFalse(condition.shouldDisplay(null, null));

        mockController.verify();
    }

    @Test
    public void testNoFilter()
    {
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        expect(sessionSearchObjectManagerFactory.createSearchRequestManager(requestSession))
                .andReturn(sessionSearchRequestManager);
        
        expect(sessionSearchRequestManager.getCurrentObject())
                .andReturn(null);

        mockController.replay();

        assertFalse(condition.shouldDisplay(null, null));

        mockController.verify();
    }

    @Test
    public void testHasFilter()
    {
        SearchRequest sr = new SearchRequest();
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        expect(sessionSearchObjectManagerFactory.createSearchRequestManager(requestSession))
                .andReturn(sessionSearchRequestManager);

        expect(sessionSearchRequestManager.getCurrentObject())
                .andReturn(sr);

        mockController.replay();

        assertTrue(condition.shouldDisplay(null, null));

        mockController.verify();
    }


}
