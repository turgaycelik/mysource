package com.atlassian.jira.bc.project.component;

import java.util.Collection;

import com.atlassian.jira.bc.EntityNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProjectComponentStoreTester
{
    private final ProjectComponentStore store;

    public static final String NO_SUCH_NAME = "Rwaya";
    public static final Long PROJECT_ID_1 = new Long(1);
    public static final Long PROJECT_ID_2 = new Long(2);
    public static final Long COMPONENT_ID_NOT_STORED = new Long(123);

    public ProjectComponentStoreTester(ProjectComponentStore store)
    {
        this.store = store;
    }

    public void testFind()
    {
        verifyFindWithInvalidIds();

        verifyFindWithValidId();

        verifyFindWithInvalidIds();
    }

    private void verifyFindWithValidId()
    {
        try
        {
            // insert one component
            MutableProjectComponent component = new MutableProjectComponent(null, "name", "desc", null, 0, PROJECT_ID_1);
            component = store.store(component);
            // test if component can be found
            MutableProjectComponent pc = store.find(component.getId());
            assertNotNull(pc);
            assertEquals(component.getId(), pc.getId());
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
    }

    private void verifyFindWithInvalidIds()
    {
        try
        {
            store.find(null);
            fail();
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            store.find(COMPONENT_ID_NOT_STORED);
            fail();
        }
        catch (EntityNotFoundException e)
        {
            //expected
        }
    }

    public void testFindAllForProject()
    {
        verifyFindAllWithInvalidProjectId();

        try
        {
            // insert two components for PROJECT_ID_2 project
            MutableProjectComponent component1 = new MutableProjectComponent(null, "name1", "desc", null, 0, PROJECT_ID_2);
            MutableProjectComponent component2 = new MutableProjectComponent(null, "name2", "desc", null, 0, PROJECT_ID_2);
            component1 = store.store(component1);
            component2 = store.store(component2);

            // verify components can be found
            Collection components = store.findAllForProject(PROJECT_ID_2);
            assertNotNull(components);
            assertEquals(2, components.size());
            assertTrue(components.contains(component1));
            assertTrue(components.contains(component2));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        verifyFindAllWithInvalidProjectId();
    }

    public void testFindByComponentName()
    {
        verifyFindAllWithInvalidProjectId();

        try
        {
            // insert two components for PROJECT_ID_2 project
            MutableProjectComponent component1 = new MutableProjectComponent(null, "name1", "desc", null, 0, PROJECT_ID_2);
            MutableProjectComponent component2 = new MutableProjectComponent(null, "name", "desc", null, 0, PROJECT_ID_2);
            MutableProjectComponent component3 = new MutableProjectComponent(null, "Name", "desc", null, 0, PROJECT_ID_1);
            component1 = store.store(component1);
            component2 = store.store(component2);
            component3 = store.store(component3);

            // verify components can be found
            Collection components = store.findByComponentNameCaseInSensitive("name");
            assertNotNull(components);
            assertEquals(2, components.size());
            assertTrue(components.contains(component2));
            assertTrue(components.contains(component3));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
    }

    private void verifyFindAllWithInvalidProjectId()
    {
        Collection components = store.findAllForProject(PROJECT_ID_1);
        assertNotNull(components);
        assertTrue(components.isEmpty());
    }

    public void testFindProjectIdForComponent()
    {
        try
        {
            // insert one component
            MutableProjectComponent component = new MutableProjectComponent(null, "name", "desc", null, 0, PROJECT_ID_1);
            component = store.store(component);
            // verify the project ID is same
            assertEquals(PROJECT_ID_1, store.findProjectIdForComponent(component.getId()));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        try
        {
            store.findProjectIdForComponent(COMPONENT_ID_NOT_STORED);
            fail();
        }
        catch (EntityNotFoundException e)
        {
        }
    }

    public void testDelete()
    {
        try
        {
            // insert two components
            MutableProjectComponent component1 = new MutableProjectComponent(null, "name1", "desc", null, 0, PROJECT_ID_1);
            MutableProjectComponent component2 = new MutableProjectComponent(null, "name2", "desc", null, 0, PROJECT_ID_1);
            component1 = store.store(component1);
            component2 = store.store(component2);

            // verify components can be found
            assertNotNull(store.find(component1.getId()));
            assertNotNull(store.find(component2.getId()));
            assertTrue(store.containsName(component1.getName(), PROJECT_ID_1));
            assertTrue(store.containsName(component2.getName(), PROJECT_ID_1));
            assertEquals(2, store.findAllForProject(PROJECT_ID_1).size());

            // delete first component
            store.delete(component1.getId());

            // verify deleted component can NOT be found
            try
            {
                store.find(component1.getId());
                fail();
            }
            catch (EntityNotFoundException e)
            {
            }
            assertNotNull(store.find(component2.getId()));
            assertFalse(store.containsName(component1.getName(), PROJECT_ID_1));
            assertTrue(store.containsName(component2.getName(), PROJECT_ID_1));
            assertEquals(1, store.findAllForProject(PROJECT_ID_1).size());

            // delete second component
            store.delete(component2.getId());

            // verify all deleted components can NOT be found
            try
            {
                store.find(component1.getId());
                fail();
            }
            catch (EntityNotFoundException e)
            {
            }
            // verify deleted component can NOT be found
            try
            {
                store.find(component2.getId());
                fail();
            }
            catch (EntityNotFoundException e)
            {
            }
            assertFalse(store.containsName(component1.getName(), PROJECT_ID_1));
            assertFalse(store.containsName(component2.getName(), PROJECT_ID_1));
            assertEquals(0, store.findAllForProject(PROJECT_ID_1).size());

        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
    }

    public void testUpdate()
    {
        MutableProjectComponent component1 = new MutableProjectComponent(new Long(1), "name1", "desc", null, 0, PROJECT_ID_1);
        try
        {
            store.store(component1);
            fail();
        }
        catch (EntityNotFoundException e)
        {
            // exception should be caught
        }
        MutableProjectComponent component2 = new MutableProjectComponent(new Long(2), "name1", "desc", null, 0, PROJECT_ID_1);
        try
        {
            store.store(component2);
            fail();
        }
        catch (EntityNotFoundException e)
        {
            // exception should be caught
        }
    }

    public void testContainsName()
    {
        try
        {
            store.containsName(null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // nulls are invalid
        }
        try
        {
            store.containsName("name", null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // nulls are invalid
        }
        try
        {
            store.containsName(null, new Long(1));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // nulls are invalid
        }
    }


}
