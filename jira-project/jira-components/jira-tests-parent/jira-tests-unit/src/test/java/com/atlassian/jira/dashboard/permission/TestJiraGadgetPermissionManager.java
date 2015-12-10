package com.atlassian.jira.dashboard.permission;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.gadgets.plugins.GadgetLocationTranslator;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.MockGlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import static com.atlassian.gadgets.plugins.PluginGadgetSpec.Key;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestJiraGadgetPermissionManager
{
    private ApplicationUser admin = new MockApplicationUser("admin");
    private ApplicationUser user = new MockApplicationUser("user");

    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @AvailableInContainer public GlobalPermissionManager globalPermissionManager = MockGlobalPermissionManager.withSystemGlobalPermissions();
    @AvailableInContainer public GadgetLocationTranslator gadgetLocationTranslator = Mockito.mock(GadgetLocationTranslator.class);
    @Test
    public void testVoteOnNull()
    {
        JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(null, null, null);
        exception.expect(Exception.class);
        permissionManager.voteOn((PluginGadgetSpec) null, (ApplicationUser) null);
    }

    @Test
    public void testVoteOnNonPluginGadget()
    {
        final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        when(mockPluginAccessor.getEnabledPluginModule("somerandomthing:stuff")).thenReturn(null);

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mock(PermissionManager.class), mockPluginAccessor, mock(DashboardPermissionService.class));
        Vote vote = permissionManager.voteOn("somerandomthing:stuff", user);
        assertEquals(Vote.ALLOW, vote);
    }

    @Test
    public void testVoteOnLoginGadget()
    {
        final Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getKey()).thenReturn("com.atlassian.jira.gadgets");

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mock(PermissionManager.class), mock(PluginAccessor.class), mock(DashboardPermissionService.class));

        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "login-gadget", "rest/login.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.DENY, vote);

        //login gadget should be shown for logged out user.
        vote = permissionManager.voteOn(mockSpec,(ApplicationUser)  null);
        assertEquals(Vote.ALLOW, vote);
    }

    @Test
    public void testVoteOnGadgetNoRolesSpecified()
    {
        final Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getKey()).thenReturn("com.atlassian.jira.gadgets");
        final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mock(ModuleDescriptor.class);
        when(mockModuleDescriptor.getParams()).thenReturn(Collections.<String, String>emptyMap());
        final ModuleDescriptor mockModuleDescriptor2 = mock(ModuleDescriptor.class);
        when(mockModuleDescriptor2.getParams()).thenReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "").toMap());

        when(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).thenReturn(mockModuleDescriptor);
        when(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget2")).thenReturn(mockModuleDescriptor2);

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mock(PermissionManager.class), mockPluginAccessor, mock(DashboardPermissionService.class));

        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        final PluginGadgetSpec mockSpec2 = new PluginGadgetSpec(mockPlugin, "intro-gadget2", "rest/intro.xml", MapBuilder.<String, String>newBuilder().add("roles-required", "").toMap());
        Vote vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.ALLOW, vote);

        vote = permissionManager.voteOn(mockSpec2, (ApplicationUser) null);
        assertEquals(Vote.ALLOW, vote);
    }

    @Test
    public void testVoteOnGadgetInvalidRolesSpecified()
    {
        final Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getKey()).thenReturn("com.atlassian.jira.gadgets");
        final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mock(ModuleDescriptor.class);
        when(mockModuleDescriptor.getParams()).thenReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "someinvalidmumbojumbo").toMap());

        when(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).thenReturn(mockModuleDescriptor);

        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mockPermissionManager, mockPluginAccessor, mock(DashboardPermissionService.class));

        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.PASS, vote);
    }

    @Test
    public void testVoteOnGadgetGlobalRolesSpecified()
    {
        final Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getKey()).thenReturn("com.atlassian.jira.gadgets");
        final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mock(ModuleDescriptor.class);
        when(mockModuleDescriptor.getParams()).thenReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "use").toMap());
        when(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).thenReturn(mockModuleDescriptor);

        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(false);
        when(mockPermissionManager.hasPermission(Permissions.USE, admin)).thenReturn(true);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        when(mockPermissionManager.hasPermission(Permissions.USE, user)).thenReturn(false);

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mockPermissionManager, mockPluginAccessor, mock(DashboardPermissionService.class));

        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, admin);
        assertEquals(Vote.ALLOW, vote);

        vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.DENY, vote);
    }

    @Test
    public void testVoteOnGadgetProjectRolesSpecified() throws Exception
    {
        final Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getKey()).thenReturn("com.atlassian.jira.gadgets");
        final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mock(ModuleDescriptor.class);
        when(mockModuleDescriptor.getParams()).thenReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "browse").toMap());
        when(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).thenReturn(mockModuleDescriptor);

        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(false);
        when(mockPermissionManager.hasProjects(Permissions.BROWSE, admin)).thenReturn(true);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        when(mockPermissionManager.hasProjects(Permissions.BROWSE, user)).thenReturn(false);

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mockPermissionManager, mockPluginAccessor, mock(DashboardPermissionService.class));

        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, admin);
        assertEquals(Vote.ALLOW, vote);

        vote = permissionManager.voteOn(mockSpec, user);
        assertEquals(Vote.DENY, vote);
    }

    @Test
    public void testVoteOnGadgetGlobalAdmin() throws Exception
    {
        final Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getKey()).thenReturn("com.atlassian.jira.gadgets");

        final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        final ModuleDescriptor mockModuleDescriptor = mock(ModuleDescriptor.class);

        when(mockModuleDescriptor.getParams()).thenReturn(MapBuilder.<String, String>newBuilder().add("roles-required", "browse").toMap());
        when(mockPluginAccessor.getEnabledPluginModule("com.atlassian.jira.gadgets:intro-gadget")).thenReturn(mockModuleDescriptor);

        final PermissionManager mockPermissionManager = mock(PermissionManager.class);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, admin)).thenReturn(true);

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mockPermissionManager, mockPluginAccessor, mock(DashboardPermissionService.class));

        final PluginGadgetSpec mockSpec = new PluginGadgetSpec(mockPlugin, "intro-gadget", "rest/intro.xml", Collections.<String, String>emptyMap());
        Vote vote = permissionManager.voteOn(mockSpec, admin);
        assertEquals(Vote.ALLOW, vote);
    }

    @Test
    public void testFilterNullDashboardState()
    {
        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mock(PermissionManager.class), mock(PluginAccessor.class), mock(DashboardPermissionService.class));
        try
        {
            permissionManager.filterGadgets(null, (ApplicationUser) null);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
    }

    @Test
    public void testFilterReadOnlyDashboardState()
    {
        List<List<GadgetState>> columns = new ArrayList<List<GadgetState>>();
        List<GadgetState> columnOne = new ArrayList<GadgetState>();
        columnOne.add(createGadgetStateAndAddToTranslationService("100", "rest/gadgets/1.0/g/someothergadget.xml", null));
        columnOne.add(createGadgetStateAndAddToTranslationService("100", "rest/gadgets/1.0/g/someothergadget2.xml", null));
        columnOne.add(createGadgetStateAndAddToTranslationService("100", "http://www.atlassian.com/stream.xml", null));
        List<GadgetState> columnTwo = new ArrayList<GadgetState>();
        columnTwo.add(createGadgetStateAndAddToTranslationService("100", "rest/gadgets/1.0/g/com.atlassian.jira.gadgets:login-gadget/login.xml",
                new Key("com.atlassian.jira.gadgets", "login.xml")));
        columnTwo.add(createGadgetStateAndAddToTranslationService("100", "http://www.atlassian.com/stream3.xml", null));
        columns.add(columnOne);
        columns.add(columnTwo);

        final DashboardState state = DashboardState.dashboard(DashboardId.valueOf("1")).title("System Dashboard").columns(columns).build();

        final DashboardPermissionService mockPermissionService = mock(DashboardPermissionService.class);
        when(mockPermissionService.isWritableBy(DashboardId.valueOf("1"), user.getName())).thenReturn(false);

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mock(PermissionManager.class), mock(PluginAccessor.class), mockPermissionService);
        final DashboardState filteredState = permissionManager.filterGadgets(state, user);

        assertNotSame(state, filteredState);
        assertEquals(Layout.AA, filteredState.getLayout());
        Iterator<GadgetState> stateIterator = filteredState.getGadgetsInColumn(DashboardState.ColumnIndex.ZERO).iterator();
        assertEquals(URI.create("rest/gadgets/1.0/g/someothergadget.xml"), stateIterator.next().getGadgetSpecUri());
        assertEquals(URI.create("rest/gadgets/1.0/g/someothergadget2.xml"), stateIterator.next().getGadgetSpecUri());
        assertEquals(URI.create("http://www.atlassian.com/stream.xml"), stateIterator.next().getGadgetSpecUri());
        assertFalse(stateIterator.hasNext());
        stateIterator = filteredState.getGadgetsInColumn(DashboardState.ColumnIndex.ONE).iterator();
        assertEquals(URI.create("http://www.atlassian.com/stream3.xml"), stateIterator.next().getGadgetSpecUri());
        assertFalse(stateIterator.hasNext());
    }

    @Test
    public void testFilterWriteableDashboardState()
    {
        List<List<GadgetState>> columns = new ArrayList<List<GadgetState>>();
        List<GadgetState> columnOne = new ArrayList<GadgetState>();
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/someothergadget.xml")).build());
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/someothergadget2.xml")).build());
        columnOne.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("http://www.atlassian.com/stream.xml")).build());
        List<GadgetState> columnTwo = new ArrayList<GadgetState>();
        columnTwo.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:login-gadget/login.xml")).build());
        columnTwo.add(GadgetState.gadget(GadgetId.valueOf("100")).specUri(URI.create("http://www.atlassian.com/stream3.xml")).build());
        columns.add(columnOne);
        columns.add(columnTwo);

        final DashboardState state = DashboardState.dashboard(DashboardId.valueOf("1")).title("System Dashboard").columns(columns).build();

        final DashboardPermissionService mockPermissionService = mock(DashboardPermissionService.class);
        when(mockPermissionService.isWritableBy(DashboardId.valueOf("1"), user.getName())).thenReturn(true);

        final JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(mock(PermissionManager.class), mock(PluginAccessor.class), mockPermissionService);
        final DashboardState filteredState = permissionManager.filterGadgets(state, user);

        assertEquals(state, filteredState);
    }

    @Test
    public void testExtractModuleKey()
    {
        JiraGadgetPermissionManager permissionManager = new JiraGadgetPermissionManager(null, null, null);
        String key = permissionManager.extractModuleKey("http://localhost:8090/jira/rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml");
        assertEquals("com.atlassian.jira.gadgets:admin-gadget", key);
        key = permissionManager.extractModuleKey("rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml");
        assertEquals("com.atlassian.jira.gadgets:admin-gadget", key);
        key = permissionManager.extractModuleKey("/rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml");
        assertEquals("com.atlassian.jira.gadgets:admin-gadget", key);
        key = permissionManager.extractModuleKey("rest/gadgets/1.0/g/some/gadgets/admin-gadget.xml");
        assertNull(key);
        key = permissionManager.extractModuleKey("rest/gadgets/1.0/g/admin-gadget.xml");
        assertNull(key);
    }

    private GadgetState createGadgetStateAndAddToTranslationService (String id, String url, Key gadgetSpecKey)
    {
        GadgetState gadgetState = GadgetState.gadget(GadgetId.valueOf(id)).specUri(URI.create(url)).build();
        when(gadgetLocationTranslator.translate(URI.create(url))).thenReturn(URI.create(url));
        return gadgetState;
    }
}
