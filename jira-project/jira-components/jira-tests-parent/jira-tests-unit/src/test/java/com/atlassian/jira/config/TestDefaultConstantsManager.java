/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.exception.StoreException;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.resolution.ResolutionImpl;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.StatusImpl;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class TestDefaultConstantsManager
{
    private ConstantsManager defaultConstantsManager;

    @AvailableInContainer
    private MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();

    @Mock
    @AvailableInContainer
    private SubTaskManager subTaskManager;

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        defaultConstantsManager = new DefaultConstantsManager(null, mockOfBizDelegator, new MockIssueConstantFactory(), new MemoryCacheManager());
    }

    protected static GenericValue testGetGenericOperations(String entityName, Supplier<Collection<GenericValue>> entitySupplier, Runnable entityRefresher)
    {
        return testGetGenericOperations(entityName, entitySupplier, entityRefresher, "dummyIssueType");
    }

    protected static GenericValue testGetGenericOperations(String entityName, Supplier<Collection<GenericValue>> entitySupplier, Runnable entityRefresher, String issueType)
    {
        GenericValue value1 = UtilsForTests.getTestEntity(entityName, ImmutableMap.of("id", 1L, "name", "High", "sequence", 1L, "style", issueType));
        GenericValue value2 = UtilsForTests.getTestEntity(entityName, ImmutableMap.of("id", 2L, "name", "Low", "sequence", 2L, "style", issueType));
        assertThat("Manager should contain all registered values (cache is empty)", entitySupplier.get(), Matchers.containsInAnyOrder(value1, value2));

        GenericValue value3 = UtilsForTests.getTestEntity(entityName, ImmutableMap.of("id", 3L, "name", "Another", "sequence", 3L, "style", issueType));
        assertThat("Manager should contain only cached values", entitySupplier.get(), Matchers.containsInAnyOrder(value1, value2));

        entityRefresher.run();

        assertThat("Manager should contain all registered values (cache is refreshed)", entitySupplier.get(), Matchers.containsInAnyOrder(value1, value2, value3));

        return value1;
    }

    @Test
    public void testGetPriorities()
    {

        final Supplier<Collection<GenericValue>> prioritiesSupplier = new Supplier<Collection<GenericValue>>()
        {
            @Override
            public Collection<GenericValue> get()
            {
                return defaultConstantsManager.getPriorities();
            }
        };
        final Runnable prioritiesRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                defaultConstantsManager.refreshPriorities();
            }
        };
        GenericValue priority1 = testGetGenericOperations("Priority", prioritiesSupplier, prioritiesRefresher);

        assertEquals(priority1, defaultConstantsManager.getPriorityObject("1").getGenericValue());
    }

    @Test
    public void testGetResolutions()
    {

        final Supplier<Collection<GenericValue>> resolutionsSupplier = new Supplier<Collection<GenericValue>>()
        {
            @Override
            public Collection<GenericValue> get()
            {
                return defaultConstantsManager.getResolutions();
            }
        };
        final Runnable prioritiesRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                defaultConstantsManager.refreshResolutions();
            }
        };
        GenericValue resolution1 = testGetGenericOperations("Resolution", resolutionsSupplier, prioritiesRefresher);

        assertEquals(resolution1, defaultConstantsManager.getResolutionObject("1").getGenericValue());
    }

    @Test
    public void testGetIssueTypes()
    {

        final Supplier<Collection<GenericValue>> issueTypesSupplier = new Supplier<Collection<GenericValue>>()
        {
            @Override
            public Collection<GenericValue> get()
            {
                return defaultConstantsManager.getIssueTypes();
            }
        };
        final Runnable issueTypesRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                defaultConstantsManager.refreshIssueTypes();
            }
        };

        when(subTaskManager.isSubTaskIssueType(any(GenericValue.class))).thenReturn(false);

        GenericValue issueType1 = testGetGenericOperations("IssueType", issueTypesSupplier, issueTypesRefresher);
        assertThat(defaultConstantsManager.getSubTaskIssueTypes(), Matchers.<GenericValue>empty());

        assertEquals(issueType1, defaultConstantsManager.getIssueTypeObject("1").getGenericValue());
    }


    @Test
    public void testGetConstantsByNameIgnoresCase()
    {
        GenericValue gv = UtilsForTests.getTestEntity("Resolution", ImmutableMap.of("id", "1", "name", "High"));
        Resolution resolution = new ResolutionImpl(gv, null, null, null);

        assertEquals(resolution, defaultConstantsManager.getConstantByNameIgnoreCase("Resolution", "hiGH"));
    }

    @Test
    public void testGetStatusByName()
    {
        GenericValue gv1 = UtilsForTests.getTestEntity("Status", ImmutableMap.of("id", "7", "name", "TestStatus_1"));
        Status status1 = new StatusImpl(gv1, null, null, null, null);

        assertThat(defaultConstantsManager.getStatusByName("TestStatus_1"), is(status1));
    }

    @Test
    public void testGetStatusByNameIgnoreCase()
    {
        GenericValue gv1 = UtilsForTests.getTestEntity("Status", ImmutableMap.of("id", "7", "name", "TestStatus_1"));
        Status status1 = new StatusImpl(gv1, null, null, null, null);

        assertThat(defaultConstantsManager.getStatusByNameIgnoreCase("TestStatus_1"), is(status1));
        assertThat(defaultConstantsManager.getStatusByNameIgnoreCase("tEststatus_1"), is(status1));
    }

    @Test
    public void testStoreIssueTypes() throws StoreException
    {
        GenericValue val1 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 1L, "name", "High", "sequence", 1L, "style", "dummyIssueType"));
        GenericValue val2 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 2L, "name", "Medium", "sequence", 2L, "style", "dummyIssueType"));
        GenericValue val3 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 3L, "name", "Low", "sequence", 3L, "style", "dummyIssueType"));

        final List list = ImmutableList.of(val1, val2, val3);

        final String name = "Very High";
        final String name2 = "Very Medium";
        final String name3 = "Very Low";
        val1.set("name", name);
        val2.set("name", name2);
        val3.set("name", name3);

        defaultConstantsManager.storeIssueTypes(list);

        final Collection issueTypes = defaultConstantsManager.getIssueTypes();
        assertEquals(3, issueTypes.size());
        final Iterator iterator = issueTypes.iterator();
        GenericValue subTaskIssueType = (GenericValue) iterator.next();
        assertEquals(name, subTaskIssueType.getString("name"));
        subTaskIssueType = (GenericValue) iterator.next();
        assertEquals(name3, subTaskIssueType.getString("name"));
        subTaskIssueType = (GenericValue) iterator.next();
        assertEquals(name2, subTaskIssueType.getString("name"));
    }

    @Test
    public void testUpdateIssueTypeNullId() throws StoreException, GenericEntityException
    {
        String id = null;
        try
        {
            defaultConstantsManager.updateIssueType(id, null, null, null, null, (String)null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Id cannot be null.", e.getMessage());
        }
    }

    @Test
    public void testUpdateIssueTypeIssueTypeDoesNotExist() throws StoreException, GenericEntityException
    {
        String id = "1";
        try
        {
            defaultConstantsManager.updateIssueType(id, null, null, null, null, (String)null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Issue Type with id '" + id + "' does not exist.", e.getMessage());
        }
    }

    @Test
    public void testUpdateIssueType() throws StoreException, GenericEntityException
    {
        String id = "1";
        String name = "issue type name";
        Long sequence = 1L;
        String description = "issue type description";
        String iconurl = "issue type icon url";
        String style = "issue type style";

        mockOfBizDelegator.createValue("IssueType", ImmutableMap.<String, Object>of("id", id));
        // update the issue via the ConstantsManager
        defaultConstantsManager.updateIssueType(id, name, sequence, style, description, iconurl);

        final List issueTypes = mockOfBizDelegator.findByAnd("IssueType", ImmutableMap.of("id", id));

        assertEquals(1, issueTypes.size());
        GenericValue issueTypeGV = (GenericValue) issueTypes.get(0);
        assertEquals(name, issueTypeGV.getString("name"));
        assertEquals(sequence, issueTypeGV.getLong("sequence"));
        assertEquals(description, issueTypeGV.getString("description"));
        assertEquals(iconurl, issueTypeGV.getString("iconurl"));
        assertEquals(style, issueTypeGV.getString("style"));
    }

    @Test
    public void shouldUpdateIssueTypeSetAvatarId() throws StoreException, GenericEntityException
    {
        String id = "1";
        String name = "issue type name";
        Long sequence = 1L;
        String description = "issue type description";
        Long avatarId = 123l;
        String style = "issue type style";

        mockOfBizDelegator.createValue("IssueType", ImmutableMap.<String, Object>of("id", id));
        // update the issue via the ConstantsManager
        defaultConstantsManager.updateIssueType(id, name, sequence, style, description, avatarId);

        final List issueTypes = mockOfBizDelegator.findByAnd("IssueType", ImmutableMap.of("id", id));

        assertEquals(1, issueTypes.size());
        GenericValue issueTypeGV = (GenericValue) issueTypes.get(0);
        assertEquals(name, issueTypeGV.getString("name"));
        assertEquals(sequence, issueTypeGV.getLong("sequence"));
        assertEquals(description, issueTypeGV.getString("description"));
        assertEquals(String.valueOf(avatarId), issueTypeGV.getString(IssueTypeImpl.AVATAR_FIELD));
        assertEquals(style, issueTypeGV.getString("style"));
    }

    @Test
    public void testGetSubTaskIssueTypes()
    {
        final Supplier<Collection<GenericValue>> issueTypesSupplier = new Supplier<Collection<GenericValue>>()
        {
            @Override
            public Collection<GenericValue> get()
            {
                return defaultConstantsManager.getSubTaskIssueTypes();
            }
        };
        final Runnable issueTypesRefresher = new Runnable()
        {
            @Override
            public void run()
            {
                defaultConstantsManager.refreshIssueTypes();
            }
        };

        when(subTaskManager.isSubTaskIssueType(any(GenericValue.class))).thenReturn(true);

        GenericValue issueType1 = testGetGenericOperations("IssueType", issueTypesSupplier, issueTypesRefresher);
        assertThat(defaultConstantsManager.getIssueTypes(), Matchers.<GenericValue>empty());

        assertEquals(issueType1, defaultConstantsManager.getIssueTypeObject("1").getGenericValue());
    }

    @Test
    public void testGetMixedSubtaskAndRegularIssueTypes()
    {
        GenericValue val1 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 1L, "name", "High", "sequence", 1L, "style", "Bug"));
        GenericValue val2 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 2L, "name", "Medium", "sequence", 2L, "style", "Bug"));
        GenericValue val3 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 3L, "name", "Low", "sequence", 3L, "style", "Subtask"));
        when(subTaskManager.isSubTaskIssueType(val1)).thenReturn(false);
        when(subTaskManager.isSubTaskIssueType(val2)).thenReturn(false);
        when(subTaskManager.isSubTaskIssueType(val3)).thenReturn(true);

        assertThat("Manager should contain all registered values (cache is refreshed)", defaultConstantsManager.getIssueTypes(), Matchers.containsInAnyOrder(val1, val2));
        assertThat("Manager should contain all registered values (cache is refreshed)", defaultConstantsManager.getSubTaskIssueTypes(), Matchers.containsInAnyOrder(val3));
    }

    @Test
    public void testExpandIssueTypeIds()
    {
        GenericValue it1 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 11L, "name", "High", "sequence", 1L, "style", "Bug"));
        GenericValue it2 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 12L, "name", "Medium", "sequence", 2L, "style", "Subtask"));
        GenericValue it3 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 13L, "name", "Low", "sequence", 3L, "style", "Subtask"));
        GenericValue it4 = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", 14L, "name", "Lowest", "sequence", 4L, "style", "Bug"));

        when(subTaskManager.isSubTaskIssueType(it1)).thenReturn(false);
        when(subTaskManager.isSubTaskIssueType(it2)).thenReturn(true);
        when(subTaskManager.isSubTaskIssueType(it3)).thenReturn(true);
        when(subTaskManager.isSubTaskIssueType(it4)).thenReturn(false);

        List expectedIds = ImmutableList.of("1", "2", "3");
        assertEquals(expectedIds, defaultConstantsManager.expandIssueTypeIds(expectedIds));

        final List<String> randomIdsAndStandard = ImmutableList.of("1", "2", ConstantsManager.ALL_STANDARD_ISSUE_TYPES, "4");
        assertThat(defaultConstantsManager.expandIssueTypeIds(randomIdsAndStandard), Matchers.containsInAnyOrder("11", "14"));

        final List<String> randomIdsAndSubtask = ImmutableList.of("34", "12", ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES, "41");
        assertThat(defaultConstantsManager.expandIssueTypeIds(randomIdsAndSubtask), Matchers.containsInAnyOrder("12", "13"));

        final List<String> randomIdsAndAll = ImmutableList.of("34", "12", ConstantsManager.ALL_ISSUE_TYPES, "41");
        assertThat(defaultConstantsManager.expandIssueTypeIds(randomIdsAndAll), Matchers.containsInAnyOrder("11", "12", "13", "14"));

    }

}
