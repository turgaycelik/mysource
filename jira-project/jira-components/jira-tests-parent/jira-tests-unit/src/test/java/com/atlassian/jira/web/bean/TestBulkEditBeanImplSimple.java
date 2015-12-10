package com.atlassian.jira.web.bean;

import java.util.Collections;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.MockSubTaskManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.security.IssueSecurityHelper;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;

import com.google.common.collect.Lists;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestBulkEditBeanImplSimple
{
    @Test
    public void testTargetIssueObjects()
    {
        //we want to test moving two issues to a different project and issue type.
        MockIssueManager mockIssueManager = new MockIssueManager();
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        //need an old and new issue type
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "old")));
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "Improvement")));
        MockIssueFactory.setConstantsManager(mockConstantsManager);

        Mock mockIssueSecurityLevelManager = new Mock(IssueSecurityLevelManager.class);
        mockIssueSecurityLevelManager.expectAndReturn("getIssueSecurityLevel", P.ANY_ARGS, null);
        MockIssueFactory.setIssueSecurityLevelManager((IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy());
        MockIssueFactory.setSubTaskManager(new MockSubTaskManager());
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockProject(new Long(1), "ABC"));
        mockProjectManager.addProject(new MockProject(new Long(42), "HSP"));
        MockIssueFactory.setProjectManager(mockProjectManager);
        MutableIssue issue1 = MockIssueFactory.createIssue(1);
        //project with id 1 is the old project
        issue1.setProjectObject(new MockProject(1));
        issue1.setIssueTypeId("old");
        issue1.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(223))));
        MutableIssue issue2 = MockIssueFactory.createIssue(2);
        issue2.setProjectObject(new MockProject(1));
        issue2.setIssueTypeId("old");
        mockIssueManager.addIssue(issue1);
        mockIssueManager.addIssue(issue2);

        final Mock mockIssueSecurityHelper = new Mock(IssueSecurityHelper.class);
        mockIssueSecurityHelper.expectAndReturn("securityLevelNeedsMove", P.ANY_ARGS, Boolean.TRUE);

        BulkEditBeanImpl bulkEditBean = new BulkEditBeanImpl(mockIssueManager)
        {
            IssueSecurityHelper getIssueSecurityHelper()
            {
                return (IssueSecurityHelper) mockIssueSecurityHelper.proxy();
            }
        };
        bulkEditBean.initSelectedIssues(Lists.<Issue>newArrayList(issue1, issue2));
        //this is the new issuetype we're moving to
        bulkEditBean.setTargetIssueTypeId("Improvement");
        //let set the new project we're moving to
        bulkEditBean.setTargetProject(new MockGenericValue("Project", EasyMap.build("id", new Long(42))));

        // Finally call getTargetIssueObjects()
        Map targetIssueObjects = bulkEditBean.getTargetIssueObjects();
        assertEquals(2, targetIssueObjects.size());
        // Get the new version for Issue 1
        Issue newIssue1 = (Issue) targetIssueObjects.get(issue1);
        Issue newIssue2 = (Issue) targetIssueObjects.get(issue2);
        assertEquals(new Long(1), newIssue1.getId());
        //check the project and issuetype have been updated correctly in the target issues.
        assertEquals(new Long(42), newIssue1.getProjectObject().getId());
        //JRA-13990 - check that the security level has been set to null, as the issue's security level requires a move.
        assertNull(newIssue1.getSecurityLevel());
        assertNull(newIssue2.getSecurityLevel());
        assertEquals("Improvement", newIssue1.getIssueTypeObject().getId());
    }

    @Test
    public void testTargetIssueObjectsRetainSecurityLevel()
    {
        //we want to test moving two issues to a different project and issue type.
        MockIssueManager mockIssueManager = new MockIssueManager();
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        //need an old and new issue type
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "old")));
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "Improvement")));
        MockIssueFactory.setConstantsManager(mockConstantsManager);

        Mock mockIssueSecurityLevelManager = new Mock(IssueSecurityLevelManager.class);
        mockIssueSecurityLevelManager.expectAndReturn("getIssueSecurityLevel", P.ANY_ARGS, null);
        MockIssueFactory.setIssueSecurityLevelManager((IssueSecurityLevelManager) mockIssueSecurityLevelManager.proxy());
        MockIssueFactory.setSubTaskManager(new MockSubTaskManager());
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockProject(new Long(1), "ABC"));
        mockProjectManager.addProject(new MockProject(new Long(42), "HSP"));
        MockIssueFactory.setProjectManager(mockProjectManager);
        MutableIssue issue1 = MockIssueFactory.createIssue(1);
        //project with id 1 is the old project
        issue1.setProjectObject(new MockProject(1));
        issue1.setIssueTypeId("old");
        issue1.setSecurityLevel(new MockGenericValue("SecurityLevel", EasyMap.build("id", new Long(223))));
        MutableIssue issue2 = MockIssueFactory.createIssue(2);
        issue2.setProjectObject(new MockProject(1));
        issue2.setIssueTypeId("old");
        mockIssueManager.addIssue(issue1);
        mockIssueManager.addIssue(issue2);

        final Mock mockIssueSecurityHelper = new Mock(IssueSecurityHelper.class);
        mockIssueSecurityHelper.expectAndReturn("securityLevelNeedsMove", P.ANY_ARGS, Boolean.FALSE);

        BulkEditBean bulkEditBean = new BulkEditBeanImpl(mockIssueManager)
        {
            IssueSecurityHelper getIssueSecurityHelper()
            {
                return (IssueSecurityHelper) mockIssueSecurityHelper.proxy();
            }
        };
        bulkEditBean.initSelectedIssues(Lists.<Issue>newArrayList(issue1, issue2));
        //this is the new issuetype we're moving to
        bulkEditBean.setTargetIssueTypeId("Improvement");
        //let set the new project we're moving to
        bulkEditBean.setTargetProject(new MockGenericValue("Project", EasyMap.build("id", new Long(42), "key", "HSP")));

        // Finally call getTargetIssueObjects()
        Map targetIssueObjects = bulkEditBean.getTargetIssueObjects();
        assertEquals(2, targetIssueObjects.size());
        // Get the new version for Issue 1
        Issue newIssue1 = (Issue) targetIssueObjects.get(issue1);
        Issue newIssue2 = (Issue) targetIssueObjects.get(issue2);
        assertEquals(new Long(1), newIssue1.getId());
        //check the project and issuetype have been updated correctly in the target issues.
        assertEquals(new Long(42), newIssue1.getProjectObject().getId());
        //JRA-13990 - check that the security level has been retained, as the issue's security level should stay the same
        assertEquals(new Long(223), newIssue1.getSecurityLevelId());
        assertNull(newIssue2.getSecurityLevel());
        assertEquals("Improvement", newIssue1.getIssueTypeObject().getId());
    }

    @Test
    public void testTargetIssueObjectsWithSubtasks()
    {
        //this is the same test as above, but we're now moving subtasks.  We need to check
        //that the moved subtasks will have the cloned parent issue from the parent bulkeditbean as
        //parents
        MockIssueManager mockIssueManager = new MockIssueManager();
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "old")));
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "Improvement")));
        MockIssueFactory.setConstantsManager(mockConstantsManager);

        //this is needed for the issue.isSubtask() check.
        Mock mockSubTaskManager = new Mock(SubTaskManager.class);
        mockSubTaskManager.expectAndReturn("getParentIssueId", P.ANY_ARGS, new Long(3));
        mockSubTaskManager.expectAndReturn("getSubTaskObjects", P.ANY_ARGS, Collections.EMPTY_LIST);

        MockIssueFactory.setSubTaskManager((SubTaskManager) mockSubTaskManager.proxy());
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockProject(new Long(1), "ABC"));
        mockProjectManager.addProject(new MockProject(new Long(42), "HSP"));
        MockIssueFactory.setProjectManager(mockProjectManager);
        MutableIssue issue1 = MockIssueFactory.createIssue(1);
        issue1.setProjectObject(new MockProject(1));
        issue1.setIssueTypeId("old");
        issue1.setParentId(new Long(3));
        MutableIssue issue2 = MockIssueFactory.createIssue(2);
        issue2.setProjectObject(new MockProject(1));
        issue2.setIssueTypeId("old");
        issue2.setParentId(new Long(3));
        MutableIssue parentIssue = MockIssueFactory.createIssue(3);
        mockIssueManager.addIssue(issue1);
        mockIssueManager.addIssue(issue2);
        mockIssueManager.addIssue(parentIssue);
         final Mock mockIssueSecurityHelper = new Mock(IssueSecurityHelper.class);
        mockIssueSecurityHelper.expectAndReturn("securityLevelNeedsMove", P.ANY_ARGS, Boolean.TRUE);

        BulkEditBean bulkEditBean = new BulkEditBeanImpl(mockIssueManager)
        {
            IssueSecurityHelper getIssueSecurityHelper()
            {
                return (IssueSecurityHelper) mockIssueSecurityHelper.proxy();
            }
        };

        BulkEditBean parentBulkEditBean = new BulkEditBeanImpl(mockIssueManager)
        {
            IssueSecurityHelper getIssueSecurityHelper()
            {
                return (IssueSecurityHelper) mockIssueSecurityHelper.proxy();
            }
        };
        parentBulkEditBean.initSelectedIssues(Lists.<Issue>newArrayList(parentIssue));

        bulkEditBean.setParentBulkEditBean(parentBulkEditBean);
        bulkEditBean.initSelectedIssues(Lists.<Issue>newArrayList(issue1, issue2));
        bulkEditBean.setTargetIssueTypeId("Improvement");
        bulkEditBean.setTargetProject(new MockGenericValue("Project", EasyMap.build("id", new Long(42))));

        // Finally call getTargetIssueObjects()
        Map targetIssueObjects = bulkEditBean.getTargetIssueObjects();
        assertEquals(2, targetIssueObjects.size());
        // Get the new version for Issue 1
        Issue newIssue1 = (Issue) targetIssueObjects.get(issue1);
        assertEquals(new Long(1), newIssue1.getId());
        assertEquals(new Long(42), newIssue1.getProjectObject().getId());
        assertEquals("Improvement", newIssue1.getIssueTypeObject().getId());
        assertEquals(parentIssue.getId(), newIssue1.getParentObject().getId());
        //ensure the parentObject of the new subtask is in fact a clone of the original issue.
        assertNotSame(parentIssue, newIssue1.getParentObject());
    }

    @Test
    public void testTargetIssueObjectsWithSubtasksWithNoParent()
    {
        //finally lets check an error condition, where the parentBulkEditBean does not contain the parent issue
        //for the subtasks we're moving.  This should throw an IllegalStateException.
        MockIssueManager mockIssueManager = new MockIssueManager();
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "old")));
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "Improvement")));
        MockIssueFactory.setConstantsManager(mockConstantsManager);

        Mock mockSubTaskManager = new Mock(SubTaskManager.class);
        mockSubTaskManager.expectAndReturn("getParentIssueId", P.ANY_ARGS, new Long(3));
        mockSubTaskManager.expectAndReturn("getSubTaskObjects", P.ANY_ARGS, Collections.EMPTY_LIST);

        MockIssueFactory.setSubTaskManager((SubTaskManager) mockSubTaskManager.proxy());
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockProject(new Long(1)));
        mockProjectManager.addProject(new MockProject(new Long(42)));
        MockIssueFactory.setProjectManager(mockProjectManager);
        MutableIssue issue1 = MockIssueFactory.createIssue(1);
        issue1.setProjectObject(new MockProject(1));
        issue1.setIssueTypeId("old");
        issue1.setParentId(new Long(3));
        MutableIssue issue2 = MockIssueFactory.createIssue(2);
        issue2.setProjectObject(new MockProject(1));
        issue2.setIssueTypeId("old");
        issue2.setParentId(new Long(3));
        MutableIssue parentIssue = MockIssueFactory.createIssue(3);
        mockIssueManager.addIssue(issue1);
        mockIssueManager.addIssue(issue2);
        mockIssueManager.addIssue(parentIssue);
        BulkEditBean bulkEditBean = new BulkEditBeanImpl(mockIssueManager);

        //setup a parentBulkEditBean, but no parent is being set this time round
        BulkEditBean parentBulkEditBean = new BulkEditBeanImpl(mockIssueManager);

        bulkEditBean.setParentBulkEditBean(parentBulkEditBean);
        bulkEditBean.initSelectedIssues(Lists.<Issue>newArrayList(issue1, issue2));
        bulkEditBean.setTargetIssueTypeId("Improvement");
        bulkEditBean.setTargetProject(new MockGenericValue("Project", EasyMap.build("id", new Long(42))));

        // Finally call getTargetIssueObjects()
        try
        {
            bulkEditBean.getTargetIssueObjects();
            fail("Should have thrown an exception, since the parent bulk edit bean does not have any parent issues.");
        }
        catch (IllegalStateException e)
        {
            //expected exception.
        }
    }
}
