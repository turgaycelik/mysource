package com.atlassian.jira.workflow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.config.Configuration;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.MockClassControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/** This tests the OSWorkflowManager. Currently just tests migrateStatus changing of the updated date */
@RunWith(MockitoJUnitRunner.class)
public class TestOSWorkflowManager
{
    private static final String ID_2 = "2";
    private static final String ID_1 = "1";

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @org.mockito.Mock
    @AvailableInContainer
    private UserManager userManager;

    private Mock mockConfiguration;
    private OSWorkflowManager osWorkflowManager;
    private DraftWorkflowStore draftWorkflowStore;
    private EventPublisher eventPublisher;
    private JiraAuthenticationContext ctx;

    @org.mockito.Mock
    private WorkflowSchemeManager workflowSchemeManager;

    private ApplicationUser applicationUser = new MockApplicationUser("userkey", "testuser", "Testy Tester", "test@test.com");

    @Before
    public void setUp() throws Exception
    {
        mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        draftWorkflowStore = EasyMock.createMock(DraftWorkflowStore.class);
        eventPublisher = EasyMock.createNiceMock(EventPublisher.class);

        ctx = new MockSimpleAuthenticationContext(new MockUser("Something"), Locale.ENGLISH, new NoopI18nHelper());
        
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(), draftWorkflowStore, eventPublisher, null, null, ctx);


