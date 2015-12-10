package com.atlassian.jira.plugin.webfragment.conditions;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestHasSubTasksAvailableCondition
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final FieldManager fieldManager = mocksControl.createMock(FieldManager.class);
        final Option option = mocksControl.createMock(Option.class);

        IssueTypeSystemField field = new IssueTypeSystemField(null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public Collection getOptionsForIssue(Issue issue, boolean isSubTask)
            {
                return CollectionBuilder.list(option);
            }
        };

        expect(fieldManager.getIssueTypeField()).andReturn(field);

        final HasSubTasksAvailableCondition condition = new HasSubTasksAvailableCondition(fieldManager);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testFalseEmpty()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final FieldManager fieldManager = mocksControl.createMock(FieldManager.class);

        IssueTypeSystemField field = new IssueTypeSystemField(null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public Collection getOptionsForIssue(Issue issue, boolean isSubTask)
            {
                return Collections.emptyList();
            }
        };

        expect(fieldManager.getIssueTypeField()).andReturn(field);

        final HasSubTasksAvailableCondition condition = new HasSubTasksAvailableCondition(fieldManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testFalseNull()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final FieldManager fieldManager = mocksControl.createMock(FieldManager.class);

        IssueTypeSystemField field = new IssueTypeSystemField(null, null, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public Collection getOptionsForIssue(Issue issue, boolean isSubTask)
            {
                return null;
            }
        };

        expect(fieldManager.getIssueTypeField()).andReturn(field);

        final HasSubTasksAvailableCondition condition = new HasSubTasksAvailableCondition(fieldManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }
}
