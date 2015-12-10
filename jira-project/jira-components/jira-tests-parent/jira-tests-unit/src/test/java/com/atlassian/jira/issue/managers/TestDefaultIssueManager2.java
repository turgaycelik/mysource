package com.atlassian.jira.issue.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Lightweight tests for DefaultIssueManager
 *
 * @since v6.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultIssueManager2
{

    public static final String VERY_LONG_LINE = "This simulates a very long line";

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    OfBizDelegator ofBizDelegator;

    MockProjectManager projectManager = new MockProjectManager();

    @Mock
    MovedIssueKeyStore movedIssueKeyStore;

    @Mock
    ProjectKeyStore projectKeyStore;

    @Mock
    TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;

    @Mock
    WorkflowManager workflowManager;

    @Before
    public void setUp()
    {
        when(projectKeyStore.getProjectKeys(anyLong())).thenReturn(ImmutableSet.of("ABC"));
    }

    @Test
    public void testIssueWatchers()
    {
        final MockIssue issue = new MockIssue(12L);
        UserAssociationStore userAssociationStore = Mockito.mock(UserAssociationStore.class);
        List<ApplicationUser> users = Arrays.<ApplicationUser>asList(new MockApplicationUser("bob"), new MockApplicationUser("cathy"));
        when(userAssociationStore.getUsersFromSink("WatchIssue", issue.getGenericValue())).thenReturn(users);

        DefaultIssueManager defaultIssueManager = new DefaultIssueManager(null, null, null, userAssociationStore, null, null, null, projectKeyStore, textFieldCharacterLengthValidator);
        Collection<ApplicationUser> watchers = defaultIssueManager.getWatchersFor(issue);
        Assert.assertEquals(2, watchers.size());
        Assert.assertEquals(users, watchers);
    }

    @Test
    public void testFindMovedIssue()
    {
        final MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        projectManager.addProject(new MockProject(1L, "ABC"));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 123L));
        DefaultIssueManager defaultIssueManager = new DefaultIssueManager(ofBizDelegator, null, null, null, null, null, movedIssueKeyStore, projectKeyStore, textFieldCharacterLengthValidator)
        {
            @Override
            public MutableIssue getIssueObject(final Long id) throws DataAccessException
            {
                return new MockIssue(id);
            }
        };

        when(movedIssueKeyStore.getMovedIssueId("ABC-1")).thenReturn(null);
        Issue issue = defaultIssueManager.findMovedIssue("ABC-1");
        Assert.assertNull(issue);

        when(movedIssueKeyStore.getMovedIssueId("ABC-1")).thenReturn(123L);
        issue = defaultIssueManager.findMovedIssue("ABC-1");
        Assert.assertEquals(new Long(123), issue.getId());
    }

    @Test
    public void testGetPreviousIssueKeysWithInvalidOriginalKey()
    {
        final DefaultIssueManager issueManager = new DefaultIssueManager(null, null, null, null, null, null, null, projectKeyStore, textFieldCharacterLengthValidator);

        try
        {
            issueManager.getAllIssueKeys(null);
            fail("Null's not allowed!");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("issueId should not be null!", e.getMessage());
        }
    }

    @Test
    public void testGetPreviousIssueKeysWithId()
    {
        final MutableIssue issue = new MockIssue(10023, "ABC-25");
        final MockProject project = new MockProject(1L);
        project.setKey("HSP");
        issue.setProjectObject(project);
        issue.setNumber(25L);

         when(ofBizDelegator.findByCondition
                        (
                                eq("MovedIssueKey"),
                                any(EntityCondition.class),
                                eq(ImmutableList.of("oldIssueKey")),
                                eq(ImmutableList.of("id"))
                        )
        ).thenReturn(getMockChangeItemGVs());



        final DefaultIssueManager issueManager = new DefaultIssueManager(ofBizDelegator, null, null, null, null, null, null, projectKeyStore, textFieldCharacterLengthValidator)
        {
            @Override
            public MutableIssue getIssueObject(final Long id) throws DataAccessException
            {
                return issue;
            }
        };

        final Collection<String> previousKeys = issueManager.getAllIssueKeys(10023L);

        assertFalse(previousKeys.isEmpty());
        assertEquals(4, previousKeys.size());
        assertTrue(previousKeys.containsAll(ImmutableList.of("MKY-12", "STUFF-23", "BLAH-2", "ABC-25")));
    }

    private List<GenericValue> getMockChangeItemGVs()
    {
        return ImmutableList.<GenericValue>of(
                new MockGenericValue("ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>of("id", 10023L, "oldIssueKey", "MKY-12")),
                new MockGenericValue("ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>of("id", 10023L, "oldIssueKey", "STUFF-23")),
                new MockGenericValue("ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>of("id", 10023L, "oldIssueKey", "BLAH-2"))
        );
    }

    @Test
    public void testGetIssueByCurrentKeyReturnsIssue() throws Exception
    {
        when(movedIssueKeyStore.getMovedIssueId("ABC-25")).thenReturn(null);
        final DefaultIssueManager issueManager = createIssueManagerForTestingGetIssueByCurrentKey();
        assertThat(issueManager.getIssueByCurrentKey("ABC-25"), CoreMatchers.any(Issue.class));
    }


    @Test
    public void testGetIssueByCurrentKeyReturnsNullForWrongKey() throws Exception
    {
        when(movedIssueKeyStore.getMovedIssueId("WRONG")).thenReturn(null);
        final DefaultIssueManager issueManager = new DefaultIssueManager(null, null, null, null, null, null, movedIssueKeyStore, projectKeyStore, textFieldCharacterLengthValidator);
        assertThat(issueManager.getIssueByCurrentKey("WRONG"), nullValue());
    }

    private DefaultIssueManager createIssueManagerForTestingGetIssueByCurrentKey()
    {
        final MutableIssue issue = new MockIssue(10023, "ABC-25");
        final GenericValue gv = new MockGenericValue("Issue");
        gv.setString("key", "ABC-25");
        final DefaultIssueManager.IssueFinder issueFinder = Mockito.mock(DefaultIssueManager.IssueFinder.class);
        try
        {
            when(issueFinder.getIssue("ABC-25")).thenReturn(gv);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }

        return new DefaultIssueManager(null, null, null, null, null, null, movedIssueKeyStore, projectKeyStore, textFieldCharacterLengthValidator)
        {
            @Override
            protected MutableIssue getIssueObject(final GenericValue issueGV)
            {
                return issue;
            }

            @Override
            IssueFinder getIssueFinder()
            {
                return issueFinder;
            }
        };

    }

    @Test
    public void createIssueHappyPath() throws Exception
    {
        final DefaultIssueManager issueManager = getDefaultIssueManager();
        Issue issue = Mockito.mock(Issue.class);

        setCharacterLimit(Integer.MAX_VALUE);

        issueManager.createIssue((User) null, issue);
    }

    @Test
    public void createIssueHappyPathFromMap() throws Exception
    {
        final DefaultIssueManager issueManager = getDefaultIssueManager();
        Issue issue = Mockito.mock(Issue.class);

        setCharacterLimit(Integer.MAX_VALUE);

        issueManager.createIssue("foo", (Map) ImmutableMap.of("issue", issue));
    }

    @Test
    public void createIssueThrowsCreateExceptionWhenDescriptionTooLong() throws Exception
    {
        final DefaultIssueManager issueManager = getDefaultIssueManager();
        Issue issue = Mockito.mock(Issue.class);
        when(issue.getDescription()).thenReturn(VERY_LONG_LINE);

        setCharacterLimit(VERY_LONG_LINE.length() - 1);
        expectedException.expect(CreateException.class);
        expectedException.expectMessage(containsString("field exceeds character limit"));

        issueManager.createIssue((User) null, issue);
    }

    @Test
    public void issueManagerValidatesEnvironmentLength() throws Exception
    {
        final DefaultIssueManager issueManager = getDefaultIssueManager();
        Issue issue = Mockito.mock(Issue.class);
        when(issue.getEnvironment()).thenReturn(VERY_LONG_LINE);

        setCharacterLimit(VERY_LONG_LINE.length() - 1);
        expectedException.expect(CreateException.class);
        expectedException.expectMessage(containsString("field exceeds character limit"));

        issueManager.createIssue((User) null, issue);
    }

    @Test
    public void issueManagerValidatesDescriptionLengthFromMap() throws Exception
    {
        final DefaultIssueManager issueManager = getDefaultIssueManager();
        Issue issue = Mockito.mock(Issue.class);
        String description = VERY_LONG_LINE;
        when(issue.getDescription()).thenReturn(description);

        setCharacterLimit(description.length() - 1);
        expectedException.expect(CreateException.class);
        expectedException.expectMessage(containsString("field exceeds character limit"));

        issueManager.createIssue("foo", (Map) ImmutableMap.of("issue", issue));
    }

    @Test
    public void issueManagerValidatesEnvironmentLengthFromMap() throws Exception
    {
        final DefaultIssueManager issueManager = getDefaultIssueManager();
        Issue issue = Mockito.mock(Issue.class);
        String environment = VERY_LONG_LINE;
        when(issue.getEnvironment()).thenReturn(environment);

        setCharacterLimit(environment.length() - 1);
        expectedException.expect(CreateException.class);
        expectedException.expectMessage(containsString("field exceeds character limit"));

        issueManager.createIssue("bar", (Map) ImmutableMap.of("issue", issue));
    }

    private DefaultIssueManager getDefaultIssueManager()
    {
        return new DefaultIssueManager(ofBizDelegator, workflowManager, null, null, null, null, null, null, textFieldCharacterLengthValidator);
    }

    private void setCharacterLimit(final long limit)
            throws CreateException
    {
        when(textFieldCharacterLengthValidator.isTextTooLong(anyString())).thenAnswer(new Answer<Boolean>()
        {
            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable
            {
                String input = (String) invocation.getArguments()[0];
                return input != null && input.length() > limit;
            }
        });
        when(textFieldCharacterLengthValidator.getMaximumNumberOfCharacters()).thenReturn(limit);
    }



}