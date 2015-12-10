package com.atlassian.jira.jql.resolver;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestProjectIndexInfoResolver extends MockControllerTestCase
{
    @Test
    public void testGetIndexedValuesNullValue() throws Exception
    {
        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);
        try
        {
            projectIndexInfoResolver.getIndexedValues((String)null);
            fail("Should throw exception");
        }
        catch (Exception e)
        {
            // expected
        }
    }

    @Test
    public void testGetIndexedValuesNullProjectWithId() throws Exception
    {
        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.getIdsFromName("12345");
        mockController.setReturnValue(Collections.emptyList());
        nameResolver.get(12345L);
        mockController.setReturnValue(null);
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);
        final List<String> list = projectIndexInfoResolver.getIndexedValues("12345");
        assertNotNull(list);
        assertTrue(list.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesNullProjectWithString() throws Exception
    {
        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.getIdsFromName("TST");
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);
        final List<String> list = projectIndexInfoResolver.getIndexedValues("TST");
        assertNotNull(list);
        assertTrue(list.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesFoundById() throws Exception
    {
        final MockProject mockProject = new MockProject(12345, "TST");

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.getIdsFromName("12345");
        mockController.setReturnValue(Collections.emptyList());
        nameResolver.get(12345L);
        mockController.setReturnValue(mockProject);
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);
        final List<String> list = projectIndexInfoResolver.getIndexedValues("12345");
        assertNotNull(list);
        assertEquals(1, list.size());

        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesFoundByString() throws Exception
    {
        final MockProject mockProject = new MockProject(12345, "TST");

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.getIdsFromName("TST");
        mockController.setReturnValue(Collections.singletonList(mockProject));
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);
        final List<String> list = projectIndexInfoResolver.getIndexedValues("TST");
        assertNotNull(list);
        assertEquals(1, list.size());

        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesLongFoundById() throws Exception
    {
        final MockProject mockProject = new MockProject(12345, "TST");

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.get(12345L);
        mockController.setReturnValue(mockProject);
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);
        final List<String> list = projectIndexInfoResolver.getIndexedValues(12345L);
        assertNotNull(list);
        assertEquals(1, list.size());

        mockController.verify();
    }

    @Test
    public void testGetIndexedValuesLongNullValue() throws Exception
    {
        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);
        try
        {
            projectIndexInfoResolver.getIndexedValues((Long)null);
            fail("Should throw exception");
        }
        catch (Exception e)
        {
            // expected
        }
    }

    @Test
    public void testGetIndexedValuesLongFallBackToString() throws Exception
    {
        final MockProject mockProject = new MockProject(12345, "TST");

        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        nameResolver.get(12345L);
        mockController.setReturnValue(null);
        nameResolver.getIdsFromName("12345");
        mockController.setReturnValue(Collections.singletonList(mockProject));
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);

        final List<String> list = projectIndexInfoResolver.getIndexedValues(12345L);
        assertNotNull(list);
        assertEquals(1, list.size());

        mockController.verify();
    }

    @Test
    public void testGetIndexedValue() throws Exception
    {
        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        mockController.replay();

        final MockProject mockProject = new MockProject(12345, "TST");
        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);

        assertEquals("12345", projectIndexInfoResolver.getIndexedValue(mockProject));
    }

    @Test
    public void testGetIndexedValueNullProject() throws Exception
    {
        final NameResolver<Project> nameResolver = mockController.getMock(NameResolver.class);
        mockController.replay();

        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(nameResolver);

        try
        {
            projectIndexInfoResolver.getIndexedValue(null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}        
    }
    
}
