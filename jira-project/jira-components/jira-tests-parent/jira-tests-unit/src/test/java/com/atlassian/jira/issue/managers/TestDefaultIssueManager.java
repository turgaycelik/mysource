package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultIssueManager
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private OfBizDelegator delegator = new MockOfBizDelegator();

    @Mock
    @AvailableInContainer
    private DelegatorInterface delegatorInterface;

    @Mock
    private WorkflowManager workflowManager;

    @Mock
    private NodeAssociationStore nodeAssociationStore;

    @Mock
    private UserAssociationStore userAssociationStore;

    @Mock
    private IssueUpdater issueUpdater;

    @AvailableInContainer
    @Mock
    private PermissionManager permissionManager;

    @Mock
    private MovedIssueKeyStore movedIssueKeyStore;

    @Mock
    private TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;

    MockProjectManager projectManager = new MockProjectManager();

    @Mock
    private ProjectKeyStore projectKeyStore;

    @Mock
    @AvailableInContainer
    private IssueFactory issueFactory;

    private GenericValue issue1;
    private GenericValue issue2;
    private GenericValue issue3;

    private MockIssue issueObject1;
    private MockIssue issueObject2;
    private MockIssue issueObject3;

    private ApplicationUser user;
    DefaultIssueManager issueManager;

    @Before
    public void setUp() throws Exception
    {
        issueManager = new DefaultIssueManager(delegator, workflowManager, nodeAssociationStore, userAssociationStore,
                issueUpdater, permissionManager, movedIssueKeyStore, projectKeyStore, textFieldCharacterLengthValidator);
        issue1 = UtilsForTests.getTestEntity("Issue",
                EasyMap.build("id", new Long(1), "number", 7348L, "workflowId", new Long(1000), "priority", "C",
                        "project", new Long(10)));
        issue2 = UtilsForTests.getTestEntity("Issue",
                EasyMap.build("id", new Long(2), "number", 7349L, "workflowId", new Long(1001), "priority", "B",
                        "project", new Long(10)));
        issue3 = UtilsForTests.getTestEntity("Issue",
                EasyMap.build("id", new Long(3), "number", 7350L, "workflowId", new Long(1002), "priority", "A",
                        "project", new Long(10)));

        issueObject1 = new MockIssue(1);
        issueObject2 = new MockIssue(2);
        issueObject3 = new MockIssue(3);

        when(issueFactory.getIssue(issue1)).thenReturn(issueObject1);
        when(issueFactory.getIssue(issue2)).thenReturn(issueObject2);
        when(issueFactory.getIssue(issue3)).thenReturn(issueObject3);
        user = new MockApplicationUser("SomeUsername");
    }

    @Test
    public void testIssueLong() throws Exception
    {
        issueManager.getIssue(new Long(1));
    }

    @Test
    public void testIssueString() throws Exception
    {
        issueManager.getIssue("ABC-7348");
    }

    @Test
    public void testIssueWorkflow() throws Exception
    {
        issueManager.getIssueByWorkflow(new Long(1000));
    }

    @Test
    public void testEntitiesByIssueUsesAssociationStore() throws Exception
    {
        when(nodeAssociationStore.getSinksFromSource(issue1, "Version", IssueRelationConstants.VERSION))
                .thenReturn(ImmutableList.of(issue1, issue2, issue3));

        List<GenericValue> issues = issueManager.getEntitiesByIssue(IssueRelationConstants.VERSION, issue1);
        assertThat(issues, Matchers.containsInAnyOrder(issue1, issue2, issue3));
    }

    @Test
    public void testEntitiesByIssueReturnsEmptyListForUnknownArguments() throws Exception
    {
        assertEquals(0, issueManager.getEntitiesByIssue("foobar", null).size());
    }

    @Test
    public void testGetVotedIssuesUsesAssociationStore() throws Exception
    {
        when(userAssociationStore.getSinksFromUser("VoteIssue", user, Entity.Name.ISSUE))
                .thenReturn(Lists.newArrayList(issue1, issue2));

        final List<Issue> votedIssues = issueManager.getVotedIssuesOverrideSecurity(user);

        assertThat(votedIssues, Matchers.<Issue>containsInAnyOrder(issueObject1, issueObject2));
    }

    @Test
    public void testGetVotedIssuesChecksBrowsePermissions() throws Exception
    {
        when(userAssociationStore.getSinksFromUser("VoteIssue", user, Entity.Name.ISSUE))
                .thenReturn(Lists.newArrayList(issue1, issue2, issue3));
        when(permissionManager.hasPermission(Permissions.BROWSE, issueObject1, user)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issueObject2, user)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issueObject3, user)).thenReturn(true);

        final List<Issue> votedIssues = issueManager.getVotedIssues(user);

        assertThat(votedIssues, Matchers.<Issue>containsInAnyOrder(issueObject1, issueObject3));
    }

    @Test
    public void testGetWatchedIssuesUsesAssociationStore() throws Exception
    {
        when(userAssociationStore.getSinksFromUser("WatchIssue", user, Entity.Name.ISSUE))
                .thenReturn(Lists.newArrayList(issue1, issue2));

        final List<Issue> votedIssues = issueManager.getWatchedIssuesOverrideSecurity(user);

        assertThat(votedIssues, Matchers.<Issue>containsInAnyOrder(issueObject1, issueObject2));
    }

    @Test
    public void testGetWatchedIssuesChecksBrowsePermissions() throws Exception
    {
        when(userAssociationStore.getSinksFromUser("WatchIssue", user, Entity.Name.ISSUE))
                .thenReturn(Lists.newArrayList(issue1, issue2, issue3));
        when(permissionManager.hasPermission(Permissions.BROWSE, issueObject1, user)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, issueObject2, user)).thenReturn(false);
        when(permissionManager.hasPermission(Permissions.BROWSE, issueObject3, user)).thenReturn(true);

        final List<Issue> votedIssues = issueManager.getWatchedIssues(user);

        assertThat(votedIssues, Matchers.<Issue>containsInAnyOrder(issueObject1, issueObject3));
    }

    @Test
    public void testIssueIdsForProjectHappyPath() throws Exception
    {
        final Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(new Long(10));
        assertThat(issueIdsForProject, Matchers.containsInAnyOrder(1L, 2L, 3L));
    }

    @Test(expected = NullPointerException.class)
    public void testIssueIdsForProjectThrowsNullPointerForNullArgument() throws Exception
    {
        issueManager.getIssueIdsForProject(null);
    }

    @Test
    public void testIssueIdsForProjectReturnsEmptyForNonExistingProject() throws Exception
    {
        final Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(new Long(12344532L));
        assertTrue("Should return emtpy string is project does not exists", issueIdsForProject.isEmpty());
    }

    @Test
    public void testGetComments() throws GenericEntityException
    {
        final GenericValue genericValue = mock(GenericValue.class);

        issueManager.getEntitiesByIssue(IssueRelationConstants.COMMENTS, genericValue);

        final ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(genericValue).getRelatedByAnd(eq("ChildAction"), captor.capture());
        assertEquals(ActionConstants.TYPE_COMMENT, captor.getValue().get("type"));
    }

    @Test
    public void testGetIssueObjectWithNoIssueReturnsNull()
    {
        assertNull(issueManager.getIssueObject(33333L));
        assertNull(issueManager.getIssueObject("BS-1"));
    }

    @Test
    public void testUpdatePassesHistoryMetadata() throws Exception
    {
        // having
        final HistoryMetadata historyMetadata = HistoryMetadata.builder("testUpdatePassesHistoryMetadata").build();
        issueObject1.setModifiedFields(Maps.newHashMap());

        // when
        issueManager.updateIssue(user, issueObject1,
                UpdateIssueRequest.builder().historyMetadata(historyMetadata).build());

        // then
        verify(issueUpdater).doUpdate(argThat(new BaseMatcher<IssueUpdateBean>()
        {
            @Override
            public boolean matches(final Object item)
            {
                return item instanceof IssueUpdateBean &&
                        ((IssueUpdateBean) item).getHistoryMetadata().equals(historyMetadata);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("IssueUpdateBean with history metadata: " + historyMetadata);
            }
        }), anyBoolean());
    }

    @Test
    public void testFindProjectIssueTypes()
    {
        final MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        Project project1 = new MockProject(1L, "ABC");
        projectManager.addProject(project1);
        ofBizDelegator
                .createValue("Issue", new FieldMap("id", 123L).add("type", "2").add("project", 1L).add("number", 1L));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 124L).add("type", "3").add("project", 1L).add("number",
                2L));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 125L).add("type", "2").add("project", 1L).add("number",
                3L));
        Project project2 = new MockProject(2L, "DEF");
        projectManager.addProject(project2);
        ofBizDelegator.createValue("Issue", new FieldMap("id", 223L).add("type", "3").add("project", 2L).add("number",
                1L));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 224L).add("type", "4").add("project", 2L).add("number",
                2L));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 225L).add("type", "3").add("project", 2L).add("number",
                3L));
        DefaultIssueManager defaultIssueManager =
                new DefaultIssueManager(ofBizDelegator, null, null, null, null, null, movedIssueKeyStore,
                        projectKeyStore, textFieldCharacterLengthValidator)
                {
                    @Override
                    public MutableIssue getIssueObject(final Long id) throws DataAccessException
                    {
                        return new MockIssue(id);
                    }
                };

        when(projectKeyStore.getProjectId("ABC")).thenReturn(1L);
        when(projectKeyStore.getProjectId("DEF")).thenReturn(2L);

        Set<Pair<Long, String>> expectedResult =
                Sets.newHashSet(Pair.of(1L, "2"), Pair.of(1L, "3"), Pair.of(2L, "3"), Pair.of(2L, "4"));

        Set<Pair<Long, String>> keyProjectTypes =
                defaultIssueManager.getProjectIssueTypePairsByIds(Sets.newHashSet(123L, 124L, 125L, 223L, 224L, 225L));
        Assert.assertEquals(keyProjectTypes, expectedResult);

        keyProjectTypes = defaultIssueManager
                .getProjectIssueTypePairsByKeys(Sets.newHashSet("ABC-1", "ABC-2", "ABC-3", "DEF-1", "DEF-2", "DEF-3"));
        Assert.assertEquals(keyProjectTypes, expectedResult);

        expectedResult = Sets.newHashSet(Pair.of(1L, "2"), Pair.of(2L, "3"));

        keyProjectTypes =
                defaultIssueManager.getProjectIssueTypePairsByIds(Sets.newHashSet(123L, 1240L, 1L, 223L));
        Assert.assertEquals(keyProjectTypes, expectedResult);

        keyProjectTypes = defaultIssueManager
                .getProjectIssueTypePairsByKeys(Sets.newHashSet("ABC-1", "ABC2", "123456", "", "DEF-1"));
        Assert.assertEquals(keyProjectTypes, expectedResult);
    }

    @Test
    public void testFindMissingProjectKeysIds()
    {
        final MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        Project project1 = new MockProject(1L, "ABC");
        projectManager.addProject(project1);
        ofBizDelegator
                .createValue("Issue", new FieldMap("id", 123L).add("type", "2").add("project", 1L).add("number", 1L));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 124L).add("type", "3").add("project", 1L).add("number",
                2L));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 125L).add("type", "2").add("project", 1L).add("number",
                3L));
        Project project2 = new MockProject(2L, "DEF");
        projectManager.addProject(project2);
        ofBizDelegator.createValue("Issue", new FieldMap("id", 223L).add("type", "3").add("project", 2L).add("number",
                1L));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 224L).add("type", "4").add("project", 2L).add("number",
                2L));
        ofBizDelegator.createValue("Issue", new FieldMap("id", 225L).add("type", "3").add("project", 2L).add("number",
                3L));
        DefaultIssueManager defaultIssueManager =
                new DefaultIssueManager(ofBizDelegator, null, null, null, null, null, movedIssueKeyStore,
                        projectKeyStore, textFieldCharacterLengthValidator)
                {
                    @Override
                    public MutableIssue getIssueObject(final Long id) throws DataAccessException
                    {
                        return new MockIssue(id);
                    }
                };

        when(projectKeyStore.getProjectId("ABC")).thenReturn(1L);
        when(projectKeyStore.getProjectId("DEF")).thenReturn(2L);

        Set<Long> missingIds =
                defaultIssueManager.getIdsOfMissingIssues(Sets.newHashSet(123L, 124L, 125L, 223L, 224L, 225L));
        Assert.assertEquals(missingIds, new HashSet<Long>());

        missingIds =
                defaultIssueManager.getIdsOfMissingIssues(Sets.newHashSet(123L, 124L, 223L, 1001L, 1002L));
        Assert.assertEquals(missingIds, Sets.newHashSet(1001L, 1002L));

        Set<String> missingKeys =
                defaultIssueManager
                        .getKeysOfMissingIssues(Sets.newHashSet("ABC-1", "ABC-2", "ABC-3", "DEF-1", "DEF-2", "DEF-3"));
        Assert.assertEquals(missingKeys, new HashSet<String>());

        missingKeys =
                defaultIssueManager.getKeysOfMissingIssues(
                        Sets.newHashSet("ABC-1", "ABC-2", "DEF-1", "ABC2", "", "123456"));
        Assert.assertEquals(missingKeys, Sets.newHashSet("ABC2", "", "123456"));
    }
}
