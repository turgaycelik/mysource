package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.config.SubTaskManager;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestSubTasksEnabledCondition
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final SubTaskManager subTaskManager = mocksControl.createMock(SubTaskManager.class);

        expect(subTaskManager.isSubTasksEnabled()).andReturn(true);

        final SubTasksEnabledCondition condition = new SubTasksEnabledCondition(subTaskManager);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, null));
        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final SubTaskManager subTaskManager = mocksControl.createMock(SubTaskManager.class);

        expect(subTaskManager.isSubTasksEnabled()).andReturn(false);

        final SubTasksEnabledCondition condition = new SubTasksEnabledCondition(subTaskManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, null));
        mocksControl.verify();

    }

}
