package com.atlassian.jira.portal;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.jira.dashboard.permission.GadgetPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.index.MockSharedEntityIndexer;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.mock.propertyset.MockPropertySet;
import com.opensymphony.module.propertyset.PropertySet;
import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link com.atlassian.jira.issue.search.DefaultSearchRequestManager}
 *
 * @since v3.13
 */
public class TestDefaultPortalPageManagerClone
{
    private ApplicationUser user;
    private MockControl portalPageStoreCtrl;
    private PortalPageStore portalPageStore;

    private MockControl shareManagerCtrl;
    private ShareManager shareManager;

    private MockControl portletConfigurationManagerCtrl;
    private PortletConfigurationManager portletConfigurationManager;

    private EventPublisher eventPublisher;
    private JiraAuthenticationContext authenticationContext;

    private SharePermission perm2;
    private SharePermission perm1;

    private PortalPage portalPage1;
    private PortalPage portalPage2;
    private PortalPage portalPageWithPortlets;

    private MockControl gadgetPermissionManagerCtrl;
    private GadgetPermissionManager gadgetPermissionManager;

    private static final Long DEVIL_ID = new Long(666);
    private static final String DASHBOARD = "dashboard";
    private static final String OWEN = "user";
    private static URI GADGET_XML;

    @Before
    public void setUp() throws Exception
    {
        GADGET_XML = new URI("http://atlassian.com");

        eventPublisher = EasyMock.createNiceMock(EventPublisher.class);
        authenticationContext = EasyMock.createNiceMock(JiraAuthenticationContext.class);
        EasyMock.replay(eventPublisher, authenticationContext);

        portalPageStoreCtrl = MockControl.createStrictControl(PortalPageStore.class);
        portalPageStore = (PortalPageStore) portalPageStoreCtrl.getMock();

        shareManagerCtrl = MockControl.createStrictControl(ShareManager.class);
        shareManager = (ShareManager) shareManagerCtrl.getMock();

        portletConfigurationManagerCtrl = MockControl.createStrictControl(PortletConfigurationManager.class);
        portletConfigurationManager = (PortletConfigurationManager) portletConfigurationManagerCtrl.getMock();

        gadgetPermissionManagerCtrl = MockControl.createStrictControl(GadgetPermissionManager.class);
        gadgetPermissionManager = (GadgetPermissionManager) gadgetPermissionManagerCtrl.getMock();

        user = new MockApplicationUser("admin");
        perm1 = new SharePermissionImpl(GroupShareType.TYPE, "jira-user", null);
        perm2 = new SharePermissionImpl(GlobalShareType.TYPE, null, null);

        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(user).build();
        portalPage2 = PortalPage.id(2L).name("two").description("two description").owner(user).build();
        portalPageWithPortlets = PortalPage.id(DEVIL_ID).name(DASHBOARD).description(DASHBOARD).owner(new MockApplicationUser(OWEN)).layout(Layout.AA).version(0L).build();

    }

    @After
    public void tearDown() throws Exception
    {
        gadgetPermissionManagerCtrl = null;
        gadgetPermissionManager = null;
    }

    private PortalPageManager createDefaultPortalPageManager()
    {
        portalPageStoreCtrl.replay();
        shareManagerCtrl.replay();
        portletConfigurationManagerCtrl.replay();
        gadgetPermissionManagerCtrl.replay();

        return new DefaultPortalPageManager(shareManager, portalPageStore, portletConfigurationManager, new MockSharedEntityIndexer(), eventPublisher, authenticationContext)
        {
            @Override
            GadgetPermissionManager getGadgetPermissionManager()
            {
                return gadgetPermissionManager;
            }
        };
    }

    private void verifyMocks()
    {
        portalPageStoreCtrl.verify();
        shareManagerCtrl.verify();
        portletConfigurationManagerCtrl.verify();
        gadgetPermissionManagerCtrl.verify();
    }

