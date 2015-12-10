package com.atlassian.jira.jql.validator;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.VersionIndexInfoResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.VersionManager;
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
public class TestVersionValuesExistValidator extends MockControllerTestCase
{
    private VersionIndexInfoResolver indexInfoResolver;
    private PermissionManager permissionManager;
    private VersionManager versionManager;
    private JqlOperandResolver operandResolver;
    private I18nHelper.BeanFactory beanFactory;

    @Before
    public void setUp()
    {
        operandResolver = mockController.getMock(JqlOperandResolver.class);
        indexInfoResolver = mockController.getMock(VersionIndexInfoResolver.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        versionManager = mockController.getMock(VersionManager.class);
        beanFactory = mockController.getMock(I18nHelper.BeanFactory.class);
    }

    @Test
    public void testVersionExistsAndHasPermssion() throws Exception
    {
        final String name = "blah";
        final Long id = 10L;
        final MockProject project = new MockProject();
        final MockVersion version = new MockVersion(id, name, project);

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder(id.toString()).asList());

        versionManager.getVersion(id);
        mockController.setReturnValue(version);

        permissionManager.hasPermission(Permissions.BROWSE, project, (User) null);
        mockController.setReturnValue(true);

        VersionValuesExistValidator validator = mockController.instantiate(VersionValuesExistValidator.class);
        assertTrue(validator.stringValueExists(null, name));

        mockController.verify();
    }

    @Test
    public void testVersionExistsAndHasNoPermssion() throws Exception
    {
        final String name = "blah";
        final Long id = 10L;
        final MockProject project = new MockProject();
        final MockVersion version = new MockVersion(id, name, project);

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder(id.toString()).asList());

        versionManager.getVersion(id);
        mockController.setReturnValue(version);

        permissionManager.hasPermission(Permissions.BROWSE, project, (User) null);
        mockController.setReturnValue(false);

        VersionValuesExistValidator validator = mockController.instantiate(VersionValuesExistValidator.class);
        assertFalse(validator.stringValueExists(null, name));

        mockController.verify();
    }

    @Test
    public void testTwoVersionsExistsAndOneHasNoPermssion() throws Exception
    {
        final String name = "blah";
        final Long id1 = 10L;
        final Long id2 = 10L;
        final MockProject project1 = new MockProject();
        final MockProject project2 = new MockProject();
        final MockVersion version1 = new MockVersion(id1, name, project1);
        final MockVersion version2 = new MockVersion(id2, name, project2);

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder(id1.toString(), id2.toString()).asList());

        versionManager.getVersion(id1);
        mockController.setReturnValue(version1);

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(false);

        versionManager.getVersion(id2);
        mockController.setReturnValue(version2);

        permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null);
        mockController.setReturnValue(true);

        VersionValuesExistValidator validator = mockController.instantiate(VersionValuesExistValidator.class);
        assertTrue(validator.stringValueExists(null, name));

        mockController.verify();
    }

    @Test
    public void testNoVersionExist() throws Exception
    {
        final String name = "blah";

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        VersionValuesExistValidator validator = mockController.instantiate(VersionValuesExistValidator.class);
        assertFalse(validator.stringValueExists(null, name));

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
        VersionValuesExistValidator validator = new VersionValuesExistValidator(operandResolver, indexInfoResolver, permissionManager, versionManager, beanFactory)
        {
            @Override
            boolean versionExists(final User searcher, final List<String> ids)
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
