package com.atlassian.jira.jql.resolver;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestProjectCategoryResolver extends MockControllerTestCase
{
    private ProjectManager projectManager;

    @Before
    public void setUp() throws Exception
    {
        projectManager = mockController.getMock(ProjectManager.class);
    }

    @Test
    public void testGetProjectCategoryUsingStringHappyPath() throws Exception
    {
        final GenericValue expectedCategory = createMockProjectCategory(1L, "TheName");

        EasyMock.expect(projectManager.getProjectCategoryByNameIgnoreCase("TheName"))
                .andReturn(expectedCategory);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final GenericValue category = resolver.getProjectCategory(createLiteral("TheName"));
        assertEquals(expectedCategory, category);

        verify();
    }

    @Test
    public void testGetProjectCategoryUsingStringNameDoesntExistIsntId() throws Exception
    {
        EasyMock.expect(projectManager.getProjectCategoryByNameIgnoreCase("TheName"))
                .andReturn(null);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final GenericValue category = resolver.getProjectCategory(createLiteral("TheName"));
        assertNull(category);

        verify();
    }

    @Test
    public void testGetProjectCategoryUsingStringNameDoesntExistIsIdAndExists() throws Exception
    {
        final MockGenericValue expectedCategory = createMockProjectCategory(1L, "TheName");

        EasyMock.expect(projectManager.getProjectCategoryByNameIgnoreCase("1"))
                .andReturn(null);
        EasyMock.expect(projectManager.getProjectCategory(1L))
                .andReturn(expectedCategory);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final GenericValue category = resolver.getProjectCategory(createLiteral("1"));
        assertEquals(expectedCategory, category);

        verify();
    }

    @Test
    public void testGetProjectCategoryUsingStringNameDoesntExistIsIdDoesntExist() throws Exception
    {
        EasyMock.expect(projectManager.getProjectCategoryByNameIgnoreCase("1"))
                .andReturn(null);
        EasyMock.expect(projectManager.getProjectCategory(1L))
                .andReturn(null);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final GenericValue category = resolver.getProjectCategory(createLiteral("1"));
        assertNull(category);

        verify();
    }

    @Test
    public void testGetProjectCategoryUsingEmptyLiteral() throws Exception
    {
        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final GenericValue category = resolver.getProjectCategory(new QueryLiteral());
        assertNull(category);

        verify();
    }

    @Test
    public void testGetProjectCategoryUsingLongHappyPath() throws Exception
    {
        final GenericValue expectedCategory = createMockProjectCategory(1L, "TheName");

        EasyMock.expect(projectManager.getProjectCategory(1L))
                .andReturn(expectedCategory);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final GenericValue category = resolver.getProjectCategory(createLiteral(1L));
        assertEquals(expectedCategory, category);

        verify();
    }

    @Test
    public void testGetProjectCategoryUsingLongIdDoesntExistNameDoes() throws Exception
    {
        final MockGenericValue expectedCategory = createMockProjectCategory(1L, "1");

        EasyMock.expect(projectManager.getProjectCategory(1L))
                .andReturn(null);
        EasyMock.expect(projectManager.getProjectCategoryByNameIgnoreCase("1"))
                .andReturn(expectedCategory);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final GenericValue category = resolver.getProjectCategory(createLiteral(1L));
        assertEquals(expectedCategory, category);

        verify();
    }

    @Test
    public void testGetProjectCategoryUsingLongIdDoesntExistNameDoesnt() throws Exception
    {
        EasyMock.expect(projectManager.getProjectCategory(1L))
                .andReturn(null);
        EasyMock.expect(projectManager.getProjectCategoryByNameIgnoreCase("1"))
                .andReturn(null);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final GenericValue category = resolver.getProjectCategory(createLiteral(1L));
        assertNull(category);

        verify();
    }

    @Test
    public void testGetProjectsForCategoryEmptyLiteral() throws Exception
    {
        final Collection<Project> expectedProjects = ImmutableList.<Project>of(new MockProject(555L));
        EasyMock.expect(projectManager.getProjectObjectsWithNoCategory())
                .andReturn(expectedProjects);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager);

        final Collection<Project> projects = resolver.getProjectsForCategory(new QueryLiteral());
        assertEquals(expectedProjects, projects);

        verify();
    }

    @Test
    public void testGetProjectsForCategoryLiteralResolves() throws Exception
    {
        final QueryLiteral inputLiteral = createLiteral(2L);
        final Collection<Project> expectedProjects = Collections.<Project>singleton(new MockProject(555L));
        final GenericValue expectedCategory = createMockProjectCategory(2L, "Cat");

        EasyMock.expect(projectManager.getProjectObjectsFromProjectCategory(2L))
                .andReturn(expectedProjects);

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager)
        {
            @Override
            public GenericValue getProjectCategory(final QueryLiteral literal)
            {
                assertEquals(inputLiteral, literal);
                return expectedCategory;
            }
        };

        final Collection<Project> projects = resolver.getProjectsForCategory(inputLiteral);
        assertEquals(expectedProjects, projects);

        verify();
    }

    @Test
    public void testGetProjectsForCategoryLiteralDoesntResolve() throws Exception
    {
        final QueryLiteral inputLiteral = createLiteral(2L);
        final Collection<Project> expectedProjects = Collections.emptySet();

        replay();
        ProjectCategoryResolver resolver = new ProjectCategoryResolver(projectManager)
        {
            @Override
            public GenericValue getProjectCategory(final QueryLiteral literal)
            {
                assertEquals(inputLiteral, literal);
                return null;
            }
        };

        final Collection<Project> projects = resolver.getProjectsForCategory(inputLiteral);
        assertEquals(expectedProjects, projects);

        verify();
    }

    private MockGenericValue createMockProjectCategory(final Long id, final String name)
    {
        return new MockGenericValue("ProjectCategory", ImmutableMap.of("id", id, "name", name));
    }
}
