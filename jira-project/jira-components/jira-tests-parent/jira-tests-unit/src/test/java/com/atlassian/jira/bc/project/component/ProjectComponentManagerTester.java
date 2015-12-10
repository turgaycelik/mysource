package com.atlassian.jira.bc.project.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.ValidationErrorsException;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.util.MockUserManager;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProjectComponentManagerTester
{

    protected final ProjectComponentStore store;

    DefaultProjectComponentManager managerEnt;
    DefaultProjectComponentManager managerNonEnt;

    private static final String INVALID_USER = "Bob";
    private static final String VALID_USER = "Robert";
    protected static final String UNIQUE_COMPONENT_NAME = "Component Name";
    protected static final Long PROJECT_ID_STORED = new Long(1);
    public static final Long MY_PROJECT_ID_STORED = new Long(10);
    private static final Long PROJECT_ID_NOT_STORED = new Long(100);

    private final ProjectComponent COMPONENT_NOT_STORED =
            new ProjectComponentImpl("Not saved", "component not persisted in the store", "John", 0);

    protected ProjectComponent COMPONENT_STORED;


    public ProjectComponentManagerTester(ProjectComponentStore store)
    {
        this.store = store;
        managerEnt = new DefaultProjectComponentManager(store, null, new MockEventPublisher(),new MockUserManager(), null);
        managerNonEnt = new DefaultProjectComponentManager(store, null, new MockEventPublisher(), new MockUserManager(), null);

        MutableProjectComponent component =
                new MutableProjectComponent(null, "test 1", "just a test", null, 0, PROJECT_ID_STORED);
        try
        {
            COMPONENT_STORED = managerEnt.convertToProjectComponent(store.store(component));
        }
        catch (EntityNotFoundException e)
        {
            throw new RuntimeException("Failed to store the component in the store");
        }
    }

    public void testCreateSuccess()
    {

        // test for valid input values
        try
        {
            ProjectComponent component = managerEnt.create(UNIQUE_COMPONENT_NAME, null, null, 0, PROJECT_ID_STORED);
            assertNotNull(component);
            assertEquals(UNIQUE_COMPONENT_NAME, component.getName());
            assertNull(component.getDescription());
            assertNull(component.getLead());

            // test that we can find the newly created component
            assertEquals(component, managerEnt.find(component.getId()));
            assertTrue(managerEnt.findAllForProject(PROJECT_ID_STORED).contains(component));
        }
        catch (ValidationErrorsException ex)
        {
            fail();
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }
    }

    public void testFindAll()
    {

        // Create components
        ProjectComponent pc1 = managerEnt.create("pc1", "ptest1", null, 0, PROJECT_ID_STORED);
        ProjectComponent pc2 = managerEnt.create("pc2", "ptest2", null, 0, PROJECT_ID_STORED);
        ProjectComponent pc3 = managerEnt.create("pc3", "ptest3", null, 0, PROJECT_ID_STORED);
        ProjectComponent my1 = managerEnt.create("my1", "ptest1", null, 0, MY_PROJECT_ID_STORED);
        ProjectComponent my2 = managerEnt.create("my2", "ptest2", null, 0, MY_PROJECT_ID_STORED);
        ProjectComponent my3 = managerEnt.create("my3", "ptest3", null, 0, MY_PROJECT_ID_STORED);

        // Verify correct components retrieved for both projects
        Collection components = managerEnt.findAll();
        assertNotNull(components);
        assertEquals(7, components.size());
        assertTrue(components.contains(COMPONENT_STORED));
        assertFalse(components.contains(COMPONENT_NOT_STORED));
        assertTrue(components.contains(pc1));
        assertTrue(components.contains(pc2));
        assertTrue(components.contains(pc3));
        assertTrue(components.contains(my1));
        assertTrue(components.contains(my2));
        assertTrue(components.contains(my3));

    }

    public void testFindByComponentName()
    {
        // Create components
        ProjectComponent pc1 = managerEnt.create("name", "ptest1", null, 0, PROJECT_ID_STORED);
        ProjectComponent pc2 = managerEnt.create("pc2", "ptest2", null, 0, PROJECT_ID_STORED);
        ProjectComponent pc3 = managerEnt.create("pc3", "ptest3", null, 0, PROJECT_ID_STORED);
        ProjectComponent my1 = managerEnt.create("Name", "ptest1", null, 0, MY_PROJECT_ID_STORED);
        ProjectComponent my2 = managerEnt.create("my2", "ptest2", null, 0, MY_PROJECT_ID_STORED);
        ProjectComponent my3 = managerEnt.create("my3", "ptest3", null, 0, MY_PROJECT_ID_STORED);
        Collection components = managerEnt.findByComponentNameCaseInSensitive("name");
        assertEquals(2, components.size());
        assertTrue(components.contains(pc1));
        assertTrue(components.contains(my1));
    }

    // create and test components found for one project
    public void testFindAllForProject1()
    {
        Collection components = managerEnt.findAllForProject(PROJECT_ID_NOT_STORED);
        assertNotNull(components);
        assertTrue(components.isEmpty());

        try
        {
            // Create MY_PROJECT_ID_STORED components
            ProjectComponent pc1 = managerEnt.create("pc1", "ptest1", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent pc2 = managerEnt.create("pc2", "ptest2", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent pc3 = managerEnt.create("pc3", "ptest3", null, 0, MY_PROJECT_ID_STORED);

            // Retrieve all components for MY_PROJECT_ID_STORED
            Collection myComponents = managerEnt.findAllForProject(MY_PROJECT_ID_STORED);
            assertNotNull(myComponents);
            assertEquals(3, myComponents.size());
            assertTrue(myComponents.contains(pc1));
            assertTrue(myComponents.contains(pc2));
            assertTrue(myComponents.contains(pc3));
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }
    }

    // test that creation of components does not affect components of other projects
    public void testFindAllForProject2()
    {
        try
        {
            // Create MY_PROJECT_ID_STORED components
            managerEnt.create("pc1", "ptest1", null, 0, MY_PROJECT_ID_STORED);
            managerEnt.create("pc2", "ptest2", null, 0, MY_PROJECT_ID_STORED);
            managerEnt.create("pc3", "ptest3", null, 0, MY_PROJECT_ID_STORED);

            // Retrieve all components for PROJECT_ID_STORED
            Collection components = managerEnt.findAllForProject(PROJECT_ID_STORED);
            assertNotNull(components);
            assertEquals(1, components.size());
            assertTrue(components.contains(COMPONENT_STORED));
            assertFalse(components.contains(COMPONENT_NOT_STORED));
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }
    }

    //
    public void testFindAllForProject3()
    {
        try
        {
            // Create components
            ProjectComponent pc1 = managerEnt.create("pc1", "ptest1", null, 0, PROJECT_ID_STORED);
            ProjectComponent pc2 = managerEnt.create("pc2", "ptest2", null, 0, PROJECT_ID_STORED);
            ProjectComponent pc3 = managerEnt.create("pc3", "ptest3", null, 0, PROJECT_ID_STORED);
            ProjectComponent my1 = managerEnt.create("my1", "ptest1", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent my2 = managerEnt.create("my2", "ptest2", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent my3 = managerEnt.create("my3", "ptest3", null, 0, MY_PROJECT_ID_STORED);

            // Delete one component
            managerEnt.delete(pc2.getId());

            // Verify correct components retrieved for both projects
            Collection components = managerEnt.findAllForProject(PROJECT_ID_STORED);
            assertNotNull(components);
            assertEquals(3, components.size());
            assertTrue(components.contains(COMPONENT_STORED));
            assertTrue(components.contains(pc1));
            assertFalse(components.contains(pc2));
            assertTrue(components.contains(pc3));
            assertFalse(components.contains(COMPONENT_NOT_STORED));

            Collection myComponents = managerEnt.findAllForProject(MY_PROJECT_ID_STORED);
            assertNotNull(myComponents);
            assertEquals(3, myComponents.size());
            assertTrue(myComponents.contains(my1));
            assertTrue(myComponents.contains(my2));
            assertTrue(myComponents.contains(my3));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }
    }

    public void testFindAllForProject4()
    {
        try
        {
            // Create components
            ProjectComponent pc1 = managerEnt.create("pc1", "ptest1", null, 0, PROJECT_ID_STORED);
            ProjectComponent pc2 = managerEnt.create("pc2", "ptest2", null, 0, PROJECT_ID_STORED);
            ProjectComponent pc3 = managerEnt.create("pc3", "ptest3", null, 0, PROJECT_ID_STORED);
            ProjectComponent my1 = managerEnt.create("my1", "ptest1", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent my2 = managerEnt.create("my2", "ptest2", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent my3 = managerEnt.create("my3", "ptest3", null, 0, MY_PROJECT_ID_STORED);

            // Delete all components for PROJECT_ID_STORED - in random order
            managerEnt.delete(pc1.getId());
            managerEnt.delete(pc3.getId());
            managerEnt.delete(COMPONENT_STORED.getId());
            managerEnt.delete(pc2.getId());

            // Verify correct components retrieved for both projects
            Collection components = managerEnt.findAllForProject(PROJECT_ID_STORED);
            assertNotNull(components);
            assertEquals(0, components.size());
            assertFalse(components.contains(COMPONENT_STORED));
            assertFalse(components.contains(pc1));
            assertFalse(components.contains(pc2));
            assertFalse(components.contains(pc3));
            assertFalse(components.contains(COMPONENT_NOT_STORED));
            Collection myComponents = managerEnt.findAllForProject(MY_PROJECT_ID_STORED);
            assertNotNull(myComponents);
            assertEquals(3, myComponents.size());
            assertTrue(myComponents.contains(my1));
            assertTrue(myComponents.contains(my2));
            assertTrue(myComponents.contains(my3));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }
    }

    public void testFindAllForProject5()
    {
        try
        {
            // Create components
            ProjectComponent pc1 = managerEnt.create("pc1", "ptest1", null, 0, PROJECT_ID_STORED);
            ProjectComponent pc2 = managerEnt.create("pc2", "ptest2", null, 0, PROJECT_ID_STORED);
            ProjectComponent pc3 = managerEnt.create("pc3", "ptest3", null, 0, PROJECT_ID_STORED);
            ProjectComponent my1 = managerEnt.create("my1", "ptest1", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent my2 = managerEnt.create("my2", "ptest2", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent my3 = managerEnt.create("my3", "ptest3", null, 0, MY_PROJECT_ID_STORED);

            // Delete all components
            managerEnt.delete(COMPONENT_STORED.getId());
            managerEnt.delete(pc1.getId());
            managerEnt.delete(pc2.getId());
            managerEnt.delete(pc3.getId());
            managerEnt.delete(my1.getId());
            managerEnt.delete(my2.getId());
            managerEnt.delete(my3.getId());

            // Verify all components deleted for both projects
            Collection components = managerEnt.findAllForProject(PROJECT_ID_STORED);
            assertNotNull(components);
            assertEquals(0, components.size());
            assertFalse(components.contains(COMPONENT_STORED));
            assertFalse(components.contains(pc1));
            assertFalse(components.contains(pc2));
            assertFalse(components.contains(pc3));
            assertFalse(components.contains(COMPONENT_NOT_STORED));
            Collection myComponents = managerEnt.findAllForProject(MY_PROJECT_ID_STORED);
            assertNotNull(myComponents);
            assertEquals(0, myComponents.size());
            assertFalse(myComponents.contains(my1));
            assertFalse(myComponents.contains(my2));
            assertFalse(myComponents.contains(my3));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }


    }

    public void testCreateAndDelete()
    {
        try
        {
            // Create components
            ProjectComponent pc1 = managerEnt.create("pc1", "ptest1", null, 0, PROJECT_ID_STORED);
            ProjectComponent pc2 = managerEnt.create("pc2", "ptest2", null, 0, PROJECT_ID_STORED);
            ProjectComponent pc3 = managerEnt.create("pc3", "ptest3", null, 0, PROJECT_ID_STORED);
            ProjectComponent my1 = managerEnt.create("my1", "ptest1", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent my2 = managerEnt.create("my2", "ptest2", null, 0, MY_PROJECT_ID_STORED);
            ProjectComponent my3 = managerEnt.create("my3", "ptest3", null, 0, MY_PROJECT_ID_STORED);

            // Delete all components
            managerEnt.delete(COMPONENT_STORED.getId());
            managerEnt.delete(pc1.getId());
            managerEnt.delete(pc2.getId());
            managerEnt.delete(pc3.getId());
            managerEnt.delete(my1.getId());
            managerEnt.delete(my2.getId());
            managerEnt.delete(my3.getId());

            // re-create all components for both projects
            // NOTE: IDs can be different
            pc1 = managerEnt.create("pc1", "ptest1", null, 0, PROJECT_ID_STORED);
            pc2 = managerEnt.create("pc2", "ptest2", null, 0, PROJECT_ID_STORED);
            pc3 = managerEnt.create("pc3", "ptest3", null, 0, PROJECT_ID_STORED);
            my1 = managerEnt.create("my1", "test1", null, 0, MY_PROJECT_ID_STORED);
            my2 = managerEnt.create("my2", "test2", null, 0, MY_PROJECT_ID_STORED);
            my3 = managerEnt.create("my3", "test3", null, 0, MY_PROJECT_ID_STORED);

            // Verify correct components retrieved for both projects
            Collection components = managerEnt.findAllForProject(PROJECT_ID_STORED);
            assertNotNull(components);
            assertEquals(3, components.size());
            assertTrue(components.contains(pc1));
            assertTrue(components.contains(pc2));
            assertTrue(components.contains(pc3));
            Collection myComponents = managerEnt.findAllForProject(MY_PROJECT_ID_STORED);
            assertNotNull(myComponents);
            assertEquals(3, myComponents.size());
            assertTrue(myComponents.contains(my1));
            assertTrue(myComponents.contains(my2));
            assertTrue(myComponents.contains(my3));
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }
    }

    public void testUpdateSingleComponent()
    {
        try
        {
            ProjectComponent projectComponent = managerEnt.find(COMPONENT_STORED.getId());
            assertNotNull(projectComponent);
            assertEquals(COMPONENT_STORED, projectComponent);

            // update component
            MutableProjectComponent modified = MutableProjectComponent.copy(COMPONENT_STORED);
            modified.setName("test 2");
            modified.setDescription("another test");
            modified.setLead(null);

            projectComponent = managerEnt.update(modified);

            // verify that the original component was not changed - should be no longer used anyway
            assertEquals("test 1", COMPONENT_STORED.getName());
            assertEquals("just a test", COMPONENT_STORED.getDescription());
            assertNull(COMPONENT_STORED.getLead());

            // verify the updated component
            assertEquals(COMPONENT_STORED, projectComponent);
            assertEquals("test 2", projectComponent.getName());
            assertEquals("another test", projectComponent.getDescription());
            assertNull(projectComponent.getLead());

            // verify the component returned by find() method by the original ID
            ProjectComponent foundComponent = managerEnt.find(COMPONENT_STORED.getId());
            assertEquals(foundComponent, projectComponent);
            assertEquals(foundComponent.getName(), projectComponent.getName());
            assertEquals(foundComponent.getDescription(), projectComponent.getDescription());
            assertEquals(foundComponent.getLead(), projectComponent.getLead());

            // update component with same name
            modified = MutableProjectComponent.copy(projectComponent);
            modified.setName(projectComponent.getName());
            projectComponent = managerEnt.update(modified);

            // verify the updated component
            assertEquals("test 2", projectComponent.getName());
            assertEquals("another test", projectComponent.getDescription());
            assertNull(projectComponent.getLead());
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
    }

    public void testUpdateNonPersisted()
    {
        try
        {
            try
            {
                managerEnt.find(COMPONENT_NOT_STORED.getId());
                fail();
            }
            catch (EntityNotFoundException e)
            {
                fail();
            }
            catch (IllegalArgumentException e)
            {
                // ID is null
            }
            MutableProjectComponent modified = new MutableProjectComponent(COMPONENT_STORED.getId(), "blah", "stuff", null, AssigneeTypes.PROJECT_DEFAULT, PROJECT_ID_STORED);
            managerEnt.update(modified);
            fail();
        }
        catch (EntityNotFoundException e)
        {
            // should throw exception
        }
    }

    public void testUpdateIsConsistent()
    {
        try
        {
            // Create new components for PROJECT_ID_STORED
            managerEnt.create("c1", "test1", null, 0, PROJECT_ID_STORED);
            managerEnt.create("c2", "test2", null, 0, PROJECT_ID_STORED);
            ProjectComponent c3 = managerEnt.create("c3", "test3", null, 0, PROJECT_ID_STORED);

            // assert the number of components stored remains constant before and after an update
            try
            {
                int sizeBeforeUpdate = managerEnt.findAllForProject(PROJECT_ID_STORED).size();
                MutableProjectComponent modifiedC3 = MutableProjectComponent.copy(c3);
                modifiedC3.setName("noname");
                modifiedC3.setLead(null);
                COMPONENT_STORED = managerEnt.update(modifiedC3);
                assertEquals("noname", COMPONENT_STORED.getName());
                assertEquals(sizeBeforeUpdate, managerEnt.findAllForProject(PROJECT_ID_STORED).size());
            }
            catch (EntityNotFoundException e)
            {
                fail();
            }
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }
    }

    public void testDelete()
    {
        try
        {
            ProjectComponent projectComponent = managerEnt.find(COMPONENT_STORED.getId());
            assertNotNull(projectComponent);
            assertEquals(COMPONENT_STORED, projectComponent);
            managerEnt.delete(COMPONENT_STORED.getId());
            try
            {
                managerEnt.find(COMPONENT_STORED.getId());
                fail();
            }
            catch (EntityNotFoundException e)
            {
            }
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        // test for deletion of non-persisted component
        // should throw exception
        try
        {
            try
            {
                managerEnt.find(COMPONENT_NOT_STORED.getId());
                fail();
            }
            catch (EntityNotFoundException e)
            {
                fail();
            }
            catch (IllegalArgumentException e)
            {
                // ID is null
            }
            managerEnt.delete(COMPONENT_NOT_STORED.getId());
            fail();
        }
        catch (EntityNotFoundException e)
        {
        }

    }

    public void testDeleteAndUpdate()
    {
        try
        {
            ProjectComponent projectComponent = managerEnt.find(COMPONENT_STORED.getId());
            assertNotNull(projectComponent);
            assertEquals(COMPONENT_STORED, projectComponent);

            // delete component
            managerEnt.delete(COMPONENT_STORED.getId());

            // verify it cannot be updated
            try
            {
                MutableProjectComponent modified = MutableProjectComponent.copy(projectComponent);
                modified.setName("some name");
                modified.setDescription("some text");
                modified.setLead(null);
                managerEnt.update(modified);
                fail();
            }
            catch (EntityNotFoundException e)
            {
                // it should come here as the component was already deleted
            }

        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
    }

    public void findAllUniqueNamesForProjects()
    {
        managerEnt.create("pc1", "ptest1", null, 0, 1L);
        managerEnt.create("Pc1", "ptest2", null, 0, 2L);
        managerEnt.create("pc2", "ptest3", null, 0, 3L);
        managerEnt.create("pC2", "ptest1", null, 0, 4L);
        managerEnt.create("pc3", "ptest2", null, 0, 5L);
        managerEnt.create("PC3", "ptest3", null, 0, 6L);
        managerEnt.create("pc4", "ptest2", null, 0, 7L);
        managerEnt.create("PC4", "ptest3", null, 0, 8L);

        List<Project> projects = Arrays.<Project>asList(new MockProject(1L), new MockProject(2L), new MockProject(3L),
                new MockProject(4L), new MockProject(5L), new MockProject(6L));

        Collection<String> allUniqueNames = managerEnt.findAllUniqueNamesForProjectObjects(projects);
        allUniqueNames = Lists.transform(Lists.newArrayList(allUniqueNames), new Function<String, String>()
        {
            @Override
            public String apply(String name)
            {
                return name.toLowerCase();
            }
        });

        assertEquals(asList("pc1", "pc2", "pc3", "test 1"), Lists.newArrayList(allUniqueNames));
    }

    public void testFindAllUniqueNamesForProjects()
    {
        managerEnt.create("pc1", "ptest1", null, 0, 1L);
        managerEnt.create("Pc1", "ptest2", null, 0, 2L);
        managerEnt.create("pc2", "ptest3", null, 0, 3L);
        managerEnt.create("pC2", "ptest1", null, 0, 4L);
        managerEnt.create("pc3", "ptest2", null, 0, 5L);
        managerEnt.create("PC3", "ptest3", null, 0, 6L);

        Collection<String> allUniqueNames = managerEnt.findAllUniqueNamesForProjects(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L));
        allUniqueNames = Lists.transform(Lists.newArrayList(allUniqueNames), new Function<String, String>()
        {
            @Override
            public String apply(String name)
            {
                return name.toLowerCase();
            }
        });

        assertEquals(asList("pc1", "pc2", "pc3", "test 1"), Lists.newArrayList(allUniqueNames));
    }
}
