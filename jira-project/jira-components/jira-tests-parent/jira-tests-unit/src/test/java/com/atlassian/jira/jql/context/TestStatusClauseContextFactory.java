package com.atlassian.jira.jql.context;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.StatusResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestStatusClauseContextFactory
{
    private static final String field = "status";

    private IssueTypeSchemeManager issueTypeSchemeManager;
    private WorkflowManager workflowManager;
    private PermissionManager permissionManager;
    private JqlOperandResolver jqlOperandResolver;
    private StatusResolver statusResolver;
    private WorkflowSchemeManager workflowSchemeManager;
    private Project project1;
    private Project project2;
    private IssueType issueType1;
    private IssueType issueType2;

    private Status status1;
    private Status status2;

    private IMocksControl mockController;

    @Before
    public void setUp() throws Exception
    {
        mockController = EasyMock.createNiceControl();
        issueTypeSchemeManager = mockController.createMock(IssueTypeSchemeManager.class);
        workflowManager = mockController.createMock(WorkflowManager.class);
        permissionManager = mockController.createMock(PermissionManager.class);
        jqlOperandResolver = mockController.createMock(JqlOperandResolver.class);
        statusResolver = mockController.createMock(StatusResolver.class);
        workflowSchemeManager = mockController.createMock(WorkflowSchemeManager.class);

        project1 = new MockProject(1);
        project2 = new MockProject(2);

        issueType1 = new MockIssueType("10", "name");
        issueType2 = new MockIssueType("20", "name");

        status1 = new MockStatus("72828", "Status1");
        status2 = new MockStatus("45454", "Status2");
    }

    @After
    public void tearDown() throws Exception
    {
        issueTypeSchemeManager = null;
        workflowManager = null;
        permissionManager = null;
        jqlOperandResolver = null;
        statusResolver = null;
        workflowSchemeManager = null;

        project1 = null;
        project2 = null;

        issueType1 = null;
        issueType2 = null;

        status1 = null;
        status2 = null;
    }

    @Test
    public void testGetClauseContextIsEmpty() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.IS, operand);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        mockController.replay();

        final StatusClauseContextFactory factory = new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager);
        final ClauseContext result = factory.getClauseContext(null, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextIsNotEmpty() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.IS_NOT, operand);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        mockController.replay();

        final StatusClauseContextFactory factory = new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager);
        final ClauseContext result = factory.getClauseContext(null, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextBadOperator() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.GREATER_THAN, operand);

        mockController.replay();

        final StatusClauseContextFactory factory = new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager);
        final ClauseContext result = factory.getClauseContext(null, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    //
    //Test when status is matched by no workflow.
    //
    @Test
    public void testGetClauseContextStatusNoneFound() throws Exception
    {
        final JiraWorkflow workflow = mockController.createMock(JiraWorkflow.class);

        // project1 -> {(null, workflow1[11])}

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1).asList());
        expect(workflowSchemeManager.getWorkflowMap(project1)).andReturn(MapBuilder.<String, String>build(null, "workflow1"));
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow);
        expect(workflow.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(new MockStatus("11", "dontMatchMe")));

        mockController.replay();

        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId(), status1.getId()).asListOrderedSet();
        assertEquals(Collections.<ProjectIssueTypeContext>emptySet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), true));

        mockController.verify();
    }

    //
    // Test what happens when status matches are for all project, issue type combinations. It should return global
    //
    @Test
    public void testGetClauseContextStatusFoundGloabal() throws Exception
    {
        final JiraWorkflow workflow1 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow2 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow3 = mockController.createMock(JiraWorkflow.class);

        //project1 -> {(null, workflow1[10]), (34, workflow2[20])}
        //project1 -> {(null, workflow3[20])}

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1, project2).asList());

        expect(workflowSchemeManager.getWorkflowMap(project1))
                .andReturn(MapBuilder.<String, String>newBuilder(null, "workflow1").add("34", "workflow2").toMutableMap());
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow1);
        expect(workflow1.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1));
        expect(issueTypeSchemeManager.getIssueTypesForProject(project1)).andReturn(CollectionBuilder.list(issueType1, issueType2));
        expect(workflowManager.getWorkflow("workflow2")).andReturn(workflow2);
        expect(workflow2.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status2));


        expect(workflowSchemeManager.getWorkflowMap(project2))
                .andReturn(MapBuilder.<String, String>newBuilder(null, "workflow3").toMutableMap());
        expect(workflowManager.getWorkflow("workflow3")).andReturn(workflow3);
        expect(issueTypeSchemeManager.getIssueTypesForProject(project2)).andReturn(CollectionBuilder.list(issueType2));
        expect(workflow3.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status2));

        mockController.replay();

        final CollectionBuilder<ProjectIssueTypeContext> result = CollectionBuilder.<ProjectIssueTypeContext>newBuilder()
                .add(ProjectIssueTypeContextImpl.createGlobalContext());

        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId(), status2.getId()).asListOrderedSet();
        assertEquals(result.asSet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), true));

        mockController.verify();
    }

    //
    // Test what happens when status matches are for all the issue types in one project.
    //
    @Test
    public void testGetClauseContextStatusFoundProject() throws Exception
    {
        final JiraWorkflow workflow1 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow2 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow3 = mockController.createMock(JiraWorkflow.class);

        //project1 -> {(null, workflow1[10]), (34, workflow2[20])}
        //project2 -> {(null, workflow3[484949])}

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1, project2).asList());

        expect(workflowSchemeManager.getWorkflowMap(project1))
                .andReturn(MapBuilder.<String, String>newBuilder(null, "workflow1").add("34", "workflow2").toMutableMap());
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow1);
        expect(workflow1.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1));
        expect(issueTypeSchemeManager.getIssueTypesForProject(project1)).andReturn(CollectionBuilder.list(issueType1, issueType2));
        expect(workflowManager.getWorkflow("workflow2")).andReturn(workflow2);
        expect(workflow2.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status2));


        expect(workflowSchemeManager.getWorkflowMap(project2))
                .andReturn(MapBuilder.<String, String>newBuilder(null, "workflow3").toMutableMap());
        expect(workflowManager.getWorkflow("workflow3")).andReturn(workflow3);
        expect(workflow3.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(new MockStatus("484949", "dontMatch")));

        mockController.replay();

        final CollectionBuilder<ProjectIssueTypeContext> result = CollectionBuilder.<ProjectIssueTypeContext>newBuilder()
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project1.getId()), AllIssueTypesContext.getInstance()));

        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId(), status2.getId()).asListOrderedSet();
        assertEquals(result.asSet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), true));

        mockController.verify();
    }

    //
    // Test what happens when status matches are for project & issue type combinations.
    //
    @Test
    public void testGetClauseContextStatusFoundProjectIssueType() throws Exception
    {
        final JiraWorkflow workflow1 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow2 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow3 = mockController.createMock(JiraWorkflow.class);

        //project1 -> {(null, workflow1[10]), (34, workflow2[484949])}
        //project2 -> {(null, workflow3[484949])}

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1, project2).asList());

        expect(workflowSchemeManager.getWorkflowMap(project1))
                .andReturn(MapBuilder.<String, String>newBuilder(null, "workflow1").add("34", "workflow2").toMutableMap());
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow1);
        expect(workflow1.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1));
        expect(issueTypeSchemeManager.getIssueTypesForProject(project1)).andReturn(CollectionBuilder.list(issueType1, issueType2));
        expect(workflowManager.getWorkflow("workflow2")).andReturn(workflow2);
        expect(workflow2.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status2));


        expect(workflowSchemeManager.getWorkflowMap(project2))
                .andReturn(MapBuilder.<String, String>newBuilder(null, "workflow3").toMutableMap());
        expect(workflowManager.getWorkflow("workflow3")).andReturn(workflow3);
        expect(workflow3.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status2));

        mockController.replay();

        final CollectionBuilder<ProjectIssueTypeContext> result = CollectionBuilder.<ProjectIssueTypeContext>newBuilder()
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project1.getId()), new IssueTypeContextImpl(issueType1.getId())))
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project1.getId()), new IssueTypeContextImpl(issueType2.getId())));

        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId()).asListOrderedSet();
        assertEquals(result.asSet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), true));

        mockController.verify();
    }

    //
    // Test a simple combination of issue contexts.
    //
    @Test
    public void testGetClauseContextStatusFoundCombination() throws Exception
    {
        final String issueType3Id = "45";

        final JiraWorkflow workflow1 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow2 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow3 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow4 = mockController.createMock(JiraWorkflow.class);

        //project1 -> {(null, workflow1[10]), (34, workflow2[10])}
        //project2 -> {(null, workflow3[89]), (45, workflow4[10])}

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1, project2).asList());

        expect(workflowSchemeManager.getWorkflowMap(project1))
                .andReturn(MapBuilder.<String, String>newBuilder(null, "workflow1").add("34", "workflow2").toMutableMap());
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow1);
        expect(workflow1.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1));
        expect(issueTypeSchemeManager.getIssueTypesForProject(project1)).andReturn(CollectionBuilder.list(issueType1, issueType2));
        expect(workflowManager.getWorkflow("workflow2")).andReturn(workflow2);
        expect(workflow2.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1));

        expect(workflowSchemeManager.getWorkflowMap(project2))
                .andReturn(MapBuilder.<String, String>newBuilder(null, "workflow3").add(issueType3Id, "workflow4").toMutableMap());
        expect(workflowManager.getWorkflow("workflow3")).andReturn(workflow3);
        expect(workflow3.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(new MockStatus("89", "dontMatch")));
        expect(workflowManager.getWorkflow("workflow4")).andReturn(workflow4);
        expect(workflow4.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1));

        mockController.replay();

        final CollectionBuilder<ProjectIssueTypeContext> result = CollectionBuilder.<ProjectIssueTypeContext>newBuilder()
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project1.getId()), AllIssueTypesContext.INSTANCE))
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project2.getId()), new IssueTypeContextImpl(issueType3Id)));

        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId(), status2.getId()).asListOrderedSet();
        assertEquals(result.asSet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), true));

        mockController.verify();
    }

    //
    // Test what happens when every workflow matches during negation.
    //
    @Test
    public void testGetClauseContextStatusNegationNoneFound() throws Exception
    {
        final JiraWorkflow workflow = mockController.createMock(JiraWorkflow.class);

        //project1 -> {(null, workflow1[10])}

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1).asList());
        expect(workflowSchemeManager.getWorkflowMap(project1)).andReturn(MapBuilder.<String, String>build(null, "workflow1"));
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow);
        expect(workflow.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1));

        mockController.replay();

        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId(), status2.getId()).asListOrderedSet();
        assertEquals(Collections.<ProjectIssueTypeContext>emptySet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), false));

        mockController.verify();
    }

    //
    // Test what happens when one workflow matches during negation.
    //
    @Test
    public void testGetClauseContextStatusNegationOneFound() throws Exception
    {
        //project1 -> {(null, workflow1[10]), (434343, workflow2[11])}

        final String issueType3Id = "434343";
        final JiraWorkflow workflow = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow2 = mockController.createMock(JiraWorkflow.class);

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1).asList());
        expect(workflowSchemeManager.getWorkflowMap(project1)).andReturn(MapBuilder.<String, String>newBuilder(issueType3Id, "workflow1").add(null, "workflow2").toMutableMap());
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow);
        expect(workflow.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(new MockStatus("11", "dontMatchMe")));
        expect(workflowManager.getWorkflow("workflow2")).andReturn(workflow2);
        expect(workflow2.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status2));

        mockController.replay();

        final CollectionBuilder<ProjectIssueTypeContext> result = CollectionBuilder.<ProjectIssueTypeContext>newBuilder()
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project1.getId()), new IssueTypeContextImpl(issueType3Id)));


        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId(), status2.getId()).asListOrderedSet();
        assertEquals(result.asSet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), false));

        mockController.verify();
    }

    //
    // Test that caching works.
    //
    @Test
    public void testGetClauseContextStatusCaching() throws Exception
    {
        //project1 -> {(null, workflow1[10]), (1, workflow2[11])}
        //project2 -> {(null, workflow2[11]), (1, workflow1[10])}

        final JiraWorkflow workflow1 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow2 = mockController.createMock(JiraWorkflow.class);

        final MockStatus dontMatchStatus = new MockStatus("11", "dontMatchMe");

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1, project2).asList());
        expect(workflowSchemeManager.getWorkflowMap(project1)).andReturn(MapBuilder.<String, String>newBuilder(null, "workflow1").add("1", "workflow2").toMutableMap());
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow1).once();
        expect(workflow1.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1)).once();
        expect(issueTypeSchemeManager.getIssueTypesForProject(project1)).andReturn(Collections.singletonList(issueType1));
        expect(workflowManager.getWorkflow("workflow2")).andReturn(workflow2).once();
        expect(workflow2.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(dontMatchStatus)).once();

        //For the second project we don't expect any calls to managers because the result of the previous calculations should have been cached.
        expect(workflowSchemeManager.getWorkflowMap(project2)).andReturn(MapBuilder.<String, String>newBuilder(null, "workflow2").add("1", "workflow1").toMutableMap());

        mockController.replay();

        final CollectionBuilder<ProjectIssueTypeContext> result = CollectionBuilder.<ProjectIssueTypeContext>newBuilder()
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project1.getId()), new IssueTypeContextImpl(issueType1.getId())))
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project2.getId()), new IssueTypeContextImpl("1")));

        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId(), status2.getId()).asListOrderedSet();
        assertEquals(result.asSet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), true));

        mockController.verify();
    }

    //
    // Test matching "default workflow" returns the correct issue types.
    //
    @Test
    public void testGetClauseContext() throws Exception
    {
        //project1 -> {(null, workflow1[10]), (10, workflow2[11])}

        final JiraWorkflow workflow1 = mockController.createMock(JiraWorkflow.class);
        final JiraWorkflow workflow2 = mockController.createMock(JiraWorkflow.class);

        final MockStatus dontMatchStatus = new MockStatus("11", "dontMatchMe");

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(CollectionBuilder.newBuilder(project1).asList());
        expect(workflowSchemeManager.getWorkflowMap(project1)).andReturn(MapBuilder.<String, String>newBuilder(null, "workflow1").add("10", "workflow2").toMutableMap());
        expect(workflowManager.getWorkflow("workflow1")).andReturn(workflow1);
        expect(workflow1.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(status1));
        expect(issueTypeSchemeManager.getIssueTypesForProject(project1)).andReturn(CollectionBuilder.list(issueType1, issueType2));
        expect(workflowManager.getWorkflow("workflow2")).andReturn(workflow2);
        expect(workflow2.getLinkedStatusObjects()).andReturn(Collections.<Status>singletonList(dontMatchStatus));

        mockController.replay();

        final CollectionBuilder<ProjectIssueTypeContext> result = CollectionBuilder.<ProjectIssueTypeContext>newBuilder()
                .add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(project1.getId()), new IssueTypeContextImpl(issueType2.getId())));

        final Set<String> statusIds = CollectionBuilder.newBuilder(status1.getId()).asListOrderedSet();
        assertEquals(result.asSet(), createForIds(statusIds).getContextFromStatusValues(null, new TerminalClauseImpl("blarg", Operator.EQUALS, "one"), true));

        mockController.verify();
    }

    // Asserts fix for JRA-19026
    @Test
    public void testGetClauseContextStatusContainsNull() throws Exception
    {
        final Set<String> statusIds = CollectionBuilder.newBuilder("15").asListOrderedSet();
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        _testGetClauseContextWithNullStatus(statusIds, Operator.EQUALS, expectedResult);
    }

    @Test
    public void testGetIdsNullLiterals() throws Exception
    {
        final Operand operand = new SingleValueOperand(10L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        expect(jqlOperandResolver.getValues((User) null, operand, clause)).andReturn(null);

        mockController.replay();
        final StatusClauseContextFactory factory = new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager);

        final Set<String> result = factory.getIds(null, clause);
        final Set<String> expectedResult = CollectionBuilder.<String>newBuilder().asListOrderedSet();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetIdsLong() throws Exception
    {
        final Operand operand = new SingleValueOperand(10L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        expect(jqlOperandResolver.getValues((User) null, operand, clause)).andReturn(CollectionBuilder.newBuilder(createLiteral(10L)).asList());
        expect(statusResolver.idExists(10L)).andReturn(true);

        mockController.replay();
        final StatusClauseContextFactory factory = new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager);

        final Set<String> result = factory.getIds(null, clause);
        final Set<String> expectedResult = CollectionBuilder.newBuilder("10").asListOrderedSet();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetIdsString() throws Exception
    {
        Operand operand = new SingleValueOperand("name");
        final TerminalClauseImpl clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        expect(jqlOperandResolver.getValues((User) null, operand, clause)).andReturn(CollectionBuilder.newBuilder(createLiteral("name")).asList());
        expect(statusResolver.getIdsFromName("name")).andReturn(CollectionBuilder.newBuilder("10", "20").asList());

        mockController.replay();
        final StatusClauseContextFactory factory = new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager);

        final Set<String> result = factory.getIds(null, clause);
        final Set<String> expectedResult = CollectionBuilder.newBuilder("10", "20").asListOrderedSet();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetIdsMixed() throws Exception
    {
        Operand operand = new MultiValueOperand(CollectionBuilder.<Operand>newBuilder(new SingleValueOperand("name"), new SingleValueOperand(30L), new SingleValueOperand(10L)).asList());
        final TerminalClauseImpl clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        expect(jqlOperandResolver.getValues((User) null, operand, clause))
                .andReturn(CollectionBuilder.newBuilder(createLiteral("name"), createLiteral(10L), createLiteral(30L)).asList());

        expect(statusResolver.getIdsFromName("name")).andReturn(CollectionBuilder.newBuilder("10", "20").asList());
        expect(statusResolver.idExists(10L)).andReturn(true);
        expect(statusResolver.idExists(30L)).andReturn(true);

        mockController.replay();
        final StatusClauseContextFactory factory = new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager);

        final Set<String> result = factory.getIds(null, clause);
        final Set<String> expectedResult = CollectionBuilder.newBuilder("10", "20", "30").asListOrderedSet();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    private void _testGetClauseContextWithNullStatus(final Set<String> statusIds, final Operator operator, final ClauseContext expectedResult)
    {
        final JiraWorkflow workflow1 = mockController.createMock(JiraWorkflow.class);
        final SingleValueOperand operand = new SingleValueOperand("blah");

        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null))
                .andReturn(CollectionBuilder.newBuilder(project1).asList());

        expect(workflowSchemeManager.getWorkflowMap(project1))
                .andReturn(MapBuilder.newBuilder(issueType1.getId(), "workflow1").toMutableMap());

        expect(workflowManager.getWorkflow("workflow1"))
                .andReturn(workflow1);

        expect(workflow1.getLinkedStatusObjects())
                .andReturn(Collections.<Status>singletonList(null));

        expect(workflow1.getName()).andReturn("Test Workflow");

        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        final StatusClauseContextFactory factory = new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager)
        {
            @Override
            Set<String> getIds(final User searcher, final TerminalClause clause)
            {
                called.set(true);
                return statusIds;
            }
        };

        final ClauseContext result = factory.getClauseContext(null, new TerminalClauseImpl("blah", operator, operand));

        assertTrue(called.get());
        assertEquals(expectedResult, result);

        mockController.verify();
    }
    
    private StatusClauseContextFactory createForIds(final Set<String> ids)
    {
        return new StatusClauseContextFactory(jqlOperandResolver, statusResolver, workflowManager, permissionManager, issueTypeSchemeManager, workflowSchemeManager)
        {
            @Override
            Set<String> getIds(final User searcher, final TerminalClause clause)
            {
                return ids;
            }
        };
    }
}
