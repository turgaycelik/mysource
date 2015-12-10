package com.atlassian.jira.bc.issue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.events.WorkflowManualTransitionExecutionEvent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.issue.changehistory.DefaultChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUserLocaleStore;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.TransitionOptions;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowProgressAware;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.mock.Strict.strict;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the issue service.
 *
 * @since v4.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultIssueService
{
    private final ApplicationUser user = new MockApplicationUser("ID123", "fred", "Fred Flinstone", "fred@example.com");
    private EventPublisher eventPublisher = mock(EventPublisher.class, strict());
    private WorkflowManager workflowManager = mock(WorkflowManager.class, strict());
    private JiraWorkflow jiraWorkflow = mock(JiraWorkflow.class, strict());
    private ActionDescriptor actionDescriptor = mock(ActionDescriptor.class, strict());
    private WorkflowDescriptor workflowDescriptor = mock(WorkflowDescriptor.class, strict());
    private FieldScreenRendererFactory fieldScreenRendererFactory = mock(FieldScreenRendererFactory.class, strict());
    private IssueWorkflowManager issueWorkflowManager = mock(IssueWorkflowManager.class, strict());
    private MutableIssue mutableIssue = mock(MutableIssue.class, strict());
    private PermissionManager permissionManager = mock(PermissionManager.class, strict());
    private IssueFactory issueFactory = mock(IssueFactory.class, strict());
    private IssueManager issueManager = mock(IssueManager.class, strict());
    private FieldScreenRenderer fieldScreenRenderer = mock(FieldScreenRenderer.class, strict());
    private IssueCreationHelperBean issueCreationHelperBean = mock(IssueCreationHelperBean.class, strict());
    private FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem = Mockito.mock(FieldScreenRenderLayoutItem.class, strict());
    private OrderableField orderableField = Mockito.mock(OrderableField.class, strict());
    private FieldScreenRenderTab fieldScreenRenderTab = Mockito.mock(FieldScreenRenderTab.class, strict());
    private FieldManager fieldManager = Mockito.mock(FieldManager.class, strict());
    private FieldLayoutItem fieldLayoutItem = Mockito.mock(FieldLayoutItem.class, strict());

    @Mock
    private InstrumentRegistry instrumentRegistry;

    @Mock
    private Counter counter;

    @Before
    public void setUp() throws Exception
    {
        final MockComponentWorker worker = new MockComponentWorker()
                .addMock(UserLocaleStore.class, new MockUserLocaleStore())
                .addMock(I18nHelper.BeanFactory.class, new MockI18nBean.MockI18nBeanFactory())
                .init();

        // instrumentation api
        when(instrumentRegistry.pullCounter(any(String.class))).thenReturn(counter);
        worker.addMock(InstrumentRegistry.class, instrumentRegistry);
        ComponentAccessor.initialiseWorker(worker);
    }

    @Test
    public void testGetIssueById() throws Exception
    {
        doReturn(mutableIssue).when(issueManager).getIssueObject(12L);
        final AtomicBoolean getIssueCalled = new AtomicBoolean(false);
        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            MutableIssue getIssue(final User user, final MutableIssue issue, final I18nHelper i18n, final ErrorCollection errorCollection)
            {
                getIssueCalled.set(true);
                return mutableIssue;
            }
        };

        final IssueService.IssueResult issueResult = defaultIssueService.getIssue(null, new Long(12));

        assertTrue(issueResult.isValid());
        assertTrue(getIssueCalled.get());
        verify(issueManager).getIssueObject(12L);
    }

    @Test
    public void testGetIssueByKey() throws Exception
    {
        doReturn(mutableIssue).when(issueManager).getIssueObject("TST-1");
        final AtomicBoolean getIssueCalled = new AtomicBoolean(false);

        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            MutableIssue getIssue(final User user, final MutableIssue issue, final I18nHelper i18n, final ErrorCollection errorCollection)
            {
                getIssueCalled.set(true);
                return mutableIssue;
            }
        };

        final IssueService.IssueResult issueResult = defaultIssueService.getIssue(null, "TST-1");

        assertTrue(issueResult.isValid());
        assertTrue(getIssueCalled.get());
        verify(issueManager).getIssueObject("TST-1");
    }

    @Test
    public void testGetIssueNullIssue() throws Exception
    {
        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        assertNull(defaultIssueService.getIssue(null, null, new MockI18nBean(), errorCollection));

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("The issue no longer exists.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetIssueNoPermission() throws Exception
    {
        final MockIssue issue = new MockIssue();
        doReturn(false).when(permissionManager).hasPermission(Permissions.BROWSE, issue, (User) null);
        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, null, permissionManager, null, null, null, null, null, eventPublisher);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        assertNull(defaultIssueService.getIssue(null, issue, new MockI18nBean(), errorCollection));

        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You do not have the permission to see the specified issue.", errorCollection.getErrorMessages().iterator().next());
        verify(permissionManager).hasPermission(Permissions.BROWSE, issue, (User) null);
    }

    @Test
    public void testGetIssueHappy() throws Exception
    {
        final MockIssue mockIssue = new MockIssue();
        doReturn(true).when(permissionManager).hasPermission(Permissions.BROWSE, mockIssue, (User) null);

        DefaultIssueService defaultIssueService = new DefaultIssueService(null, null, null, null, permissionManager, null, null, null, null, null, eventPublisher);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final Issue issue = defaultIssueService.getIssue(null, mockIssue, new MockI18nBean(), null);

        assertEquals(mockIssue, issue);
        assertFalse(errorCollection.hasAnyErrors());
        verify(permissionManager).hasPermission(Permissions.BROWSE, mockIssue, (User) null);
    }

    @Test
    public void testValidateCreateDefaultProvidedFields()
    {
        final AtomicBoolean validateCreateCalled = new AtomicBoolean(true);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected CreateValidationResult validateCreate(User user, MutableIssue issue, IssueInputParameters issueInputParameters)
            {
                assertNull(issueInputParameters.getProvidedFields());
                validateCreateCalled.set(true);
                return new CreateValidationResult(issue, new SimpleErrorCollection(), new HashMap<String, Object>());
            }

            @Override
            MutableIssue constructNewIssue()
            {
                return mutableIssue;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueService.validateCreate(null, issueInputParameters);

        assertTrue(validateCreateCalled.get());
    }

    @Test
    public void testValidateSubTaskCreate()
    {
        doNothing().when(mutableIssue).setParentId(111L);

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final AtomicBoolean validateCreateCalled = new AtomicBoolean(true);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected CreateValidationResult validateCreate(User user, MutableIssue issue, IssueInputParameters issueInputParameters)
            {
                validateCreateCalled.set(true);
                return new CreateValidationResult(issue, new SimpleErrorCollection(), issueInputParameters.getFieldValuesHolder());
            }

            @Override
            MutableIssue constructNewIssue()
            {
                return mutableIssue;
            }
        };

        issueService.validateSubTaskCreate(null, 111L, issueInputParameters);

        assertTrue(validateCreateCalled.get());
        verify(mutableIssue).setParentId(111L);
    }

    @Test
    public void testValidateSubTaskCreateWithProvidedFields()
    {
        doNothing().when(mutableIssue).setParentId(111L);
        final Collection<String> providedFields = Collections.singleton("test");

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setProvidedFields(providedFields);

        final AtomicBoolean validateCreateCalled = new AtomicBoolean(true);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected CreateValidationResult validateCreate(User user, MutableIssue issue, IssueInputParameters issueInputParameters)
            {
                assertEquals(providedFields, issueInputParameters.getProvidedFields());
                validateCreateCalled.set(true);
                return new CreateValidationResult(issue, new SimpleErrorCollection(), issueInputParameters.getFieldValuesHolder());
            }

            @Override
            MutableIssue constructNewIssue()
            {
                return mutableIssue;
            }
        };

        issueService.validateSubTaskCreate(null, new Long(111), issueInputParameters);

        assertTrue(validateCreateCalled.get());
        verify(mutableIssue).setParentId(111L);
    }

    @Test
    public void testCreate() throws CreateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        mockIssue.setProjectObject(new MockProject(12L, "TST"));
        final MockGenericValue issueGV = new MockGenericValue("Issue");
        Map<String, Object> fields = MapBuilder.<String, Object>newBuilder().add("issue", mockIssue).add(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, mockIssue).add("pkey", "TST").toMap();

        doReturn(mockIssue).when(issueFactory).getIssue(issueGV);
        doReturn(mockIssue).when(issueManager).getIssueObject(123L);
        doReturn(issueGV).when(issueManager).createIssue((User) null, fields);

        DefaultIssueService issueService = new DefaultIssueService(issueFactory, null, null, issueManager, null, null, null, null, null, null, eventPublisher);

        issueService.create(null, new IssueService.CreateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()));
        verify(issueFactory).getIssue(issueGV);
        verify(issueManager).getIssueObject(123L);
        verify(issueManager).createIssue((User) null, fields);
    }

    @Test
    public void testCreateWithAuxSubmit() throws CreateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        mockIssue.setProjectObject(new MockProject(12L, "TST"));
        final MockGenericValue issueGV = new MockGenericValue("Issue");
        Map fields = ImmutableMap.of("issue", mockIssue, WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, mockIssue, "pkey", "TST", "submitbutton", "AuxSubmitName");

        doReturn(mockIssue).when(issueFactory).getIssue(issueGV);
        doReturn(mockIssue).when(issueManager).getIssueObject(123L);
        doReturn(issueGV).when(issueManager).createIssue((User) null, fields);


        DefaultIssueService issueService = new DefaultIssueService(issueFactory, null, null, issueManager, null, null, null, null, null, null, eventPublisher);
        issueService.create(null, new IssueService.CreateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), "AuxSubmitName");

        verify(issueFactory).getIssue(issueGV);
        verify(issueManager).getIssueObject(123L);
        verify(issueManager).createIssue((User) null, fields);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testCreateWithNullResult() throws CreateException
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.create(null, null, "AuxSubmitName");
    }

    @Test (expected = IllegalArgumentException.class)
    public void testCreateWithNullIssueInResult() throws CreateException
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.create(null, new IssueService.CreateValidationResult(null, new SimpleErrorCollection(), new HashMap<String, Object>()), "AuxSubmitName");
    }

    @Test (expected = IllegalStateException.class)
    public void testCreateWithInvalidResult() throws CreateException
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("blah");
        issueService.create(null, new IssueService.CreateValidationResult(new MockIssue(), errorCollection, new HashMap<String, Object>()), "AuxSubmitName");
    }

    @Test
    public void testCreateWithAuxSubmitCreateExceptionThrown() throws CreateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        mockIssue.setProjectObject(new MockProject(12L, "TST"));
        Map<String, Object> fields = MapBuilder.<String, Object>newBuilder().add("issue", mockIssue).add(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, mockIssue).add("pkey", "TST").add("submitbutton", "AuxSubmitName").toMap();

        doReturn(mockIssue).when(issueManager).getIssueObject(123L);
        doThrow(new CreateException("Problem with create")).when(issueManager).createIssue((User) null, fields);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher);

        final IssueService.IssueResult issueResult = issueService.create(null, new IssueService.CreateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), "AuxSubmitName");
        assertFalse(issueResult.isValid());
        assertNull(issueResult.getIssue());
        assertEquals("Error creating issue: Problem with create", issueResult.getErrorCollection().getErrorMessages().iterator().next());

        verify(issueManager).getIssueObject(123L);
        verify(issueManager).createIssue((User) null, fields);
    }

    @Test
    public void testValidateUpdate()
    {
        MockIssue mockIssue = new MockIssue(123L);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        final AtomicBoolean getRendererCalled = new AtomicBoolean(false);

        doReturn(mockIssue).when(issueManager).getIssueObject(123L);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, boolean updateComment, Integer workflowActionId)
            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }

            @Override
            FieldScreenRenderer getUpdateFieldScreenRenderer(final User user, final Issue issue)
            {
                getRendererCalled.set(true);
                return null;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, 123L, issueInputParameters);
        assertEquals(mockIssue, issueValidationResult.getIssue());

        assertTrue(issueValidationResult.isValid());
        assertTrue(hasPermCalled.get());
        assertTrue(copyCalled.get());
        assertTrue(valAndUpCalled.get());
        assertTrue(getRendererCalled.get());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateUpdateErrorInUpdateValidate()
    {
        MockIssue mockIssue = new MockIssue(123L);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        final AtomicBoolean getRendererCalled = new AtomicBoolean(false);

        doReturn(mockIssue).when(issueManager).getIssueObject(123L);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment, Integer workflowActionId)
            {
                errorCollection.addErrorMessage("I am an error");
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }

            @Override
            FieldScreenRenderer getUpdateFieldScreenRenderer(final User user, final Issue issue)
            {
                getRendererCalled.set(true);
                return null;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, 123L, issueInputParameters);
        assertNull(issueValidationResult.getIssue());

        assertFalse(issueValidationResult.isValid());
        assertTrue(hasPermCalled.get());
        assertTrue(copyCalled.get());
        assertTrue(valAndUpCalled.get());
        assertTrue(getRendererCalled.get());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateUpdateNoPerm()
    {
        MockIssue mockIssue = new MockIssue(123L);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);

        doReturn(mockIssue).when(issueManager).getIssueObject(123L);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                errors.addErrorMessage("Perm error");
                hasPermCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment, Integer workflowActionId)
            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, 123L, issueInputParameters);
        assertNull("No perm to update should return null", issueValidationResult.getIssue());

        assertFalse(issueValidationResult.isValid());
        assertTrue(hasPermCalled.get());
        assertFalse(copyCalled.get());
        assertFalse(valAndUpCalled.get());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateUpdateNullIssueInputParameters()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment, Integer workflowActionId)

            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }
        };

        try
        {
            issueService.validateUpdate(null, 123L, null);
            fail("should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        assertFalse(hasPermCalled.get());
        assertFalse(copyCalled.get());
        assertFalse(valAndUpCalled.get());
    }

    @Test
    public void testValidateUpdateIncludesHistoryMetadata() throws Exception
    {
        // having
        final IssueManager issueManager = mock(IssueManager.class);
        final DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher);
        final MockIssue mockIssue = new MockIssue(123L);
        final HistoryMetadata metadata = HistoryMetadata.builder("test").build();
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters().setHistoryMetadata(metadata);

        // when
        doReturn(mockIssue).when(issueManager).getIssueObject(mockIssue.getId());
        final IssueService.UpdateValidationResult result = issueService.validateUpdate(user.getDirectoryUser(), mockIssue.getId(), issueInputParameters);

        // then
        assertThat(result.getHistoryMetadata(), sameInstance(metadata));
        verify(issueManager).getIssueObject(mockIssue.getId());
    }

    @Test
    public void testValidateUpdateNullIssueId()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, boolean updateComment, Integer workflowActionId)

            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, null, issueInputParameters);

        assertFalse(issueValidationResult.isValid());
        assertEquals("You can not update a null issue.", issueValidationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertFalse(hasPermCalled.get());
        assertFalse(copyCalled.get());
        assertFalse(valAndUpCalled.get());
    }

    @Test
    public void testValidateUpdateIssueIdDoesNotResolve()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean valAndUpCalled = new AtomicBoolean(false);
        final AtomicBoolean copyCalled = new AtomicBoolean(false);

        doReturn(null).when(issueManager).getIssueObject(123L);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToEdit(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment, Integer workflowActionId)

            {
                valAndUpCalled.set(true);
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                copyCalled.set(true);
                return issue;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateUpdate(null, 123L, issueInputParameters);

        assertFalse(issueValidationResult.isValid());
        assertEquals("You can not update a null issue.", issueValidationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertFalse(hasPermCalled.get());
        assertFalse(copyCalled.get());
        assertFalse(valAndUpCalled.get());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testUpdate() throws UpdateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;
        final HistoryMetadata metadata = HistoryMetadata.builder("test").build();

        doReturn(mockIssue).when(issueManager).updateIssue(null, mockIssue, UpdateIssueRequest.builder()
                .eventDispatchOption(eventDispatchOption)
                .historyMetadata(metadata)
                .sendMail(true)
                .build());

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher);

        final IssueService.UpdateValidationResult issueValidationResult = new IssueService.UpdateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>(), metadata);
        final IssueService.IssueResult result = issueService.update(null, issueValidationResult, eventDispatchOption, true);

        assertTrue(result.isValid());
        assertEquals(mockIssue, result.getIssue());

        verify(issueManager).updateIssue(null, mockIssue, UpdateIssueRequest.builder()
                .eventDispatchOption(eventDispatchOption)
                .historyMetadata(metadata)
                .sendMail(true)
                .build());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testUpdateNullEventDispatch() throws UpdateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = null;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.update(null, new IssueService.UpdateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), eventDispatchOption, true);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testUpdateNullValidationResult() throws UpdateException
    {
        EventDispatchOption eventDispatchOption = null;
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.update(null, null, eventDispatchOption, true);
    }

    @Test (expected = IllegalStateException.class)
    public void testUpdateInvalidValidationResult() throws UpdateException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = null;
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("blah");
        issueService.update(null, new IssueService.UpdateValidationResult(mockIssue, errorCollection, new HashMap<String, Object>()), eventDispatchOption, true);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testUpdateNullIssue() throws UpdateException
    {
        MockIssue mockIssue = null;
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);

        issueService.update(null, new IssueService.UpdateValidationResult(mockIssue, new SimpleErrorCollection(), new HashMap<String, Object>()), eventDispatchOption, true);
    }

    @Test
    public void testUpdateWithDefaultEventDispatchOption() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            public IssueResult update(final User user, final UpdateValidationResult issueValidationResult, final EventDispatchOption eventDispatchOption, final boolean sendMail)
            {
                assertEquals(EventDispatchOption.ISSUE_UPDATED, eventDispatchOption);
                return null;
            }
        };

        issueService.update(null, null);
    }

    @Test
    public void testValidateDelete()
    {
        final MockIssue mockIssue = new MockIssue(123L);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        doReturn(mockIssue).when(issueManager).getIssueObject(123L);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToDelete(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }
        };

        final IssueService.IssueValidationResult issueValidationResult = issueService.validateDelete(null, 123L);
        assertTrue(issueValidationResult.isValid());
        assertTrue(hasPermCalled.get());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateDeleteNoPermission()
    {
        final MockIssue mockIssue = new MockIssue(123L);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        doReturn(mockIssue).when(issueManager).getIssueObject(123L);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToDelete(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }
        };

        issueService.validateDelete(null, new Long(123));
        assertTrue(hasPermCalled.get());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateDeleteNullIssueId()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToDelete(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }
        };

        final IssueService.DeleteValidationResult validationResult = issueService.validateDelete(null, null);
        assertFalse(validationResult.isValid());
        assertEquals("You can not delete a null issue.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertFalse(hasPermCalled.get());
    }

    @Test
    public void testValidateDeleteNoIssueForId()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        doReturn(null).when(issueManager).getIssueObject(123L);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean hasPermissionToDelete(final User user, final Issue issue, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return false;
            }
        };

        final IssueService.DeleteValidationResult validationResult = issueService.validateDelete(null, 123L);
        assertFalse(validationResult.isValid());
        assertEquals("You can not delete a null issue.", validationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertFalse(hasPermCalled.get());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testDelete() throws RemoveException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        doNothing().when(issueManager).deleteIssue((User) null, mockIssue, eventDispatchOption, true);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, errorCollection), eventDispatchOption, true);

        assertFalse(errorCollection.hasAnyErrors());
        verify(issueManager).deleteIssue((User) null, mockIssue, eventDispatchOption, true);
    }

    @Test
    public void testDeleteWithRemoveException() throws RemoveException
    {
        MockIssue mockIssue = new MockIssue(123L);
        mockIssue.setKey("TST-1");
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;

        doThrow(new RemoveException("I can't remove the issue")).when(issueManager).deleteIssue((User) null, mockIssue, eventDispatchOption, true);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final ErrorCollection results = issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, errorCollection), eventDispatchOption, true);
        assertTrue(results.hasAnyErrors());
        assertEquals("There was a system error trying to delete the issue 'TST-1'.", results.getErrorMessages().iterator().next());

        verify(issueManager).deleteIssue((User) null, mockIssue, eventDispatchOption, true);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testDeleteNullEventDispatch() throws RemoveException
    {
        MockIssue mockIssue = new MockIssue(123L);
        EventDispatchOption eventDispatchOption = null;

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, new SimpleErrorCollection()), eventDispatchOption, true);
    }

    @Test
    public void testDeleteNullIssue() throws RemoveException
    {
        try
        {
            MockIssue mockIssue = null;
            EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;
            DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
            issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, new SimpleErrorCollection()), eventDispatchOption, true);
        }
        catch (IllegalArgumentException e)
        {
            //expected
            assertEquals("You can not delete a null issue.", e.getMessage());
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void testDeleteNullValidationResult() throws RemoveException
    {
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.delete(null, null, eventDispatchOption, true);
    }

    @Test (expected = IllegalStateException.class)
    public void testDeleteInvalidValidationResult() throws RemoveException
    {
        MockIssue mockIssue = null;
        EventDispatchOption eventDispatchOption = EventDispatchOption.DO_NOT_DISPATCH;
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("blah");
        issueService.delete(null, new IssueService.DeleteValidationResult(mockIssue, errorCollection), eventDispatchOption, true);
    }

    @Test
    public void testDeleteWithDefaultEventDispatchOption() throws RemoveException
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            public ErrorCollection delete(final User user, final DeleteValidationResult issueValidationResult, final EventDispatchOption eventDispatchOption, final boolean sendMail)
            {
                assertEquals(EventDispatchOption.ISSUE_DELETED, eventDispatchOption);
                return null;
            }
        };

        issueService.delete(null, null);
    }

    @Test
    public void testValidateAndUpdateIssueFromFieldsBadParams()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, errorCollection);
        I18nHelper i18n = new MockI18nBean();

        final AtomicBoolean valAndPopCalled = new AtomicBoolean(false);
        final AtomicBoolean updateCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected void validateAndPopulateParams(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final OperationContext operationContext, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer)
            {
                errorCollection.addErrorMessage("Bad params");
                valAndPopCalled.set(true);
            }

            @Override
            protected void updateIssueFromFields(final FieldScreenRenderer fieldScreenRenderer, final MutableIssue issue, final User user, final Map<String, Object> fieldValuesHolder, final boolean updateComment, IssueInputParameters issueInputParameters)
            {
                updateCalled.set(true);
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        assertNull(issueService.validateAndUpdateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18n, null, false, null));

        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertTrue(valAndPopCalled.get());
        assertFalse(updateCalled.get());
    }

    @Test
    public void testValidateAndUpdateIssueFromFields()
    {
        MockIssue mockIssue = new MockIssue(123L);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, errorCollection);
        I18nHelper i18n = new MockI18nBean();

        final AtomicBoolean valAndPopCalled = new AtomicBoolean(false);
        final AtomicBoolean checkAttachCalled = new AtomicBoolean(false);
        final AtomicBoolean updateCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected void validateAndPopulateParams(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final OperationContext operationContext, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer)

            {
                valAndPopCalled.set(true);
            }

            @Override
            protected void updateIssueFromFields(final FieldScreenRenderer fieldScreenRenderer, final MutableIssue issue, final User user, final Map<String, Object> fieldValuesHolder, final boolean updateComment, IssueInputParameters issueInputParameters)
            {
                updateCalled.set(true);
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        assertEquals(mockIssue, issueService.validateAndUpdateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18n, null, false, null));

        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals(0, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertTrue(valAndPopCalled.get());
        assertTrue(updateCalled.get());
        assertFalse(checkAttachCalled.get());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testValidateRealCreateNullIssueInputParameters()
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateCreate(null, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testValidateRealCreateNullIssue()
    {
        MockIssue mockIssue = null;
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateCreate(null, mockIssue, new IssueInputParametersImpl());
    }

    @Test
    public void testValidateRealCreateLicenseDoesNotAllowCreate()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean licenseInvalidCalled = new AtomicBoolean(false);
        final AtomicBoolean validateProjectCalled = new AtomicBoolean(false);
        final AtomicBoolean validateIssueTypeCalled = new AtomicBoolean(false);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateFromFieldsCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean licenseInvalidForIssueCreation(final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                licenseInvalidCalled.set(true);
                errorCollection.addErrorMessage("License error");
                return true;
            }

            @Override
            protected MutableIssue validateAndSetProject(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateProjectCalled.set(true);
                return issue;
            }

            @Override
            protected MutableIssue validateAndSetIssueType(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateIssueTypeCalled.set(true);
                return issue;
            }

            @Override
            protected boolean hasPermissionToCreate(final User user, final Project project, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndCreateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateFromFieldsCalled.set(true);
                return issue;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        final IssueService.IssueValidationResult result = issueService.validateCreate(null, mockIssue, issueInputParameters);
        assertNull(result.getIssue());

        assertFalse(result.isValid());
        assertEquals(1, result.getErrorCollection().getErrorMessages().size());
        assertTrue(licenseInvalidCalled.get());
        assertFalse(validateProjectCalled.get());
        assertFalse(validateIssueTypeCalled.get());
        assertFalse(hasPermCalled.get());
        assertFalse(validateFromFieldsCalled.get());
    }

    @Test
    public void testValidateRealCreateProjectIssueTypeValidationFails()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean licenseInvalidCalled = new AtomicBoolean(false);
        final AtomicBoolean validateProjectCalled = new AtomicBoolean(false);
        final AtomicBoolean validateIssueTypeCalled = new AtomicBoolean(false);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateFromFieldsCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean licenseInvalidForIssueCreation(final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                licenseInvalidCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndSetProject(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateProjectCalled.set(true);
                errorCollection.addErrorMessage("Project error");
                return issue;
            }

            @Override
            protected MutableIssue validateAndSetIssueType(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateIssueTypeCalled.set(true);
                errorCollection.addErrorMessage("Issue Type error");
                return issue;
            }

            @Override
            protected boolean hasPermissionToCreate(final User user, final Project project, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndCreateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateFromFieldsCalled.set(true);
                return issue;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult result = issueService.validateCreate(null, mockIssue, issueInputParameters);
        assertNull(result.getIssue());

        assertFalse(result.isValid());
        assertEquals(2, result.getErrorCollection().getErrorMessages().size());
        assertTrue(licenseInvalidCalled.get());
        assertTrue(validateProjectCalled.get());
        assertTrue(validateIssueTypeCalled.get());
        assertFalse(hasPermCalled.get());
        assertFalse(validateFromFieldsCalled.get());
    }

    @Test
    public void testValidateRealCreateNoPermission()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean licenseInvalidCalled = new AtomicBoolean(false);
        final AtomicBoolean validateProjectCalled = new AtomicBoolean(false);
        final AtomicBoolean validateIssueTypeCalled = new AtomicBoolean(false);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateFromFieldsCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean licenseInvalidForIssueCreation(final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                licenseInvalidCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndSetProject(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateProjectCalled.set(true);
                return issue;
            }

            @Override
            protected MutableIssue validateAndSetIssueType(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateIssueTypeCalled.set(true);
                return issue;
            }

            @Override
            protected boolean hasPermissionToCreate(final User user, final Project project, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                errors.addErrorMessage("no permission");
                return false;
            }

            @Override
            protected MutableIssue validateAndCreateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateFromFieldsCalled.set(true);
                return issue;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        final IssueService.IssueValidationResult result = issueService.validateCreate(null, mockIssue, issueInputParameters);
        assertNull(result.getIssue());

        assertFalse(result.isValid());
        assertEquals(1, result.getErrorCollection().getErrorMessages().size());
        assertTrue(licenseInvalidCalled.get());
        assertTrue(validateProjectCalled.get());
        assertTrue(validateIssueTypeCalled.get());
        assertTrue(hasPermCalled.get());
        assertFalse(validateFromFieldsCalled.get());
    }

    @Test
    public void testValidateRealCreateHappyPath()
    {
        MockIssue mockIssue = new MockIssue(123L);

        final AtomicBoolean licenseInvalidCalled = new AtomicBoolean(false);
        final AtomicBoolean validateProjectCalled = new AtomicBoolean(false);
        final AtomicBoolean validateIssueTypeCalled = new AtomicBoolean(false);
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateFromFieldsCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            protected boolean licenseInvalidForIssueCreation(final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                licenseInvalidCalled.set(true);
                return false;
            }

            @Override
            protected MutableIssue validateAndSetProject(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateProjectCalled.set(true);
                return issue;
            }

            @Override
            protected MutableIssue validateAndSetIssueType(final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateIssueTypeCalled.set(true);
                return issue;
            }

            @Override
            protected boolean hasPermissionToCreate(final User user, final Project project, final I18nHelper i18n, final ErrorCollection errors)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected MutableIssue validateAndCreateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n)
            {
                validateFromFieldsCalled.set(true);
                return issue;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.IssueValidationResult result = issueService.validateCreate(null, mockIssue, issueInputParameters);
        assertEquals(mockIssue, result.getIssue());

        assertTrue(result.isValid());
        assertTrue(licenseInvalidCalled.get());
        assertTrue(validateProjectCalled.get());
        assertTrue(validateIssueTypeCalled.get());
        assertTrue(hasPermCalled.get());
        assertTrue(validateFromFieldsCalled.get());
    }

    @Test
    public void testValidateAndCreateIssueFromFieldsDefaultFields()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, errorCollection);

        doReturn(Collections.EMPTY_LIST).when(issueCreationHelperBean).getProvidedFieldNames(mockIssue);
        doNothing().when(issueCreationHelperBean).validateCreateIssueFields(jiraServiceContext, Collections.<String>emptyList(), mockIssue, fieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters, i18nBean);
        doNothing().when(issueCreationHelperBean).updateIssueFromFieldValuesHolder(fieldScreenRenderer, mockIssue, issueInputParameters.getFieldValuesHolder());

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            FieldScreenRenderer getCreateFieldScreenRenderer(final User user, final Issue issue)
            {
                return fieldScreenRenderer;
            }
        };

        issueService.validateAndCreateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertFalse(errorCollection.hasAnyErrors());
        verify(issueCreationHelperBean).getProvidedFieldNames(mockIssue);
        verify(issueCreationHelperBean).validateCreateIssueFields(jiraServiceContext, Collections.<String>emptyList(), mockIssue, fieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters, i18nBean);
        verify(issueCreationHelperBean).updateIssueFromFieldValuesHolder(fieldScreenRenderer, mockIssue, issueInputParameters.getFieldValuesHolder());
    }

    @Test
    public void testValidateAndCreateIssueFromFieldsWithProvidedFields()
    {
        MockIssue mockIssue = new MockIssue(123L);
        final List<String> providedFields = CollectionBuilder.list("test", "test2");
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        issueInputParameters.setProvidedFields(providedFields);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, errorCollection);

        doNothing().when(issueCreationHelperBean).validateCreateIssueFields(jiraServiceContext, providedFields, mockIssue, fieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters, i18nBean);
        doNothing().when(issueCreationHelperBean).updateIssueFromFieldValuesHolder(fieldScreenRenderer, mockIssue, issueInputParameters.getFieldValuesHolder());

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            FieldScreenRenderer getCreateFieldScreenRenderer(final User user, final Issue issue)
            {
                return fieldScreenRenderer;
            }

        };

        issueService.validateAndCreateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertFalse(errorCollection.hasAnyErrors());
        verify(issueCreationHelperBean).validateCreateIssueFields(jiraServiceContext, providedFields, mockIssue, fieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters, i18nBean);
        verify(issueCreationHelperBean).updateIssueFromFieldValuesHolder(fieldScreenRenderer, mockIssue, issueInputParameters.getFieldValuesHolder());
    }

    @Test
    public void testValidateAndCreateIssueFromFieldsValidationError()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError(IssueFieldConstants.PROJECT, " I am an error");

        final MockI18nBean i18nBean = new MockI18nBean();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, errorCollection);

        doReturn(Collections.EMPTY_LIST).when(issueCreationHelperBean).getProvidedFieldNames(mockIssue);
        doNothing().when(issueCreationHelperBean).validateCreateIssueFields(jiraServiceContext, Collections.<String>emptyList(), mockIssue, fieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters, i18nBean);

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            FieldScreenRenderer getCreateFieldScreenRenderer(final User user, final Issue issue)
            {
                return fieldScreenRenderer;
            }

        };

        issueService.validateAndCreateIssueFromFields(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertTrue(errorCollection.hasAnyErrors());
        verify(issueCreationHelperBean).getProvidedFieldNames(mockIssue);
        verify(issueCreationHelperBean).validateCreateIssueFields(jiraServiceContext, Collections.<String>emptyList(), mockIssue, fieldScreenRenderer, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters, i18nBean);
    }

    @Test
    public void testValidateAndSetIssueType()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("123");

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();
        doNothing().when(issueCreationHelperBean).validateIssueType(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateAndSetIssueType(mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertEquals("123", mockIssue.getIssueTypeId());
        assertFalse(errorCollection.hasAnyErrors());
        verify(issueCreationHelperBean).validateIssueType(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);
    }

    @Test
    public void testValidateAndSetIssueTypeErrorWithIssueType()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("123");

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError(IssueFieldConstants.ISSUE_TYPE, "I am an error");
        final MockI18nBean i18nBean = new MockI18nBean();
        doNothing().when(issueCreationHelperBean).validateIssueType(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateAndSetIssueType(mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertNull("The issue type should not have been set on the issue", mockIssue.getIssueTypeId());
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        assertTrue(errorCollection.getErrors().containsKey("issuetype"));
        verify(issueCreationHelperBean).validateIssueType(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);

    }

    @Test
    public void testValidateAndSetProject() throws Exception
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(123L);
        issueInputParameters.setIssueTypeId("789");

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();
        doNothing().when(issueCreationHelperBean).validateProject(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateAndSetProject(mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertEquals(new Long(123), mockIssue.getProjectId());
        assertFalse(errorCollection.hasAnyErrors());
        verify(issueCreationHelperBean).validateProject(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);
    }

    @Test
    public void testValidateAndSetProjectErrorWithProject() throws Exception
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(123L);
        issueInputParameters.setIssueTypeId("789");

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("pid", "I am an error");
        final MockI18nBean i18nBean = new MockI18nBean();

        doNothing().when(issueCreationHelperBean).validateProject(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateAndSetProject(mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), errorCollection, i18nBean);

        assertNull("The project id should not have been set.", mockIssue.getProjectId());
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrors().size());
        assertTrue(errorCollection.getErrors().containsKey("pid"));
        verify(issueCreationHelperBean).validateProject(mockIssue, new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder()), issueInputParameters.getActionParameters(), errorCollection, i18nBean);
    }

    @Test
    public void testHasPermissionToEditNoPerm()
    {
        MockIssue mockIssue = new MockIssue(123L);
        doReturn(false).when(issueManager).isEditable(mockIssue, null);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(issueService.hasPermissionToEdit(null, mockIssue, new MockI18nBean(), errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You do not have permission to edit issues in this project.", errorCollection.getErrorMessages().iterator().next());

        verify(issueManager).isEditable(mockIssue, null);
    }

    @Test
    public void testHasPermissionToEdit()
    {
        MockIssue mockIssue = new MockIssue(123L);
        doReturn(true).when(issueManager).isEditable(mockIssue, null);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());
        assertTrue(issueService.hasPermissionToEdit(null, mockIssue, new MockI18nBean(), null));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());

        verify(issueManager).isEditable(mockIssue, null);
    }

    @Test
    public void testHasPermissionToCreateNoPerm()
    {
        MockProject mockProject = new MockProject(123, "TST", "Test Project");
        doReturn(false).when(permissionManager).hasPermission(Permissions.CREATE_ISSUE, mockProject, (User) null);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, permissionManager, null, null, null, null, null, eventPublisher);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(issueService.hasPermissionToCreate(null, mockProject, new MockI18nBean(), errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You do not have permission to create issues in this project.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasPermissionToCreate()
    {
        MockProject mockProject = new MockProject(123, "TST", "Test Project");
        doReturn(true).when(permissionManager).hasPermission(Permissions.CREATE_ISSUE, mockProject, (User) null);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, permissionManager, null, null, null, null, null, eventPublisher);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertTrue(issueService.hasPermissionToCreate(null, mockProject, new MockI18nBean(), errorCollection));
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testHasPermissionToDeleteNoPerm()
    {
        MockIssue mockIssue = new MockIssue(123L);
        doReturn(false).when(permissionManager).hasPermission(Permissions.DELETE_ISSUE, mockIssue, (User) null);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, permissionManager, null, null, null, null, null, eventPublisher);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(issueService.hasPermissionToDelete(null, mockIssue, new MockI18nBean(), errorCollection));
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals("You do not have permission to delete issues in this project.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasPermissionToDelete()
    {
        MockIssue mockIssue = new MockIssue(123L);
        doReturn(true).when(permissionManager).hasPermission(Permissions.DELETE_ISSUE, mockIssue, (User) null);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, permissionManager, null, null, null, null, null, eventPublisher);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());
        assertTrue(issueService.hasPermissionToDelete(null, mockIssue, new MockI18nBean(), null));
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testLicenseInvalidForIssueCreationNotValid() throws Exception
    {
        final AtomicBoolean validateLicenseCalled = new AtomicBoolean(false);

        IssueCreationHelperBean issueCreationHelperBean = new IssueCreationHelperBean()
        {
            public void validateCreateIssueFields(final JiraServiceContext jiraServiceContext, final Collection<String> providedFields, final Issue issueObject, final FieldScreenRenderer fieldScreenRenderer, final OperationContext operationContext, final Map<String, String[]> parameters, boolean applyDefaults, final I18nHelper i18n)
            {
            }

            @Override
            public void validateCreateIssueFields(final JiraServiceContext jiraServiceContext, final Collection<String> providedFields, final Issue issueObject, final FieldScreenRenderer fieldScreenRenderer, final OperationContext operationContext, final IssueInputParameters issueInputParameters, final I18nHelper i18n)
            {
            }

            public void validateLicense(final ErrorCollection errors, final I18nHelper i18n)
            {
                validateLicenseCalled.set(true);
                errors.addErrorMessage("I am a test error");
            }

            public void updateIssueFromFieldValuesHolder(final FieldScreenRenderer fieldScreenRenderer, final User remoteUser, final MutableIssue issueObject, final Map customFieldValuesHolder)
            {
            }

            @Override
            public void updateIssueFromFieldValuesHolder(final FieldScreenRenderer fieldScreenRenderer, final MutableIssue issueObject, final Map fieldValuesHolder)
            {
                throw new UnsupportedOperationException("Not implemented");
            }

            public FieldScreenRenderer createFieldScreenRenderer(final User remoteUser, final Issue issueObject)
            {
                return null;
            }

            @Override
            public FieldScreenRenderer createFieldScreenRenderer(final Issue issueObject)
            {
                throw new UnsupportedOperationException("Not implemented");
            }

            public List<String> getProvidedFieldNames(final User remoteUser, final Issue issueObject)
            {
                return null;
            }

            @Override
            public List<String> getProvidedFieldNames(final Issue issueObject)
            {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public List<OrderableField> getFieldsForCreate(User user, Issue issueObject)
            {
                return null;
            }

            public void validateProject(final Issue issue, final OperationContext operationContext, final Map actionParams, final ErrorCollection errors, final I18nHelper i18n)
            {
            }

            public void validateIssueType(final Issue issue, final OperationContext operationContext, final Map actionParams, final ErrorCollection errors, final I18nHelper i18n)
            {
            }

            @Override
            public void validateSummary(Issue issue, OperationContext operationContext, Map actionParams, ErrorCollection errors, I18nHelper i18n)
            {
                throw new UnsupportedOperationException("Not implemented");
            }

            public void updateFieldValuesHolderWithDefaults(final Issue issueObject, final GenericValue project, final String issuetype, final Map actionParams, final Map<String, Object> fieldValuesHolder, final User remoteUser, final FieldScreenRenderer fieldScreenRenderer)
            {
            }
        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = new MockI18nBean();

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher);

        assertTrue(issueService.licenseInvalidForIssueCreation(errorCollection, i18nBean));
        assertTrue(validateLicenseCalled.get());
    }

    @Test
    public void testLicenseInvalidForIssueCreation() throws Exception
    {
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MockI18nBean i18nBean = new MockI18nBean();
        doNothing().when(issueCreationHelperBean).validateLicense(new SimpleErrorCollection(), i18nBean);

        DefaultIssueService issueService = new DefaultIssueService(null, issueCreationHelperBean, null, null, null, null, null, null, null, null, eventPublisher);

        assertFalse(issueService.licenseInvalidForIssueCreation(errorCollection, i18nBean));
        verify(issueCreationHelperBean).validateLicense(new SimpleErrorCollection(), i18nBean);
    }

    @Test
    public void testConstructNewIssue() throws Exception
    {
        final MockIssue issue = new MockIssue();
        doReturn(issue).when(issueFactory).getIssue();

        DefaultIssueService issueService = new DefaultIssueService(issueFactory, null, null, null, null, null, null, null, null, null, eventPublisher);
        assertEquals(issue, issueService.constructNewIssue());
        verify(issueFactory).getIssue();
    }

    @Test
    public void testCopyIssue() throws Exception
    {
        final MockGenericValue mockGenericValue = new MockGenericValue("Issue");
        final MockIssue issue = new MockIssue();
        issue.setGenericValue(mockGenericValue);

        doReturn(issue).when(issueFactory).getIssue(mockGenericValue);

        DefaultIssueService issueService = new DefaultIssueService(issueFactory, null, null, null, null, null, null, null, null, null, eventPublisher);
        assertEquals(issue, issueService.copyIssue(issue));
        verify(issueFactory).getIssue(mockGenericValue);
    }

    @Test
    public void testGetCreateFieldScreenRenderer() throws Exception
    {
        doReturn(null).when(fieldScreenRendererFactory).getFieldScreenRenderer((User) null, null, IssueOperations.CREATE_ISSUE_OPERATION, false);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, fieldScreenRendererFactory, null, null, null, null, eventPublisher);
        issueService.getCreateFieldScreenRenderer(null, null);

        verify(fieldScreenRendererFactory).getFieldScreenRenderer((User) null, null, IssueOperations.CREATE_ISSUE_OPERATION, false);
    }

    @Test
    public void testGetUpdateFieldScreenRenderer() throws Exception
    {
        doReturn(null).when(fieldScreenRendererFactory).getFieldScreenRenderer((User) null, null, IssueOperations.EDIT_ISSUE_OPERATION, false);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, fieldScreenRendererFactory, null, null, null, null, eventPublisher);
        issueService.getUpdateFieldScreenRenderer(null, null);

        verify(fieldScreenRendererFactory).getFieldScreenRenderer((User) null, null, IssueOperations.EDIT_ISSUE_OPERATION, false);
    }

    @Test
    public void testValidateAndPopulateParamsRetainIssueValues()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        OperationContext operationContext = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder());
        ErrorCollection errorCollection = new SimpleErrorCollection();
        I18nHelper i18n = new MockI18nBean();

        doReturn("1000").when(orderableField).getId();
        doNothing().when(orderableField).populateFromIssue(issueInputParameters.getFieldValuesHolder(), mockIssue);
        doNothing().when(orderableField).validateParams(operationContext, errorCollection, i18n, mockIssue, fieldScreenRenderLayoutItem);
        doReturn(true).when(fieldScreenRenderLayoutItem).isShow(mockIssue);
        doReturn(orderableField).when(fieldScreenRenderLayoutItem).getOrderableField();
        doReturn(Lists.newArrayList(fieldScreenRenderLayoutItem)).when(fieldScreenRenderTab).getFieldScreenRenderLayoutItemsForProcessing();
        doReturn(Lists.newArrayList(fieldScreenRenderTab)).when(fieldScreenRenderer).getFieldScreenRenderTabs();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateAndPopulateParams(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), operationContext, errorCollection, i18n, fieldScreenRenderer);

        verify(orderableField).populateFromIssue(issueInputParameters.getFieldValuesHolder(), mockIssue);
        verify(orderableField).validateParams(operationContext, errorCollection, i18n, mockIssue, fieldScreenRenderLayoutItem);
        verify(fieldScreenRenderLayoutItem).isShow(mockIssue);
        verify(fieldScreenRenderLayoutItem).getOrderableField();
        verify(fieldScreenRenderTab).getFieldScreenRenderLayoutItemsForProcessing();
        verify(fieldScreenRenderer).getFieldScreenRenderTabs();
    }

    @Test
    public void testValidateAndPopulateParams()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(false);
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        OperationContext operationContext = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder());
        ErrorCollection errorCollection = new SimpleErrorCollection();
        I18nHelper i18n = new MockI18nBean();

        doNothing().when(orderableField).populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        doNothing().when(orderableField).validateParams(operationContext, errorCollection, i18n, mockIssue, fieldScreenRenderLayoutItem);
        doReturn(true).when(fieldScreenRenderLayoutItem).isShow(mockIssue);
        doReturn(orderableField).when(fieldScreenRenderLayoutItem).getOrderableField();
        doReturn(Lists.newArrayList(fieldScreenRenderLayoutItem)).when(fieldScreenRenderTab).getFieldScreenRenderLayoutItemsForProcessing();
        doReturn(Lists.newArrayList(fieldScreenRenderTab)).when(fieldScreenRenderer).getFieldScreenRenderTabs();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateAndPopulateParams(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), operationContext, errorCollection, i18n, fieldScreenRenderer);

        verify(orderableField).populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        verify(orderableField).validateParams(operationContext, errorCollection, i18n, mockIssue, fieldScreenRenderLayoutItem);
        verify(fieldScreenRenderLayoutItem).isShow(mockIssue);
        verify(fieldScreenRenderLayoutItem).getOrderableField();
        verify(fieldScreenRenderTab).getFieldScreenRenderLayoutItemsForProcessing();
        verify(fieldScreenRenderer).getFieldScreenRenderTabs();
    }

    @Test
    public void testValidateAndPopulateParamsWithComment()
    {
        MockIssue mockIssue = new MockIssue(123L);
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(false);
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setComment("Comment");

        OperationContext operationContext = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, issueInputParameters.getFieldValuesHolder());
        ErrorCollection errorCollection = new SimpleErrorCollection();
        I18nHelper i18n = new MockI18nBean();

        doNothing().when(orderableField).populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        doNothing().when(orderableField).validateParams(operationContext, errorCollection, i18n, mockIssue, fieldScreenRenderLayoutItem);
        doReturn(true).when(fieldScreenRenderLayoutItem).isShow(mockIssue);
        doReturn(orderableField).when(fieldScreenRenderLayoutItem).getOrderableField();
        doReturn(Lists.newArrayList(fieldScreenRenderLayoutItem)).when(fieldScreenRenderTab).getFieldScreenRenderLayoutItemsForProcessing();
        doReturn(Lists.newArrayList(fieldScreenRenderTab)).when(fieldScreenRenderer).getFieldScreenRenderTabs();

        final OrderableField mockCommentField = Mockito.mock(OrderableField.class, strict());
        doNothing().when(mockCommentField).populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        doNothing().when(mockCommentField).validateParams(operationContext, errorCollection, i18n, mockIssue, null);
        doReturn(mockCommentField).when(fieldManager).getField(IssueFieldConstants.COMMENT);

        final AtomicBoolean getLayoutForFieldCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, fieldManager, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            FieldScreenRenderLayoutItem getFieldScreenRendererLayoutItemForField(final User user, final Issue issue, final OrderableField field)
            {
                getLayoutForFieldCalled.set(true);
                return null;
            }
        };

        issueService.validateAndPopulateParams(null, mockIssue, issueInputParameters, issueInputParameters.getFieldValuesHolder(), operationContext, errorCollection, i18n, fieldScreenRenderer);
        assertTrue(getLayoutForFieldCalled.get());

        verify(orderableField).populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        verify(orderableField).validateParams(operationContext, errorCollection, i18n, mockIssue, fieldScreenRenderLayoutItem);
        verify(fieldScreenRenderLayoutItem).isShow(mockIssue);
        verify(fieldScreenRenderLayoutItem).getOrderableField();
        verify(fieldScreenRenderTab).getFieldScreenRenderLayoutItemsForProcessing();
        verify(fieldScreenRenderer).getFieldScreenRenderTabs();
        verify(mockCommentField).populateFromParams(issueInputParameters.getFieldValuesHolder(), issueInputParameters.getActionParameters());
        verify(mockCommentField).validateParams(operationContext, errorCollection, i18n, mockIssue, null);
        verify(fieldManager).getField(IssueFieldConstants.COMMENT);
    }

    @Test
    public void testUpdateIssueFromFieldsNoCommentUpdate()
    {
        assertUpdateIssueFromFields(false);
    }

    @Test
    public void testUpdateIssueFromFieldsFalseCommentUpdate()
    {
        assertUpdateIssueFromFields(true);
    }

    private void assertUpdateIssueFromFields(final boolean updateComment)
    {
        MockIssue mockIssue = new MockIssue(123L);
        Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();

        doReturn("summary").when(orderableField).getId();
        doNothing().when(orderableField).updateIssue(fieldLayoutItem, mockIssue, fieldValuesHolder);
        doReturn(true).when(fieldScreenRenderLayoutItem).isShow(mockIssue);
        doReturn(orderableField).when(fieldScreenRenderLayoutItem).getOrderableField();
        doReturn(fieldLayoutItem).when(fieldScreenRenderLayoutItem).getFieldLayoutItem();
        doReturn(Lists.newArrayList(fieldScreenRenderLayoutItem)).when(fieldScreenRenderTab).getFieldScreenRenderLayoutItemsForProcessing();
        doReturn(Lists.newArrayList(fieldScreenRenderTab)).when(fieldScreenRenderer).getFieldScreenRenderTabs();

        final AtomicBoolean updateIssueWithCommentCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher)
        {
            void updateIssueWithComment(final MutableIssue issue, final User user, final Map<String, Object> fieldValuesHolder)
            {
                // No-op
                updateIssueWithCommentCalled.set(true);
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueService.updateIssueFromFields(fieldScreenRenderer, mockIssue, null, fieldValuesHolder, updateComment, issueInputParameters);

        assertTrue(updateIssueWithCommentCalled.get() == updateComment);
        verify(orderableField).updateIssue(fieldLayoutItem, mockIssue, fieldValuesHolder);
        verify(fieldScreenRenderLayoutItem).isShow(mockIssue);
        verify(fieldScreenRenderLayoutItem).getOrderableField();
        verify(fieldScreenRenderLayoutItem).getFieldLayoutItem();
        verify(fieldScreenRenderTab).getFieldScreenRenderLayoutItemsForProcessing();
        verify(fieldScreenRenderer).getFieldScreenRenderTabs();
    }

    @Test
    public void testUpdateIssueWithComment() throws Exception
    {
        MockIssue mockIssue = new MockIssue(123L);
        Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();

        doNothing().when(orderableField).updateIssue(null, mockIssue, fieldValuesHolder);
        doReturn(orderableField).when(fieldManager).getField(IssueFieldConstants.COMMENT);
        doReturn(null).when(fieldScreenRenderLayoutItem).getFieldLayoutItem();

        final AtomicBoolean getLayoutForFieldCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, fieldManager, null, null, null, null, null, null, null, eventPublisher)
        {
            FieldScreenRenderLayoutItem getFieldScreenRendererLayoutItemForField(final User user, final Issue issue, final OrderableField field)
            {
                getLayoutForFieldCalled.set(true);
                return fieldScreenRenderLayoutItem;
            }
        };

        issueService.updateIssueWithComment(mockIssue, null, fieldValuesHolder);

        assertTrue(getLayoutForFieldCalled.get());
        verify(orderableField).updateIssue(null, mockIssue, fieldValuesHolder);
        verify(fieldManager).getField(IssueFieldConstants.COMMENT);
        verify(fieldScreenRenderLayoutItem).getFieldLayoutItem();
    }

    @Test
    public void testGetFieldScreenRendererLayoutItemForField() throws Exception
    {
        MockIssue mockIssue = new MockIssue(123L);
        doReturn(null).when(fieldScreenRenderer).getFieldScreenRenderLayoutItem(null);
        doReturn(fieldScreenRenderer).when(fieldScreenRendererFactory).getFieldScreenRenderer((User) null, mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, false);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, fieldScreenRendererFactory, null, null, null, null, eventPublisher);
        issueService.getFieldScreenRendererLayoutItemForField(null, mockIssue, null);

        verify(fieldScreenRenderer).getFieldScreenRenderLayoutItem(null);
        verify(fieldScreenRendererFactory).getFieldScreenRenderer((User) null, mockIssue, IssueOperations.VIEW_ISSUE_OPERATION, false);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testTransitionNullTransitionResult() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.transition(user.getDirectoryUser(), null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testTransitionNullIssue() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.transition(user.getDirectoryUser(), new IssueService.TransitionValidationResult(null, new SimpleErrorCollection(), null, Collections.EMPTY_MAP, 1));
    }

    @Test (expected = IllegalStateException.class)
    public void testTransitionInvalidValidationResult() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("blah");
        issueService.transition(user.getDirectoryUser(), new IssueService.TransitionValidationResult(null, errorCollection, null, Collections.EMPTY_MAP, 1));
    }

    @Test
    public void testAutomaticTransitionHappyPath() throws Exception
    {
        final MockIssue issue = new MockIssue(12, "TEST-10");
        doReturn(issue).when(issueManager).getIssueObject(issue.getId());
        doNothing().when(workflowManager).doWorkflowAction(org.mockito.Matchers.any(WorkflowProgressAware.class));

        TransitionOptions.Builder transitionChecks = new TransitionOptions.Builder();
        transitionChecks.setAutomaticTransition();
        TransitionOptions transitionOptions = transitionChecks.build();
        Map additionInputs = transitionOptions.getWorkflowParams();

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, workflowManager, null, null, null, eventPublisher);
        IssueService.TransitionValidationResult transitionResult = new IssueService.TransitionValidationResult(issue, new SimpleErrorCollection(), Collections.<String, Object>emptyMap(), additionInputs, 1);

        issueService.transition(user.getDirectoryUser(), transitionResult);

        verify(issueManager).getIssueObject(issue.getId());
        verify(workflowManager).doWorkflowAction(org.mockito.Matchers.any(WorkflowProgressAware.class));
        verify(eventPublisher, never()).publish(Matchers.anything());

        // instrumentation for automatic transition counter
        verify(instrumentRegistry).pullCounter(InstrumentationName.WORKFLOW_AUTOMATIC_TRANSITION.getInstrumentName());
        verify(counter).incrementAndGet();
    }

    @Test
    public void testManualTransitionHappyPath() throws Exception
    {
        final MockIssue issue = new MockIssue(12, "TEST-10");
        doReturn(issue).when(issueManager).getIssueObject(issue.getId());
        doNothing().when(workflowManager).doWorkflowAction(org.mockito.Matchers.any(WorkflowProgressAware.class));
        doNothing().when(eventPublisher).publish(org.mockito.Matchers.any(WorkflowManualTransitionExecutionEvent.class));

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, workflowManager, null, null, null, eventPublisher);
        IssueService.TransitionValidationResult transitionResult = new IssueService.TransitionValidationResult(issue, new SimpleErrorCollection(), Collections.<String, Object>emptyMap(), Collections.EMPTY_MAP, 1);

        issueService.transition(user.getDirectoryUser(), transitionResult);

        verify(issueManager).getIssueObject(issue.getId());
        verify(workflowManager).doWorkflowAction(org.mockito.Matchers.any(WorkflowProgressAware.class));
        verify(eventPublisher).publish(org.mockito.Matchers.any(WorkflowManualTransitionExecutionEvent.class));

        // instrumentation for automatic transition counter
        verify(instrumentRegistry).pullCounter(InstrumentationName.WORKFLOW_MANUAL_TRANSITION.getInstrumentName());
        verify(counter).incrementAndGet();
    }

    @Test
    public void testGetActionDescriptorNullWorkflow() throws Exception
    {
        final MockIssue issue = new MockIssue();
        doReturn(null).when(workflowManager).getWorkflow(issue);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, workflowManager, null, null, null, eventPublisher);

        assertNull(issueService.getActionDescriptor(issue, 2));
        verify(workflowManager).getWorkflow(issue);
    }

    @Test
    public void testGetActionDescriptorNullDescriptor() throws Exception
    {
        final MockIssue issue = new MockIssue();
        doReturn(null).when(jiraWorkflow).getDescriptor();
        doReturn(jiraWorkflow).when(workflowManager).getWorkflow(issue);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, workflowManager, null, null, null, eventPublisher);

        assertNull(issueService.getActionDescriptor(issue, 2));
        verify(jiraWorkflow).getDescriptor();
        verify(workflowManager).getWorkflow(issue);
    }

    @Test
    public void testGetActionDescriptorHappyPath() throws Exception
    {
        final MockIssue issue = new MockIssue();
        doReturn(jiraWorkflow).when(workflowManager).getWorkflow(issue);
        doReturn(workflowDescriptor).when(jiraWorkflow).getDescriptor();
        doReturn(actionDescriptor).when(workflowDescriptor).getAction(2);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, workflowManager, null, null, null, eventPublisher);

        assertEquals(actionDescriptor, issueService.getActionDescriptor(issue, 2));

        verify(workflowManager).getWorkflow(issue);
        verify(jiraWorkflow).getDescriptor();
        verify(workflowDescriptor).getAction(2);
    }

    @Test
    public void testGetTransitionFieldScreenRenderer() throws Exception
    {
        final MockIssue issue = new MockIssue();
        doReturn(null).when(fieldScreenRendererFactory).getFieldScreenRenderer((User) null, issue, actionDescriptor);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, fieldScreenRendererFactory, null, null, null, null, eventPublisher);

        issueService.getTransitionFieldScreenRenderer(null, issue, actionDescriptor);
        verify(fieldScreenRendererFactory).getFieldScreenRenderer((User) null, issue, actionDescriptor);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testValidateTransitionNullIssueInputParameters() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);
        issueService.validateTransition(user.getDirectoryUser(), 123L, 2, null);
    }

    @Test
    public void testValidateTransitionNullIssueId() throws Exception
    {
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, null, null, null, null, null, null, null, eventPublisher);

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user.getDirectoryUser(), null, 2, issueInputParameters);

        assertFalse(transitionValidationResult.isValid());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("You can not transition a null issue.", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateTransitionNullIssueFromId() throws Exception
    {
        doReturn(null).when(issueManager).getIssueObject(123L);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher);

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user.getDirectoryUser(), 123L, 2, issueInputParameters);

        assertFalse(transitionValidationResult.isValid());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("You can not transition a null issue.", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateTransitionNoSuchAction() throws Exception
    {
        final MockIssue issue = new MockIssue();
        doReturn(issue).when(issueManager).getIssueObject(123L);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return null;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");

        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user.getDirectoryUser(), 123L, 2, issueInputParameters);
        assertNull(transitionValidationResult.getIssue());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("The workflow operation with action id '2' does not exist in the workflow.", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateTransitionInvalidAction() throws Exception
    {
        final MockIssue issue = new MockIssue(1, "TST-1");
        final TransitionOptions transitionOptions = TransitionOptions.defaults();

        doReturn("Test").when(actionDescriptor).getName();
        doReturn(false).when(issueWorkflowManager).isValidAction(issue, 2, transitionOptions, user);
        doReturn(issue).when(issueManager).getIssueObject(123L);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, issueWorkflowManager, null, null, eventPublisher)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return actionDescriptor;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user.getDirectoryUser(), 123L, 2, issueInputParameters, transitionOptions);
        assertNull(transitionValidationResult.getIssue());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("It seems that you have tried to perform a workflow operation (Test) that is not valid for the current state of this issue (TST-1). The likely cause is that somebody has changed the issue recently, please look at the issue history for details.", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());

        verify(actionDescriptor).getName();
        verify(issueWorkflowManager).isValidAction(issue, 2, transitionOptions, user);
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateTransitionUpdateIssueUpdateHasError() throws Exception
    {
        final int actionId = 2;
        final Issue issue = newIssueWith(1L, "TST-1");
        final TransitionOptions transitionOptions = TransitionOptions.defaults();

        doReturn("BlahView").when(actionDescriptor).getView();
        doReturn(true).when(issueWorkflowManager).isValidAction(issue, actionId, transitionOptions, null);
        doReturn(issue).when(issueManager).getIssueObject(123L);
        doReturn("issueStatus").when(workflowManager).getNextStatusIdForAction(issue, actionId);

        final AtomicBoolean getFieldRendererCalled = new AtomicBoolean(false);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, workflowManager, issueWorkflowManager, null, null, eventPublisher)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return actionDescriptor;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment, Integer workflowActionId)
            {
                // Set a bs error
                errorCollection.addErrorMessage("I am a bs error");
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                return issue;
            }

            @Override
            FieldScreenRenderer getTransitionFieldScreenRenderer(final User user, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                getFieldRendererCalled.set(true);
                return null;
            }
        };

        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        // We should still get a transition result but the issue should be null and we should see the errors
        final IssueService.TransitionValidationResult transitionResult = issueService.validateTransition(null, 123L, actionId, issueInputParameters, transitionOptions);
        assertNull(transitionResult.getIssue());
        assertEquals(1, transitionResult.getErrorCollection().getErrorMessages().size());
        assertEquals("I am a bs error", transitionResult.getErrorCollection().getErrorMessages().iterator().next());

        assertTrue(getFieldRendererCalled.get());
        verify(actionDescriptor).getView();
        verify(issueWorkflowManager).isValidAction(issue, actionId, transitionOptions, null);
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateTransitionUpdateIssueUpdateHasErrorWithComment() throws Exception
    {
        final Map<String, Object> addParams = MapBuilder.<String, Object>singletonMap("one", "value");
        final int actionId = 2;
        final Issue issue = newIssueWith(1L, "TST-1");
        final TransitionOptions transitionOptions = TransitionOptions.defaults();

        doReturn("BlahView").when(actionDescriptor).getView();
        doReturn(true).when(issueWorkflowManager).isValidAction(issue, actionId, transitionOptions, user);
        doReturn(issue).when(issueManager).getIssueObject(123L);
        doReturn("issueStatus").when(workflowManager).getNextStatusIdForAction(issue, actionId);

        final AtomicBoolean getFieldRendererCalled = new AtomicBoolean(false);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, workflowManager, issueWorkflowManager, null, null, eventPublisher)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return actionDescriptor;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment, Integer workflowActionId)
            {
                // Set a bs error
                errorCollection.addErrorMessage("I am a bs error");
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                return issue;
            }

            @Override
            FieldScreenRenderer getTransitionFieldScreenRenderer(final User user, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                getFieldRendererCalled.set(true);
                return null;
            }

            @Override
            Map<String, Object> createAdditionalParameters(ApplicationUser user, Map<String, Object> fieldValuesHolder,
                    TransitionOptions skipTransitionChecks, HistoryMetadata historyMetadata, String originalAssigneeId)
            {
                return addParams;
            }
        };

        // We should still get a transition result but the issue should be null and we should see the errors
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setComment("I am a comment", "jira-devs");
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(user.getDirectoryUser(), 123L, actionId, issueInputParameters, transitionOptions);
        assertNull(transitionValidationResult.getIssue());
        assertEquals(1, transitionValidationResult.getErrorCollection().getErrorMessages().size());
        assertEquals("I am a bs error", transitionValidationResult.getErrorCollection().getErrorMessages().iterator().next());

        assertTrue(getFieldRendererCalled.get());
        verify(actionDescriptor).getView();
        verify(issueWorkflowManager).isValidAction(issue, actionId, transitionOptions, user);
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateTransitionUpdateIssueHappyPath() throws Exception
    {
        final Map<String, Object> addParams = MapBuilder.<String, Object>singletonMap("one", "value");
        final int actionId = 2;
        final Issue issue = newIssueWith(1, "TST-1");
        final TransitionOptions transitionOptions = TransitionOptions.defaults();

        doReturn("BlahView").when(actionDescriptor).getView();
        doReturn(true).when(issueWorkflowManager).isValidAction(issue, actionId, transitionOptions, user);
        doReturn(issue).when(issueManager).getIssueObject(123L);
        doReturn("issueStatus").when(workflowManager).getNextStatusIdForAction(issue, actionId);

        final AtomicBoolean getFieldRendererCalled = new AtomicBoolean(false);

        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, workflowManager, issueWorkflowManager, null, null, eventPublisher)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return actionDescriptor;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment, Integer workflowActionId)
            {
                // Set a bs error
                return issue;
            }

            @Override
            MutableIssue copyIssue(final MutableIssue issue)
            {
                return issue;
            }

            @Override
            FieldScreenRenderer getTransitionFieldScreenRenderer(final User user, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                getFieldRendererCalled.set(true);
                return null;
            }

            @Override
            Map<String, Object> createAdditionalParameters(ApplicationUser user, Map<String, Object> fieldValuesHolder, TransitionOptions skipTransitionChecks, HistoryMetadata historyMetadata, String originalAssigneeId)
            {
                return addParams;
            }
        };

        // We should still get a transition result but the issue should be null and we should see the errors
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setComment("I am a comment", "jira-devs");
        final IssueService.TransitionValidationResult transitionResult = issueService.validateTransition(user.getDirectoryUser(), 123L, actionId, issueInputParameters, transitionOptions);
        assertEquals(issue, transitionResult.getIssue());
        assertEquals(addParams, transitionResult.getAdditionInputs());

        assertTrue(getFieldRendererCalled.get());
        verify(actionDescriptor).getView();
        verify(issueWorkflowManager).isValidAction(issue, actionId, transitionOptions, user);
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateTransitionHappyPath() throws Exception
    {
        final Map<String, Object> addParams = MapBuilder.<String, Object>singletonMap("one", "value");
        final MockIssue issue = new MockIssue(1, "TST-1");
        final TransitionOptions transitionOptions = TransitionOptions.defaults();

        doReturn(null).when(actionDescriptor).getView();
        doReturn(true).when(issueWorkflowManager).isValidAction(issue, 2, transitionOptions, user);
        doReturn(issue).when(issueManager).getIssueObject(123L);

        final AtomicBoolean getFieldRendererCalled = new AtomicBoolean(false);
        DefaultIssueService issueService = new DefaultIssueService(null, null, null, issueManager, null, null, null, issueWorkflowManager, null, null, eventPublisher)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return actionDescriptor;
            }

            @Override
            protected MutableIssue validateAndUpdateIssueFromFields(final User user, final MutableIssue issue, final IssueInputParameters issueInputParameters, final Map<String, Object> fieldValuesHolder, final ErrorCollection errorCollection, final I18nHelper i18n, final FieldScreenRenderer fieldScreenRenderer, final boolean updateComment, Integer workflowActionId)
            {
                // Set a bs error
                return issue;
            }

            @Override
            FieldScreenRenderer getTransitionFieldScreenRenderer(final User user, final Issue issue, final ActionDescriptor actionDescriptor)
            {
                getFieldRendererCalled.set(true);
                return null;
            }

            @Override
            Map<String, Object> createAdditionalParameters(ApplicationUser user, Map<String, Object> fieldValuesHolder, TransitionOptions skipTransitionChecks, HistoryMetadata historyMetadata, String originalAssigneeId)
            {
                return addParams;
            }
        };

        // We should still get a transition result but the issue should be null and we should see the errors
        IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
        issueInputParameters.setProjectId(456L);
        issueInputParameters.setIssueTypeId("789");
        issueInputParameters.setComment("I am a comment", "jira-devs");
        final IssueService.TransitionValidationResult transitionResult = issueService.validateTransition(user.getDirectoryUser(), 123L, 2, issueInputParameters, transitionOptions);
        assertEquals(issue, transitionResult.getIssue());
        assertSame(addParams, transitionResult.getAdditionInputs());

        assertFalse(getFieldRendererCalled.get());
        verify(actionDescriptor).getView();
        verify(issueWorkflowManager).isValidAction(issue, 2, transitionOptions, user);
        verify(issueManager).getIssueObject(123L);
    }

    @Test
    public void testValidateTransitionPutsMetadataInAdditionalParams() throws Exception
    {
        // having
        final MockIssue issue = new MockIssue(1L);

        final HistoryMetadata metadata = HistoryMetadata.builder("TestDefaultIssueService").build();
        final IssueInputParameters issueInputParameters = new IssueInputParametersImpl().setHistoryMetadata(metadata);

        final IssueManager issueManager = mock(IssueManager.class);
        final IssueWorkflowManager issueWorkflowManager = mock(IssueWorkflowManager.class);
        final MockFieldManager fieldManager = new MockFieldManager()
        {
            @Override
            public Field getField(final String id)
            {
                return IssueFieldConstants.COMMENT.equals(id) ? mock(CommentSystemField.class) : null;
            }
        };

        final IssueService issueService = new DefaultIssueService(null, null, fieldManager, issueManager, null, null, null,
                issueWorkflowManager, null, null, eventPublisher)
        {
            @Override
            ActionDescriptor getActionDescriptor(final Issue issue, final int actionId)
            {
                return DescriptorFactory.getFactory().createActionDescriptor();
            }
        };

        when(issueManager.getIssueObject(issue.getId())).thenReturn(issue);
        when(issueWorkflowManager.isValidAction(eq(issue), anyInt(), any(TransitionOptions.class), eq(user))).thenReturn(true);

        // when
        final IssueService.TransitionValidationResult validationResult = issueService.validateTransition(user.getDirectoryUser(), issue.getId(), 0, issueInputParameters);
        final Object paramMetadata = validationResult.getAdditionInputs().get(DefaultChangeHistoryManager.HISTORY_METADATA_KEY);

        // then
        assertThat(paramMetadata, instanceOf(HistoryMetadata.class));
        assertThat((HistoryMetadata) paramMetadata, sameInstance(metadata));
    }

    @Test
    public void testCreateAdditionalParametersNullUser() throws Exception
    {
        String originalAssigneeId = "assignee";
        Map<String, Object> fvh = MapBuilder.singletonMap(IssueFieldConstants.COMMENT, new Object());
        Map<String, Object> outputMap = MapBuilder.build("userKey", null, "originalAssigneeId", (Object) originalAssigneeId);

        final FieldManager fieldManager = Mockito.mock(FieldManager.class);
        final CommentSystemField csf = Mockito.mock(CommentSystemField.class);
        doReturn(csf).when(fieldManager).getOrderableField(IssueFieldConstants.COMMENT);
        csf.populateAdditionalInputs(fvh, outputMap);

        final DefaultIssueService service = new DefaultIssueService(null, null, fieldManager, null, null, null, null, null, null, null, eventPublisher);

        final Map<String, Object> actualMap = service.createAdditionalParameters(null, fvh, TransitionOptions.defaults(), null, originalAssigneeId);
        assertEquals(outputMap, actualMap);
        verify(fieldManager).getOrderableField(IssueFieldConstants.COMMENT);
    }

    @Test
    public void testCreateAdditionalParametersWithUser() throws Exception
    {
        String originalAssigneeId = "assignee";
        Map<String, Object> fvh = MapBuilder.singletonMap(IssueFieldConstants.COMMENT, new Object());
        Map<String, Object> outputMap = MapBuilder.<String, Object>build("userKey", user.getKey(), "originalAssigneeId", originalAssigneeId);

        final FieldManager fieldManager = Mockito.mock(FieldManager.class);
        final CommentSystemField csf = Mockito.mock(CommentSystemField.class);
        doReturn(csf).when(fieldManager).getOrderableField(IssueFieldConstants.COMMENT);
        csf.populateAdditionalInputs(fvh, outputMap);

        final DefaultIssueService service = new DefaultIssueService(null, null, fieldManager, null, null, null, null, null, null, null, eventPublisher);
        final Map<String, Object> actualMap = service.createAdditionalParameters(user, fvh, TransitionOptions.defaults(), null, originalAssigneeId);
        assertEquals(outputMap, actualMap);
        verify(fieldManager).getOrderableField(IssueFieldConstants.COMMENT);
    }

    private MutableIssue newIssueWith(long id, String key)
    {
        MutableIssue issue = mock(MutableIssue.class);
        when(issue.getId()).thenReturn(1L);
        when(issue.getKey()).thenReturn("TST-1");
        when(issue.getStatusObject()).thenReturn(new MockStatus("1", "Open"));
        return issue;
    }
}
