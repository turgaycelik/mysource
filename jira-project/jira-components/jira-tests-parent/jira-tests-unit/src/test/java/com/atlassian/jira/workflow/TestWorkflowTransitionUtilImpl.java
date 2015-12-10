package com.atlassian.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.permission.ProjectPermissions.TRANSITION_ISSUES;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestWorkflowTransitionUtilImpl
{
    private ApplicationUser appUser;
    private GenericValue project;
    @Mock @AvailableInContainer
    private PermissionSchemeManager permissionSchemeManager;
    @Mock @AvailableInContainer
    private VersionManager versionManagerMock;
    @Mock @AvailableInContainer
    private JiraAuthenticationContext mockAuthenticationContext;
    @Mock @AvailableInContainer
    private ProjectManager mockProjectManager;
    @Mock @AvailableInContainer
    private PermissionManager mockPermissionManager;
    @Mock @AvailableInContainer
    private WorkflowManager mockWorkflowManager;
    @Mock @AvailableInContainer
    private FieldScreenRendererFactory mockFieldScreenRendererFactory;
    @Mock
    private OrderableField mockOrderableField;
    @Mock
    private CommentService mockCommentService;
    @Mock
    private I18nBean.BeanFactory mockI18nFactory;
    @Mock @AvailableInContainer
    private UserManager mockUserManager;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        //Try using another xml file for the BasicWorkflow so that the entire get stored in the memory database
        appUser = new MockApplicationUser("owen");
        when(mockI18nFactory.getInstance(appUser)).thenReturn(new MockI18nBean());
        setupMockAuthenticationContext();

        // Create a project
        project = new MockGenericValue("Project", FieldMap.build("name", "Project A", "counter", 0L, "key", "ABC"));
    }

    private void setupMockAuthenticationContext()
    {
        // let's allow user to be set in context
        doAnswer(new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                final ApplicationUser newUser = (ApplicationUser) invocation.getArguments()[0];
                when(mockAuthenticationContext.getUser()).thenReturn(newUser);
                //noinspection deprecation
                when(mockAuthenticationContext.getLoggedInUser()).thenReturn(newUser.getDirectoryUser());
                return null;
            }
        }).when(mockAuthenticationContext).setLoggedInUser(Mockito.any(ApplicationUser.class));

        // that would have serious influence on our tests...
        //noinspection deprecation
        doThrow(new UnsupportedOperationException("Not expected to execute this method"))
                .when(mockAuthenticationContext).setLoggedInUser(any(User.class));

        // set default user
        mockAuthenticationContext.setLoggedInUser(appUser);
    }

    @Test
    public void testGetAdditionalInputs() throws Exception
    {
        final WorkflowTransitionUtilImpl wu = createWorkflowTransitionUtil();

        wu.setAction(1);

        final String commentString = "Comment";
        wu.setParams(ImmutableMap.of(
                WorkflowTransitionUtil.FIELD_COMMENT, commentString
        ));

        final Map inputs = wu.getAdditionalInputs();
        assertEquals(commentString, inputs.get(WorkflowTransitionUtil.FIELD_COMMENT));
        Assert.assertNull(inputs.get(WorkflowTransitionUtil.FIELD_COMMENT_LEVEL));
        assertEquals(appUser.getKey(), WorkflowFunctionUtils.getCallerKey(null, inputs));
    }

    @Test
    public void testValidateNoCommentPermission() throws Exception
    {

        final WorkflowTransitionUtilImpl wu = createWorkflowTransitionUtil();

        final MutableIssue mockIssue = mock(MutableIssue.class);
        when(mockIssue.getProjectObject()).thenReturn(new ProjectImpl(project));

        wu.setParams(ImmutableMap.of("comment", "some comment"));
        wu.setIssue(mockIssue);

        final ErrorCollection errorCollection = wu.validate();
        Assert.assertTrue(errorCollection.hasAnyErrors());

        Assert.assertThat(errorCollection.getErrorMessages(), Matchers.hasItem("The user '" + appUser.getName() + "' does not have permission to comment on issues in this project."));
    }

    @Test
    /**
     * JRA-16112 and JRA-16915
     *
     * @throws Exception if stuff goes wrong
     */
    public void testValidateResolutionField() throws Exception
    {
        final MutableIssue mockIssue = mock(MutableIssue.class);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem1 = mock(FieldScreenRenderLayoutItem.class);
        when(mockFieldScreenRenderLayoutItem1.isShow(eq(mockIssue))).thenReturn(Boolean.TRUE);
        when(mockFieldScreenRenderLayoutItem1.getOrderableField()).thenReturn(mockOrderableField);

        final int actionId = 5;
        final ActionDescriptor ad = DescriptorFactory.getFactory().createActionDescriptor();
        ad.setId(actionId);

        when(mockOrderableField.getId()).thenReturn(IssueFieldConstants.RESOLUTION);
        when(mockFieldScreenRenderLayoutItem1.getFieldScreenLayoutItem()).thenReturn(null);
        when(mockFieldScreenRenderLayoutItem1.getFieldLayoutItem()).thenReturn(null);

        final WorkflowDescriptor wd = DescriptorFactory.getFactory().createWorkflowDescriptor();
        wd.addGlobalAction(ad);

        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflow.getDescriptor()).thenReturn(wd);

        when(mockWorkflowManager.getWorkflow(eq(mockIssue))).thenReturn(mockWorkflow);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem2 = mock(FieldScreenRenderLayoutItem.class);
        when(mockFieldScreenRenderLayoutItem2.isShow(eq(mockIssue))).thenReturn(Boolean.FALSE);

        final FieldScreenRenderTab mockFieldScreenRenderTab = mock(FieldScreenRenderTab.class);
        when(mockFieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing()).thenReturn(
                ImmutableList.of(mockFieldScreenRenderLayoutItem1, mockFieldScreenRenderLayoutItem2));

        final FieldScreenRenderer mockFieldScreenRenderer = mock(FieldScreenRenderer.class);
        when(mockFieldScreenRenderer.getFieldScreenRenderTabs()).thenReturn(ImmutableList.of(mockFieldScreenRenderTab));

        grantPermission(appUser, mockIssue, TRANSITION_ISSUES);

        final WorkflowTransitionUtilImpl wu = createWorkflowTransitionUtil(mockFieldScreenRenderer);
        wu.setAction(actionId);
        wu.setIssue(mockIssue);
        final ErrorCollection errorCollection = wu.validate();
        Assert.assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidate() throws Exception
    {
        final MutableIssue mockIssue = mock(MutableIssue.class);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem1 = mock(FieldScreenRenderLayoutItem.class);
        when(mockFieldScreenRenderLayoutItem1.isShow(eq(mockIssue))).thenReturn(Boolean.TRUE);
        when(mockFieldScreenRenderLayoutItem1.getOrderableField()).thenReturn(mockOrderableField);

        final int actionId = 5;
        final ActionDescriptor ad = DescriptorFactory.getFactory().createActionDescriptor();
        ad.setId(actionId);

        when(mockOrderableField.getId()).thenReturn("blah");

        final WorkflowDescriptor wd = DescriptorFactory.getFactory().createWorkflowDescriptor();
        wd.addGlobalAction(ad);

        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflow.getDescriptor()).thenReturn(wd);

        when(mockWorkflowManager.getWorkflow(eq(mockIssue))).thenReturn(mockWorkflow);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem2 = mock(FieldScreenRenderLayoutItem.class);
        when(mockFieldScreenRenderLayoutItem2.isShow(eq(mockIssue))).thenReturn(Boolean.FALSE);

        final FieldScreenRenderTab mockFieldScreenRenderTab = mock(FieldScreenRenderTab.class);
        when(mockFieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing()).thenReturn(ImmutableList.of(mockFieldScreenRenderLayoutItem1, mockFieldScreenRenderLayoutItem2));

        final FieldScreenRenderer mockFieldScreenRenderer = mock(FieldScreenRenderer.class);
        when(mockFieldScreenRenderer.getFieldScreenRenderTabs()).thenReturn(ImmutableList.of(mockFieldScreenRenderTab));

        grantPermission(appUser, mockIssue, TRANSITION_ISSUES);

        final WorkflowTransitionUtilImpl wu = createWorkflowTransitionUtil(mockFieldScreenRenderer);
        wu.setAction(actionId);
        wu.setIssue(mockIssue);

        final ErrorCollection errorCollection = wu.validate();
        Assert.assertThat(errorCollection.getErrorMessages(), Matchers.<String>empty());
        Assert.assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateShouldReturnErrorsWhenUserDoesntHaveTransitionPermission() throws Exception
    {
        final MutableIssue mockIssue = mock(MutableIssue.class);

        final WorkflowTransitionUtilImpl workflowTransitionUtil = createWorkflowTransitionUtil();
        workflowTransitionUtil.setIssue(mockIssue);
        final ErrorCollection errorCollection = workflowTransitionUtil.validate();

        Assert.assertThat(errorCollection.getErrorMessages(), Matchers.hasItem("The user '" + appUser.getKey() + "' does not have permission to perform action on given issue."));
    }

    @Test
    public void testProgress()
    {
        final Map params = new HashMap();
        final int actionId = 1;

        final MutableIssue mockIssue = mock(MutableIssue.class);

        final WorkflowTransitionUtilImpl uitl = prepareTestCase(mockIssue, actionId);

        grantPermission(appUser, mockIssue, TRANSITION_ISSUES);

        uitl.setAction(actionId);
        uitl.setParams(params);
        uitl.setIssue(mockIssue);

        final ErrorCollection errorCollection = uitl.progress();
        Assert.assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testUserParamShouldBeRespectedWhenPerformingProgress()
    {
        final ApplicationUser impersonatedUser = new MockApplicationUser("impersonated");

        final MutableIssue mockIssue = mock(MutableIssue.class);
        final int actionId = 1;

        grantPermission(impersonatedUser, mockIssue, TRANSITION_ISSUES);
        when(mockUserManager.getUserByKey(impersonatedUser.getKey())).thenReturn(impersonatedUser);

        final WorkflowTransitionUtilImpl util = prepareTestCase(mockIssue, actionId);

        // verify user that will be used by workflow manager
        doAnswer(new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                final WorkflowProgressAware from = (WorkflowProgressAware) invocation.getArguments()[0];
                assertEquals(impersonatedUser, from.getRemoteApplicationUser());
                //noinspection deprecation
                assertEquals(impersonatedUser.getDirectoryUser(), from.getRemoteUser());
                return null;
            }
        }).when(mockWorkflowManager).doWorkflowAction(util);

        util.setUserkey(impersonatedUser.getKey());

        final ErrorCollection errorCollection = util.progress();
        Assert.assertFalse(errorCollection.hasAnyErrors());

        assertEquals(appUser, mockAuthenticationContext.getUser());

        Mockito.verify(mockWorkflowManager).doWorkflowAction(util);
    }

    @Test
    public void testUserShouldBeImpersonatedWhenAskingForWorkflow()
    {
        final MutableIssue mockIssue = mock(MutableIssue.class);
        final int actionId = 1;
        final ApplicationUser impersonatedUser = new MockApplicationUser("impersonated");

        // build: workflow -> descriptor -> action
        final WorkflowDescriptor mockWorkflowDescriptor = mock(WorkflowDescriptor.class);
        final ActionDescriptor mockActionDescriptor = mock(ActionDescriptor.class);
        when(mockWorkflowDescriptor.getAction(actionId)).thenReturn(mockActionDescriptor);
        final JiraWorkflow mockJiraWorkflow = mock(JiraWorkflow.class);
        when(mockJiraWorkflow.getDescriptor()).thenReturn(mockWorkflowDescriptor);

        when(mockUserManager.getUserByKey(impersonatedUser.getKey())).thenReturn(impersonatedUser);
        grantPermission(impersonatedUser, mockIssue, TRANSITION_ISSUES);

        final WorkflowTransitionUtilImpl util = createWorkflowTransitionUtil();
        util.setIssue(mockIssue);
        util.setAction(actionId);
        util.setUserkey(impersonatedUser.getKey());

        when(mockWorkflowManager.getWorkflow(mockIssue)).thenAnswer(new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                assertEquals(impersonatedUser, mockAuthenticationContext.getUser());
                return mockJiraWorkflow;
            }
        });

        final ActionDescriptor actionDescriptor = util.getActionDescriptor();

        assertEquals(mockActionDescriptor, actionDescriptor);
        assertEquals(appUser, mockAuthenticationContext.getUser());
    }

    @Test
    public void testUserParamShouldBeRespectedWhenPerformingValidation()
    {
        final ApplicationUser impersonatedUser = new MockApplicationUser("impersonated");

        final MutableIssue mockIssue = mock(MutableIssue.class);
        final int actionId = 1;
        final String commentLevel = "comment level";

        grantPermission(impersonatedUser, mockIssue, TRANSITION_ISSUES);
        grantPermission(impersonatedUser, mockIssue, ProjectPermissions.ADD_COMMENTS);
        when(mockUserManager.getUserByKey(impersonatedUser.getKey())).thenReturn(impersonatedUser);

        final WorkflowTransitionUtilImpl util = prepareTestCase(mockIssue, actionId);
        util.setUserkey(impersonatedUser.getKey());
        util.setParams(ImmutableMap.of(
                WorkflowTransitionUtil.FIELD_COMMENT, "test comment",
                WorkflowTransitionUtil.FIELD_COMMENT_LEVEL, commentLevel
        ));

        // verify impersonation
        final Answer verifyThatUserWasImpersonated = new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                assertEquals(impersonatedUser, mockAuthenticationContext.getUser());
                return null;
            }
        };

        doAnswer(verifyThatUserWasImpersonated).when(mockOrderableField).validateParams(
                any(OperationContext.class), any(ErrorCollection.class), any(I18nHelper.class),
                eq(mockIssue), any(FieldScreenRenderLayoutItem.class)
        );

        // run validation
        final ErrorCollection errorCollection = util.validate();
        Assert.assertFalse(errorCollection.hasAnyErrors());

        final Visibility visibility = Visibilities.groupVisibility(commentLevel);

        // verify that comment was validated against expected user
        verify(mockCommentService).isValidCommentVisibility(eq(impersonatedUser), eq(mockIssue), eq(visibility),
                any(ErrorCollection.class));
        verifyNoMoreInteractions(mockCommentService);

        // verify that user was restored
        assertEquals(appUser, mockAuthenticationContext.getUser());
    }

    private void grantPermission(final ApplicationUser user, final MutableIssue mockIssue, final ProjectPermissionKey permission)
    {
        when(mockPermissionManager.hasPermission(eq(permission), eq(mockIssue), eq(user))).thenReturn(true);
    }

    private WorkflowTransitionUtilImpl prepareTestCase(final MutableIssue mockIssue, final int actionId)
    {
        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem1 = mock(FieldScreenRenderLayoutItem.class);
        when(mockFieldScreenRenderLayoutItem1.isShow(eq(mockIssue))).thenReturn(Boolean.TRUE);
        when(mockFieldScreenRenderLayoutItem1.getOrderableField()).thenReturn(mockOrderableField);
        when(mockFieldScreenRenderLayoutItem1.getFieldLayoutItem()).thenReturn(null);

        final FieldScreenRenderLayoutItem mockFieldScreenRenderLayoutItem2 = mock(FieldScreenRenderLayoutItem.class);
        when(mockFieldScreenRenderLayoutItem2.isShow(eq(mockIssue))).thenReturn(Boolean.FALSE);

        final FieldScreenRenderTab mockFieldScreenRenderTab = mock(FieldScreenRenderTab.class);
        when(mockFieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing()).thenReturn(ImmutableList.of(mockFieldScreenRenderLayoutItem1, mockFieldScreenRenderLayoutItem2));

        final FieldScreenRenderer mockFieldScreenRenderer = mock(FieldScreenRenderer.class);
        when(mockFieldScreenRenderer.getFieldScreenRenderTabs()).thenReturn(ImmutableList.of(mockFieldScreenRenderTab));

        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        final WorkflowDescriptor workflowDescriptor = DescriptorFactory.getFactory().createWorkflowDescriptor();
        final ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor();
        actionDescriptor.setId(actionId);
        actionDescriptor.setView(WorkflowTransitionUtil.VIEW_RESOLVE);
        workflowDescriptor.addGlobalAction(actionDescriptor);
        when(mockWorkflow.getDescriptor()).thenReturn(workflowDescriptor);

        when(mockWorkflowManager.getWorkflow(eq(mockIssue))).thenReturn(mockWorkflow);

        final WorkflowTransitionUtilImpl util = createWorkflowTransitionUtil(mockFieldScreenRenderer);
        util.setAction(actionId);
        util.setIssue(mockIssue);
        return util;
    }

    private WorkflowTransitionUtilImpl createWorkflowTransitionUtil(final FieldScreenRenderer mockFieldScreenRenderer)
    {
        return new WorkflowTransitionUtilImpl(mockAuthenticationContext, mockWorkflowManager,
                mockPermissionManager, mockFieldScreenRendererFactory, mockCommentService, mockI18nFactory)
        {
            @Override
            public FieldScreenRenderer getFieldScreenRenderer()
            {
                return mockFieldScreenRenderer;
            }
        };
    }

    private WorkflowTransitionUtilImpl createWorkflowTransitionUtil()
    {
        return new WorkflowTransitionUtilImpl(mockAuthenticationContext, mockWorkflowManager, mockPermissionManager,
                mockFieldScreenRendererFactory, mockCommentService, mockI18nFactory);
    }
}