    @Test
    public void test_createWithClone_NullPortalPage() throws Exception
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.createBasedOnClone(user, null, portalPage2);
            fail("Should not accept null search portalPage.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void test_createWithClone_NullPortalPageClone() throws Exception
    {
        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        try
        {
            portalPageManager.createBasedOnClone((ApplicationUser) null, portalPage2, null);
            fail("Should not accept null search portalPage clone.");
        }
        catch (final IllegalArgumentException e)
        {
            // exception.
        }
    }

    @Test
    public void test_createWithClone_NoPerms() throws Exception
    {
        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(Collections.emptyList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(SharedEntity.SharePermissions.PRIVATE).build();

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(SharedEntity.SharePermissions.PRIVATE);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
        assertNotNull(portalPage);
        assertEquals(portalPage1, portalPage);
        assertTrue(portalPage.getPermissions().isPrivate());

        verifyMocks();
    }

    @Test
    public void test_createWithClone_hasPerms() throws Exception
    {
        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(Collections.emptyList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        final PortalPage portalPage = portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
        assertNotNull(portalPage);
        assertEquals(portalPage1, portalPage);
        assertEquals(permissions, portalPage.getPermissions());

        verifyMocks();

    }


    @Test
    public void test_createWithClone_gadgets_hasPerms() throws Exception
    {

        PortletConfiguration gadget = makePC(DEVIL_ID, 123L, null, 1, 1, null, GADGET_XML);
        PortletConfiguration copyOfGadget = makePC(1L, 123L, null, 1, 1, null, GADGET_XML);

        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(CollectionBuilder.newBuilder(gadget).asList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        gadgetPermissionManager.extractModuleKey(GADGET_XML.toASCIIString());
        gadgetPermissionManagerCtrl.setReturnValue("module-key");

        gadgetPermissionManager.voteOn("module-key", user.getDirectoryUser());
        gadgetPermissionManagerCtrl.setReturnValue(Vote.ALLOW);

        portletConfigurationManager.addGadget(1L, 1, 1, GADGET_XML, Color.color1,  new HashMap<String, String>());
        portletConfigurationManagerCtrl.setReturnValue(copyOfGadget);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();
        portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
    }

    @Test
    public void test_createWithClone_gadgets_noPerms() throws Exception
    {

        PortletConfiguration gadget = makePC(DEVIL_ID, 123L, null, 1, 1, null, GADGET_XML);

        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(CollectionBuilder.newBuilder(gadget).asList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        gadgetPermissionManager.extractModuleKey(GADGET_XML.toASCIIString());
        gadgetPermissionManagerCtrl.setReturnValue("module-key");

        gadgetPermissionManager.voteOn("module-key", user.getDirectoryUser());
        gadgetPermissionManagerCtrl.setReturnValue(Vote.DENY);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
    }

    @Test
    public void test_createWithClone_gadgets_externalGadget() throws Exception
    {

        PortletConfiguration gadget = makePC(DEVIL_ID, 123L, null, 1, 1, null, GADGET_XML);
        PortletConfiguration copyOfGadget = makePC(1L, 123L, null, 1, 1, null, GADGET_XML);

        final HashSet permSet1 = new HashSet();
        permSet1.add(perm1);
        permSet1.add(perm2);
        final SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(permSet1);

        portalPage1 = PortalPage.portalPage(portalPage1).permissions(permissions).build();

        portletConfigurationManager.getByPortalPage(portalPageWithPortlets.getId());
        portletConfigurationManagerCtrl.setReturnValue(CollectionBuilder.newBuilder(gadget).asList());

        portalPageStore.create(portalPage1);
        portalPageStoreCtrl.setReturnValue(portalPage1);

        shareManager.updateSharePermissions(portalPage1);
        shareManagerCtrl.setReturnValue(permissions);

        gadgetPermissionManager.extractModuleKey(GADGET_XML.toASCIIString());
        gadgetPermissionManagerCtrl.setReturnValue(null);

        portletConfigurationManager.addGadget(1L, 1, 1, GADGET_XML, Color.color1,  new HashMap<String, String>());
        portletConfigurationManagerCtrl.setReturnValue(copyOfGadget);

        final PortalPageManager portalPageManager = createDefaultPortalPageManager();

        portalPageManager.createBasedOnClone(user, portalPage1, portalPageWithPortlets);
    }

    MockPropertySet makeFilledPS()
    {
        final MockPropertySet mockPropertySet = new MockPropertySet();
        final Map map = mockPropertySet.getMap();
        map.put("string", "val1");
        mockPropertySet.getMap().put("stringbuffer", new StringBuffer("sb"));
        mockPropertySet.getMap().put("long", new Long(1));
        return mockPropertySet;
    }

    PortletConfiguration makePC(final Long id, final Long portalPageId, final String portletKey, final Integer suggestedCol, final Integer suggestRow, final PropertySet propertySet, final URI gadgetXml)
    {
        return new MockPortletConfiguration(id, suggestedCol, suggestRow, portalPageId, portletKey, propertySet, gadgetXml);
    }
}
