package com.atlassian.jira.issue.action;

import java.util.Date;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryStub;
import com.atlassian.jira.issue.comments.CommentImpl;
import com.atlassian.jira.issue.fields.renderer.comment.CommentFieldRenderer;
import com.atlassian.jira.issue.tabpanels.CommentAction;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.issue.tabpanels.WorklogAction;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestIssueActionComparator
{

    private static DateTimeFormatter dateTimeFormatterFactory = new DateTimeFormatterFactoryStub().formatter();
    private static User user = new MockUser("user");

    /**
     * Tests that IssueActionComparator correctly handles nulls
     */
    @Test
    public void testIssueActionComparatorNulls()
    {
        IssueActionComparator comp = new IssueActionComparator();
        MockCommentAction comment = new MockCommentAction(new Date(System.currentTimeMillis()));

        try
        {
            //null is not istanceof IssueAction
            assertTrue(0 == comp.compare(null, null));
            fail("can only compare issue actions");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Can only compare with '" + IssueAction.class.getName() + "'.", e.getMessage());
        }

        assertTrue(comp.compare(null, comment) < 0);
        assertTrue(comp.compare(comment, null) > 0);
    }

    /**
     * Tests that the earlier date is returned, and that IssueActions implementing getTimePerformed() to return
     * a Date and/or a Timestamp can be correctly compared
     */
    @Test
    public void testIssueActionComparatorWithActionsImplementingGetTimePerformed()
    {
        IssueActionComparator comp = new IssueActionComparator();

        long currentDate = System.currentTimeMillis();

        //CommentAction implements getTimePerformed() returning a Date
        MockCommentAction comment1 = new MockCommentAction(new Date(currentDate));
        MockCommentAction comment2 = new MockCommentAction(new Date(currentDate + 1000));

        //WorklogAction implements getTimePerformed() returning a Timestamp
        MockWorklogAction worklog1 = new MockWorklogAction(new Date(currentDate + 500));
        MockWorklogAction worklog2 = new MockWorklogAction(new Date(currentDate + 1500));

        //Both return a Date
        assertTrue(comp.compare(comment1, comment2) < 0);
        assertTrue(comp.compare(comment2, comment1) > 0);

        //Both return a Timestamp
        assertTrue(comp.compare(worklog1, worklog2) < 0);
        assertTrue(comp.compare(worklog2, worklog1) > 0);

        //One Timestamp, one Date
        assertTrue(comp.compare(comment1, worklog1) < 0);
        assertTrue(comp.compare(comment2, worklog1) > 0);
    }

    /**
     * Tests that if only one issue properly implements getTimePerformed() (and the other throws an
     * UnsupportedOperationException), the object of the type of the properly implementing class is returned
     */
    @Test
    public void testIssueActionComparatorWithActionsNotImplementingGetTimePerformed()
    {
        IssueActionComparator comp = new IssueActionComparator();

        long currentDate = System.currentTimeMillis();
        MockCommentAction comment = new MockCommentAction(new Date(currentDate));
        GenericMessageAction noTime1 = new GenericMessageAction("message 1");
        GenericMessageAction noTime2 = new GenericMessageAction("message 2");

        assertTrue(comp.compare(noTime1, comment) < 0); //first does not have time performed
        assertTrue(comp.compare(comment, noTime1) > 0); //second does not have time performed

        //time performed not impl (both handled as null)
        assertTrue(comp.compare(noTime1, noTime2) == 0);
        assertTrue(comp.compare(noTime2, noTime1) == 0);

        //same issue action
        assertTrue(comp.compare(comment, comment) == 0);
        assertTrue(comp.compare(noTime1, noTime1) == 0);
    }

    class MockWorklogAction extends WorklogAction
    {
        public MockWorklogAction(Date startDate)
        {
            super(null, new WorklogImpl(null,null, null, null,null,startDate,null,null, new Long(1)), null, true, true, null, null, null);
        }
    }

    class MockCommentAction extends CommentAction
    {
        public MockCommentAction(Date created)
        {
            super(null, new CommentImpl(null,null,null,null,null,null,created,null,null), false, false, false, null, null, dateTimeFormatterFactory,
                    mock(CommentFieldRenderer.class), mock(CommentPropertyService.class), mock(JiraAuthenticationContext.class));
        }
    }

}
