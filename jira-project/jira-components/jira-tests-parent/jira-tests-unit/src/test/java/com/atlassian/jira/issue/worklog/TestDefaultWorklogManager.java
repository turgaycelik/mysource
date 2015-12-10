/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.worklog;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.mock.issue.MockIssue;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultWorklogManager
{

    @Test
    public void testCreateWorklogNullWorklog()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null);
        try
        {
            defaultWorklogManager.create(null, null, null, true);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Worklog must not be null.", e.getMessage());
        }
    }

    @Test
    public void testCreateWorklogNullIssueInWorklog()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null);
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);
        try
        {
            defaultWorklogManager.create(null, (Worklog) mockWorklog.proxy(), null, true);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("The worklogs issue must not be null.", e.getMessage());
        }
    }

    @Test
    public void testCreateWorklogHappyPath()
    {
        final Mock mockTimeTrackingIssueUpdater = new Mock(TimeTrackingIssueUpdater.class);
        mockTimeTrackingIssueUpdater.expectVoid("updateIssueOnWorklogCreate", P.ANY_ARGS);

        final Mock mockOrigWorklog = new Mock(Worklog.class);
        mockOrigWorklog.expectAndReturn("getIssue", new MockIssue());

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);
        mockWorklog.expectAndReturn("getComment", null);

        final Mock mockWorklogStore = new Mock(WorklogStore.class);
        mockWorklogStore.expectAndReturn("create", P.ANY_ARGS, mockWorklog.proxy());

        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null,
            (WorklogStore) mockWorklogStore.proxy(), (TimeTrackingIssueUpdater) mockTimeTrackingIssueUpdater.proxy());
        defaultWorklogManager.create(null, (Worklog) mockOrigWorklog.proxy(), null, true);

        mockTimeTrackingIssueUpdater.verify();
        mockOrigWorklog.verify();
        mockWorklogStore.verify();
    }

    @Test
    public void testUpdateWorklogNullWorklog()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null);
        try
        {
            defaultWorklogManager.update(null, null, null, true);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Worklog must not be null.", e.getMessage());
        }
    }

    @Test
    public void testUpdateWorklogNullIssueInWorklog()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null);
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);
        try
        {
            defaultWorklogManager.update(null, (Worklog) mockWorklog.proxy(), null, true);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("The worklogs issue must not be null.", e.getMessage());
        }
    }

    @Test
    public void testUpdateWorklogNullOriginalWorklog()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null)
        {
            @Override
            public Worklog getById(final Long id)
            {
                return null;
            }
        };

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(10));

        try
        {
            defaultWorklogManager.update(null, (Worklog) mockWorklog.proxy(), null, true);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Unable to find a worklog in the datastore for the provided id: '10'", e.getMessage());
        }
    }

    @Test
    public void testUpdateWorklogHappyPath()
    {
        final Mock mockTimeTrackingIssueUpdater = new Mock(TimeTrackingIssueUpdater.class);
        final Long originalTimeSpent = new Long(10);
        mockTimeTrackingIssueUpdater.expectVoid(
            "updateIssueOnWorklogUpdate",
            new Constraint[] { new IsAnything(), new IsAnything(), new IsAnything(), new IsEqual(originalTimeSpent), new IsAnything(), new IsAnything() });

        // This is the original worklog as stored in the DB, it provides the original time spent for the worklog
        final Mock mockStoredWorklog = new Mock(Worklog.class);
        mockStoredWorklog.expectAndReturn("getTimeSpent", originalTimeSpent);

        // This is the worklog that we want to update the stored value to be, it only needs an issue so that we pass
        // basic validation.
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));

        // This is the worklog that is passed back from the store because of the edit call to the store.
        final Mock mockEditedWorklog = new Mock(Worklog.class);
        final Mock mockWorklogStore = new Mock(WorklogStore.class);
        mockWorklogStore.expectAndReturn("update", P.ANY_ARGS, mockEditedWorklog.proxy());

        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null,
            (WorklogStore) mockWorklogStore.proxy(), (TimeTrackingIssueUpdater) mockTimeTrackingIssueUpdater.proxy())
        {
            @Override
            public Worklog getById(final Long id)
            {
                return (Worklog) mockStoredWorklog.proxy();
            }
        };
        defaultWorklogManager.update(null, (Worklog) mockWorklog.proxy(), null, true);

        mockTimeTrackingIssueUpdater.verify();
        mockStoredWorklog.verify();
        mockWorklog.verify();
        mockWorklogStore.verify();
    }

    @Test
    public void testDeleteWorklogHappyPath()
    {
        final Mock mockTimeTrackingIssueUpdater = new Mock(TimeTrackingIssueUpdater.class);
        mockTimeTrackingIssueUpdater.expectVoid("updateIssueOnWorklogDelete", P.ANY_ARGS);

        final Mock mockWorklogStore = new Mock(WorklogStore.class);
        mockWorklogStore.expectAndReturn("delete", P.ANY_ARGS, Boolean.TRUE);

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getId", null);

        final AtomicBoolean validateWorklogCalled = new AtomicBoolean(false);
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, (WorklogStore) mockWorklogStore.proxy(),
            (TimeTrackingIssueUpdater) mockTimeTrackingIssueUpdater.proxy())
        {
            @Override
            void validateWorklog(final Worklog worklog, final boolean create)
            {
                validateWorklogCalled.set(true);
            }
        };

        defaultWorklogManager.delete(null, (Worklog) mockWorklog.proxy(), null, false);

        mockTimeTrackingIssueUpdater.verify();
        assertTrue(validateWorklogCalled.get());
        mockWorklogStore.verify();
        mockWorklog.verify();
    }

    @Test
    public void testValidateWorklogNullWorklog()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null);

        try
        {
            defaultWorklogManager.validateWorklog(null, false);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            //expected
            assertEquals("Worklog must not be null.", e.getMessage());
        }
    }

    @Test
    public void testValidateWorklogNullIssueOnWorklog()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null);

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);

        try
        {
            defaultWorklogManager.validateWorklog((Worklog) mockWorklog.proxy(), false);
            fail("Should have thrown IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            //expected
            assertEquals("The worklogs issue must not be null.", e.getMessage());
        }
    }

    @Test
    public void testValidateWorklogNullWorklogIdNotCreate()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null);

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", null);

        try
        {
            defaultWorklogManager.validateWorklog((Worklog) mockWorklog.proxy(), true);
        }
        catch (final IllegalArgumentException e)
        {
            fail("Should NOT have thrown IllegalArgumentException");
        }
    }

    @Test
    public void testValidateWorklogNullWorklogId()
    {
        final DefaultWorklogManager defaultWorklogManager = new DefaultWorklogManager(null, null, null);

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", null);

        try
        {
            defaultWorklogManager.validateWorklog((Worklog) mockWorklog.proxy(), false);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("Can not modify a worklog with a null id.", e.getMessage());
        }
    }
}
