package com.atlassian.jira.bc.project.component;

import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.project.AssigneeTypes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestOfBizProjectComponentStore
{
    protected ProjectComponentStoreTester tester;
    protected MockOfBizDelegator ofBizDelegator;
    private static final String COMPONENT_DESCRIPTION = "component description";

    @Before
    public void setUp() throws Exception
    {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    @After
    public void tearDown() throws Exception
    {
        UtilsForTestSetup.deleteAllEntities();
    }

    @Test
    public void testFind()
    {
        GenericValue gv1 = createMockGenericValue(1000, "name", "desc", null, ProjectComponentStoreTester.PROJECT_ID_1, AssigneeTypes.PROJECT_DEFAULT);
        ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(gv1));
        tester = new ProjectComponentStoreTester(createStore(ofBizDelegator));
        tester.testFind();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testFindAllForProject()
    {
        GenericValue gv1 = createMockGenericValue(1000, "name1", "desc", null, ProjectComponentStoreTester.PROJECT_ID_2, AssigneeTypes.PROJECT_DEFAULT);
        GenericValue gv2 = createMockGenericValue(1001, "name2", "desc", null, ProjectComponentStoreTester.PROJECT_ID_2, AssigneeTypes.PROJECT_DEFAULT);
        ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(gv1, gv2));
        tester = new ProjectComponentStoreTester(createStore(ofBizDelegator));
        tester.testFindAllForProject();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testFindByComponentName()
    {
        GenericValue gv1 = createMockGenericValue(1000, "name1", "desc", null, ProjectComponentStoreTester.PROJECT_ID_2, AssigneeTypes.PROJECT_DEFAULT);
        GenericValue gv2 = createMockGenericValue(1001, "name", "desc", null, ProjectComponentStoreTester.PROJECT_ID_2, AssigneeTypes.PROJECT_DEFAULT);
        GenericValue gv3 = createMockGenericValue(1002, "Name", "desc", null, ProjectComponentStoreTester.PROJECT_ID_1, AssigneeTypes.PROJECT_DEFAULT);
        ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(gv1, gv2, gv3));
        tester = new ProjectComponentStoreTester(createStore(ofBizDelegator));
        tester.testFindByComponentName();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testFindProjectIdForComponent()
    {
        GenericValue gv1 = createMockGenericValue(1000, "name", "desc", null, ProjectComponentStoreTester.PROJECT_ID_1, AssigneeTypes.PROJECT_DEFAULT);
        ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(gv1));
        tester = new ProjectComponentStoreTester(createStore(ofBizDelegator));
        tester.testFindProjectIdForComponent();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testDelete()
    {
        ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentStoreTester(createStore(ofBizDelegator));
        tester.testDelete();
        ofBizDelegator.verifyAll();
    }

    @Test
    public void testUpdateCommon()
    {
        ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentStoreTester(createStore(ofBizDelegator));
        tester.testUpdate();
        ofBizDelegator.verifyAll();
    }

    /**
     * Test that the components description is never set to empty string when creating new components. JRA-12193
     */
    @Test
    public void testDescriptionIsNeverSetToEmptyStringOnCreate()
    {
        //initially no existing components
        List initialDb = EasyList.build();
        //expected components at end of test
        List expectedDb = EasyList.build(createMockGenericValue(MockOfBizDelegator.STARTING_ID, "name1", COMPONENT_DESCRIPTION, null, ProjectComponentStoreTester.PROJECT_ID_1, 0),
                                         createMockGenericValue(MockOfBizDelegator.STARTING_ID+1, "name2", null /* description */, null, ProjectComponentStoreTester.PROJECT_ID_1, 0),
                                         createMockGenericValue(MockOfBizDelegator.STARTING_ID+2, "name3", null /* description */, null, ProjectComponentStoreTester.PROJECT_ID_1, 0));
        MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(initialDb, expectedDb);

        ProjectComponentStore store = createStore(mockOfBizDelegator);

        //add a component with description and assert it has not changed
        try
        {
            MutableProjectComponent component = new MutableProjectComponent(null, "name1", COMPONENT_DESCRIPTION, null, 0, ProjectComponentStoreTester.PROJECT_ID_1);
            assertEquals(COMPONENT_DESCRIPTION, component.getDescription());
            assertNull(COMPONENT_DESCRIPTION, component.getGenericValue());//check its creating new component
            component = store.store(component);
            assertEquals(COMPONENT_DESCRIPTION, component.getDescription());
            assertNotNull(component.getGenericValue());//check its created
            assertEquals(COMPONENT_DESCRIPTION, component.getGenericValue().getString("description"));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        //add a component with NULL description and assert it remains NULL
        try
        {
            MutableProjectComponent component = new MutableProjectComponent(null, "name2", null /* description */, null, 0, ProjectComponentStoreTester.PROJECT_ID_1);
            assertNull(component.getDescription());
            assertNull(component.getGenericValue());//check its creating new component
            component = store.store(component);
            assertNull(component.getDescription());
            assertNotNull(component.getGenericValue());//check its created
            assertNull(component.getGenericValue().getString("description"));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        //add a component with no description (empty string "") and assert it is NULL
        try
        {
            MutableProjectComponent component = new MutableProjectComponent(null, "name3", "" /* description */, null, 0, ProjectComponentStoreTester.PROJECT_ID_1);
            assertNull(component.getDescription());
            assertNull(COMPONENT_DESCRIPTION, component.getGenericValue());//check its creating new component
            component = store.store(component);
            assertNull(component.getDescription());
            assertNotNull(component.getGenericValue());//check its created
            assertNull(component.getGenericValue().getString("description"));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        mockOfBizDelegator.verifyAll();
    }

    /**
     * Test that the components description is never set to empty string when updating components. JRA-12193
     */
    @Test
    public void testDescriptionIsNeverSetToEmptyStringOnUpdate()
    {
        //initially no existing components
        List initialDb = EasyList.build();
        //expected components at end of test
        List expectedDb = EasyList.build(createMockGenericValue(MockOfBizDelegator.STARTING_ID, "existing component", null, null, ProjectComponentStoreTester.PROJECT_ID_1, 0));
        MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(initialDb, expectedDb);

        ProjectComponentStore store = createStore(mockOfBizDelegator);

        //Create a component and continually update the component for the following tests
        MutableProjectComponent existingComponent = new MutableProjectComponent(null, "existing component", COMPONENT_DESCRIPTION, null, 0, ProjectComponentStoreTester.PROJECT_ID_1);
        try
        {
            existingComponent = store.store(existingComponent);
        }
        catch (EntityNotFoundException e)
        {
            fail("Failed to create a new component for testing");
        }

        //update existingComponent with new description
        try
        {
            assertEquals(COMPONENT_DESCRIPTION, existingComponent.getDescription());
            assertNotNull(COMPONENT_DESCRIPTION, existingComponent.getGenericValue());//check its updating an existing component
            assertEquals(COMPONENT_DESCRIPTION, existingComponent.getGenericValue().getString("description"));
            //set the new component description
            existingComponent.setDescription("EDITED " + COMPONENT_DESCRIPTION);
            existingComponent = store.store(existingComponent);
            assertEquals("EDITED " + COMPONENT_DESCRIPTION, existingComponent.getDescription());
            assertEquals("EDITED " + COMPONENT_DESCRIPTION, existingComponent.getGenericValue().getString("description"));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        //update existingComponent with new NULL description
        try
        {
            assertNotNull(existingComponent.getGenericValue());//check its updating an existing component
            assertNotNull(existingComponent.getGenericValue().getString("description"));
            //set the new component description
            existingComponent.setDescription(null);
            existingComponent = store.store(existingComponent);
            assertNull(existingComponent.getDescription());
            assertNull(existingComponent.getGenericValue().getString("description"));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        //update existingComponent with new empty string ("") description
        try
        {
            assertNotNull(existingComponent.getGenericValue());//check its updating an existing component
            assertNull(existingComponent.getGenericValue().getString("description"));
            //set the new component description
            existingComponent.setDescription("");
            existingComponent = store.store(existingComponent);
            assertNull(existingComponent.getDescription());
            assertNull(existingComponent.getGenericValue().getString("description"));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        mockOfBizDelegator.verifyAll();
    }

    @Test
    public void testUpdate()
    {
        ProjectComponentStore store = createStore(new MockOfBizDelegator(null, null));

        MutableProjectComponent component1 = new MutableProjectComponent(null, "name1", "desc", null, 0, ProjectComponentStoreTester.PROJECT_ID_1);
        MutableProjectComponent component2 = new MutableProjectComponent(null, "name1", "desc", null, 0, ProjectComponentStoreTester.PROJECT_ID_1);

        // test that the store does not care about duplicate component name in insert operation
        try
        {
            store.store(component1);
            store.store(component2);
            // it passess here for OfBiz
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        // insert valid second component
        try
        {
            component2.setName("name2");
            store.store(component2);
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        // test that the store does not care about duplicate component name in update operation
        try
        {
            component1.setName("name2");
            store.store(component1);
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
    }

    /**
     * Check that the {@link com.atlassian.jira.bc.project.component.OfBizProjectComponentStore} checks that
     * the component has a non-null name
     */
    @Test
    public void testOfbizProjectComponentStoreValidatesName()
    {
        ProjectComponentStore store = createStore(new MockOfBizDelegator(null, null));

        MutableProjectComponent component1 = new MutableProjectComponent(null, "name1", "desc", null, 0, ProjectComponentStoreTester.PROJECT_ID_1);

        // test that component must have a name for create
        try
        {
            component1.setName(null);
            store.store(component1);
            fail("Error: added component with no name");
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //should get null name error
        }

        // test that component must have a name for update
        try
        {
            //first create the component
            store.store(component1);
            //update the component with null name
            component1.setName(null);
            store.store(component1);
            fail("Error: updated component with no name");
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //should get null name error
        }
    }

    /**
     * Check that the {@link com.atlassian.jira.bc.project.component.OfBizProjectComponentStore} checks that
     * the component has a non-null project id
     */
    @Test
    public void testOfbizProjectComponentStoreValidatesProject()
    {
        ProjectComponentStore store = createStore(new MockOfBizDelegator(null, null));

        //setup a component with no project id
        MutableProjectComponent component1 = new MutableProjectComponent(null, "name1", "desc", null, 0, null);

        // test that component must have a project id
        try
        {
            store.store(component1);
            fail("Error: added component with no project id");
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //should get null name error
        }

        //theres no way to change the project id to test update
    }

    @Test
    public void testContainsName()
    {
        ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentStoreTester(createStore(ofBizDelegator));
        tester.testContainsName();
    }

    protected ProjectComponentStore createStore(MockOfBizDelegator ofBizDelegator)
    {
        return new OfBizProjectComponentStore(ofBizDelegator);
    }

    private MockGenericValue createMockGenericValue(int id, String name, String description, String lead, Long projectId, long assigneetype)
    {
        return new MockGenericValue("Component", EasyMap.build("id", new Long(id), "name", name, "description", description, "lead", lead, "project", projectId, "assigneetype", new Long(assigneetype)));
    }



}
