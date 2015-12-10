package com.atlassian.jira.web.bean;

import java.util.Comparator;
import java.util.Map;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.type.ShareQueryFactory;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypePermissionChecker;
import com.atlassian.jira.sharing.type.ShareTypeRenderer;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.sharing.type.ShareTypeValidator;
import com.atlassian.jira.user.MockUser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.web.bean.ShareTypeRendererBean}.
 * 
 * @since v3.13
 */
public class TestShareTypeRendererBean
{
    private static final String MOCK_SHARE_TYPE = "MockShareType";
    private static final String EDITOR = "<select><option>SingleOption</option></select>";
    @Rule
    public final RuleChain rule = MockitoMocksInContainer.forTest(this);
    private User testUser;
    private JiraAuthenticationContext authenticationContext;
    private Mock mock;

    @Before
    public void setUp() throws Exception
    {
        testUser = new MockUser("test");
        authenticationContext = new MockAuthenticationContext(testUser);
        mock = new Mock(ShareTypeRenderer.class);
        mock.setStrict(true);
    }

    @After
    public void tearDown() throws Exception
    {
        testUser = null;
        authenticationContext = null;
        mock = null;
    }

    @Test
    public void testConstructionWithNullShareType()
    {
        try
        {
            new ShareTypeRendererBean(null, authenticationContext, RenderMode.EDIT, PortalPage.ENTITY_TYPE);
            fail("This should throw a runtime exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testConstructionWithNullAuthCtx()
    {
        final ShareType stype = (ShareType) DuckTypeProxy.getProxy(ShareType.class, new Object());

        try
        {
            new ShareTypeRendererBean(stype, null, RenderMode.EDIT, PortalPage.ENTITY_TYPE);
            fail("This should throw a runtime exception.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testConstructionWithNullRenderMode()
    {
        final ShareType stype = (ShareType) DuckTypeProxy.getProxy(ShareType.class, new Object());

        try
        {
            new ShareTypeRendererBean(stype, authenticationContext, null, PortalPage.ENTITY_TYPE);
            fail("This should throw a runtime exception.");
        }
        catch (final IllegalArgumentException expected)
        {
            // expected.
        }
    }

    @Test
    public void testConstructionWithNullType()
    {
        final ShareType stype = (ShareType) DuckTypeProxy.getProxy(ShareType.class, new Object());

        try
        {
            new ShareTypeRendererBean(stype, authenticationContext, RenderMode.EDIT, null);
            fail("This should throw a runtime exception.");
        }
        catch (final IllegalArgumentException expected)
        {
            // expected.
        }
    }

    @Test
    public void testGetShareType()
    {
        final ShareTypeRendererBean bean = new ShareTypeRendererBean(createShareType(TestShareTypeRendererBean.MOCK_SHARE_TYPE, null), authenticationContext, RenderMode.EDIT, PortalPage.ENTITY_TYPE);
        assertEquals(TestShareTypeRendererBean.MOCK_SHARE_TYPE, bean.getShareType());
    }

    @Test
    public void testGetShareTypeEditor()
    {
        mock.expectAndReturn("getShareTypeEditor", P.args(P.eq(authenticationContext)), TestShareTypeRendererBean.EDITOR);
        final ShareTypeRendererBean bean = new ShareTypeRendererBean(createShareType(TestShareTypeRendererBean.MOCK_SHARE_TYPE, (ShareTypeRenderer) mock.proxy()), authenticationContext, RenderMode.EDIT, PortalPage.ENTITY_TYPE);
        assertEquals(TestShareTypeRendererBean.EDITOR, bean.getShareTypeEditor());

        mock.verify();
    }

    @Test
    public void testIsAddButtonNeeded()
    {
        mock.expectAndReturn("isAddButtonNeeded", P.args(P.eq(authenticationContext)), Boolean.FALSE);
        final ShareTypeRendererBean bean = new ShareTypeRendererBean(createShareType(TestShareTypeRendererBean.MOCK_SHARE_TYPE, (ShareTypeRenderer) mock.proxy()), authenticationContext, RenderMode.EDIT, PortalPage.ENTITY_TYPE);
        assertEquals(false, bean.isAddButtonNeeded());

        mock.verify();
    }

    @Test
    public void testGetShareTypeLabel()
    {
        mock.expectAndReturn("getShareTypeLabel", P.args(P.eq(authenticationContext)), TestShareTypeRendererBean.MOCK_SHARE_TYPE);
        final ShareTypeRendererBean bean = new ShareTypeRendererBean(createShareType(TestShareTypeRendererBean.MOCK_SHARE_TYPE, (ShareTypeRenderer) mock.proxy()), authenticationContext, RenderMode.EDIT, PortalPage.ENTITY_TYPE);
        assertEquals(TestShareTypeRendererBean.MOCK_SHARE_TYPE, bean.getShareTypeLabel());

        mock.verify();
    }

    @Test
    public void testGetTranslatedTemplatesEditMode()
    {
        final Map expectedMap = EasyMap.build("msg1", "This is message one");

        mock.expectAndReturn("getTranslatedTemplates", P.args(P.eq(authenticationContext), P.same(PortalPage.ENTITY_TYPE), P.same(RenderMode.EDIT)), expectedMap);
        final ShareTypeRendererBean bean = new ShareTypeRendererBean(createShareType(TestShareTypeRendererBean.MOCK_SHARE_TYPE, (ShareTypeRenderer) mock.proxy()), authenticationContext, RenderMode.EDIT, PortalPage.ENTITY_TYPE);
        assertEquals(expectedMap, bean.getTranslatedTemplates());

        mock.verify();
    }

    @Test
    public void testGetTranslatedTemplatesSearchMode()
    {
        final Map expectedMap = EasyMap.build("msg2", "This is message one", "badmessage", "bad message");

        mock.expectAndReturn("getTranslatedTemplates", P.args(P.eq(authenticationContext), P.same(SearchRequest.ENTITY_TYPE), P.same(RenderMode.SEARCH)), expectedMap);
        final ShareTypeRendererBean bean = new ShareTypeRendererBean(createShareType(TestShareTypeRendererBean.MOCK_SHARE_TYPE, (ShareTypeRenderer) mock.proxy()), authenticationContext, RenderMode.SEARCH, SearchRequest.ENTITY_TYPE);
        assertEquals(expectedMap, bean.getTranslatedTemplates());

        mock.verify();
    }

    private ShareType createShareType(final String type, final ShareTypeRenderer renderer)
    {
        return new ShareType()
        {
            public ShareType.Name getType()
            {
                return new ShareType.Name(type);
            }

            public boolean isSingleton()
            {
                return false;
            }

            public int getPriority()
            {
                return 0;
            }

            public ShareTypeRenderer getRenderer()
            {
                return renderer;
            }

            public ShareTypeValidator getValidator()
            {
                return null;
            }

            public ShareTypePermissionChecker getPermissionsChecker()
            {
                return null;
            }

            public Comparator getComparator()
            {
                return null;
            }

            public ShareQueryFactory getQueryFactory()
            {
                return null;
            }
        };
    }

}
