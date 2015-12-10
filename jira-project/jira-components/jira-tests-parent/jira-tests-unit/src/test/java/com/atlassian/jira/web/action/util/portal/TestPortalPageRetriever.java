package com.atlassian.jira.web.action.util.portal;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.I18nBean;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link PortalPageRetriever}.
 *
 * @since v3.13
 */
public class TestPortalPageRetriever extends MockControllerTestCase
{
    private PortalPageService portalPageService;
    private UserHistoryManager userHistoryManager;
    private JiraAuthenticationContext authenticationContext;
    private PortalPage page1;
    private PortalPage page2;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        portalPageService = (PortalPageService) mockController.getMock(PortalPageService.class);
        page1 = PortalPage.id(10L).name("page1").description("description of page 1").owner(new MockApplicationUser("mine")).build();
        page2 = PortalPage.id(10L).name("page2").description("description of page 2").owner(new MockApplicationUser("yours")).build();
        userHistoryManager = mockController.getMock(UserHistoryManager.class);
        authenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        user = new MockUser("admin");
    }

    /**
     * Make sure the {@link PortalPageRetriever#getPageId()} works when a page id is in the session.
     */
    @Test
    public void testGetPageIdWithPageIdInSession()
    {
        final Long expectedSessionId = new Long(10);
        authenticationContext.getLoggedInUser();
        mockController.setReturnValue(user);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "10")));

        final PortalPageRetriever retriever = createRetriever(userHistoryManager, authenticationContext);

        //should get this from the session.
        assertEquals(expectedSessionId, retriever.getPageId());
        retriever.setRequestedPageId(new Long(11));

        //should ignore the session.
        assertEquals(new Long(11), retriever.getPageId());

        retriever.setRequestedPageId(null);
        //should get this from the session.
        assertEquals(expectedSessionId, retriever.getPageId());

        mockController.verify();
    }

    /**
     * Make sure the {@link PortalPageRetriever#getPageId()} works when no page id is in the session.
     */
    @Test
    public void testGetPageIdWithPageIdNotInSession()
    {
        authenticationContext.getLoggedInUser();
        mockController.setReturnValue(user);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(Collections.<Object>emptyList());
        final PortalPageRetriever retriever = createRetriever(userHistoryManager, authenticationContext);

        //should get this from the session.
        assertNull(null, retriever.getPageId());
        retriever.setRequestedPageId(new Long(11));

        //should ignore the session.
        assertEquals(new Long(11), retriever.getPageId());

        retriever.setRequestedPageId(null);

        //should get this from the session.
        assertEquals(null, retriever.getPageId());

        mockController.verify();
    }


    /**
     * Make sure the {@link PortalPageRetriever#getPortalPage(JiraServiceContext)}} works when a page id is in the session.
     */
    @Test
    public void testGetPortalPageWithPageIdInSession()
    {
        final User user = new MockUser("testGetPortalPageWithPageIdInSession");
        final JiraServiceContext ctx = createContext(user);
        final Long expectedPageId = new Long(10137);
        final Long expectedSessionId = new Long(10023);

        portalPageService.getPortalPage(ctx, expectedSessionId);
        mockController.setReturnValue(page2);

        portalPageService.getPortalPage(ctx, expectedPageId);
        mockController.setReturnValue(page1);

        portalPageService.getPortalPage(ctx, expectedSessionId);
        mockController.setReturnValue(page2);

        authenticationContext.getLoggedInUser();
        mockController.setReturnValue(user);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "10023")));

        final PortalPageRetriever retriever = createRetriever(userHistoryManager, authenticationContext);

        //this should call through to the service. Id should be from the session.
        assertSame(page2, retriever.getPortalPage(ctx));
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        //this should be cached.
        assertSame(page2, retriever.getPortalPage(ctx));

        retriever.setRequestedPageId(expectedPageId);

        //this should call through to the service. Id should be from object.
        assertSame(page1, retriever.getPortalPage(ctx));
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        //this should be cached.
        assertEquals(page1, retriever.getPortalPage(ctx));

        retriever.setRequestedPageId(null);

        //this should call through to the service. Id should be from the session.
        assertSame(page2, retriever.getPortalPage(ctx));
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        //this should be cached.
        assertSame(page2, retriever.getPortalPage(ctx));

        mockController.verify();
    }

    /**
     * Make sure the {@link PortalPageRetriever#getPortalPage(JiraServiceContext)}} works when a page id is not in the session.
     */
    @Test
    public void testGetPortalPageWithNoPageIdInSession()
    {
        final User user = new MockUser("testGetPortalPageWithNoPageIdInSession");
        final JiraServiceContext ctx = createContext(user);
        final Long expectedPageId = new Long(10137);

        portalPageService.getPortalPage(ctx, expectedPageId);
        mockController.setReturnValue(page2);

        portalPageService.getPortalPage(ctx, expectedPageId);
        mockController.setReturnValue(page1);

         authenticationContext.getLoggedInUser();
        mockController.setReturnValue(user);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(Collections.<Object>emptyList());
        final PortalPageRetriever retriever = createRetriever(userHistoryManager, authenticationContext);
        retriever.setRequestedPageId(expectedPageId);

        //this should call through to the service. Id should be from the session.
        assertSame(page2, retriever.getPortalPage(ctx));
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        //this should be cached.
        assertSame(page2, retriever.getPortalPage(ctx));

        retriever.setRequestedPageId(expectedPageId);

        //this should call through to the service. Id should be from object.
        assertSame(page1, retriever.getPortalPage(ctx));
        assertFalse(ctx.getErrorCollection().hasAnyErrors());

        //this should be cached.
        assertEquals(page1, retriever.getPortalPage(ctx));

        mockController.verify();
    }

    /**
     * Make sure the {@link PortalPageRetriever#getPortalPage(JiraServiceContext)}} works when no page id is supplied.
     */
    @Test
    public void testGetPortalWithNoPageIdError()
    {
        final User user = new MockUser("testGetPortalWithNoPageIdError");

        final JiraServiceContext ctx = createContext(user);

        authenticationContext.getLoggedInUser();
        mockController.setReturnValue(user);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(Collections.<Object>emptyList());
        final PortalPageRetriever retriever = createRetriever(userHistoryManager, authenticationContext);

        //this should call through to the service. Id should be from the session.
        assertNull(retriever.getPortalPage(ctx));
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(ctx.getErrorCollection().getErrorMessages().contains("dashboard.no.id.specified"));

        //this should be cached.
        assertNull(retriever.getPortalPage(ctx));
    }

    private PortalPageRetriever createRetriever(final UserHistoryManager userHistoryManager, final JiraAuthenticationContext authenticationContext)
    {
        mockController.replay();
        return new PortalPageRetriever(portalPageService, userHistoryManager, authenticationContext);
    }

    private JiraServiceContext createContext(final User user)
    {
        return new JiraServiceContextImpl(user, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new I18nBean(LocaleParser.parseLocale("en_AU"))
                {
                    public String getText(final String key, final Object parameters)
                    {
                        return key;
                    }
                };
            }
        };
    }
}
