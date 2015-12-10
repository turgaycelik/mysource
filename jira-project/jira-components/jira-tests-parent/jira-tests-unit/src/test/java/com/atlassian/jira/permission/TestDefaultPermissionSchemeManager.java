package com.atlassian.jira.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.event.permission.PermissionSchemeDeletedEvent;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.user.MockUser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.permission.DefaultPermissionSchemeManager.SCHEME_ENTITY_NAME;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the DefaultPermissionSchemeManager class.
 *
 * @since v3.12
 */
@SuppressWarnings("deprecation")
public class TestDefaultPermissionSchemeManager
{
    private static final List<GenericValue> NO_SCHEMES = emptyList();

    @Mock
    private EventPublisher mockEventPublisher;
    @Mock private GroupManager mockGroupManager;
    @Mock private NodeAssociationStore mockNodeAssociationStore;
    @Mock private OfBizDelegator mockOfBizDelegator;
    @Mock private PermissionContextFactory mockPermissionContextFactory;
    @Mock private PermissionTypeManager mockPermissionTypeManager;
    @Mock private ProjectManager mockProjectManager;
    @Mock private SchemeFactory mockSchemeFactory;

    private PermissionSchemeManager permissionSchemeManager;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init().addMock(OfBizDelegator.class, mockOfBizDelegator);
        permissionSchemeManager = new DefaultPermissionSchemeManager(
                mockProjectManager, mockPermissionTypeManager, mockPermissionContextFactory, mockOfBizDelegator,
                mockSchemeFactory, mockNodeAssociationStore, mockGroupManager, mockEventPublisher, new MemoryCacheManager());

    }

    @Test
    public void getSchemeShouldReturnNullWhenSchemeIdIsUnknown() throws Exception
    {
        // Set up
        final long unknownId = Long.MAX_VALUE;
        when(mockOfBizDelegator.findById(SCHEME_ENTITY_NAME, unknownId)).thenReturn(null);

        // Invoke and check
        assertNull(permissionSchemeManager.getScheme(unknownId));
    }

    @Test
    public void getSchemeShouldReturnSchemeWhenItsIdIsKnown() throws Exception
    {
        // Set up
        final long knownId = Long.MAX_VALUE;
        final GenericValue mockScheme = mock(GenericValue.class);
        when(mockOfBizDelegator.findById(SCHEME_ENTITY_NAME, knownId)).thenReturn(mockScheme);

        // Invoke and check
        assertSame(mockScheme, permissionSchemeManager.getScheme(knownId));
    }

    @Test
    public void gettingSchemeByNameShouldReturnNullWhenMatchingSchemesListIsNull() throws Exception
    {
        // Set up
        final String schemeName = "MyScheme";
        when(mockOfBizDelegator.findByAnd(SCHEME_ENTITY_NAME, singletonMap("name", schemeName))).thenReturn(null);

        // Invoke and check
        assertNull(permissionSchemeManager.getScheme(schemeName));
    }

    @Test
    public void gettingSchemeByNameShouldReturnNullWhenMatchingSchemesListIsEmpty() throws Exception
    {
        // Set up
        final String schemeName = "MyScheme";
        when(mockOfBizDelegator.findByAnd(SCHEME_ENTITY_NAME, singletonMap("name", schemeName))).thenReturn(NO_SCHEMES);

        // Invoke and check
        assertNull(permissionSchemeManager.getScheme(schemeName));
    }

    @Test
    public void gettingSchemeByNameShouldReturnThatScheme() throws Exception
    {
        // Set up
        final String schemeName = "MyScheme";
        final GenericValue mockScheme = mock(GenericValue.class);
        final List<GenericValue> schemes = Collections.singletonList(mockScheme);
        when(mockOfBizDelegator.findByAnd(SCHEME_ENTITY_NAME, singletonMap("name", schemeName))).thenReturn(schemes);

        // Invoke and check
        assertEquals(mockScheme, permissionSchemeManager.getScheme(schemeName));
    }

    @Test
    public void schemeShouldNotBeReportedToExistWhenItDoesNot() throws Exception
    {
        // Set up
        final String schemeName = "MyScheme";
        when(mockOfBizDelegator.findByAnd(SCHEME_ENTITY_NAME, singletonMap("name", schemeName))).thenReturn(NO_SCHEMES);

        // Invoke and check
        assertFalse("Scheme should not exist", permissionSchemeManager.schemeExists(schemeName));
    }

    @Test
    public void schemeShouldBeReportedToExistWhenItDoes() throws Exception
    {
        // Set up
        final String schemeName = "MyScheme";
        final GenericValue mockScheme = mock(GenericValue.class);
        final List<GenericValue> schemes = Collections.singletonList(mockScheme);
        when(mockOfBizDelegator.findByAnd(SCHEME_ENTITY_NAME, singletonMap("name", schemeName))).thenReturn(schemes);

        // Invoke and check
        assertTrue("Scheme should exist", permissionSchemeManager.schemeExists(schemeName));
    }

    @Test
    public void shouldBeAbleToCreateSchemeThatDoesNotAlreadyExist() throws Exception
    {
        // Set up
        final GenericValue mockScheme = mock(GenericValue.class);
        final String schemeName = "This Name";
        final String schemeDescription = "Description";
        final ImmutableMap<String, Object> expectedFields =
                ImmutableMap.<String, Object>of("name", schemeName, "description", schemeDescription);
        when(mockOfBizDelegator.createValue(SCHEME_ENTITY_NAME, expectedFields)).thenReturn(mockScheme);

        // Invoke
        final GenericValue createdScheme = permissionSchemeManager.createScheme(schemeName, schemeDescription);

        // Check
        assertSame(mockScheme, createdScheme);
    }

    @Test(expected = GenericEntityException.class)
    public void shouldNotBeAbleToCreateSchemeWithSameNameAsExistingScheme() throws Exception
    {
        // Set up
        final String schemeName = "someScheme";
        final GenericValue existingScheme = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd(SCHEME_ENTITY_NAME, ImmutableMap.of("name", schemeName)))
                .thenReturn(singletonList(existingScheme));

        // Invoke
        permissionSchemeManager.createScheme(schemeName, "AnyDescription");
    }

    @Test
    public void shouldBeAbleToUpdateScheme() throws GenericEntityException
    {
        // Set up
        final GenericValue mockScheme = mock(GenericValue.class);

        // Invoke
        permissionSchemeManager.updateScheme(mockScheme);

        // Check
        verify(mockScheme).store();
    }

    @Test
    public void shouldBeAbleToDeleteScheme() throws GenericEntityException
    {
        // Set up
        final long schemeId = 123;
        final GenericValue mockScheme = mock(GenericValue.class);
        when(mockOfBizDelegator.findById(SCHEME_ENTITY_NAME, schemeId)).thenReturn(mockScheme);
        when(mockOfBizDelegator.findByAnd(eq(SCHEME_ENTITY_NAME), anyMap())).thenReturn(ImmutableList.of(mockScheme));
        when(mockSchemeFactory.getScheme(mockScheme)).thenReturn(mock(Scheme.class));

        // Invoke
        permissionSchemeManager.deleteScheme(schemeId);

        // Check
        verify(mockNodeAssociationStore).removeAssociationsFromSink(mockScheme);
        verify(mockOfBizDelegator).removeRelated("ChildSchemePermissions", mockScheme);
        verify(mockOfBizDelegator).removeValue(mockScheme);
        final ArgumentCaptor<PermissionSchemeDeletedEvent> eventCaptor =
                ArgumentCaptor.forClass(PermissionSchemeDeletedEvent.class);
        verify(mockEventPublisher).publish(eventCaptor.capture());
        assertEquals(schemeId, eventCaptor.getValue().getId().longValue());
    }

    @Test
    public void testHasSchemePermissionWithUserLegacy() throws GenericEntityException
    {
        // Set up
        final User user = new MockUser("John");

        final SecurityType schemeType = mock(SecurityType.class);
        when(schemeType.isValidForPermission(ProjectPermissions.BROWSE_PROJECTS)).thenReturn(false);
        when(schemeType.isValidForPermission(ProjectPermissions.ADMINISTER_PROJECTS)).thenReturn(true);
        when(schemeType.hasPermission((GenericValue) null, null, user, false)).thenReturn(true);

        final Map<String, SecurityType> types = singletonMap("typename", schemeType);

        final PermissionTypeManager mockPermissionTypeManager = mock(PermissionTypeManager.class);
        when(mockPermissionTypeManager.getTypes()).thenReturn(types);

        final GenericValue permission =
                new MockGenericValue("permission", EasyMap.build("type", "typename", "parameter", null));
        final DefaultPermissionSchemeManager defaultPermissionSchemeManager =
                createDefaultPermissionSchemeManager(mockPermissionTypeManager, singletonList(permission));

        assertFalse(defaultPermissionSchemeManager.hasSchemePermission((long) Permissions.BROWSE, null, null, user, false));
        assertTrue(defaultPermissionSchemeManager.hasSchemePermission((long) Permissions.PROJECT_ADMIN, null, null, user, false));
    }

    @Test
    public void testHasSchemePermissionWithUser() throws GenericEntityException
    {
        // Set up
        final User user = new MockUser("John");

        final SecurityType schemeType = mock(SecurityType.class);
        when(schemeType.isValidForPermission(ProjectPermissions.BROWSE_PROJECTS)).thenReturn(false);
        when(schemeType.isValidForPermission(ProjectPermissions.ADMINISTER_PROJECTS)).thenReturn(true);
        when(schemeType.hasPermission((GenericValue) null, null, user, false)).thenReturn(true);

        final Map<String, SecurityType> types = singletonMap("typename", schemeType);

        final PermissionTypeManager mockPermissionTypeManager = mock(PermissionTypeManager.class);
        when(mockPermissionTypeManager.getTypes()).thenReturn(types);

        final GenericValue permission =
                new MockGenericValue("permission", EasyMap.build("type", "typename", "parameter", null));
        final DefaultPermissionSchemeManager defaultPermissionSchemeManager =
                createDefaultPermissionSchemeManager(mockPermissionTypeManager, singletonList(permission));

        assertFalse(defaultPermissionSchemeManager.hasSchemePermission(ProjectPermissions.BROWSE_PROJECTS, null, null, user, false));
        assertTrue(defaultPermissionSchemeManager.hasSchemePermission(ProjectPermissions.ADMINISTER_PROJECTS, null, null, user, false));
    }

    @Test
    public void testHasSchemePermissionWithUserAndNullSecurityTypesLegacy() throws GenericEntityException
    {
        // Set up
        final User user = new MockUser("John");

        final Map<String, SecurityType> types = singletonMap("typename", null);

        final PermissionTypeManager mockPermissionTypeManager = mock(PermissionTypeManager.class);
        when(mockPermissionTypeManager.getTypes()).thenReturn(types);

        final GenericValue permission = new MockGenericValue("permission", EasyMap.build("type", "typename", "parameter", null));
        final DefaultPermissionSchemeManager defaultPermissionSchemeManager = createDefaultPermissionSchemeManager(mockPermissionTypeManager, singletonList(permission));

        assertFalse(defaultPermissionSchemeManager.hasSchemePermission((long) Permissions.BROWSE, null, null, user, false));
        assertFalse(defaultPermissionSchemeManager.hasSchemePermission((long) Permissions.PROJECT_ADMIN, null, null, user, false));
    }

    @Test
    public void testHasSchemePermissionWithUserAndNullSecurityTypes() throws GenericEntityException
    {
        // Set up
        final User user = new MockUser("John");

        final Map<String, SecurityType> types = singletonMap("typename", null);

        final PermissionTypeManager mockPermissionTypeManager = mock(PermissionTypeManager.class);
        when(mockPermissionTypeManager.getTypes()).thenReturn(types);

        final GenericValue permission = new MockGenericValue("permission", EasyMap.build("type", "typename", "parameter", null));
        final DefaultPermissionSchemeManager defaultPermissionSchemeManager = createDefaultPermissionSchemeManager(mockPermissionTypeManager, singletonList(permission));

        assertFalse(defaultPermissionSchemeManager.hasSchemePermission(ProjectPermissions.BROWSE_PROJECTS, null, null, user, false));
        assertFalse(defaultPermissionSchemeManager.hasSchemePermission(ProjectPermissions.ADMINISTER_PROJECTS, null, null, user, false));
    }

    @Test
    public void testHasSchemePermissionWithAnonymousUserLegacy() throws GenericEntityException
    {
        // type 1 does not give permission
        final SecurityType schemeType1 = mock(SecurityType.class);
        when(schemeType1.hasPermission((GenericValue) null, null)).thenReturn(false);

        // type 2 does give permission
        final SecurityType schemeType2 = mock(SecurityType.class);
        when(schemeType2.hasPermission((GenericValue) null, null)).thenReturn(true);

        final Map<String, SecurityType> types = new HashMap<String, SecurityType>();
        types.put("type1", schemeType1);

        final PermissionTypeManager mockPermissionTypeManager = mock(PermissionTypeManager.class);
        when(mockPermissionTypeManager.getTypes()).thenReturn(types);

        final MockGenericValue permission1Gv =
                new MockGenericValue("permission", EasyMap.build("type", "type1", "parameter", null));
        final List<GenericValue> permissions = new ArrayList<GenericValue>();
        permissions.add(permission1Gv);
        final DefaultPermissionSchemeManager defaultPermissionSchemeManager =
                createDefaultPermissionSchemeManager(mockPermissionTypeManager, permissions);

        assertFalse(defaultPermissionSchemeManager.hasSchemePermission((long) Permissions.PROJECT_ADMIN, null, null, null, false));
        final MockGenericValue permission2Gv =
                new MockGenericValue("permission", EasyMap.build("type", "type2", "parameter", null));
        permissions.add(permission2Gv);
        types.put("type2", schemeType2);
        assertTrue(defaultPermissionSchemeManager.hasSchemePermission((long) Permissions.PROJECT_ADMIN, null, null, null, false));
    }

    @Test
    public void testHasSchemePermissionWithAnonymousUser() throws GenericEntityException
    {
        // type 1 does not give permission
        final SecurityType schemeType1 = mock(SecurityType.class);
        when(schemeType1.hasPermission((GenericValue) null, null)).thenReturn(false);

        // type 2 does give permission
        final SecurityType schemeType2 = mock(SecurityType.class);
        when(schemeType2.hasPermission((GenericValue) null, null)).thenReturn(true);

        final Map<String, SecurityType> types = new HashMap<String, SecurityType>();
        types.put("type1", schemeType1);

        final PermissionTypeManager mockPermissionTypeManager = mock(PermissionTypeManager.class);
        when(mockPermissionTypeManager.getTypes()).thenReturn(types);

        final MockGenericValue permission1Gv =
                new MockGenericValue("permission", EasyMap.build("type", "type1", "parameter", null));
        final List<GenericValue> permissions = new ArrayList<GenericValue>();
        permissions.add(permission1Gv);
        final DefaultPermissionSchemeManager defaultPermissionSchemeManager =
                createDefaultPermissionSchemeManager(mockPermissionTypeManager, permissions);

        assertFalse(defaultPermissionSchemeManager.hasSchemePermission(ProjectPermissions.ADMINISTER_PROJECTS, null, null, null, false));
        final MockGenericValue permission2Gv =
                new MockGenericValue("permission", EasyMap.build("type", "type2", "parameter", null));
        permissions.add(permission2Gv);
        types.put("type2", schemeType2);
        assertTrue(defaultPermissionSchemeManager.hasSchemePermission(ProjectPermissions.ADMINISTER_PROJECTS, null, null, null, false));
    }

    private DefaultPermissionSchemeManager createDefaultPermissionSchemeManager(
            final PermissionTypeManager permissionTypeManager, final List<GenericValue> permissions)
    {
        return new DefaultPermissionSchemeManager(null, permissionTypeManager, null, null, null, null, null, null, new MemoryCacheManager())
        {
            public List<GenericValue> getEntities(GenericValue scheme, String permissionKey)
            {
                return permissions;
            }
        };
    }
}
