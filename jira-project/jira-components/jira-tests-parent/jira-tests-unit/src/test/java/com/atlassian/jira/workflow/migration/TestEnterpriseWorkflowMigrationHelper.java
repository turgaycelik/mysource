package com.atlassian.jira.workflow.migration;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.task.StatefulTaskProgressSink;
import com.atlassian.jira.workflow.MockAssignableWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEnterpriseWorkflowMigrationHelper
{
    @Mock
    private SchemeManager expectedSchemeManager;

    @Mock
    private OfBizDelegator delegator;

    @Mock
    private ConstantsManager constantsManager;

    @Mock
    private OfBizListIterator listIterator;

    @Mock
    private WorkflowSchemeManager workflowSchemeManager;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private ProjectManager projectManager;

    private final Project expectedProject = new MockProject(123456789L, "KEY", "Name");
    private final MockAssignableWorkflowScheme expectedScheme = new MockAssignableWorkflowScheme(10101L, "WorkflowScheme");

    @Before
    public void setup()
    {
        Mockito.stub(constantsManager.getAllIssueTypes()).toReturn(Collections.<GenericValue>emptyList());
    }

    @Test
    public void testCannotQuickMigrate() throws GenericEntityException
    {
        AssignableWorkflowSchemeMigrationHelper migrationHelper = new AssignableWorkflowSchemeMigrationHelper(expectedProject, expectedScheme, null,
                delegator, null, null, null, constantsManager, null, null, workflowSchemeManager, null)
        {
            @Override
            public boolean isHaveIssuesToMigrate()
            {
                return true;
            }

            @Override
            void assignSchemeToTemplate(StatefulTaskProgressSink migrationSink)
            {
                throw new RuntimeException("this shouldn't be called!");
            }

            @Override
            void assignSchemeToProjectTemplate(StatefulTaskProgressSink migrationSink) throws GenericEntityException
            {
                throw new RuntimeException("this shouldn't be called!");
            }
        };
        Assert.assertFalse(migrationHelper.doQuickMigrate());
    }

    @Test
    public void testCanQuickMigrate() throws GenericEntityException
    {
        final AtomicBoolean assigned = new AtomicBoolean(false);
        AssignableWorkflowSchemeMigrationHelper migrationHelper = new AssignableWorkflowSchemeMigrationHelper(expectedProject, expectedScheme, null,
                delegator, expectedSchemeManager, null, null, constantsManager, null, null, workflowSchemeManager, eventPublisher)
        {
            @Override
            public boolean isHaveIssuesToMigrate()
            {
                return false;
            }

            @Override
            void assignSchemeToProjectTemplate(StatefulTaskProgressSink migrationSink) throws GenericEntityException
            {
                throw new RuntimeException("this shouldn't be called!");
            }

            @Override
            void assignSchemeToTemplate(StatefulTaskProgressSink migrationSink)
            {
                assigned.set(true);
            }
        };
        Assert.assertTrue(migrationHelper.doQuickMigrate());
        Assert.assertTrue(assigned.get());
    }

    @Test
    public void testHasIssuesToMigrate() throws GenericEntityException
    {
        ArgumentCaptor<EntityFieldMap> captor = ArgumentCaptor.forClass(EntityFieldMap.class);
        Mockito.stub(listIterator.next()).toReturn(new MockGenericValue(""));
        Mockito.stub(delegator.findListIteratorByCondition(Mockito.eq("Issue"), captor.capture())).toReturn(listIterator);
        AssignableWorkflowSchemeMigrationHelper migrationHelper = new AssignableWorkflowSchemeMigrationHelper(expectedProject, expectedScheme, null,
                delegator, null, null, null, constantsManager, null, null, workflowSchemeManager, null);
        Assert.assertTrue(migrationHelper.isHaveIssuesToMigrate());
        EntityFieldMap entityFieldMap = captor.getValue();
        Assert.assertEquals(expectedProject.getId(), entityFieldMap.getField("project"));
        Mockito.verify(listIterator, Mockito.times(1)).close();
    }

    @Test
    public void testHasNoIssuesToMigrate() throws GenericEntityException
    {
        ArgumentCaptor<EntityFieldMap> captor = ArgumentCaptor.forClass(EntityFieldMap.class);
        Mockito.stub(listIterator.next()).toReturn(null);
        Mockito.stub(delegator.findListIteratorByCondition(Mockito.eq("Issue"), captor.capture())).toReturn(listIterator);
        AssignableWorkflowSchemeMigrationHelper migrationHelper = new AssignableWorkflowSchemeMigrationHelper(expectedProject, expectedScheme, null,
                delegator, null, null, null, constantsManager, null, null, workflowSchemeManager, null);
        Assert.assertFalse(migrationHelper.isHaveIssuesToMigrate());
        EntityFieldMap entityFieldMap = captor.getValue();
        Assert.assertEquals(expectedProject.getId(), entityFieldMap.getField("project"));
        Mockito.verify(listIterator, Mockito.times(1)).close();
    }
}
