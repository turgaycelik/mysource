package com.atlassian.jira.sharing.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.template.mocks.VelocityTemplatingEngineMocks;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;
import com.opensymphony.util.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link com.atlassian.jira.sharing.type.GroupShareTypeRenderer}. We test the #getShareTypeEditor in functional tests as it relies on
 * Velocity.
 *
 * @since v3.13
 */

public class TestGroupShareTypeRenderer
{
    private static final String GROUP1 = "group1";
    private static final String GROUP2 = "abcgroup2";
    private static final SharePermission GROUP_PREM_1 = new SharePermissionImpl(GroupShareType.TYPE, TestGroupShareTypeRenderer.GROUP1, null);
    private static final SharePermission GROUP_PREM_XSS = new SharePermissionImpl(GroupShareType.TYPE, "<b>XSS</b>", null);
    private static final SharePermission GLOBAL_PREM = new SharePermissionImpl(GlobalShareType.TYPE, null, null);
    private static final String VELOCITY_RETURN = "<selector><option>b</option></selector>";
    private static final String GROUPS_KEY = "groups";

    private JiraAuthenticationContext userCtx;
    private JiraAuthenticationContext anonymousCtx;
    private GroupShareTypeRenderer renderer;
    private User user;
    private VelocityTemplatingEngine templatingEngine;
    private EncodingConfiguration encoding = new EncodingConfiguration()
    {
        public String getEncoding()
        {
            return "UTF-8";
        }
    };

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();
        user = new MockUser("test");
        userCtx = createAuthenticationContext(user, new MockI18nBean());
        anonymousCtx = createAuthenticationContext(null, new MockI18nBean());
        templatingEngine = VelocityTemplatingEngineMocks.alwaysOutput(VELOCITY_RETURN).get();
        renderer = new GroupShareTypeRenderer(encoding, templatingEngine, null);
    }

    @After
    public void tearDown() throws Exception
    {
        renderer = null;
        userCtx = null;
        anonymousCtx = null;
        user = null;
        encoding = null;
    }

    @Test
    public void testRenderPermission()
    {
        final String html = renderer.renderPermission(TestGroupShareTypeRenderer.GROUP_PREM_1, userCtx);
        assertNotNull(html);
        assertFalse(isBlank(html));
        assertTrue(html.contains(TestGroupShareTypeRenderer.GROUP1));
    }

    @Test
    public void testRenderPermissionXSS()
    {
        final String html = renderer.renderPermission(TestGroupShareTypeRenderer.GROUP_PREM_XSS, userCtx);
        assertNotNull(html);
        assertFalse(isBlank(html));
        assertFalse(html.contains(TestGroupShareTypeRenderer.GROUP_PREM_XSS.getParam1()));
        assertTrue(html.contains(TextUtils.htmlEncode(TestGroupShareTypeRenderer.GROUP_PREM_XSS.getParam1())));
    }

    @Test
    public void testRenderPermissionWithNullUser()
    {
        final String html = renderer.renderPermission(TestGroupShareTypeRenderer.GROUP_PREM_1, anonymousCtx);
        assertNotNull(html);
        assertFalse(isBlank(html));
        assertTrue(html.contains(TestGroupShareTypeRenderer.GROUP1));
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldFailToRenderANullPermission()
    {
        renderer.renderPermission(null, userCtx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToRenderAPermissionGivenANullUserContext()
    {
        renderer.renderPermission(TestGroupShareTypeRenderer.GROUP_PREM_1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldOnlyAcceptToRenderAGroupShareType()
    {
        renderer.renderPermission(TestGroupShareTypeRenderer.GLOBAL_PREM, anonymousCtx);
    }

    @Test
    public void testGetShareTypeLabel()
    {
        final String result = renderer.getShareTypeLabel(userCtx);
        assertNotNull(result);
        assertFalse(isBlank(result));
    }

    @Test
    public void testGetShareTypeLabelWithAnonymousUser()
    {
        final String result = renderer.getShareTypeLabel(anonymousCtx);
        assertNotNull(result);
        assertFalse(isBlank(result));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToGetAShareTypeLabelGivenANullAuthenticationContext()
    {
        renderer.getShareTypeLabel(null);
    }

    @Test
    public void testIsAddButtonNeededTrue()
    {
        renderer = new GroupShareTypeRenderer(encoding, templatingEngine, null)
        {
            List<String> getGroupsForUser(final User user)
            {
                assertEquals(TestGroupShareTypeRenderer.this.user, user);
                return Collections.singletonList(TestGroupShareTypeRenderer.GROUP1);
            }
        };

        assertTrue(renderer.isAddButtonNeeded(userCtx));
    }

    @Test
    public void testIsAddButtonNeededFalse()
    {
        renderer = new GroupShareTypeRenderer(encoding, templatingEngine, null)
        {
            List<String> getGroupsForUser(final User user)
            {
                assertEquals(TestGroupShareTypeRenderer.this.user, user);
                return Collections.emptyList();
            }
        };

        assertFalse(renderer.isAddButtonNeeded(userCtx));
    }

    @Test
    public void testIsAddButtonNeededWithAnonymousUser()
    {
        renderer = new GroupShareTypeRenderer(encoding, templatingEngine, null)
        {
            List<String> getGroupsForUser(final User user)
            {
                assertNull(user);
                return Collections.emptyList();
            }
        };

        assertFalse(renderer.isAddButtonNeeded(anonymousCtx));
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkingWhetherTheAddButtonIsNeededOrNotShouldFailGivenANullAuthenticationContext()
    {
        renderer.isAddButtonNeeded(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingTheEditorForAShareTypeShouldFailGivenANullAuthenticationContext()
    {
        renderer.getShareTypeEditor(null);
    }

    @Test
    public void testGetShareTypeEditor()
    {
        renderer = new GroupShareTypeRenderer(encoding, templatingEngine, null)
        {
            List<String> getGroupsForUser(final User user)
            {
                assertEquals(TestGroupShareTypeRenderer.this.user, user);
                return ImmutableList.of(TestGroupShareTypeRenderer.GROUP1, TestGroupShareTypeRenderer.GROUP2);
            }

            Map<String, Object> addDefaultVelocityParameters(final Map<String, Object> params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestGroupShareTypeRenderer.GROUPS_KEY));
                final List groups = (List) params.get(TestGroupShareTypeRenderer.GROUPS_KEY);
                assertNotNull(groups);
                assertEquals(ImmutableList.of(TestGroupShareTypeRenderer.GROUP2, TestGroupShareTypeRenderer.GROUP1), groups);

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(userCtx);
        assertEquals(TestGroupShareTypeRenderer.VELOCITY_RETURN, html);
    }

    @Test
    public void testGetShareTypeEditorNoGroups()
    {
        renderer = new GroupShareTypeRenderer(encoding, templatingEngine, null)
        {
            List<String> getGroupsForUser(final User user)
            {
                assertEquals(TestGroupShareTypeRenderer.this.user, user);
                return Collections.emptyList();
            }

            Map<String, Object> addDefaultVelocityParameters(final Map<String, Object> params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestGroupShareTypeRenderer.GROUPS_KEY));
                final List groups = (List) params.get(TestGroupShareTypeRenderer.GROUPS_KEY);
                assertTrue(groups.isEmpty());

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(userCtx);
        assertEquals(TestGroupShareTypeRenderer.VELOCITY_RETURN, html);
    }

    @Test
    public void testGetShareTypeEditorAnonymousUser()
    {
        renderer = new GroupShareTypeRenderer(encoding, templatingEngine, null)
        {
            List<String> getGroupsForUser(final User user)
            {
                assertNull(user);
                return Collections.emptyList();
            }

            Map<String, Object> addDefaultVelocityParameters(final Map<String, Object> params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestGroupShareTypeRenderer.GROUPS_KEY));
                final List groups = (List) params.get(TestGroupShareTypeRenderer.GROUPS_KEY);
                assertTrue(groups.isEmpty());

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(anonymousCtx);
        assertEquals(TestGroupShareTypeRenderer.VELOCITY_RETURN, html);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingTheTranslatedTemplateForAnEntityAndARenderModeShouldFailGivenANullAuthenticationContext()
    {
        renderer.getTranslatedTemplates(null, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingTheTranslatedTemplatesShouldFailGivenANullEntity()
    {
        renderer.getTranslatedTemplates(anonymousCtx, null, RenderMode.EDIT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void retrievingTheTranslatedEntitiesShouldFailGivenANullRenderMode()
    {
        renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, null);
    }

    @Test
    public void testGetTranslatedTemplates()
    {
        Map<String, String> actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, SearchRequest.ENTITY_TYPE, RenderMode.EDIT);
        assertNotNull(actualTemplates);
        assertTemplates(ImmutableList.of("share_group_display", "share_group_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
        assertNotNull(actualTemplates);
        assertTemplates(ImmutableList.of("share_group_display", "share_group_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, SearchRequest.ENTITY_TYPE, RenderMode.SEARCH);
        assertNotNull(actualTemplates);
        assertTemplates(ImmutableList.of("share_group_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.SEARCH);
        assertNotNull(actualTemplates);
        assertTemplates(ImmutableList.of("share_group_description"), actualTemplates);
    }

    private void assertTemplates(final List<String> expectedKeys, final Map<String, String> actualTemplates)
    {
        assertEquals(expectedKeys.size(), actualTemplates.size());
        assertTrue(actualTemplates.keySet().containsAll(expectedKeys));
        for (Map.Entry entry : actualTemplates.entrySet())
        {
            assertTrue("Template for key '" + entry.getKey() + "' is blank.", isNotBlank((String) entry.getValue()));
        }
    }

    private JiraAuthenticationContext createAuthenticationContext(final User user, final I18nBean i18n)
    {
        return new MockAuthenticationContext(user);
    }
}
