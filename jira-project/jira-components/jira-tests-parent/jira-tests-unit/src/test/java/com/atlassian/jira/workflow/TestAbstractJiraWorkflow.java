package com.atlassian.jira.workflow;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;

import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestAbstractJiraWorkflow
{
    @Test
    public void testGetUpdated()
    {
        final Date now = new Date();
        final Map<String, String> metaAtts = new HashMap<String, String>();
        metaAtts.put(JiraWorkflow.JIRA_META_UPDATED_DATE, String.valueOf(now.getTime()));
        JiraWorkflow jiraWorkflow = getJiraWorkflow(metaAtts);

        assertEquals(now, jiraWorkflow.getUpdatedDate());
    }

    @Test
    public void testInvalidUpdatedString()
    {
        final Map<String, String> metaAtts = new HashMap<String, String>();
        metaAtts.put(JiraWorkflow.JIRA_META_UPDATED_DATE, "imaninvalidstring");

        JiraWorkflow jiraWorkflow = getJiraWorkflow(metaAtts);

        assertNull(jiraWorkflow.getUpdatedDate());
    }

    @Test
    public void testGetUpdateAuthorKey()
    {
        MockComponentWorker worker = new MockComponentWorker().init();
        final Map<String, String> metaAtts = new HashMap<String, String>();
        metaAtts.put(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY, "tom");
        final MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockApplicationUser("tom", "tommy"));
        worker.addMock(UserManager.class, userManager);
        JiraWorkflow jiraWorkflow = getJiraWorkflow(metaAtts);

        assertEquals("tom", jiraWorkflow.getUpdateAuthor().getKey());
    }

    private WorkflowDescriptor getMockWorkflowDescriptor(Map metaAtts)
    {
        final MockControl mockWorkflowDescriptorControl = MockClassControl.createControl(WorkflowDescriptor.class);
        final WorkflowDescriptor mockWorkflowDescriptor = (WorkflowDescriptor) mockWorkflowDescriptorControl.getMock();

        mockWorkflowDescriptor.getMetaAttributes();
        mockWorkflowDescriptorControl.setDefaultReturnValue(metaAtts);
        mockWorkflowDescriptorControl.replay();
        return mockWorkflowDescriptor;
    }

    private JiraWorkflow getJiraWorkflow(final Map<String, String> metaAtts)
    {
        JiraWorkflow jiraWorkflow = new AbstractJiraWorkflow(null, getMockWorkflowDescriptor(metaAtts))
        {

            public String getName()
            {
                return "bsname";
            }

            public void store(User user) throws WorkflowException
            {
                //noop
            }

            public boolean isDraftWorkflow()
            {
                return false;
            }

            public void reset()
            {
                //noop
            }
        };
        return jiraWorkflow;
    }

    @Test
    public void testGetModeLive()
    {
        AbstractJiraWorkflow workflow = new AbstractJiraWorkflow(null, new DescriptorFactory().createWorkflowDescriptor())
        {
            public String getName()
            {
                return null;
            }

            public void store(User user) throws WorkflowException
            {
            }

            public boolean isDraftWorkflow()
            {
                return false;
            }
        };
        
        assertEquals("live", workflow.getMode());
    }

    @Test
    public void testGetModeDraft()
    {
        AbstractJiraWorkflow workflow = new AbstractJiraWorkflow(null, new DescriptorFactory().createWorkflowDescriptor())
        {
            public String getName()
            {
                return null;
            }

            public void store(User user) throws WorkflowException
            {
            }

            public boolean isDraftWorkflow()
            {
                return true;
            }
        };

        assertEquals("draft", workflow.getMode());
    }

    @Test
    public void testHasDraftWorkflow()
    {
        MockWorkflowManager mockWorkflowManager = new MockWorkflowManager();


        AbstractJiraWorkflow workflow = new AbstractJiraWorkflow(mockWorkflowManager, new DescriptorFactory().createWorkflowDescriptor())
        {
            public String getName()
            {
                return "Peter";
            }

            public void store(User user) throws WorkflowException
            {
            }

            public boolean isDraftWorkflow()
            {
                return false;
            }
        };

        // there is no draft in the mockWorkflowManager
        assertFalse(workflow.hasDraftWorkflow());
        // Now add a draft to the mockWorkflowManager
        mockWorkflowManager.updateDraftWorkflow("admin", "Peter", new MockJiraWorkflow());
        assertTrue(workflow.hasDraftWorkflow());

    }
}
