package com.atlassian.jira.jql.resolver;

import java.util.List;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestProjectResolver
{
    @Mock ProjectManager projectManager;

    ProjectResolver resolver;



    @Before
    public void setUp()
    {
        resolver = new ProjectResolver(projectManager);
    }

    @After
    public void tearDown()
    {
        projectManager = null;
        resolver = null;
    }



    @Test
    public void testGetIdsFromNameHappyPath() throws Exception
    {
        final Project project = new MockProject(1L, "key", "name");
        when(projectManager.getProjectObjByKeyIgnoreCase("name")).thenReturn(project);

        final List<String> result = resolver.getIdsFromName("name");
        assertThat(result, contains("1"));
    }

    @Test
    public void testGetIdsFromNameDoesntExistKeyDoes() throws Exception
    {
        final Project project = new MockProject(1L, "key", "name");
        when(projectManager.getProjectObjByName("key")).thenReturn(project);

        final List<String> result = resolver.getIdsFromName("key");
        assertThat(result, contains("1"));
    }
    
    @Test
    public void testGetIdsFromNameAndKeyDoesntExist() throws Exception
    {
        final List<String> result = resolver.getIdsFromName("abc");
        assertThat(result, hasSize(0));
    }

    @Test
    public void testGetIdExists() throws Exception
    {
        final Project project = new MockProject(2L, "version1");
        when(projectManager.getProjectObj(2L)).thenReturn(project);

        final Project result = resolver.get(2L);
        assertThat(result, equalTo(project));
    }

    @Test
    public void testGetIdDoesntExist() throws Exception
    {
        final Project result = resolver.get(100L);
        assertThat(result, nullValue());
    }

    @Test
    public void testNameExistsAsName() throws Exception
    {
        when(projectManager.getProjectObjByName("name")).thenReturn(new MockProject(1000, "name"));
        assertThat("nameExists", resolver.nameExists("name"), is(true));
    }

    @Test
    public void testNameExistsAsKey() throws Exception
    {
        when(projectManager.getProjectObjByKeyIgnoreCase("name")).thenReturn(new MockProject(1000, "name"));
        assertThat("nameExists", resolver.nameExists("name"), is(true));
    }

    @Test
    public void testNameAndKeyDoesntExist() throws Exception
    {
        assertThat("nameExists", resolver.nameExists("name"), is(false));
    }

    @Test
    public void testIdExists() throws Exception
    {

        when(projectManager.getProjectObj(10L)).thenReturn(new MockProject(1000, "name"));

        assertThat("idExists(10L)", resolver.idExists(10L), is(true));
        assertThat("idExists(11L)", resolver.idExists(11L), is(false));
    }
}
