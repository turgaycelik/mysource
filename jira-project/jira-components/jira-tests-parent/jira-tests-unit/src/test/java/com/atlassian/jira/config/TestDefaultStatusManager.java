package com.atlassian.jira.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultStatusManager
{
    DefaultStatusManager statusManager;
    ConstantsManager constantsManager;
    OfBizDelegator ofBizDelegator;
    IssueIndexManager issueIndexManager;
    TranslationManager translationManager;
    JiraAuthenticationContext jiraAuthenticationContext;
    WorkflowManager workflowManager;
    StatusCategoryManager statusCategoryManager;
    private MockIssueConstantFactory factory;
    private ClusterLockService clusterLockService;

    @Before
    public void setUp()
    {
        constantsManager = Mockito.mock(ConstantsManager.class);
        translationManager = Mockito.mock(TranslationManager.class);
        jiraAuthenticationContext = Mockito.mock(JiraAuthenticationContext.class);
        issueIndexManager = Mockito.mock(IssueIndexManager.class);
        ofBizDelegator = Mockito.mock(OfBizDelegator.class);
        workflowManager = Mockito.mock(WorkflowManager.class);
        statusCategoryManager = Mockito.mock(StatusCategoryManager.class);
        factory = new MockIssueConstantFactory(statusCategoryManager);
        clusterLockService = new SimpleClusterLockService();
        statusManager = new DefaultStatusManager(constantsManager, ofBizDelegator, issueIndexManager, workflowManager, factory, statusCategoryManager, clusterLockService)
        {
            @Override
            protected String getNextStringId() throws GenericEntityException
            {
                return "10";
            }

            @Override
            protected void removePropertySet(GenericValue
                    constantGv)
            {
                //DO NOTHING
            }
        };
        statusManager.start();
        when(statusCategoryManager.isStatusAsLozengeEnabled()).thenReturn(true);
    }

    @Test
    public void testCreateStatus() throws Exception
    {
        StatusCategory statusCategory = mockStatusCategory(123L);

        GenericValue statusOpenGV = new MockGenericValue("Status", 1l);
        statusOpenGV.set("sequence", Long.valueOf(1));
        Status statusOpen = factory.createStatus(statusOpenGV);

        GenericValue statusReadyForQaGV = new MockGenericValue("Status", 10000l);
        statusReadyForQaGV.set("sequence", Long.valueOf(2));
        statusReadyForQaGV.set("statuscategory", 123L);
        Status statusReadyForQa = factory.createStatus(statusReadyForQaGV);
        statusReadyForQa.setName("Ready for QA");
        statusReadyForQa.setDescription("Issue is ready to be qa-ed");
        statusReadyForQa.setIconUrl("http://test");

        when(constantsManager.getStatusObjects()).thenReturn(Lists.newArrayList(statusOpen));
        when(ofBizDelegator.createValue(eq(ConstantsManager.STATUS_CONSTANT_TYPE), argThat(new StatusFieldsdArgumentMatcher("10000", "Ready for QA", "Issue is ready to be qa-ed", "http://test", 123L)))).thenReturn(statusReadyForQaGV);

        Status status = statusManager.createStatus("Ready for QA", "Issue is ready to be qa-ed", "http://test", statusCategory);
        Assert.assertEquals("10000", status.getId());
        Assert.assertEquals("Ready for QA", status.getName());
        Assert.assertEquals("Issue is ready to be qa-ed", status.getDescription());
        Assert.assertEquals("http://test", status.getIconUrl());
        assertSame(statusCategory, status.getStatusCategory());

    }

    @Test
    public void testShouldSetDefaultCategoryWhenStatusIsCreatedWithoutCategory(){
        StatusCategory defaultStatusCategory = mockStatusCategory(123L);
        when(statusCategoryManager.getDefaultStatusCategory()).thenReturn(defaultStatusCategory);

        GenericValue statusOpenGV = new MockGenericValue("Status", 1l);
        statusOpenGV.set("sequence", Long.valueOf(1));
        Status statusOpen = factory.createStatus(statusOpenGV);

        GenericValue statusReadyForQaGV = new MockGenericValue("Status", 10000l);
        statusReadyForQaGV.set("sequence", Long.valueOf(2));
        statusReadyForQaGV.set("statuscategory", 123L);
        Status statusReadyForQa = factory.createStatus(statusReadyForQaGV);
        statusReadyForQa.setName("Ready for QA");
        statusReadyForQa.setDescription("Issue is ready to be qa-ed");
        statusReadyForQa.setIconUrl("http://test");

        when(constantsManager.getStatusObjects()).thenReturn(Lists.newArrayList(statusOpen));
        when(ofBizDelegator.createValue(eq(ConstantsManager.STATUS_CONSTANT_TYPE), argThat(new StatusFieldsdArgumentMatcher("10000", "Ready for QA", "Issue is ready to be qa-ed", "http://test", 123L)))).thenReturn(statusReadyForQaGV);

        Status status = statusManager.createStatus("Ready for QA", "Issue is ready to be qa-ed", "http://test", defaultStatusCategory);
        Assert.assertEquals("10000", status.getId());
        Assert.assertEquals("Ready for QA", status.getName());
        Assert.assertEquals("Issue is ready to be qa-ed", status.getDescription());
        Assert.assertEquals("http://test", status.getIconUrl());
        assertSame(defaultStatusCategory, status.getStatusCategory());
    }

    @Test
    public void testEditStatus() throws Exception
    {
        final BooleanHolder calledStored = new BooleanHolder();
        GenericValue statusOpenGV = new MockGenericValue("Status", 1l)
        {
            @Override
            public void store() throws GenericEntityException
            {
                calledStored.booleanValue = true;
            }
        };
        statusOpenGV.set("sequence", Long.valueOf(1));
        statusOpenGV.set("statuscategory", 2L);

        Status statusOpen = factory.createStatus(statusOpenGV);

        StatusCategory statusCategory = mockStatusCategory(3L);

        when(constantsManager.getStatusObjects()).thenReturn(Lists.newArrayList(statusOpen));

        statusManager.editStatus(statusOpen, "New Status", null, "http://myurl.com", statusCategory);

        assertTrue(calledStored.booleanValue);
        assertEquals("New Status", statusOpen.getName());
        assertEquals(null, statusOpen.getDescription());
        assertEquals("http://myurl.com", statusOpen.getIconUrl());
        assertSame(statusCategory, statusOpen.getStatusCategory());
        verify(statusCategoryManager, never()).getStatusCategory(2L);
    }


    @Test
    public void shouldNotChangeStatusCategoryWhenCategoryWasNotGiven() throws Exception
    {
        final BooleanHolder calledStored = new BooleanHolder();
        GenericValue statusOpenGV = new MockGenericValue("Status", 1l)
        {
            @Override
            public void store() throws GenericEntityException
            {
                calledStored.booleanValue = true;
            }
        };
        statusOpenGV.set("sequence", Long.valueOf(1));
        statusOpenGV.set("statuscategory", 2L);

        Status statusOpen = factory.createStatus(statusOpenGV);

        StatusCategory statusCategory = mockStatusCategory(2L);

        when(constantsManager.getStatusObjects()).thenReturn(Lists.newArrayList(statusOpen));

        statusManager.editStatus(statusOpen, "New Status", null, "http://myurl.com");

        assertTrue(calledStored.booleanValue);
        assertEquals("New Status", statusOpen.getName());
        assertEquals(null, statusOpen.getDescription());
        assertEquals("http://myurl.com", statusOpen.getIconUrl());
        assertSame(statusCategory, statusOpen.getStatusCategory());
    }

    @Test
    public void testRemoveStatus() throws Exception
    {
        final BooleanHolder removedOne = new BooleanHolder();
        GenericValue statusClosedGV = new MockGenericValue("Status", 2l)
        {
            @Override
            public void remove()
            {
                removedOne.booleanValue = true;
            }

            @Override
            public List<GenericValue> getRelated(String s) throws GenericEntityException
            {
                return Collections.emptyList();
            }
        };
        statusClosedGV.set("sequence", Long.valueOf(2));
        statusClosedGV.set("name", "Closed");
        statusClosedGV.set("description", "Issue has been closed");
        Status statusClosed = factory.createStatus(statusClosedGV);

        GenericValue statusOpenGV = new MockGenericValue("Status", 1l);
        statusOpenGV.set("sequence", Long.valueOf(2));
        statusOpenGV.set("name", "Open");
        statusOpenGV.set("description", "Open waiting for development");

        when(constantsManager.getStatusObject("2")).thenReturn(statusClosed);
        JiraWorkflow jiraWorkflow = mock(JiraWorkflow.class);
        when(jiraWorkflow.getLinkedStatuses()).thenReturn(Lists.<GenericValue>newArrayList(statusOpenGV));
        when(workflowManager.getWorkflowsIncludingDrafts()).thenReturn(Lists.newArrayList(jiraWorkflow));
        statusManager.removeStatus("2");
        assertTrue(removedOne.booleanValue);
    }

    @Test
    public void testRemoveStatusExistsInWorkflow() throws Exception
    {
        final BooleanHolder removedOne = new BooleanHolder();
        GenericValue statusClosedGV = new MockGenericValue("Status", 2l)
        {
            @Override
            public void remove()
            {
                removedOne.booleanValue = true;
            }

            @Override
            public List<GenericValue> getRelated(String s) throws GenericEntityException
            {
                return Collections.emptyList();
            }
        };
        statusClosedGV.set("sequence", Long.valueOf(2));
        statusClosedGV.set("name", "Closed");
        statusClosedGV.set("description", "Issue has been closed");
        Status statusClosed = factory.createStatus(statusClosedGV);

        when(constantsManager.getStatusObject("2")).thenReturn(statusClosed);
        JiraWorkflow jiraWorkflow = mock(JiraWorkflow.class);
        when(jiraWorkflow.getName()).thenReturn("My Custom Workflow");
        when(jiraWorkflow.getLinkedStatuses()).thenReturn(Lists.<GenericValue>newArrayList(statusClosedGV));
        when(workflowManager.getWorkflowsIncludingDrafts()).thenReturn(Lists.newArrayList(jiraWorkflow));
        try
        {
            statusManager.removeStatus("2");
            fail("Expected failure due to status is associated with a workflow.");
        }
        catch (IllegalStateException ex)
        {
            assertEquals("Cannot delete a status which is associated with a workflow. Status is associated with workflow My Custom Workflow", ex.getMessage());
        }
    }

    @Test
    public void testMoveStatus() throws Exception
    {
        GenericValue openStatusGV = new MockGenericValue("Status", 1L);
        openStatusGV.set("name", "Open");
        openStatusGV.set("sequence", Long.valueOf(1));

        GenericValue inProgressStatusGV = new MockGenericValue("Status", 2L);
        inProgressStatusGV.set("name", "In Progress");
        inProgressStatusGV.set("sequence", Long.valueOf(2));

        GenericValue doneStatusGV = new MockGenericValue("Status", 3L);
        doneStatusGV.set("name", "Done");
        doneStatusGV.set("sequence", Long.valueOf(3));

        Status openStatus = factory.createStatus(openStatusGV);
        Status inProgressStatus = factory.createStatus(inProgressStatusGV);
        Status doneStatus = factory.createStatus(doneStatusGV);

        when(constantsManager.getStatusObject("2")).thenReturn(inProgressStatus);
        when(constantsManager.getStatusObjects()).thenReturn(Lists.newArrayList(openStatus, inProgressStatus, doneStatus));
        statusManager.moveStatusUp("2");

        verify(ofBizDelegator).storeAll(eq(Lists.newArrayList(inProgressStatusGV, openStatusGV, doneStatusGV)));
        verify(constantsManager).refreshStatuses();

        reset(ofBizDelegator);
        reset(constantsManager);
        when(constantsManager.getStatusObject("2")).thenReturn(inProgressStatus);
        when(constantsManager.getStatusObjects()).thenReturn(Lists.newArrayList(openStatus, inProgressStatus, doneStatus));

        statusManager.moveStatusDown("2");
        verify(ofBizDelegator).storeAll(eq(Lists.newArrayList(openStatusGV, doneStatusGV, inProgressStatusGV)));
        verify(constantsManager).refreshStatuses();
    }

    @Test
    public void testStatusCategoryDefault() throws Exception
    {
        GenericValue openStatusGV = new MockGenericValue("Status", 1L);
        openStatusGV.set("name", "Open");
        openStatusGV.set("sequence", Long.valueOf(1));

        Status openStatus = factory.createStatus(openStatusGV);

        when(statusCategoryManager.getStatusCategory(null)).thenReturn(null);

        StatusCategory defaultStatusCategory = mockStatusCategory(123L);
        when(statusCategoryManager.getDefaultStatusCategory()).thenReturn(defaultStatusCategory);

        assertThat("Status Category should default instead of returning null", openStatus.getStatusCategory(), is(defaultStatusCategory));
    }

    StatusCategory mockStatusCategory(Long id){
        StatusCategory statusCategory = mock(StatusCategory.class);
        when(statusCategory.getId()).thenReturn(id);
        when(statusCategoryManager.getStatusCategory(id)).thenReturn(statusCategory);
        return statusCategory;
    }

    class StatusFieldsdArgumentMatcher extends ArgumentMatcher<Map<String, Object>>
    {
        final String id;
        private final String name;
        private final String descpription;
        private final String iconUrl;
        private final Long statusCategory;

        StatusFieldsdArgumentMatcher(String id, String name, String descpription, String iconUrl, Long statusCategory)
        {
            this.id = id;
            this.name = name;
            this.descpription = descpription;
            this.iconUrl = iconUrl;
            this.statusCategory = statusCategory;
        }

        public boolean matches(Object o)
        {
            Map<String, Object> gv = (Map<String, Object>) o;
            return id.equals(gv.get("id")) && name.equals(gv.get("name")) && descpription.equals(gv.get("description"))
                    && iconUrl.equals(gv.get("iconurl")) && statusCategory.equals(gv.get("statuscategory"));
        }
    }

}
