package com.atlassian.jira.issue.worklog;

import java.util.Date;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith (ListeningMockitoRunner.class)
public class TestWorklogImpl
{

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    private static final ApplicationUser CREATE_AUTHOR = new MockApplicationUser("test create author");
    private static final Long TIME_SPENT = 1000L;
    private static final Date TIME_PERFORMED = new Date(20000);
    private static final Date UPDATED_DATE = new Date(34343);
    private static final Date CREATED_DATE = new Date(123456);
    private static final ApplicationUser UPDATED_AUTHOR =  new MockApplicationUser("updated author");

    @Before
    public void before(){
        when(userManager.getUserByKey(CREATE_AUTHOR.getKey())).thenReturn(CREATE_AUTHOR);
        when(userManager.getUserByKey(UPDATED_AUTHOR.getKey())).thenReturn(UPDATED_AUTHOR);
    }

    @Test
    public void testShortConstructorHappyPath()
    {
        WorklogImpl worklog = new WorklogImpl(null, null, null, CREATE_AUTHOR.getKey(), null, TIME_PERFORMED, null, null, TIME_SPENT);
        assertAuthors(worklog, CREATE_AUTHOR, CREATE_AUTHOR);
        assertNotNull(worklog.getCreated());
        assertEquals(worklog.getCreated(), worklog.getUpdated());
        assertEquals(TIME_PERFORMED, worklog.getStartDate());
    }

    @Test
    public void testShortConstructorNoStartDate()
    {
        WorklogImpl worklog = new WorklogImpl(null, null, null, CREATE_AUTHOR.getKey(), null, null, null, null, TIME_SPENT);
        assertAuthors(worklog, CREATE_AUTHOR, CREATE_AUTHOR);
        assertNotNull(worklog.getCreated());
        assertEquals(worklog.getCreated(), worklog.getUpdated());
        assertEquals(worklog.getCreated(), worklog.getStartDate());
    }

    @Test
    public void testShortConstructorNoTimeSpent()
    {
        try
        {
            WorklogImpl worklog = new WorklogImpl(null, null, null, CREATE_AUTHOR.getKey(), null, TIME_PERFORMED, null, null, null);
            fail("Should have thrown IllegalArgumentException if no timeSpent specified");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testLongConstructorHappyPath()
    {
        WorklogImpl worklog = new WorklogImpl(null, null, null, CREATE_AUTHOR.getKey(), null, TIME_PERFORMED, null, null, TIME_SPENT, UPDATED_AUTHOR.getKey(), CREATED_DATE, UPDATED_DATE);
        assertAuthors(worklog, CREATE_AUTHOR, UPDATED_AUTHOR);
        assertEquals(CREATED_DATE, worklog.getCreated());
        assertEquals(UPDATED_DATE, worklog.getUpdated());
        assertEquals(TIME_PERFORMED, worklog.getStartDate());
    }

    private void assertAuthors(WorklogImpl worklog, ApplicationUser createAuthor, ApplicationUser updatedAuthor)
    {
        assertEquals(createAuthor.getKey(), worklog.getAuthor());
        assertEquals(createAuthor.getKey(), worklog.getAuthorKey());
        assertSame(createAuthor, worklog.getAuthorObject());
        assertEquals(updatedAuthor.getKey(), worklog.getUpdateAuthor());
        assertEquals(updatedAuthor.getKey(), worklog.getUpdateAuthorKey());
        assertSame(updatedAuthor, worklog.getUpdateAuthorObject());
    }
}
