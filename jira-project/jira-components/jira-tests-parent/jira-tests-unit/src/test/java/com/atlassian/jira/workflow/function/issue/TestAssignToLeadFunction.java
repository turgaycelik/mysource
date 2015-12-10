package com.atlassian.jira.workflow.function.issue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

/*
 * required mocks
 * issue : getComponents(), getProjectObject(), isCreated(), setAssignee(), store()
 * project : getLead()

 * required overrides
 * getLead()
 */
public class TestAssignToLeadFunction
{
    private MockProject project;
    private User user;
    private User componentLead;

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();
        user = new MockUser("admin");
        componentLead = new MockUser("testuser");

        project = new MockProject();
        project.setLead(user);
    }

    @After
    public void tearDown() throws Exception
    {
        project = null;
        user = null;
        componentLead = null;
    }

    @Test
    public void testBrandNewIssueDoesntStore()
    {
        AssignToLeadFunction func = createFunction(user);

        Mock mockIssue = createMockIssue(Collections.EMPTY_LIST, false, user);
        Issue issue = (Issue) mockIssue.proxy();
        Map transientVars = EasyMap.build("issue", issue);

        func.execute(transientVars, null, null);
        mockIssue.verify();
    }

    @Test
    public void testExistingIssueDoesStore()
    {
        AssignToLeadFunction func = createFunction(user);

        Mock mockIssue = createMockIssue(Collections.EMPTY_LIST, true, user);
        Issue issue = (Issue) mockIssue.proxy();
        Map transientVars = EasyMap.build("issue", issue);

        func.execute(transientVars, null, null);
        mockIssue.verify();
    }

    @Test
    public void testComponentLeadChosen()
    {
        AssignToLeadFunction func = createFunction(componentLead);

        GenericValue component = new MockGenericValue("Component", EasyMap.build("lead", "testuser"));
        Mock mockIssue = createMockIssue(EasyList.build(component), true, componentLead);
        Issue issue = (Issue) mockIssue.proxy();
        Map transientVars = EasyMap.build("issue", issue);

        func.execute(transientVars, null, null);
        mockIssue.verify();
    }

    @Test
    public void testNoLeadsToChoose()
    {
        AssignToLeadFunction func = createFunction(null);
        project.setLead((ApplicationUser)null);

        Mock mockIssue = createMockIssue(Collections.EMPTY_LIST, false, null);
        Issue issue = (Issue) mockIssue.proxy();
        Map transientVars = EasyMap.build("issue", issue);

        func.execute(transientVars, null, null);
        mockIssue.verify();
    }

    private Mock createMockIssue(List components, boolean isCreated, User assignee)
    {
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectAndReturn("getComponents", components);

        if (components.isEmpty())
        {
            mockIssue.expectAndReturn("getProjectObject", project);
        }
        else
        {
            mockIssue.expectNotCalled("getProjectObject");
        }

        if (assignee != null)
        {
            mockIssue.expectVoid("setAssignee", P.args(P.eq(assignee)));
            mockIssue.expectAndReturn("isCreated", (isCreated ? Boolean.TRUE : Boolean.FALSE));
        }
        else
        {
            mockIssue.expectNotCalled("setAssignee");
            mockIssue.expectNotCalled("isCreated");
        }

        if (isCreated && assignee != null)
        {
            mockIssue.expectVoid("store", P.ANY_ARGS);
        }
        else
        {
            mockIssue.expectNotCalled("store");
        }
        return mockIssue;
    }

    private AssignToLeadFunction createFunction(final User lead)
    {
        return new AssignToLeadFunction()
        {
            ApplicationUser getLead(String userName)
            {
                return new MockApplicationUser(lead.getName(), lead.getDisplayName(),lead.getEmailAddress());
            }
        };
    }
}