        when(userManager.getUserByName(applicationUser.getUsername())).thenReturn(applicationUser);
    }

    @After
    public void tearDown() throws Exception
    {
        mockConfiguration.verify();
        osWorkflowManager = null;
        mockConfiguration = null;
    }

    @Test
    public void testMigrateIssueToWorkflowSetUpdatedDate()
    {
        final MockGenericValue issueGV = updateIssue(ID_1, ID_2);

        assertNotNull(issueGV.get("updated"));
        assertEquals(ID_2, issueGV.getString("status"));
    }

    @Test
    public void testMigrateIssueToWorkflowDoNotSetUpdatedDate()
    {
        final MockGenericValue issueGV = updateIssue(ID_1, ID_1);

        assertNull(issueGV.get("updated"));
        assertEquals(ID_1, issueGV.getString("status"));
    }

    @Test
    public void testSaveWorkflowWithLastAuthorAndDate() throws WorkflowException
    {
        final Mock mockJiraWorkFlow = new Mock(JiraWorkflow.class);
        final Map metaAttributes = new HashMap();

        final MockControl mockWorkflowDescriptorControl = MockClassControl.createControl(WorkflowDescriptor.class);
        final WorkflowDescriptor mockWorkflowDescriptor = (WorkflowDescriptor) mockWorkflowDescriptorControl.getMock();
        mockWorkflowDescriptor.getMetaAttributes();
        mockWorkflowDescriptorControl.setDefaultReturnValue(metaAttributes);
        mockWorkflowDescriptorControl.replay();

        mockJiraWorkFlow.expectAndReturn("getDescriptor", mockWorkflowDescriptor);
        mockJiraWorkFlow.expectAndReturn("getName", "testWorkflow");
        mockJiraWorkFlow.expectVoid("reset");

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher,null, null, ctx)
        {

            @Override
            public boolean isActive(final JiraWorkflow workflow) throws WorkflowException
            {
                return false;
            }

        };
        osWorkflowManager.updateWorkflow("testuser", (JiraWorkflow) mockJiraWorkFlow.proxy());

        assertEquals(2, metaAttributes.size());
        assertEquals(applicationUser.getKey(), metaAttributes.get(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY));
        assertNotNull(metaAttributes.get(JiraWorkflow.JIRA_META_UPDATED_DATE));
        mockWorkflowDescriptorControl.verify();
        mockJiraWorkFlow.verify();
    }

    @Test
    public void testSaveWorkflowWithoutAuditTrail() throws WorkflowException
    {
        final Mock mockJiraWorkFlow = new Mock(JiraWorkflow.class);
        final Map metaAttributes = new HashMap();

        final MockControl mockWorkflowDescriptorControl = MockClassControl.createControl(WorkflowDescriptor.class);
        final WorkflowDescriptor mockWorkflowDescriptor = (WorkflowDescriptor) mockWorkflowDescriptorControl.getMock();
        mockWorkflowDescriptorControl.replay();

        mockJiraWorkFlow.expectAndReturn("getDescriptor", mockWorkflowDescriptor);
        mockJiraWorkFlow.expectAndReturn("getName", "testWorkflow");
        mockJiraWorkFlow.expectVoid("reset");

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx)
        {

            @Override
            public boolean isActive(final JiraWorkflow workflow) throws WorkflowException
            {
                return false;
            }
        };
        osWorkflowManager.saveWorkflowWithoutAudit((JiraWorkflow) mockJiraWorkFlow.proxy());

        assertEquals(0, metaAttributes.size());
        mockWorkflowDescriptorControl.verify();
        mockJiraWorkFlow.verify();
    }

    @Test
    public void testSaveWorkflowWithoutAuditTrailAndActiveWorkflow() throws WorkflowException
    {
        final Mock mockJiraWorkFlow = new Mock(JiraWorkflow.class);
        final Map metaAttributes = new HashMap();

        final MockControl mockWorkflowDescriptorControl = MockClassControl.createControl(WorkflowDescriptor.class);
        final WorkflowDescriptor mockWorkflowDescriptor = (WorkflowDescriptor) mockWorkflowDescriptorControl.getMock();
        mockWorkflowDescriptorControl.replay();

        mockJiraWorkFlow.expectAndReturn("getDescriptor", mockWorkflowDescriptor);
        mockJiraWorkFlow.expectAndReturn("getName", "testWorkflow");
        mockJiraWorkFlow.expectVoid("reset");

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx)
        {

            @Override
            public boolean isActive(final JiraWorkflow workflow) throws WorkflowException
            {
                return true;
            }
        };
        osWorkflowManager.saveWorkflowWithoutAudit((JiraWorkflow) mockJiraWorkFlow.proxy());

        assertEquals(0, metaAttributes.size());
        mockWorkflowDescriptorControl.verify();
        mockJiraWorkFlow.verify();
    }

    @Test
    public void testSaveWorkflowWithoutAuditTrailAndDraftWorkflow() throws WorkflowException
    {
        final Mock mockJiraWorkFlow = new Mock(JiraWorkflow.class);
        final Map metaAttributes = new HashMap();

        final MockControl mockWorkflowDescriptorControl = MockClassControl.createControl(WorkflowDescriptor.class);
        final WorkflowDescriptor mockWorkflowDescriptor = (WorkflowDescriptor) mockWorkflowDescriptorControl.getMock();
        mockWorkflowDescriptorControl.replay();

        mockJiraWorkFlow.expectAndReturn("isDraftWorkflow", Boolean.TRUE);
        mockJiraWorkFlow.expectAndReturn("getName", "Test");

        JiraWorkflow workflow = (JiraWorkflow) mockJiraWorkFlow.proxy();
        EasyMock.expect(draftWorkflowStore.updateDraftWorkflowWithoutAudit("Test", workflow)).andReturn(workflow);

        EasyMock.replay(draftWorkflowStore);

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), draftWorkflowStore, eventPublisher, null, null, ctx)
        {

            @Override
            public boolean isActive(final JiraWorkflow workflow) throws WorkflowException
            {
                return true;
            }
        };
        osWorkflowManager.saveWorkflowWithoutAudit((JiraWorkflow) mockJiraWorkFlow.proxy());

        EasyMock.verify(draftWorkflowStore);
        mockWorkflowDescriptorControl.verify();
        mockJiraWorkFlow.verify();
    }

    @Test
    public void testGetDraftWorkflow()
    {
        final Mock draftWorkflowStoreMock = new Mock(DraftWorkflowStore.class);
        final Mock jiraWorfklowMock = new Mock(JiraWorkflow.class);

        draftWorkflowStoreMock.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("testworkflow") }, null);

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) draftWorkflowStoreMock.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return (JiraWorkflow) jiraWorfklowMock.proxy();
            }
        };
        osWorkflowManager.getDraftWorkflow("testworkflow");

        draftWorkflowStoreMock.verify();
        jiraWorfklowMock.verify();
    }

    @Test
    public void testGetDraftWorkflowWithNullParentWorkflow()
    {
        final Mock draftWorkflowStoreMock = new Mock(DraftWorkflowStore.class);

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) draftWorkflowStoreMock.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return null;
            }
        };
        try
        {
            osWorkflowManager.getDraftWorkflow("testworkflow");
            fail("expected exception not thrown");
        }
        catch (final IllegalArgumentException e)
        {
            //expected exception, since no parent workflow exists.
        }

        draftWorkflowStoreMock.verify();
    }

    @Test
    public void testCreateDraftWorkflow() throws WorkflowException
    {
        final Mock draftWorkflowStoreMock = new Mock(DraftWorkflowStore.class);
        final Mock jiraWorfklowMock = new Mock(JiraWorkflow.class);
        jiraWorfklowMock.expectAndReturn("isActive", Boolean.TRUE);

        draftWorkflowStoreMock.expectAndReturn("createDraftWorkflow", new Constraint[] { P.eq(applicationUser), P.eq(jiraWorfklowMock.proxy()) }, null);

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) draftWorkflowStoreMock.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return (JiraWorkflow) jiraWorfklowMock.proxy();
            }
        };
        osWorkflowManager.createDraftWorkflow("testuser", "testworkflow");

        draftWorkflowStoreMock.verify();
        jiraWorfklowMock.verify();
    }

    @Test
    public void testCreateDraftWorkflowWithNoParentWorkflow() throws WorkflowException
    {
        final Mock draftWorkflowStoreMock = new Mock(DraftWorkflowStore.class);

        draftWorkflowStoreMock.expectAndReturn("createDraftWorkflow", new Constraint[] { P.eq("testuser"), P.eq("testworkflow") }, null);

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) draftWorkflowStoreMock.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return null;
            }
        };
        try
        {
            osWorkflowManager.createDraftWorkflow("testuser", "testworkflow");
            fail("should have thrown exception");
        }
        catch (final RuntimeException e)
        {
            assertEquals("You can not create a draft workflow from a parent that does not exist.", e.getMessage());
        }
    }

    @Test
    public void testCreateDraftWorkflowWithInActiveParentWorkflow() throws WorkflowException
    {
        final Mock draftWorkflowStoreMock = new Mock(DraftWorkflowStore.class);
        final Mock jiraWorfklowMock = new Mock(JiraWorkflow.class);
        jiraWorfklowMock.expectAndReturn("isActive", Boolean.FALSE);

        draftWorkflowStoreMock.expectAndReturn("createDraftWorkflow", new Constraint[] { P.eq("testuser"), P.eq("testworkflow") }, null);

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) draftWorkflowStoreMock.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return (JiraWorkflow) jiraWorfklowMock.proxy();
            }
        };
        try
        {
            osWorkflowManager.createDraftWorkflow("testuser", "testworkflow");
            fail("should have thrown exception");
        }
        catch (final RuntimeException e)
        {
            assertEquals("You can not create a draft workflow from a parent workflow that is not active.", e.getMessage());
        }
    }

    @Test
    public void testCreateDraftWorkflowWithNullUsername()
    {
        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx);
        try
        {
            osWorkflowManager.createDraftWorkflow((String)null, "testworkflow");
            fail("should have thrown exception");
        }
        catch (final IllegalArgumentException e)
        {

        }
    }

    @Test
    public void testDeleteWorkflowDeletesAssociatedDraftWorkflow()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.expectAndReturn("getName", "testworkflow");

        final AtomicBoolean deleteCalled = new AtomicBoolean(false);
        final Mock mockConfig = new Mock(Configuration.class);
        mockConfig.setStrict(true);
        mockConfig.expectAndReturn("removeWorkflow", new Constraint[] { P.eq("testworkflow") }, Boolean.TRUE);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx)
        {
            @Override
            public boolean deleteDraftWorkflow(final String parentWorkflowName) throws IllegalArgumentException
            {
                deleteCalled.set(true);
                return true;
            }

            @Override
            public boolean isActive(final JiraWorkflow workflow) throws WorkflowException
            {
                return false;
            }

            @Override
            protected WorkflowSchemeManager getWorkflowSchemeManager()
            {
                return workflowSchemeManager;
            }
        };


        JiraWorkflow workflow = (JiraWorkflow) mockJiraWorkflow.proxy();
        when(workflowSchemeManager.getSchemesForWorkflowIncludingDrafts(workflow))
                .thenReturn(Collections.<WorkflowScheme>emptyList());
        osWorkflowManager.deleteWorkflow(workflow);
        // Assert that we called to the deleteDraftWorkflow() method
        assertTrue(deleteCalled.get());

        mockJiraWorkflow.verify();

        Collection<WorkflowScheme> schemes = Arrays.<WorkflowScheme>asList(new MockAssignableWorkflowScheme(10101938L, "Test"));
        when(workflowSchemeManager.getSchemesForWorkflowIncludingDrafts(workflow)).thenReturn(schemes);

        boolean thrown = false;
        try
        {
            osWorkflowManager.deleteWorkflow(workflow);
        }
        catch (WorkflowException e)
        {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testDeleteDraftWorkflow()
    {
        WorkflowDescriptor mockDescriptor = new DescriptorFactory().createWorkflowDescriptor();
        MockJiraWorkflow mockDraft = new MockJiraWorkflow();
        mockDraft.setName("testworkflow");
        mockDraft.setWorkflowDescriptor(mockDescriptor);

        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("testworkflow") }, mockDraft);
        mockDraftWorkflowStore.expectAndReturn("deleteDraftWorkflow", new Constraint[] { P.eq("testworkflow") }, Boolean.TRUE);


        final Mock mockConfig = new Mock(Configuration.class);
        mockConfig.expectAndReturn("getWorkflow", new Constraint[] { P.eq("testworkflow") }, mockDescriptor);

        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx);

        osWorkflowManager.deleteDraftWorkflow("testworkflow");

        mockDraftWorkflowStore.verify();
    }

    @Test
    public void testDeleteDraftWorkflowWithNullParentName()
    {
        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx);

        try
        {
            osWorkflowManager.deleteDraftWorkflow(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //happy
        }
    }

    @Test
    public void testUpdateDraftWorkflow()
    {
        final Mock mockConfig = new Mock(Configuration.class);
        final Mock jiraWorfklowMock = new Mock(JiraWorkflow.class);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflow.getDescriptor();
        mockJiraDraftWorkflowControl.setReturnValue(descriptor);
        mockJiraDraftWorkflow.isDraftWorkflow();
        mockJiraDraftWorkflowControl.setReturnValue(true);
        mockJiraDraftWorkflow.getName();
        mockJiraDraftWorkflowControl.setReturnValue("parentWorkflow", 3);

        mockJiraDraftWorkflowControl.replay();

        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.expectAndReturn("updateDraftWorkflow",
            new Constraint[] { P.eq(applicationUser), P.eq("parentWorkflow"), P.eq(mockJiraDraftWorkflow) }, null);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return (JiraWorkflow) jiraWorfklowMock.proxy();
            }
        };
        osWorkflowManager.updateWorkflow("testuser", mockJiraDraftWorkflow);

        mockJiraDraftWorkflowControl.verify();
        mockDraftWorkflowStore.verify();
    }

    @Test
    public void testUpdateWorkflowNullWorkflow()
    {
        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx);
        try
        {
            osWorkflowManager.updateWorkflow("testuser", null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //yay
            assertEquals("Can not update a workflow with a null workflow/descriptor.", e.getMessage());
        }
    }

    @Test
    public void testUpdateDraftWorkflowWithNonExistentParentWorkflow()
    {
        final Mock mockConfig = new Mock(Configuration.class);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflow.getDescriptor();
        mockJiraDraftWorkflowControl.setReturnValue(descriptor);
        mockJiraDraftWorkflow.isDraftWorkflow();
        mockJiraDraftWorkflowControl.setReturnValue(true);
        mockJiraDraftWorkflow.getName();
        mockJiraDraftWorkflowControl.setReturnValue("parentWorkflow", 3);
        mockJiraDraftWorkflowControl.replay();

        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return null;
            }
        };
        try
        {
            osWorkflowManager.updateWorkflow("testuser", mockJiraDraftWorkflow);
            fail();
        }
        catch (final Exception e)
        {
            assertEquals("You can not update a draft workflow for a parent that does not exist.", e.getMessage());
        }
    }

    @Test
    public void testUpdateWorkflow()
    {
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockConfig = new Mock(Configuration.class);
        mockConfig.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("parentWorkflow"), P.eq(descriptor), P.IS_TRUE }, Boolean.TRUE);
        //.saveWorkflow(workflow.getName(), workflow.getDescriptor(), true);
        final MockControl mockJiraWorkflowControl = MockClassControl.createControl(JiraWorkflow.class);
        final JiraWorkflow mockJiraWorkflow = (JiraWorkflow) mockJiraWorkflowControl.getMock();

        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);

        mockJiraWorkflow.getDescriptor();
        mockJiraWorkflowControl.setDefaultReturnValue(descriptor);
        mockJiraWorkflow.isDraftWorkflow();
        mockJiraWorkflowControl.setDefaultReturnValue(false);
        mockJiraWorkflow.getName();
        mockJiraWorkflowControl.setDefaultReturnValue("parentWorkflow");
        mockJiraWorkflow.isSystemWorkflow();
        mockJiraWorkflowControl.setReturnValue(false);
        mockJiraWorkflow.isActive();
        mockJiraWorkflowControl.setReturnValue(false);
        mockJiraWorkflow.reset();
        mockJiraWorkflowControl.replay();

        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return null;
            }
        };
        osWorkflowManager.updateWorkflow("testuser", mockJiraWorkflow);

        mockDraftWorkflowStore.verify();
        mockConfig.verify();
        mockJiraWorkflowControl.verify();
    }

    @Test
    public void testUpdateSystemWorkflow()
    {
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockConfig = new Mock(Configuration.class);
        mockConfig.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("parentWorkflow"), P.eq(descriptor), P.IS_TRUE }, Boolean.TRUE);
        //.saveWorkflow(workflow.getName(), workflow.getDescriptor(), true);
        final MockControl mockJiraWorkflowControl = MockClassControl.createControl(JiraWorkflow.class);
        final JiraWorkflow mockJiraWorkflow = (JiraWorkflow) mockJiraWorkflowControl.getMock();

        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.expectAndReturn("deleteDraftWorkflow", new Constraint[] { P.eq("parentWorkflow") }, Boolean.TRUE);

        mockJiraWorkflow.getDescriptor();
        mockJiraWorkflowControl.setDefaultReturnValue(descriptor);
        mockJiraWorkflow.isDraftWorkflow();
        mockJiraWorkflowControl.setDefaultReturnValue(false);
        mockJiraWorkflow.getName();
        mockJiraWorkflowControl.setDefaultReturnValue("parentWorkflow");
        mockJiraWorkflow.isSystemWorkflow();
        mockJiraWorkflowControl.setReturnValue(true);
        mockJiraWorkflow.isActive();
        mockJiraWorkflowControl.setReturnValue(false);
        mockJiraWorkflow.reset();
        mockJiraWorkflowControl.replay();

        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return null;
            }
        };
        try
        {
            osWorkflowManager.updateWorkflow("testuser", mockJiraWorkflow);
            fail("Should have thrown an exception.");
        }
        catch (final WorkflowException e)
        {
            assertEquals("Cannot change the system workflow.", e.getMessage());
        }
    }

    @Test
    public void testUpdateActiveWorkflow()
    {
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockConfig = new Mock(Configuration.class);
        mockConfig.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("parentWorkflow"), P.eq(descriptor), P.IS_TRUE }, Boolean.TRUE);
        //.saveWorkflow(workflow.getName(), workflow.getDescriptor(), true);
        final MockControl mockJiraWorkflowControl = MockClassControl.createControl(JiraWorkflow.class);
        final JiraWorkflow mockJiraWorkflow = (JiraWorkflow) mockJiraWorkflowControl.getMock();

        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.expectAndReturn("deleteDraftWorkflow", new Constraint[] { P.eq("parentWorkflow") }, Boolean.TRUE);

        mockJiraWorkflow.getDescriptor();
        mockJiraWorkflowControl.setDefaultReturnValue(descriptor);
        mockJiraWorkflow.isDraftWorkflow();
        mockJiraWorkflowControl.setDefaultReturnValue(false);
        mockJiraWorkflow.getName();
        mockJiraWorkflowControl.setDefaultReturnValue("parentWorkflow");
        mockJiraWorkflow.isSystemWorkflow();
        mockJiraWorkflowControl.setReturnValue(false);
        mockJiraWorkflow.isActive();
        mockJiraWorkflowControl.setReturnValue(true);
        mockJiraWorkflow.reset();
        mockJiraWorkflowControl.replay();

        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflow(final String name)
            {
                return null;
            }
        };
        try
        {
            osWorkflowManager.updateWorkflow("testuser", mockJiraWorkflow);
            fail("Should have thrown an exception.");
        }
        catch (final WorkflowException e)
        {
            assertEquals("Cannot save an active workflow.", e.getMessage());
        }
    }

    @Test
    public void testUpdateDraftWorkflowWithNullWorkflowDescriptor()
    {
        final Mock mockConfig = new Mock(Configuration.class);
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflow.getDescriptor();
        mockJiraDraftWorkflowControl.setReturnValue(null);
        mockJiraDraftWorkflowControl.replay();

        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx);
        try
        {
            osWorkflowManager.updateWorkflow("testuser", mockJiraDraftWorkflow);
            fail();
        }
        catch (final Exception e)
        {
            assertEquals("Can not update a workflow with a null workflow/descriptor.", e.getMessage());
        }
    }

    @Test
    public void testUpdateDraftWorkflowNullUsername()
    {
        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx);

        try
        {
            osWorkflowManager.updateWorkflow((String)null, null);
            fail();
        }
        catch (final Exception e)
        {
            assertEquals("Can not update a workflow with a null username.", e.getMessage());
        }
    }

    private MockGenericValue updateIssue(final String oldStatus, final String newStatus)
    {
        final Map issueFields = EasyMap.build("status", oldStatus);
        final MockGenericValue issueGV = new MockGenericValue("Issue", issueFields);

        final Map statusFields = EasyMap.build("id", newStatus);
        final MockGenericValue statusGV = new MockGenericValue("Status", statusFields);

        final Mock mockConfig = new Mock(Configuration.class);
        osWorkflowManager = new OSWorkflowManager((Configuration) mockConfig.proxy(), null, eventPublisher, null, null, ctx);

        osWorkflowManager.updateIssueStatusAndUpdatedDate(issueGV, statusGV);
        return issueGV;
    }

    @Test
    public void testGetWorkflowNullName()
    {
        // Creat a mock Configuration
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        mockConfiguration.expectAndThrow("getWorkflow", P.IS_NULL, new IllegalArgumentException());

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(), null, eventPublisher, null, null, ctx);
        try
        {
            osWorkflowManager.getWorkflow((String) null);
            fail("Should have thrown exception");
        }
        catch (final Exception e)
        {
            //yay
        }
    }

    @Test
    public void testGetDefaultWorkflow()
    {
        // Creat a mock Configuration
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockConfiguration.expectAndReturn("getWorkflow", new Constraint[] { P.eq("jira") }, descriptor);

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(), null, eventPublisher, null, null, ctx);

        final JiraWorkflow workflow = osWorkflowManager.getWorkflow("jira");
        assertTrue(workflow instanceof DefaultJiraWorkflow);
        assertEquals(descriptor, workflow.getDescriptor());
    }

    @Test
    public void testGetWorkflow()
    {
        // Creat a mock Configuration
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockConfiguration.expectAndReturn("getWorkflow", new Constraint[] { P.eq("someWorkflow") }, descriptor);

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(), null, eventPublisher, null, null, ctx);

        final JiraWorkflow workflow = osWorkflowManager.getWorkflow("someWorkflow");
        assertTrue(workflow instanceof ConfigurableJiraWorkflow);
        assertEquals(descriptor, workflow.getDescriptor());
    }

    @Test
    public void testGetWorflowClone()
    {
        // Creat a mock Configuration
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockConfiguration.expectAndReturn("getWorkflow", new Constraint[] { P.eq("someWorkflow") }, new ImmutableWorkflowDescriptor(descriptor));

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(), null, eventPublisher, null, null, ctx)
        {
            //package level protected for testing.
            @Override
            WorkflowDescriptor cloneDescriptor(final WorkflowDescriptor workflowDescriptor) throws FactoryException
            {
                return new DescriptorFactory().createWorkflowDescriptor();
            }
        };

        final JiraWorkflow workflow = osWorkflowManager.getWorkflowClone("someWorkflow");
        assertTrue(workflow instanceof ConfigurableJiraWorkflow);
        assertNotSame(descriptor, workflow.getDescriptor());
        assertFalse(workflow.getDescriptor() instanceof ImmutableWorkflowDescriptor);
    }

    @Test
    public void testGetDefaultWorkflowClone()
    {
        // Creat a mock Configuration
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockConfiguration.expectAndReturn("getWorkflow", new Constraint[] { P.eq("jira") }, descriptor);

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(), null, eventPublisher, null, null, ctx);

        final JiraWorkflow workflow = osWorkflowManager.getWorkflowClone("jira");
        assertTrue(workflow instanceof DefaultJiraWorkflow);
        assertEquals(descriptor, workflow.getDescriptor());
    }

    @Test
    public void testOverwriteActiveWorkflowWithNoDraftWorkflow()
    {
        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);
        mockDraftWorkflowStore.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("atestworkflow") }, null);

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager(null, (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx);

        try
        {
            osWorkflowManager.overwriteActiveWorkflow("merlin", "atestworkflow");
            fail("Should have thrown exception");
        }
        catch (final WorkflowException e)
        {
            assertEquals("No draft workflow named 'atestworkflow'", e.getMessage());
        }
        mockDraftWorkflowStore.verify();
    }

    @Test
    public void testOverwriteActiveWorkflow()
    {
        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);
        final WorkflowDescriptor originalDescriptor = new DescriptorFactory().createWorkflowDescriptor();
        final JiraWorkflow jiraWorkflow = new JiraDraftWorkflow("dupa", null, originalDescriptor);
        mockDraftWorkflowStore.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("atestworkflow") }, jiraWorkflow);
        mockDraftWorkflowStore.expectAndReturn("deleteDraftWorkflow", new Constraint[] { P.eq("atestworkflow") }, Boolean.TRUE);

        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        mockConfiguration.expectAndReturn("getWorkflow", new Constraint[] {P.eq("atestworkflow")}, originalDescriptor);
        final Constraint constraint = new Constraint()
        {

            public boolean eval(final Object o)
            {
                final WorkflowDescriptor workflowDescriptor = (WorkflowDescriptor) o;
                final String userKey = (String) workflowDescriptor.getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY);
                //check the descriptor is the same instance as from the draft workflow.  Also check the updated user and time
                //stamp meta tags were added.
                return (originalDescriptor == workflowDescriptor) && userKey.equals(applicationUser.getKey()) && workflowDescriptor.getMetaAttributes().containsKey(
                    "jira.updated.date");
            }
        };
        mockConfiguration.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("atestworkflow"), constraint, P.IS_TRUE }, Boolean.TRUE);

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(),
            (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx);

        osWorkflowManager.overwriteActiveWorkflow(applicationUser.getUsername(), "atestworkflow");
        mockDraftWorkflowStore.verify();
        mockConfiguration.verify();
    }

    @Test
    public void testOverwriteActiveWorkflowSaveReturnsFalse()
    {
        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);
        final WorkflowDescriptor originalDescriptor = new DescriptorFactory().createWorkflowDescriptor();
        final JiraWorkflow jiraWorkflow = new JiraDraftWorkflow(null, null, originalDescriptor);
        mockDraftWorkflowStore.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("atestworkflow") }, jiraWorkflow);

        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        mockConfiguration.expectAndReturn("getWorkflow", new Constraint[] { P.eq("atestworkflow") }, originalDescriptor);
        final Constraint constraint = new Constraint()
        {

            public boolean eval(final Object o)
            {
                final WorkflowDescriptor workflowDescriptor = (WorkflowDescriptor) o;
                final String userKey = (String) workflowDescriptor.getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY);
                //check the descriptor is the same instance as from the draft workflow.  Also check the updated user and time
                //stamp meta tags were added.
                return (originalDescriptor == workflowDescriptor) && userKey.equals(applicationUser.getKey()) && workflowDescriptor.getMetaAttributes().containsKey(
                    "jira.updated.date");
            }
        };
        mockConfiguration.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("atestworkflow"), constraint, P.IS_TRUE }, Boolean.FALSE);

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(),
            (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx);

        try
        {
            osWorkflowManager.overwriteActiveWorkflow(applicationUser.getUsername(), "atestworkflow");
            fail("Should have thrown a workflow exception");
        }
        catch (final WorkflowException e)
        {
            assertEquals("Workflow 'atestworkflow' could not be overwritten!", e.getMessage());
        }
        mockDraftWorkflowStore.verify();
        mockConfiguration.verify();
    }

    @Test
    public void testCopyWorkflow()
    {
        final WorkflowDescriptor workflowDescriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("Copy of Workflow"), P.eq(workflowDescriptor), P.IS_TRUE },
            Boolean.TRUE);
        mockConfiguration.setStrict(true);
        final OSWorkflowManager workflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(), null, eventPublisher, null, null, ctx)
        {
            //package level protected for testing.
            @Override
            WorkflowDescriptor cloneDescriptor(final WorkflowDescriptor workflowDescriptor) throws FactoryException
            {
                return workflowDescriptor;
            }
        };
        final MockJiraWorkflow mockJiraWorkflow = new MockJiraWorkflow();
        mockJiraWorkflow.setWorkflowDescriptor(workflowDescriptor);
        workflowManager.copyWorkflow("testuser", "Copy of Workflow", "Workflow Desc", mockJiraWorkflow);
        //basically all we can check is that saveWorkflow was called with the right params.
        mockConfiguration.verify();
    }

    @Test
    public void testCopyWorkflowNullDescription()
    {
        final WorkflowDescriptor workflowDescriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("Copy of Workflow"), P.eq(workflowDescriptor), P.IS_TRUE },
            Boolean.TRUE);
        mockConfiguration.setStrict(true);
        final OSWorkflowManager workflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(), null, eventPublisher, null, null, ctx)
        {
            //package level protected for testing.
            @Override
            WorkflowDescriptor cloneDescriptor(final WorkflowDescriptor workflowDescriptor) throws FactoryException
            {
                return workflowDescriptor;
            }
        };
        final MockJiraWorkflow mockJiraWorkflow = new MockJiraWorkflow();
        mockJiraWorkflow.setWorkflowDescriptor(workflowDescriptor);
        workflowManager.copyWorkflow("testuser", "Copy of Workflow", null, mockJiraWorkflow);
        //basically all we can check is that saveWorkflow was called with the right params.
        mockConfiguration.verify();
    }

    @Test
    public void testEditWorkflowWithNoChanges() throws WorkflowException
    {
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);
        final MockControl mockConfigurableJiraWorkflowControl = MockClassControl.createControl(ConfigurableJiraWorkflow.class);
        final ConfigurableJiraWorkflow mockConfigurableJiraWorkflow = (ConfigurableJiraWorkflow) mockConfigurableJiraWorkflowControl.getMock();
        mockConfigurableJiraWorkflow.getDescription();
        mockConfigurableJiraWorkflowControl.setReturnValue("sameDescription");
        mockConfigurableJiraWorkflowControl.replay();

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(),
            (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflowClone(final String name)
            {
                return mockConfigurableJiraWorkflow;
            }
        };
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("getName", "workflow1");
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        osWorkflowManager.updateWorkflowNameAndDescription("testuser", (JiraWorkflow) mockCurrentWorkflow.proxy(), "workflow1", "sameDescription");

        mockConfigurableJiraWorkflowControl.verify();
        mockDraftWorkflowStore.verify();
        mockConfiguration.verify();
    }

    @Test
    public void testEditWorkflowNameOfDraftWorfklow() throws WorkflowException
    {
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(),
            (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx);
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("getName", "workflow1");
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.TRUE);
        mockCurrentWorkflow.expectAndReturn("getDescription", "sameDescription");
        osWorkflowManager.updateWorkflowNameAndDescription("testuser", (JiraWorkflow) mockCurrentWorkflow.proxy(), "newWorkflowName",
            "sameDescription");

        mockCurrentWorkflow.verify();
        mockDraftWorkflowStore.verify();
        mockConfiguration.verify();
    }

    @Test
    public void testEditWorkflowNameNullWorkflow()
    {
        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager(null, null, eventPublisher, null, null, ctx);
        try
        {
            osWorkflowManager.updateWorkflowNameAndDescription("testuser", null, "newWorkflowName", "sameDescription");
            fail("Should have thrown an illegal arg");
        }
        catch (final IllegalArgumentException e)
        {
            //yay
        }
    }

    @Test
    public void testEditWorkflowDescriptionOnly() throws WorkflowException
    {
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);
        final MockControl mockConfigurableJiraWorkflowControl = MockClassControl.createControl(ConfigurableJiraWorkflow.class);
        final ConfigurableJiraWorkflow mockConfigurableJiraWorkflow = (ConfigurableJiraWorkflow) mockConfigurableJiraWorkflowControl.getMock();
        mockConfigurableJiraWorkflow.getDescription();
        mockConfigurableJiraWorkflowControl.setReturnValue("sameDescription");
        mockConfigurableJiraWorkflow.getDescriptor();
        mockConfigurableJiraWorkflowControl.setReturnValue(new DescriptorFactory().createWorkflowDescriptor());
        mockConfigurableJiraWorkflowControl.replay();

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(),
            (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflowClone(final String name)
            {

                return mockConfigurableJiraWorkflow;
            }

            @Override
            public void updateWorkflow(final ApplicationUser username, final JiraWorkflow workflow)
            {
                assertEquals(mockConfigurableJiraWorkflow, workflow);
            }
        };
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("getName", "workflow1");
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);

        osWorkflowManager.updateWorkflowNameAndDescription("testuser", (JiraWorkflow) mockCurrentWorkflow.proxy(), "workflow1", "anotherDescription");

        mockConfigurableJiraWorkflowControl.verify();
        mockDraftWorkflowStore.verify();
        mockConfiguration.verify();
    }

    @Test
    public void testEditWorkflowNameOnly() throws WorkflowException
    {
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        mockConfiguration.expectAndReturn("removeWorkflow", new Constraint[] { P.eq("workflow1") }, Boolean.TRUE);
        mockConfiguration.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("anotherWorkflow"), P.eq(descriptor), P.IS_TRUE }, Boolean.TRUE);

        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);
        mockDraftWorkflowStore.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("workflow1") }, null);

        final MockControl mockConfigurableJiraWorkflowControl = MockClassControl.createControl(ConfigurableJiraWorkflow.class);
        final ConfigurableJiraWorkflow mockConfigurableJiraWorkflow = (ConfigurableJiraWorkflow) mockConfigurableJiraWorkflowControl.getMock();
        mockConfigurableJiraWorkflow.getDescription();
        mockConfigurableJiraWorkflowControl.setReturnValue("sameDescription");
        mockConfigurableJiraWorkflow.getDescriptor();
        mockConfigurableJiraWorkflowControl.setReturnValue(descriptor);
        mockConfigurableJiraWorkflowControl.replay();

        final Mock mockWorkflowSchemeManager = new Mock(WorkflowSchemeManager.class);
        mockWorkflowSchemeManager.setStrict(true);
        mockWorkflowSchemeManager.expectVoid("updateSchemesForRenamedWorkflow", new Constraint[] { P.eq("workflow1"), P.eq("anotherWorkflow") });

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(),
            (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflowClone(final String name)
            {
                return mockConfigurableJiraWorkflow;
            }

            @Override
            public void updateWorkflow(final String username, final JiraWorkflow workflow)
            {
                assertEquals(mockConfigurableJiraWorkflow, workflow);
            }

            @Override
            protected WorkflowSchemeManager getWorkflowSchemeManager()
            {
                return (WorkflowSchemeManager) mockWorkflowSchemeManager.proxy();
            }
        };
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("getName", "workflow1");
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        osWorkflowManager.updateWorkflowNameAndDescription("testuser", (JiraWorkflow) mockCurrentWorkflow.proxy(), "anotherWorkflow",
            "sameDescription");

        mockConfigurableJiraWorkflowControl.verify();
        mockDraftWorkflowStore.verify();
        mockConfiguration.verify();
        mockWorkflowSchemeManager.verify();
    }

    @Test
    public void testEditWorkflowNameAndDescription() throws WorkflowException
    {
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        mockConfiguration.expectAndReturn("removeWorkflow", new Constraint[] { P.eq("workflow1") }, Boolean.TRUE);
        mockConfiguration.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("anotherWorkflow"), P.eq(descriptor), P.IS_TRUE }, Boolean.TRUE);

        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);
        mockDraftWorkflowStore.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("workflow1") }, null);

        final MockControl mockConfigurableJiraWorkflowControl = MockClassControl.createControl(ConfigurableJiraWorkflow.class);
        final ConfigurableJiraWorkflow mockConfigurableJiraWorkflow = (ConfigurableJiraWorkflow) mockConfigurableJiraWorkflowControl.getMock();
        mockConfigurableJiraWorkflow.getDescription();
        mockConfigurableJiraWorkflowControl.setReturnValue("sameDescription");
        mockConfigurableJiraWorkflow.getDescriptor();
        mockConfigurableJiraWorkflowControl.setReturnValue(descriptor, 2);
        mockConfigurableJiraWorkflowControl.replay();

        final Mock mockWorkflowSchemeManager = new Mock(WorkflowSchemeManager.class);
        mockWorkflowSchemeManager.setStrict(true);
        mockWorkflowSchemeManager.expectVoid("updateSchemesForRenamedWorkflow", new Constraint[] { P.eq("workflow1"), P.eq("anotherWorkflow") });

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(),
            (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflowClone(final String name)
            {

                return mockConfigurableJiraWorkflow;
            }

            @Override
            public void updateWorkflow(final ApplicationUser username, final JiraWorkflow workflow)
            {
                assertEquals(mockConfigurableJiraWorkflow, workflow);
            }

            @Override
            protected WorkflowSchemeManager getWorkflowSchemeManager()
            {
                return (WorkflowSchemeManager) mockWorkflowSchemeManager.proxy();
            }
        };
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("getName", "workflow1");
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);

        osWorkflowManager.updateWorkflowNameAndDescription("testuser", (JiraWorkflow) mockCurrentWorkflow.proxy(), "anotherWorkflow",
            "anotherDescription");

        mockConfigurableJiraWorkflowControl.verify();
        mockDraftWorkflowStore.verify();
        mockConfiguration.verify();
        mockWorkflowSchemeManager.verify();
    }

    @Test
    public void testEditWorkflowNameWithDraftWorkflow()
    {
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockConfiguration = new Mock(Configuration.class);
        mockConfiguration.setStrict(true);
        mockConfiguration.expectAndReturn("removeWorkflow", new Constraint[] { P.eq("workflow1") }, Boolean.TRUE);
        mockConfiguration.expectAndReturn("saveWorkflow", new Constraint[] { P.eq("anotherWorkflow"), P.eq(descriptor), P.IS_TRUE }, Boolean.TRUE);

        final Mock mockDraftWorkflowStore = new Mock(DraftWorkflowStore.class);
        mockDraftWorkflowStore.setStrict(true);
        final Mock mockJiraDraftWorkflow = new Mock(JiraWorkflow.class);
        mockJiraDraftWorkflow.expectAndReturn("getDescriptor", new DescriptorFactory().createWorkflowDescriptor());

        mockDraftWorkflowStore.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("workflow1") }, mockJiraDraftWorkflow.proxy());
        mockDraftWorkflowStore.expectAndReturn("deleteDraftWorkflow", new Constraint[] { P.eq("workflow1") }, Boolean.TRUE);
        mockDraftWorkflowStore.expectAndReturn("createDraftWorkflow", new Constraint[] { P.eq(applicationUser), P.IS_ANYTHING }, null);

        final MockControl mockConfigurableJiraWorkflowControl = MockClassControl.createControl(ConfigurableJiraWorkflow.class);
        final ConfigurableJiraWorkflow mockConfigurableJiraWorkflow = (ConfigurableJiraWorkflow) mockConfigurableJiraWorkflowControl.getMock();
        mockConfigurableJiraWorkflow.getDescription();
        mockConfigurableJiraWorkflowControl.setReturnValue("sameDescription");
        mockConfigurableJiraWorkflow.getDescriptor();
        mockConfigurableJiraWorkflowControl.setReturnValue(descriptor);
        mockConfigurableJiraWorkflowControl.replay();

        final Mock mockWorkflowSchemeManager = new Mock(WorkflowSchemeManager.class);
        mockWorkflowSchemeManager.setStrict(true);
        mockWorkflowSchemeManager.expectVoid("updateSchemesForRenamedWorkflow", new Constraint[] { P.eq("workflow1"), P.eq("anotherWorkflow") });

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager((Configuration) mockConfiguration.proxy(),
            (DraftWorkflowStore) mockDraftWorkflowStore.proxy(), eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getWorkflowClone(final String name)
            {
                return mockConfigurableJiraWorkflow;
            }

            @Override
            public void updateWorkflow(final ApplicationUser username, final JiraWorkflow workflow)
            {
                assertEquals(mockConfigurableJiraWorkflow, workflow);
            }

            @Override
            protected WorkflowSchemeManager getWorkflowSchemeManager()
            {
                return (WorkflowSchemeManager) mockWorkflowSchemeManager.proxy();
            }
        };
        final Mock mockCurrentWorkflow = new Mock(JiraWorkflow.class);
        mockCurrentWorkflow.expectAndReturn("getName", "workflow1");
        mockCurrentWorkflow.expectAndReturn("isDraftWorkflow", Boolean.FALSE);
        osWorkflowManager.updateWorkflowNameAndDescription("testuser", (JiraWorkflow) mockCurrentWorkflow.proxy(), "anotherWorkflow",
            "sameDescription");

        mockConfigurableJiraWorkflowControl.verify();
        mockDraftWorkflowStore.verify();
        mockConfiguration.verify();
        mockWorkflowSchemeManager.verify();
        mockJiraDraftWorkflow.verify();
    }

    @Test
    public void testCopyAndDeleteDraftWorkflowsEmptyInput()
    {
        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager(null, null, eventPublisher, null, null, ctx);
        // these shouldn't throw any errors.
        osWorkflowManager.copyAndDeleteDraftWorkflows(null, null);
        osWorkflowManager.copyAndDeleteDraftWorkflows(null, Collections.EMPTY_SET);
    }

    @Test
    public void testCopyAndDeleteDraftWorkflows()
    {
        final Mock mockJiraWorkflowActive = new Mock(JiraWorkflow.class);
        mockJiraWorkflowActive.expectAndReturn("getName", "I'm active!");
        mockJiraWorkflowActive.expectAndReturn("isActive", Boolean.TRUE);

        final Mock mockJiraWorkflowNoDraft = new Mock(JiraWorkflow.class);
        mockJiraWorkflowNoDraft.expectAndReturn("getName", "I've got no draft!");
        mockJiraWorkflowNoDraft.expectAndReturn("isActive", Boolean.FALSE);

        final Mock mockJiraWorkflowActiveWithDraft = new Mock(JiraWorkflow.class);
        mockJiraWorkflowActiveWithDraft.expectAndReturn("getName", "Yeah Baby! I've got it all!");
        mockJiraWorkflowActiveWithDraft.expectAndReturn("isActive", Boolean.FALSE);

        final Set workflowsToCopy = new HashSet(EasyList.build(mockJiraWorkflowActive.proxy(), mockJiraWorkflowNoDraft.proxy(),
            mockJiraWorkflowActiveWithDraft.proxy()));

        final AtomicBoolean copyWorkflowCalled = new AtomicBoolean();
        copyWorkflowCalled.set(false);
        final AtomicBoolean deleteDraftWorkflowCalled = new AtomicBoolean();
        deleteDraftWorkflowCalled.set(false);

        final OSWorkflowManager osWorkflowManager = new OSWorkflowManager(null, null, eventPublisher, null, null, ctx)
        {
            @Override
            public JiraWorkflow getDraftWorkflow(final String parentWorkflowName) throws IllegalArgumentException
            {
                final Mock mockDraftWorkflow = new Mock(JiraWorkflow.class);
                mockDraftWorkflow.expectAndReturn("isDraftWorkflow", Boolean.TRUE);
                if (parentWorkflowName.equals("I'm active!"))
                {
                    return (JiraWorkflow) mockDraftWorkflow.proxy();
                }
                else if (parentWorkflowName.equals("I've got no draft!"))
                {
                    return null;
                }
                else if (parentWorkflowName.equals("Yeah Baby! I've got it all!"))
                {
                    mockDraftWorkflow.expectAndReturn("getDescription", "My description is cool.");
                    return (JiraWorkflow) mockDraftWorkflow.proxy();
                }
                fail("Unexpected call to getDraftWorkflow with name '" + parentWorkflowName + "'");
                return null;
            }

            @Override
            public JiraWorkflow copyWorkflow(final String username, final String clonedWorkflowName, final String clonedWorkflowDescription, final JiraWorkflow workflowToClone)
            {
                assertEquals("fakeuser", username);
                assertEquals("Copy of Yeah Baby! I've got it all!", clonedWorkflowName);
                assertEquals(
                    "My description is cool. (This copy was automatically generated from a draft, when workflow 'Yeah Baby! I've got it all!' was made inactive.)",
                    clonedWorkflowDescription);
                assertNotNull(workflowToClone);
                copyWorkflowCalled.set(true);

                return null;
            }

            @Override
            public boolean deleteDraftWorkflow(final String parentWorkflowName) throws IllegalArgumentException
            {
                assertEquals("Yeah Baby! I've got it all!", parentWorkflowName);
                deleteDraftWorkflowCalled.set(true);
                return true;
            }

            @Override
            I18nHelper getI18nBean(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            String getClonedWorkflowName(final String parentWorkflowName, User user)
            {
                return "Copy of " + parentWorkflowName;
            }
        };

        final User remoteUser = new MockUser("fakeuser");
        osWorkflowManager.copyAndDeleteDraftWorkflows(remoteUser, workflowsToCopy);

        mockJiraWorkflowActiveWithDraft.verify();
        mockJiraWorkflowActive.verify();
        mockJiraWorkflowNoDraft.verify();

        assertTrue(copyWorkflowCalled.get());
        assertTrue(deleteDraftWorkflowCalled.get());
    }

    @Test
    public void isEditableByDefault()
    {
        osWorkflowManager = spy(osWorkflowManager);
        ProjectManager projectManager = Mockito.mock(ProjectManager.class, RETURNS_MOCKS);
        container.addMock(ProjectManager.class, projectManager);
        Issue issue = Mockito.mock(Issue.class, RETURNS_MOCKS);

        container.addMock(WorkflowSchemeManager.class, workflowSchemeManager);
        when(workflowSchemeManager.getWorkflowName(Matchers.<Project>any(), Matchers.<String>any())).thenReturn("Workflow");

        JiraWorkflow workflow = Mockito.mock(JiraWorkflow.class);
        doReturn(workflow).when(osWorkflowManager).getWorkflow(anyLong(), anyString());

        ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class, RETURNS_MOCKS);
        container.addMock(ConstantsManager.class, constantsManager);

        assertTrue("issue should be editable by default", osWorkflowManager.isEditable(issue));
    }
}
