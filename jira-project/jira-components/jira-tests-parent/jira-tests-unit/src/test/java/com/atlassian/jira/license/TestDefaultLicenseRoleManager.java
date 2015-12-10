package com.atlassian.jira.license;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.DeleteContextMatcher;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.plugin.license.LicenseRoleModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

import java.util.Set;

import static com.atlassian.jira.entity.Entity.LICENSE_ROLE_GROUP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultLicenseRoleManager
{
    private static final LicenseRoleId LICENSE_ROLE_ID_1 = new LicenseRoleId("Role 1");
    private static final LicenseRoleId LICENSE_ROLE_ID_2 = new LicenseRoleId("Role 2");
    private static final LicenseRoleId LICENSE_ROLE_ID_3 = new LicenseRoleId("Role 3");

    private static final LicenseRoleDefinition LICENSE_ROLE_DEFINITION_1
            = new MockLicenseRoleDefinition(LICENSE_ROLE_ID_1, "key");
    private static final LicenseRoleDefinition LICENSE_ROLE_DEFINITION_2
            = new MockLicenseRoleDefinition(LICENSE_ROLE_ID_2, "key");

    private static final String GROUP_1 = "Group 1";
    private static final String GROUP_2 = "Group 2";
    private static final String GROUP_3 = "Group 3";

    private static final String ENTITY_NAME = LICENSE_ROLE_GROUP.getEntityName();
    private static final String COL_NAME = "licenseRoleName";
    private static final String COL_GROUP = "groupId";

    @Mock
    private LicenseRoleGroupsCache licenseRoleGroupsCache;
    @Mock
    private EntityEngine entityEngine;
    @Mock
    private PluginAccessor pluginAccessor;

    @Mock
    private LicenseRoleModuleDescriptor moduleDescriptor1;
    @Mock
    private LicenseRoleModuleDescriptor moduleDescriptor2;

    private DefaultLicenseRoleManager defaultLicenseRoleManager;

    @Before
    public void setUp()
    {
        //These are the descriptors and registered.
        when(moduleDescriptor1.getModule()).thenReturn(LICENSE_ROLE_DEFINITION_1);
        when(moduleDescriptor2.getModule()).thenReturn(LICENSE_ROLE_DEFINITION_2);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(LicenseRoleModuleDescriptor.class))
                .thenReturn(ImmutableList.of(moduleDescriptor1, moduleDescriptor2));

        //By default roles don't have assigned groups.
        when(licenseRoleGroupsCache.getGroupsFor(Mockito.any(LicenseRoleId.class)))
                .thenReturn(ImmutableSet.<String>of());

        //Role 1 has two groups.
        when(licenseRoleGroupsCache.getGroupsFor(LICENSE_ROLE_ID_1))
                .thenReturn(ImmutableSet.of(GROUP_1, GROUP_2));

        defaultLicenseRoleManager = new DefaultLicenseRoleManager(licenseRoleGroupsCache, entityEngine, pluginAccessor);
    }

    @Test
    public void getGroupsForReturnsAllGroupsInCacheForThatLicenseRole()
    {
        assertThat(defaultLicenseRoleManager.getGroupsFor(LICENSE_ROLE_ID_1),
                equalTo(ImmutableSet.of(GROUP_1, GROUP_2)));
    }

    @Test
    public void licenseRoleHasGroupReturnsTrueWhenMappingExistsInCache()
    {
        assertThat(defaultLicenseRoleManager.licenseRoleHasGroup(LICENSE_ROLE_ID_1, GROUP_1), is(true));
    }

    @Test
    public void licenseRoleHasGroupReturnsFalseWhenMappingDoesNotExistInCache()
    {
        assertThat(defaultLicenseRoleManager.licenseRoleHasGroup(LICENSE_ROLE_ID_1, GROUP_3), is(false));
    }

    @Test
    public void getDefinedLicenseRolesReturnsLicenseRolesForAllLicenseRoleModuleDescriptors()
    {
        final Set<LicenseRoleDefinition> installedLicenseRoles = defaultLicenseRoleManager.getDefinedLicenseRoles();
        assertThat(installedLicenseRoles.size(), is(equalTo(2)));
        assertThat(installedLicenseRoles, containsInAnyOrder(LICENSE_ROLE_DEFINITION_1, LICENSE_ROLE_DEFINITION_2));
    }

    @Test
    public void getDefinedLicenseRolesReturnsUniqueLicenseRoles()
    {
        when(moduleDescriptor2.getModule()).thenReturn(LICENSE_ROLE_DEFINITION_1);

        final Set<LicenseRoleDefinition> installedLicenseRoles = defaultLicenseRoleManager.getDefinedLicenseRoles();
        assertThat(installedLicenseRoles.size(), is(equalTo(1)));
        assertThat(installedLicenseRoles, containsInAnyOrder(LICENSE_ROLE_DEFINITION_1));
    }

    @Test
    public void isLicenseRoleDefinedReturnsTrueWhenLicenseRoleModuleDescriptorDefined()
    {
        assertThat(defaultLicenseRoleManager.isLicenseRoleDefined(LICENSE_ROLE_ID_2), is(true));
    }

    @Test
    public void isLicenseRoleDefinedReturnsFalseWhenLicenseRoleModuleDescriptorNotDefined()
    {
        assertThat(defaultLicenseRoleManager.isLicenseRoleDefined(LICENSE_ROLE_ID_3), is(false));
    }

    @Test
    public void getLicenseRoleDefinitionReturnsRoleWhenItExists()
    {
        final Optional<LicenseRoleDefinition> definition = defaultLicenseRoleManager
                .getLicenseRoleDefinition(LICENSE_ROLE_ID_2);

        assertThat(definition.isPresent(), is(true));
        assertThat(definition.get(), equalTo(LICENSE_ROLE_DEFINITION_2));
    }

    @Test
    public void getLicenseRoleDefinitionReturnsAbsentWhenItDoesNotExist()
    {
        final Optional<LicenseRoleDefinition> definition = defaultLicenseRoleManager
                .getLicenseRoleDefinition(LICENSE_ROLE_ID_3);
        assertThat(definition.isPresent(), is(false));
    }

    @Test
    public void setGroupsSetsGroupsCorrectlyInTheDatabaseOnAddAndRemove()
    {
        defaultLicenseRoleManager.setGroups(LICENSE_ROLE_ID_1, ImmutableList.of(GROUP_2, GROUP_3, GROUP_3, GROUP_2));

        //Group 1 should be deleted.
        verify(entityEngine).delete(argThat(
                new DeleteContextMatcher()
                        .entity(ENTITY_NAME)
                        .and(COL_NAME, LICENSE_ROLE_ID_1.getName())
                        .and(new EntityExpr(COL_GROUP, EntityOperator.IN, ImmutableSet.of(GROUP_1)))
        ));

        //Group 3 should be added.
        verify(entityEngine).createValue(LICENSE_ROLE_GROUP,
                new LicenseRoleGroupEntry(LICENSE_ROLE_ID_1.getName(), GROUP_3));

        //Group 2 should be left alone.
        verifyNoMoreInteractions(entityEngine);

        //This should only be called once.
        verify(licenseRoleGroupsCache).invalidateCacheEntry(LICENSE_ROLE_ID_1);
    }

    @Test
    public void setGroupsSetsInvalidatesCacheEvenOnError()
    {
        when(entityEngine.delete(Matchers.any(Delete.DeleteWhereContext.class)))
                .thenThrow(new RuntimeException("Database Error"));

        try
        {
            defaultLicenseRoleManager.setGroups(LICENSE_ROLE_ID_1, ImmutableList.of(GROUP_2, GROUP_3));
            fail("Should have got a database error.");
        }
        catch (RuntimeException good)
        {
            //want this to happen.
        }

        //This should only be called once.
        verify(licenseRoleGroupsCache).invalidateCacheEntry(LICENSE_ROLE_ID_1);
    }

    @Test
    public void setGroupsSetsGroupsCorrectlyInTheDatabaseOnAddOnly()
    {
        defaultLicenseRoleManager.setGroups(LICENSE_ROLE_ID_1, ImmutableList.of(GROUP_1, GROUP_2, GROUP_3));

        //Group 3 should be added.
        verify(entityEngine).createValue(LICENSE_ROLE_GROUP,
                new LicenseRoleGroupEntry(LICENSE_ROLE_ID_1.getName(), GROUP_3));

        //Group 2 & Group 1 should be left alone.
        verifyNoMoreInteractions(entityEngine);

        //This should only be called once.
        verify(licenseRoleGroupsCache).invalidateCacheEntry(LICENSE_ROLE_ID_1);
    }

    @Test
    public void setGroupsSetsGroupsCorrectlyInTheDatabaseOnRemoveOnly()
    {
        defaultLicenseRoleManager.setGroups(LICENSE_ROLE_ID_1, ImmutableList.<String>of());

        //Group 1 should be deleted.
        verify(entityEngine).delete(argThat(
                new DeleteContextMatcher()
                        .entity(ENTITY_NAME)
                        .and(COL_NAME, LICENSE_ROLE_ID_1.getName())
                        .and(new EntityExpr(COL_GROUP, EntityOperator.IN, ImmutableSet.of(GROUP_1, GROUP_2)))
        ));

        //This should only be called once.
        verify(licenseRoleGroupsCache).invalidateCacheEntry(LICENSE_ROLE_ID_1);
    }

    @Test
    public void setGroupsSetsGroupsDoesNotInvalidate()
    {
        defaultLicenseRoleManager.setGroups(LICENSE_ROLE_ID_1, ImmutableList.of(GROUP_1, GROUP_2));

        //Groups 1 && Group 2 should be left alone.
        verifyNoMoreInteractions(entityEngine);

        //This should not be called.
        verify(licenseRoleGroupsCache, never()).invalidateCacheEntry(LICENSE_ROLE_ID_1);
    }
}
