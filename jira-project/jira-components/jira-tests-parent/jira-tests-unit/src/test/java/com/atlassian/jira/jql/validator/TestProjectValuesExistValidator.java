package com.atlassian.jira.jql.validator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestProjectValuesExistValidator extends MockControllerTestCase
{
    private JqlOperandResolver operandResolver;
    private ProjectIndexInfoResolver indexInfoResolver;
    private PermissionManager permissionManager;
    private ProjectManager projectManager;
    private I18nHelper.BeanFactory beanFactory;

    @Before
    public void setUp()
    {
        operandResolver = mockController.getMock(JqlOperandResolver.class);
        indexInfoResolver = mockController.getMock(ProjectIndexInfoResolver.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        projectManager = mockController.getMock(ProjectManager.class);
        beanFactory = mockController.getMock(I18nHelper.BeanFactory.class);
    }

    @Test
    public void testProjectExistsAndHasPermission() throws Exception
    {
        final String key = "proj";
        final Long id = 10L;

        MockProject project = new MockProject(id, key);

        indexInfoResolver.getIndexedValues(key);
        mockController.setReturnValue(Collections.singletonList(id.toString()));

        projectManager.getProjectObj(id);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project, (User) null);
        mockController.setReturnValue(true);

        ProjectValuesExistValidator validator = mockController.instantiate(ProjectValuesExistValidator.class);

        assertTrue(validator.stringValueExists(null, key));

        mockController.verify();

    }

    @Test
    public void testProjectExistsAndHasNoPermission() throws Exception
    {
        final String key = "proj";
        final Long id = 10L;

        MockProject project = new MockProject(id, key);

        indexInfoResolver.getIndexedValues(key);
        mockController.setReturnValue(Collections.singletonList(id.toString()));

        projectManager.getProjectObj(id);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project, (User) null);
        mockController.setReturnValue(false);

        ProjectValuesExistValidator validator = mockController.instantiate(ProjectValuesExistValidator.class);

        assertFalse(validator.stringValueExists(null, key));

        mockController.verify();
    }

    @Test
    public void testProjectDoesntExist() throws Exception
    {
        final String key = "proj";

        indexInfoResolver.getIndexedValues(key);
        mockController.setReturnValue(Collections.emptyList());

        ProjectValuesExistValidator validator = mockController.instantiate(ProjectValuesExistValidator.class);

        assertFalse(validator.stringValueExists(null, key));

        mockController.verify();

    }

    @Test
    public void testTwoProjectsExistsAndOneHasPermission() throws Exception
    {
        final String name = "proj";
        final Long id1 = 10L;
        final Long id2 = 20L;

        MockProject project1 = new MockProject(id1, "key", name);
        MockProject project2 = new MockProject(id2, "key", name);

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder(id1.toString(), id2.toString()).asList());

        projectManager.getProjectObj(id1);
        mockController.setReturnValue(project1);

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(false);

        projectManager.getProjectObj(id2);
        mockController.setReturnValue(project2);

        permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null);
        mockController.setReturnValue(true);

        ProjectValuesExistValidator validator = mockController.instantiate(ProjectValuesExistValidator.class);

        assertTrue(validator.stringValueExists(null, name));

        mockController.verify();
    }

    @Test
    public void testLongValueExist() throws Exception
    {
        Long id = 10L;
        indexInfoResolver.getIndexedValues(id);
        mockController.setReturnValue(Collections.emptyList());
        indexInfoResolver.getIndexedValues(id);
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);
        ProjectValuesExistValidator validator = new ProjectValuesExistValidator(operandResolver, indexInfoResolver, permissionManager, projectManager, beanFactory)
        {
            @Override
            boolean projectExists(final User searcher, final List<String> ids)
            {
                return called.incrementAndGet() == 1;
            }
        };

        assertTrue(validator.longValueExist(null, id));
        assertFalse(validator.longValueExist(null, id));

        assertEquals(2, called.get());
        mockController.verify();
    }


}
