package com.atlassian.jira.jql.validator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ComponentIndexInfoResolver;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestComponentValuesExistValidator
{
    private static final User ANONYMOUS = null;

    @Mock private JqlOperandResolver operandResolver;
    @Mock private ComponentIndexInfoResolver indexInfoResolver;
    @Mock private PermissionManager permissionManager;
    @Mock private ProjectComponentManager componentManager;
    @Mock private ProjectManager projectManager;
    @Mock private I18nHelper.BeanFactory beanFactory;

    @After
    public void tearDown()
    {
        operandResolver = null;
        indexInfoResolver = null;
        permissionManager = null;
        componentManager = null;
        projectManager = null;
        beanFactory = null;
    }


    @Test
    public void testNoComponentsExists() throws Exception
    {
        String name = "blah";

        when(indexInfoResolver.getIndexedValues(name)).thenReturn(ImmutableList.<String>of());

        ComponentValuesExistValidator validator = new Fixture();
        assertFalse(validator.stringValueExists(null, name));
    }

    @Test
    public void testComponentExistsAndHasPermission() throws Exception
    {
        String name = "blah";
        Long id = 10L;
        Long pid = 20L;
        MockProject project = new MockProject(pid);
        MockComponent component = new MockComponent(id, name, pid);

        when(indexInfoResolver.getIndexedValues(name)).thenReturn(ImmutableList.of(id.toString()));
        when(componentManager.find(id)).thenReturn(component);
        when(projectManager.getProjectObj(pid)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, ANONYMOUS)).thenReturn(true);

        ComponentValuesExistValidator validator = new Fixture();
        assertTrue(validator.stringValueExists(null, name));
    }

    @Test
    public void testComponentExistsAndNoHasPermission() throws Exception
    {
        String name = "blah";
        Long id = 10L;
        Long pid = 20L;
        MockProject project = new MockProject(pid);
        MockComponent component = new MockComponent(id, name, pid);

        when(indexInfoResolver.getIndexedValues(name)).thenReturn(ImmutableList.of(id.toString()));
        when(componentManager.find(id)).thenReturn(component);
        when(projectManager.getProjectObj(pid)).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, ANONYMOUS)).thenReturn(false);

        ComponentValuesExistValidator validator = new Fixture();
        assertFalse(validator.stringValueExists(null, name));
    }

    @Test
    public void testTwoComponentsExistsAndOneHasPermission() throws Exception
    {
        String name = "blah";
        Long id1 = 10L;
        Long id2 = 10L;
        Long pid1 = 20L;
        Long pid2 = 20L;
        MockProject project1 = new MockProject(pid1);
        MockProject project2 = new MockProject(pid2);
        MockComponent component1 = new MockComponent(id1, name, pid1);
        MockComponent component2 = new MockComponent(id2, name, pid2);

        when(indexInfoResolver.getIndexedValues(name)).thenReturn(ImmutableList.of(id1.toString(), id2.toString()));
        when(componentManager.find(id1)).thenReturn(component1);
        when(projectManager.getProjectObj(pid1)).thenReturn(project1);
        when(permissionManager.hasPermission(Permissions.BROWSE, project1, ANONYMOUS)).thenReturn(false);
        when(componentManager.find(id2)).thenReturn(component2);
        when(projectManager.getProjectObj(pid2)).thenReturn(project2);
        when(permissionManager.hasPermission(Permissions.BROWSE, project2, ANONYMOUS)).thenReturn(true);

        ComponentValuesExistValidator validator = new Fixture();
        assertTrue(validator.stringValueExists(null, name));
    }

    @Test
    public void testLongValueExist() throws Exception
    {
        Long id = 10L;
        when(indexInfoResolver.getIndexedValues(id)).thenReturn(ImmutableList.<String>of());

        final AtomicInteger called = new AtomicInteger(0);
        ComponentValuesExistValidator validator = new Fixture()
        {
            @Override
            boolean componentExists(final User searcher, final List<String> ids)
            {
                return called.incrementAndGet() == 1;
            }
        };

        assertTrue(validator.longValueExist(null, id));
        assertFalse(validator.longValueExist(null, id));

        assertEquals(2, called.get());
    }

    static class MockComponent implements ProjectComponent
    {
        private final Long id;
        private final String name;
        private final Long projectId;

        MockComponent(Long id, String name, Long projectId)
        {
            this.id = id;
            this.name = name;
            this.projectId = projectId;
        }

        public String getName()
        {
            return name;
        }

        public Long getId()
        {
            return id;
        }

        public Long getProjectId()
        {
            return projectId;
        }

        public String getDescription()
        {
            return null;
        }

        public String getLead()
        {
            return null;
        }

        @Override
        public ApplicationUser getComponentLead()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        public long getAssigneeType()
        {
            return 0;
        }

        public GenericValue getGenericValue()
        {
            return null;
        }
    }

    class Fixture extends ComponentValuesExistValidator
    {
        Fixture()
        {
            super(operandResolver, indexInfoResolver, permissionManager, componentManager, projectManager, beanFactory);
        }
    }
}
