/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.jira.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.NodeAssociationStoreImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.DefaultPermissionSchemeManager;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.plugin.ProjectPermissionTypesManager;
import com.atlassian.jira.security.type.GroupCF;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.security.util.GroupSelectorUtils;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserLocaleStore;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean.MockI18nBeanFactory;

public class TestPermissions
{

    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    private final NodeAssociationStore nodeAssociationStore = new NodeAssociationStoreImpl(ofBizDelegator);

    @AvailableInContainer
    private final UserLocaleStore userLocaleStore = new MockUserLocaleStore();

    @AvailableInContainer
    private final I18nHelper.BeanFactory i18nHelperBeanFactory = new MockI18nBeanFactory();

    @Mock
    @AvailableInContainer
    private GlobalPermissionManager globalPermissionManager;

    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;

    @Mock
    private ProjectPermissionTypesManager projectPermissionTypesManager;

    private final PermissionTypeManager permissionTypeManager = mock(PermissionTypeManager.class);

    @AvailableInContainer(interfaceClass = PermissionSchemeManager.class)
    private final PermissionSchemeManager permissionSchemeManager = new DefaultPermissionSchemeManager(mock(ProjectManager.class),
            permissionTypeManager, mock(PermissionContextFactory.class), ofBizDelegator, mock(SchemeFactory.class), nodeAssociationStore,
            mock(GroupManager.class), mock(EventPublisher.class), new MemoryCacheManager());

    private PermissionManager testedObject;

    private final User user = new MockUser("misc-user");
    private MockGenericValue defaultScheme;
    private GenericValue schemeEntity;
    private GenericValue project;

    @Before
    public void testUp() throws Exception
    {
        testedObject = new DefaultPermissionManager(projectPermissionTypesManager);

        project = UtilsForTests.getTestEntity("Project", Collections.singletonMap("name", "Project"));

        final SecurityType groupSecurityType = new GroupCF(mock(JiraAuthenticationContext.class), mock(GroupSelectorUtils.class),
                mock(CustomFieldManager.class), mock(GroupManager.class));
        when(permissionTypeManager.getTypes()).thenReturn(Collections.singletonMap("group", groupSecurityType));

        when(globalPermissionManager.isGlobalPermission(Mockito.anyInt())).thenReturn(Boolean.FALSE);
        when(projectManager.getProjects()).thenReturn(Arrays.asList(project));

        defaultScheme = (MockGenericValue) permissionSchemeManager.createDefaultScheme();
        permissionSchemeManager.addDefaultSchemeToProject(project);
        final SchemeEntity schemePermission = new SchemeEntity("group", "fake group", (long) Permissions.BROWSE);
        schemeEntity = permissionSchemeManager.createSchemeEntity(defaultScheme, schemePermission);
        defaultScheme.setRelated("ChildSchemePermissions", Arrays.asList(schemeEntity));
    }

    /**
     * Test that if i user the PermissionsManager when it has a scheme that i can retrieve the permission and that when i remove it it is
     * removed
     */
    @Test
    public void testSchemePermissionNotStoredInGlobalPermissionsCache() throws Exception
    {
        when(projectPermissionTypesManager.exists(ProjectPermissions.BROWSE_PROJECTS)).thenReturn(true);

        // Ask PermissionManager if i has a project that i can view (should have one)
        Collection<GenericValue> projects = testedObject.getProjects(Permissions.BROWSE, user);
        assertEquals(Arrays.asList(project), projects);

        // Remove the browse permissions
        permissionSchemeManager.deleteEntity(schemeEntity.getLong("id"));
        defaultScheme.setRelated("ChildSchemePermissions", Collections.emptyList());

        // Ask PermissionManager if i has a project that i can view (shouldn't have one)
        projects = testedObject.getProjects(Permissions.BROWSE, user);
        assertEquals(Collections.emptyList(), projects);
    }

}
