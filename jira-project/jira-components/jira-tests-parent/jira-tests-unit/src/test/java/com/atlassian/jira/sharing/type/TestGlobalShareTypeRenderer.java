package com.atlassian.jira.sharing.type;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.user.MockUser;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for the {@link com.atlassian.jira.sharing.type.GlobalShareTypeRenderer}.
 *
 * @since v3.13
 */
public class TestGlobalShareTypeRenderer
{
    private static final SharePermission GROUP_PREM_1 = new SharePermissionImpl(GroupShareType.TYPE, "group1", null);
    private static final SharePermission GLOBAL_PREM = new SharePermissionImpl(GlobalShareType.TYPE, null, null);
    private static final User USER = new MockUser("test");

    private JiraAuthenticationContext userCtx;
    private JiraAuthenticationContext anonymousCtx;
    private GlobalShareTypeRenderer renderer;

    @Before
    public void setUp() throws Exception
    {
        renderer = new GlobalShareTypeRenderer();
        userCtx = new MockAuthenticationContext(TestGlobalShareTypeRenderer.USER);
        anonymousCtx = new MockAuthenticationContext(null);
    }

    @After
    public void tearDown() throws Exception
    {
        renderer = null;
        userCtx = null;
        anonymousCtx = null;
    }

    @Test
    public void testRenderPermissionWithBadShare()
    {
        try
        {
            renderer.renderPermission(TestGlobalShareTypeRenderer.GROUP_PREM_1, userCtx);
            fail("Should not render invalid share permission.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testRenderPermissionWithNullShare()
    {
        try
        {
            renderer.renderPermission(null, userCtx);
            fail("Should not render invalid share permission.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testRenderPermissionWithNullCtx()
    {
        try
        {
            renderer.renderPermission(TestGlobalShareTypeRenderer.GLOBAL_PREM, null);
            fail("Should not render with null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testRenderPermission()
    {
        final String html = renderer.renderPermission(TestGlobalShareTypeRenderer.GLOBAL_PREM, userCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isBlank(html));
    }

    @Test
    public void testRenderPermissionAnonymous()
    {
        final String html = renderer.renderPermission(TestGlobalShareTypeRenderer.GLOBAL_PREM, anonymousCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isBlank(html));
    }

    @Test
    public void testGetShareTypeEditor()
    {
        final String html = renderer.getShareTypeEditor(userCtx);
        assertNotNull(html);
        assertTrue(StringUtils.isBlank(html));
    }

    @Test
    public void testGetShareTypeEditorAnonymous()
    {
        final String html = renderer.getShareTypeEditor(anonymousCtx);
        assertNotNull(html);
        assertTrue(StringUtils.isBlank(html));
    }

    @Test
    public void testGetShareTypeEditorWithNullCtx()
    {
        final String html = renderer.getShareTypeEditor(null);
        assertNotNull(html);
        assertTrue(StringUtils.isBlank(html));
    }

    @Test
    public void testIsAddButtonNeeded()
    {
        assertTrue(renderer.isAddButtonNeeded(userCtx));
    }

    @Test
    public void testIsAddButtonNeededAnonymous()
    {
        assertTrue(renderer.isAddButtonNeeded(anonymousCtx));
    }

    @Test
    public void testIsAddButtonNeededWithNullCtx()
    {
        assertTrue(renderer.isAddButtonNeeded(null));
    }

    @Test
    public void testGetShareTypeLabel()
    {
        final String html = renderer.getShareTypeLabel(userCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isEmpty(html));
    }

    @Test
    public void testGetShareTypeLabelAnonymous()
    {
        final String html = renderer.getShareTypeLabel(anonymousCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isEmpty(html));
    }

    @Test
    public void testGetShareTypeLabelNullCtx()
    {
        try
        {
            renderer.getShareTypeLabel(null);
            fail("Should not accept a null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testGetTranslatedTemplatesNullCtx()
    {
        try
        {
            renderer.getTranslatedTemplates(null, null, null);
            fail("Should not accept a null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testGetTranslatedTemplates()
    {
        final Map templates = renderer.getTranslatedTemplates(userCtx, null, null);
        assertNotNull(templates);
        assertTrue(templates.containsKey("share_global_display"));
        assertTrue(StringUtils.isNotBlank((String) templates.get("share_global_display")));
        assertTrue(templates.containsKey("share_global_description"));
        assertTrue(StringUtils.isNotBlank((String) templates.get("share_global_description")));
    }
}
