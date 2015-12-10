package com.atlassian.jira.jql.context;

import com.atlassian.jira.jql.operand.MultiValueOperandHandler;
import com.atlassian.jira.jql.operand.SingleValueOperandHandler;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.MockVersionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests VersionClauseContextFactory without deprecated MockControllerTestCase
 *
 * @since v5.2
 */
public class TestVersionClauseContextFactory2
{
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Mock @AvailableInContainer
    private UserKeyService userKeyService;
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;

    @Before
    public void setUp() throws Exception
    {
        when(userKeyService.getKeyForUsername("fred")).thenReturn("fred");
    }

    @Test
    public void test_getClauseContext_forInOperator() throws Exception
    {
        final MockJqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        final MockVersionManager mockVersionManager = new MockVersionManager();
        Project projectAbc = new MockProject(12, "ABC");
        mockVersionManager.add(new MockVersion(10, "1.0", projectAbc));
        mockVersionManager.add(new MockVersion(20, "2.0", projectAbc));
        mockVersionManager.add(new MockVersion(21, "2.0", new MockProject(51, "DEF")));
        mockVersionManager.add(new MockVersion(22, "3.0", new MockProject(61, "XYZ")));

        VersionClauseContextFactory versionClauseContextFactory = new VersionClauseContextFactory(jqlOperandResolver, new VersionResolver(mockVersionManager), new MockPermissionManager(true));

        ClauseContext clauseContext = versionClauseContextFactory.getClauseContext(new MockUser("fred"), new TerminalClauseImpl("fixVersion", Operator.IN, new MultiValueOperand("1.0", "2.0")));

        assertFalse(clauseContext.containsGlobalContext());
        assertEquals(2, clauseContext.getContexts().size());
        assertTrue(clauseContext.getContexts().contains(new ProjectIssueTypeContextImpl(new ProjectContextImpl(12L), AllIssueTypesContext.INSTANCE)));
        assertTrue(clauseContext.getContexts().contains(new ProjectIssueTypeContextImpl(new ProjectContextImpl(51L), AllIssueTypesContext.INSTANCE)));
    }

    @Test
    public void test_getClauseContext_forInOperatorWithEmpty() throws Exception
    {
        final MockJqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        final MockVersionManager mockVersionManager = new MockVersionManager();
        Project projectAbc = new MockProject(12, "ABC");
        mockVersionManager.add(new MockVersion(10, "1.0", projectAbc));
        mockVersionManager.add(new MockVersion(20, "2.0", projectAbc));
        mockVersionManager.add(new MockVersion(21, "2.0", new MockProject(51, "DEF")));
        mockVersionManager.add(new MockVersion(22, "3.0", new MockProject(61, "XYZ")));

        VersionClauseContextFactory versionClauseContextFactory = new VersionClauseContextFactory(jqlOperandResolver, new VersionResolver(mockVersionManager), new MockPermissionManager(true));

        ClauseContext clauseContext = versionClauseContextFactory.getClauseContext(new MockUser("fred"), new TerminalClauseImpl("fixVersion", Operator.IN, new MultiValueOperand(EmptyOperand.EMPTY, new SingleValueOperand("1.0"), new SingleValueOperand("2.0"))));

        assertTrue(clauseContext.containsGlobalContext());
        assertEquals(3, clauseContext.getContexts().size());
        assertTrue(clauseContext.getContexts().contains(ProjectIssueTypeContextImpl.createGlobalContext()));
        assertTrue(clauseContext.getContexts().contains(new ProjectIssueTypeContextImpl(new ProjectContextImpl(12L), AllIssueTypesContext.INSTANCE)));
        assertTrue(clauseContext.getContexts().contains(new ProjectIssueTypeContextImpl(new ProjectContextImpl(51L), AllIssueTypesContext.INSTANCE)));
    }

    @Test
    public void test_getClauseContext_forNotInOperator() throws Exception
    {
        final MockJqlOperandResolver jqlOperandResolver = new MockJqlOperandResolver();
        jqlOperandResolver.addHandler("MultiValueOperand", new MultiValueOperandHandler(jqlOperandResolver));
        jqlOperandResolver.addHandler("SingleValueOperand", new SingleValueOperandHandler());

        final MockVersionManager mockVersionManager = new MockVersionManager();
        Project projectAbc = new MockProject(12, "ABC");
        Project projectDef = new MockProject(51, "DEF");
        mockVersionManager.add(new MockVersion(10, "1.0", projectAbc));
        mockVersionManager.add(new MockVersion(20, "2.0", projectAbc));
        mockVersionManager.add(new MockVersion(21, "2.0", projectDef));
        mockVersionManager.add(new MockVersion(10021, "2.1", projectDef));
        mockVersionManager.add(new MockVersion(22, "3.0", new MockProject(61, "XYZ")));

        VersionClauseContextFactory versionClauseContextFactory = new VersionClauseContextFactory(jqlOperandResolver, new VersionResolver(mockVersionManager), new MockPermissionManager(true));

        ClauseContext clauseContext = versionClauseContextFactory.getClauseContext(new MockUser("fred"), new TerminalClauseImpl("fixVersion", Operator.NOT_IN, new MultiValueOperand("1.0", "2.0")));

        // We expect the context not to include Project ABC because we have exluded all of it's versions.
        // Project DEF will be included because version "2.1" is not excluded.
        assertFalse(clauseContext.containsGlobalContext());
        assertEquals(2, clauseContext.getContexts().size());
        assertTrue(clauseContext.getContexts().contains(new ProjectIssueTypeContextImpl(new ProjectContextImpl(51L), AllIssueTypesContext.INSTANCE)));
        assertTrue(clauseContext.getContexts().contains(new ProjectIssueTypeContextImpl(new ProjectContextImpl(61L), AllIssueTypesContext.INSTANCE)));
    }
}
