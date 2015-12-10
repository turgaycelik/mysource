package com.atlassian.jira.bc.project.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import static org.mockito.Mockito.when;

public class TestDefaultProjectComponentManagerWithOfBizStore
{
    private ProjectManager projectManager;
    protected ProjectComponentManagerTester tester;
    GenericValue mockComponent = createMockGenericValue(1000, "test 1", "just a test", null, new Long(1), AssigneeTypes.PROJECT_DEFAULT);
    // Init delegator that will have one component stored on test completion
    protected MockOfBizDelegator singleComponentOfBizDelegator = new MockOfBizDelegator(null, Collections.singletonList(mockComponent));

    @Before
    public void setup()
    {
        projectManager = Mockito.mock(ProjectManager.class);
        final MockProject projecta = new MockProject(ProjectComponentManagerTester.PROJECT_ID_STORED, "PRA");
        final MockProject projectb = new MockProject(ProjectComponentManagerTester.MY_PROJECT_ID_STORED, "PRB");
        final List<Project> projects = Arrays.asList(new Project[] { projecta, projectb });
        when(projectManager.getProjectObjects()).thenReturn(projects);
    }

    protected void tearDown() throws Exception
    {
        tester = null;
    }

    @Test
    public void testCreateSuccess()
    {
        MockGenericValue v1 = createMockGenericValue(1001, ProjectComponentManagerTester.UNIQUE_COMPONENT_NAME, null, null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        // Delegator is expected to return a component
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(mockComponent, v1));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testCreateSuccess();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testFindAllForProject()
    {
        MockGenericValue v1 = createMockGenericValue(1001, "pc1", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1002, "pc2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1003, "pc3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(mockComponent, v1, v2, v3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testFindAllForProject1();
        ofBizDelegator.verifyAll();

        ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(mockComponent, v1, v2, v3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testFindAllForProject2();
        ofBizDelegator.verifyAll();

        v1 = createMockGenericValue(1001, "pc1", "ptest1", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        v2 = createMockGenericValue(1002, "pc2", "ptest2", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        v3 = createMockGenericValue(1003, "pc3", "ptest3", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my1 = createMockGenericValue(1004, "my1", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my2 = createMockGenericValue(1005, "my2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my3 = createMockGenericValue(1006, "my3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(mockComponent, v1, v3, my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testFindAllForProject3();
        ofBizDelegator.verifyAll();

        ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testFindAllForProject4();
        ofBizDelegator.verifyAll();

        ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testFindAllForProject5();
        ofBizDelegator.verifyAll();

    }

    @Test
    public void testFindAll()
    {
        MockGenericValue v1 = createMockGenericValue(1001, "pc1", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1002, "pc2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1003, "pc3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my1 = createMockGenericValue(1004, "my1", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my2 = createMockGenericValue(1005, "my2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my3 = createMockGenericValue(1006, "my3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(mockComponent, v1, v2, v3, my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testFindAll();
    }

    @Test
    public void testCreateAndDelete()
    {
        MockGenericValue v1 = createMockGenericValue(1007, "pc1", "ptest1", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1008, "pc2", "ptest2", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1009, "pc3", "ptest3", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my1 = createMockGenericValue(1010, "my1", "test1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my2 = createMockGenericValue(1011, "my2", "test2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my3 = createMockGenericValue(1012, "my3", "test3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(v1, v2, v3, my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testCreateAndDelete();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testFindByComponentName() throws Exception
    {
        MockGenericValue v1 = createMockGenericValue(1001, "name", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1002, "pc2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1003, "pc3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my1 = createMockGenericValue(1004, "name", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my2 = createMockGenericValue(1005, "my2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my3 = createMockGenericValue(1006, "my3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(mockComponent, v1, v2, v3, my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testFindByComponentName();
    }

    @Test
    public void testUpdate()
    {
        MockGenericValue v1 = createMockGenericValue(1000, "test 2", "another test", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, Collections.singletonList(v1));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testUpdateSingleComponent();
        ofBizDelegator.verifyAll();

        tester = new ProjectComponentManagerTester(createStore(singleComponentOfBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testUpdateNonPersisted();
        singleComponentOfBizDelegator.verifyAll();

        v1 = createMockGenericValue(1001, "c1", "test1", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1002, "c2", "test2", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1003, "noname", "test3", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        ofBizDelegator = new MockOfBizDelegator(null, Lists.newArrayList(mockComponent, v1, v2, v3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testUpdateIsConsistent();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testDelete()
    {
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testDelete();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testDeleteAndUpdate()
    {
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));
        tester.testDeleteAndUpdate();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testFindAllUniqueNames()
    {
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));

        tester.findAllUniqueNamesForProjects();
    }

    @Test
    public void testFindAllUniqueNamesForProjects()
    {
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator, new MemoryCacheManager(), projectManager));

        tester.testFindAllUniqueNamesForProjects();
    }


    private MockGenericValue createMockGenericValue(int id, String name, String description, String lead, Long projectId, long assigneeType)
    {
        return new MockGenericValue("Component",
                FieldMap.build("id", new Long(id), "name", name, "description", description, "lead", lead, "project", projectId).
                        add("assigneetype", new Long(assigneeType)));
    }

    protected ProjectComponentStore createStore(MockOfBizDelegator ofBizDelegator, final CacheManager cacheManager, final ProjectManager projectManager)
    {
        return new OfBizProjectComponentStore(ofBizDelegator);
    }

}
