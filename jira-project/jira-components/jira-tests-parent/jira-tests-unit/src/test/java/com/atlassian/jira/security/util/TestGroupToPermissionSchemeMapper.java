package com.atlassian.jira.security.util;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.DefaultPermissionSchemeManager;
import com.atlassian.jira.permission.MockProjectPermission;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity;
import static com.atlassian.jira.permission.ProjectPermissions.ADD_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissions.CREATE_ISSUES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TestGroupToPermissionSchemeMapper
{
    private GenericValue schemeA;
    private GenericValue schemeB;
    private GenericValue schemeC;
    private Group groupA;
    private Group groupB;
    private Group groupC;

    @Mock
    private PermissionManager permissionManager;

    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init()
                .addMock(OfBizDelegator.class, new MockOfBizDelegator())
                .addMock(CrowdService.class, new MockCrowdService());
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    private Group createMockGroup(String groupName)
            throws OperationNotPermittedException, InvalidGroupException
    {
        final Group group = new MockGroup(groupName);
        ComponentAccessor.getCrowdService().addGroup(group);
        return group;
    }

    @Test
    public void testMapper() throws GenericEntityException, OperationNotPermittedException, InvalidGroupException
    {
        final PermissionSchemeManager permissionSchemeManager = new MockPermissionSchemeManager();

        // Setup a list of schemes
        schemeA = getTestEntity("PermissionScheme", EasyMap.build("name", "test scheme A"));
        schemeB = getTestEntity("PermissionScheme", EasyMap.build("name", "test scheme B"));
        schemeC = getTestEntity("PermissionScheme", EasyMap.build("name", "test scheme C"));

        // Setup Groups
        groupA = createMockGroup("test group A");
        groupB = createMockGroup("test group B");
        groupC = createMockGroup("test group C");

        when(permissionManager.getAllProjectPermissions()).thenReturn(Arrays.<ProjectPermission>asList(
            new MockProjectPermission(ADD_COMMENTS.permissionKey(), null, null, null),
            new MockProjectPermission(BROWSE_PROJECTS.permissionKey(), null, null, null),
            new MockProjectPermission(CREATE_ISSUES.permissionKey(), null, null, null)
        ));

        final GroupToPermissionSchemeMapper mapper = new GroupToPermissionSchemeMapper(permissionSchemeManager, permissionManager);

        // Ensure the mapper returns the correct results
        Collection permissionSchemes = mapper.getMappedValues(groupA.getName());
        List expectedSchemes = EasyList.build(schemeA);
        assertEquals(expectedSchemes, permissionSchemes);

        permissionSchemes = mapper.getMappedValues(groupB.getName());
        expectedSchemes = EasyList.build(schemeB, schemeC);
        assertEquals(expectedSchemes, permissionSchemes);

        permissionSchemes = mapper.getMappedValues(groupC.getName());
        expectedSchemes = EasyList.build(schemeA, schemeC);
        assertEquals(expectedSchemes, permissionSchemes);

        assertTrue(mapper.getMappedValues("non existant group").isEmpty());
    }

    // Build a mock for the manager so that we can return what we want from the methods called by the
    // GroupToPermissionSchemeMapper
    private class MockPermissionSchemeManager extends DefaultPermissionSchemeManager
    {
        public MockPermissionSchemeManager()
        {
            super(null, null, null, null, null, null, null, null, new MemoryCacheManager());
        }

        public List<GenericValue> getSchemes() 
        {
            return Arrays.asList(schemeA, schemeB, schemeC);
        }

        public List<GenericValue> getEntities(GenericValue scheme, String permissionKey)
        {
            if (schemeA.equals(scheme) && permissionKey.equals(ADD_COMMENTS.permissionKey()))
            {
                return Arrays.asList(
                        getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupA.getName())),
                        getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupC.getName())));
            }
            else if (schemeB.equals(scheme) && permissionKey.equals(BROWSE_PROJECTS.permissionKey()))
            {
                return Arrays.asList(getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupB.getName())));
            }
            else if (schemeC.equals(scheme) && permissionKey.equals(CREATE_ISSUES.permissionKey()))
            {
                // The record of type 'user' should be ignored by the mapper.
                return Arrays.asList(
                        getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupB.getName())),
                        getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupC.getName())),
                        getTestEntity("SchemePermissions", EasyMap.build("type", "user", "parameter", "test user")));
            }
            else
            {
                return Collections.emptyList();
            }
        }
    }
}
