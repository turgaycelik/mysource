package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestComponentResolver
{
    private ProjectComponentManager projectComponentManager;
    private ComponentResolver resolver;

    @Before
    public void setUp()
    {
        projectComponentManager = mock(ProjectComponentManager.class);
        resolver = new ComponentResolver(projectComponentManager);
    }

    @After
    public void tearDown()
    {
        projectComponentManager = null;
        resolver = null;
    }

    @Test
    public void testGetIdsFromNameHappyPath()
    {
        final ProjectComponent component1 = new MockProjectComponent(1L, "component1");
        final ProjectComponent component42 = new MockProjectComponent(42L, "component1");
        when(projectComponentManager.findByComponentNameCaseInSensitive("component1"))
                .thenReturn(ImmutableList.of(component1, component42));

        assertThat(resolver.getIdsFromName("component1"), contains("1", "42"));
    }

    @Test
    public void testGetIdsFromNameDoesntExist()
    {
        when(projectComponentManager.findByComponentNameCaseInSensitive("abc")).thenReturn(ImmutableList.<ProjectComponent>of());

        assertThat(resolver.getIdsFromName("abc"), hasSize(0));
    }

    @Test
    public void testGetIdExists() throws Exception
    {
        final ProjectComponent component = new MockProjectComponent(2L, "component1");
        when(projectComponentManager.find(2L)).thenReturn(component);

        assertThat(resolver.get(2L), sameInstance(component));
    }

    @Test
    public void testGetIdDoesntExist()
    {
        assertThat(resolver.get(100L), nullValue());
    }

    @Test
    public void testGetIdException() throws Exception
    {
        when(projectComponentManager.find(100L)).thenThrow(new EntityNotFoundException());

        assertThat(resolver.get(100L), nullValue());
    }

    @Test
    public void testNameExists()
    {
        final ProjectComponent component = new MockProjectComponent(1000L, "name");
        when(projectComponentManager.findByComponentNameCaseInSensitive("name")).thenReturn(ImmutableList.of(component));

        assertThat("nameExists(name)", resolver.nameExists("name"), is(true));
        assertThat("nameExists(noname)", resolver.nameExists("noname"), is(false));
    }

    @Test
    public void testIdExists() throws Exception
    {
        when(projectComponentManager.find(10L)).thenReturn(new MockProjectComponent(1000L, "name"));

        assertThat("idExists(10L)", resolver.idExists(10L), is(true));
        assertThat("idExists(11L)", resolver.idExists(11L), is(false));
    }

}
