package com.atlassian.jira.appconsistency.integrity.check;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.appconsistency.integrity.amendment.Amendment.ERROR;
import static com.atlassian.jira.appconsistency.integrity.amendment.Amendment.UNFIXABLE_ERROR;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestWorkflowCurrentStepCheck
{
    // Constants
    private static final int CHECK_ID = 666;
    private static final String AMENDMENT_MESSAGE = "Some message";
    private static final String AMENDMENT_MESSAGE_2 = "Some other message";

    // Fixture
    private WorkflowCurrentStepCheck checkUnderTest;
    @Mock private ConstantsManager mockConstantsManager;
    @Mock private I18nHelper mockI18nHelper;
    @Mock private IntegrityCheck mockIntegrityCheck;
    @Mock private JiraAuthenticationContext mockJiraAuthenticationContext;
    @Mock private OfBizDelegator mockOfBizDelegator;
    @Mock private WorkflowManager mockWorkflowManager;
    @Mock private WorkflowStore mockWorkflowStore;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        new MockComponentWorker().addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext).init();
        checkUnderTest =
                new WorkflowCurrentStepCheck(mockOfBizDelegator, CHECK_ID, mockConstantsManager, mockWorkflowManager);
        checkUnderTest.setIntegrityCheck(mockIntegrityCheck);
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void descriptionShouldBeInternationalised()
    {
        // Set up
        final String description = "anything";
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.desc")).thenReturn(description);

        // Invoke
        final String actualDescription = checkUnderTest.getDescription();

        // Check
        assertEquals(description, actualDescription);
    }

    @Test
    public void checkShouldAlwaysBeAvailable()
    {
        // Invoke and check
        assertTrue("Check should always be available", checkUnderTest.isAvailable());
    }

    @Test
    public void unavailableMessageCanBeAnything()
    {
        // Invoke and check
        assertEquals("", checkUnderTest.getUnavailableMessage());
    }

    private List<GenericValue> setUpIssues(final long... issueIds)
    {
        final OfBizListIterator mockOfBizListIterator = mock(OfBizListIterator.class);
        final List<GenericValue> mockIssues = new ArrayList<GenericValue>();
        for (final long issueId : issueIds)
        {
            final GenericValue mockIssue = mock(GenericValue.class);
            when(mockIssue.getLong("id")).thenReturn(issueId);
            when(mockOfBizDelegator.findById("Issue", issueId)).thenReturn(mockIssue);
            mockIssues.add(mockIssue);
        }
        if (!mockIssues.isEmpty())
        {
            OngoingStubbing<GenericValue> ongoingStubbing = when(mockOfBizListIterator.next());
            for (final GenericValue mockIssue : mockIssues)
            {
                ongoingStubbing = ongoingStubbing.thenReturn(mockIssue);
            }
            ongoingStubbing.thenReturn(null);
        }
        // actual: ofBizDelegator.findListIteratorByCondition("Issue", null, null, EasyList.build("id"), null, null)
        when(mockOfBizDelegator.findListIteratorByCondition("Issue", null, null, asList("id"), null, null))
                .thenReturn(mockOfBizListIterator);
        return mockIssues;
    }

    @Test
    public void previewShouldReturnEmptyListWhenThereAreNoIssues() throws Exception
    {
        // Set up
        setUpIssues();

        // Invoke
        final List<?> amendments = checkUnderTest.preview();

        // Check
        assertEquals(EMPTY_LIST, amendments);
    }

    @Test
    public void previewShouldReturnEmptyListWhenIssueIsNull() throws Exception
    {
        // Set up
        final long issueId = 10;
        setUpIssues(issueId);
        when(mockOfBizDelegator.findById("Issue", issueId)).thenReturn(null);

        // Invoke
        final List<?> amendments = checkUnderTest.preview();

        // Check
        assertEquals(EMPTY_LIST, amendments);
    }

    @Test
    public void previewShouldReturnEmptyListWhenIssueHasNoStatusCode() throws Exception
    {
        // Set up
        setUpIssues(10);

        // Invoke
        final List<?> amendments = checkUnderTest.preview();

        // Check
        assertEquals(EMPTY_LIST, amendments);
    }

    @Test
    public void previewShouldContainUnfixableErrorWhenStatusCodeNotKnown() throws Exception
    {
        // Set up
        final GenericValue mockIssue = setUpIssues(10).get(0);
        final String issueKey = "ABC-123";
        when(mockIssue.getString("key")).thenReturn(issueKey);
        final String statusCode = "unknownCode";
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockI18nHelper.getText("admin.integrity.check.issue.status.unfixable", issueKey, statusCode))
                .thenReturn(AMENDMENT_MESSAGE);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(1, amendments.size());
        assertAmendment(UNFIXABLE_ERROR, AMENDMENT_MESSAGE, "JRA-4714", amendments.get(0));
    }

    @Test
    public void previewShouldContainUnfixableErrorWhenWorkflowEntryIdIsNull() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(null);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.workflowid.unfixable", issueKey, statusName))
                .thenReturn(AMENDMENT_MESSAGE);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(1, amendments.size());
        assertAmendment(UNFIXABLE_ERROR, AMENDMENT_MESSAGE, "JRA-4714", amendments.get(0));
    }

    @Test
    public void previewShouldContainUnfixableErrorWhenStepInWorkflowIsNull() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.unfixable", issueKey, statusName))
                .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(1, amendments.size());
        assertAmendment(UNFIXABLE_ERROR, AMENDMENT_MESSAGE, "JRA-8326", amendments.get(0));
    }

    @Test
    public void previewShouldContainNoErrorsWhenStepInWorkflowHasNoActions() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.unfixable", issueKey, statusName))
                .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(Collections.<GenericValue>emptyList());
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepInWorkflow.getActions()).thenReturn(emptyList());

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(EMPTY_LIST, amendments);
    }

    @Test
    public void previewShouldContainErrorWhenStepInWorkflowNeedsToBeCreated() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.correct.preview", issueKey,
                String.valueOf(workflowEntryId)))
                    .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(Collections.<GenericValue>emptyList());
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepInWorkflow.getActions()).thenReturn(singletonList(new Object()));

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(1, amendments.size());
        assertAmendment(ERROR, AMENDMENT_MESSAGE, "JRA-4539", amendments.get(0));
    }

    @Test
    public void previewShouldContainErrorWhenStepBeingValidatedHasNoId() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.validate.preview", issueKey,
                String.valueOf(workflowEntryId)))
                    .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        final GenericValue mockStepGV = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(singletonList(mockStepGV));
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepGV.getInteger("stepId")).thenReturn(null);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(1, amendments.size());
        assertAmendment(ERROR, AMENDMENT_MESSAGE, "JRA-4539", amendments.get(0));
    }

    @Test
    public void previewShouldContainErrorWhenStepBeingValidatedHasWrongId() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.validate.preview", issueKey,
                String.valueOf(workflowEntryId)))
                .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        final GenericValue mockStepGV = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(singletonList(mockStepGV));
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        final int stepInWorkflowId = 789;
        when(mockStepInWorkflow.getId()).thenReturn(stepInWorkflowId);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepGV.getInteger("stepId")).thenReturn(stepInWorkflowId + 1);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(1, amendments.size());
        assertAmendment(ERROR, AMENDMENT_MESSAGE, "JRA-4539", amendments.get(0));
    }

    @Test
    public void previewShouldContainNoErrorsWhenStepBeingValidatedHasCorrectId() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.validate.preview", issueKey,
                String.valueOf(workflowEntryId)))
                    .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        final GenericValue mockStepGV = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(singletonList(mockStepGV));
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        final int stepInWorkflowId = 789;
        when(mockStepInWorkflow.getId()).thenReturn(stepInWorkflowId);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepGV.getInteger("stepId")).thenReturn(stepInWorkflowId);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(EMPTY_LIST, amendments);
    }

    @Test
    public void previewShouldContainErrorWhenWorkflowEntryHasMultipleSteps() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.delete.preview", issueKey,
                String.valueOf(workflowEntryId)))
                    .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        final GenericValue mockStepGV1 = mock(GenericValue.class);
        final GenericValue mockStepGV2 = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(asList(mockStepGV1, mockStepGV2));
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        final int stepInWorkflowId = 789;
        when(mockStepInWorkflow.getId()).thenReturn(stepInWorkflowId);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.preview();

        // Check
        assertEquals(1, amendments.size());
        assertAmendment(ERROR, AMENDMENT_MESSAGE, "JRA-4539", amendments.get(0));
    }

    @Test
    public void correctShouldCreateNewStepWhenActionHasNoStep() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final Timestamp createdOn = mock(Timestamp.class);
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        when(mockIssue.getTimestamp("created")).thenReturn(createdOn);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.correct.message", issueKey,
                String.valueOf(workflowEntryId)))
                    .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(Collections.<GenericValue>emptyList());
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepInWorkflow.getActions()).thenReturn(singletonList(new Object()));
        final int stepInWorkflowId = 789;
        when(mockStepInWorkflow.getId()).thenReturn(stepInWorkflowId);
        when(mockWorkflowManager.getStore()).thenReturn(mockWorkflowStore);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.correct();

        // Check
        // workflowEntryId.longValue(), stepInWorkflowId, null, issue.getTimestamp("created"),
        // null, issue.getString("status"), null
        verify(mockWorkflowStore).createCurrentStep(workflowEntryId, stepInWorkflowId, null, createdOn, null, statusCode, null);
        assertEquals(1, amendments.size());
        assertAmendment(ERROR, AMENDMENT_MESSAGE, "JRA-4539", amendments.get(0));
    }

    @Test
    public void correctShouldDeleteAllButFirstStepWhenActionHasMultipleStepsAndFirstStepIdIsCorrect() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final Timestamp createdOn = mock(Timestamp.class);
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        when(mockIssue.getTimestamp("created")).thenReturn(createdOn);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.delete.message2", issueKey,
                String.valueOf(workflowEntryId)))
                    .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepInWorkflow.getActions()).thenReturn(singletonList(new Object()));
        final int stepInWorkflowId = 789;
        when(mockStepInWorkflow.getId()).thenReturn(stepInWorkflowId);
        final GenericValue mockStepGV1 = mock(GenericValue.class);
        final GenericValue mockStepGV2 = mock(GenericValue.class);
        final GenericValue mockStepGV3 = mock(GenericValue.class);
        when(mockStepGV1.getInteger("stepId")).thenReturn(stepInWorkflowId);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(asList(mockStepGV1, mockStepGV2, mockStepGV3));

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.correct();

        // Check
        verify(mockStepGV2).remove();
        verify(mockStepGV3).remove();
        verifyNoMoreInteractions(mockStepGV2, mockStepGV3);
        assertEquals(1, amendments.size());
        assertAmendment(ERROR, AMENDMENT_MESSAGE, "JRA-4539", amendments.get(0));
    }

    @Test
    public void correctShouldDeleteAllButFirstStepWhenActionHasMultipleStepsAndFirstStepIdIsWrong() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final Timestamp createdOn = mock(Timestamp.class);
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        when(mockIssue.getTimestamp("created")).thenReturn(createdOn);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.delete.message1", issueKey,
                String.valueOf(workflowEntryId)))
                    .thenReturn(AMENDMENT_MESSAGE);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.delete.message2", issueKey,
                String.valueOf(workflowEntryId)))
                    .thenReturn(AMENDMENT_MESSAGE_2);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepInWorkflow.getActions()).thenReturn(singletonList(new Object()));
        final int stepInWorkflowId = 789;
        when(mockStepInWorkflow.getId()).thenReturn(stepInWorkflowId);
        final GenericValue mockStepGV1 = mock(GenericValue.class);
        final GenericValue mockStepGV2 = mock(GenericValue.class);
        when(mockStepGV1.getInteger("stepId")).thenReturn(stepInWorkflowId + 1);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(asList(mockStepGV1, mockStepGV2));

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.correct();

        // Check
        verify(mockStepGV1).set("stepId", stepInWorkflowId);
        verify(mockStepGV1).store();
        verify(mockStepGV2).remove();
        verifyNoMoreInteractions(mockStepGV2);
        assertEquals(2, amendments.size());
        assertAmendment(ERROR, AMENDMENT_MESSAGE, "JRA-4539", amendments.get(0));
        assertAmendment(ERROR, AMENDMENT_MESSAGE_2, "JRA-4539", amendments.get(1));
    }

    @Test
    public void correctShouldDoNothingWhenStepBeingValidatedHasCorrectId() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.validate.preview", issueKey,
                String.valueOf(workflowEntryId)))
                .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        final GenericValue mockStepGV = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(singletonList(mockStepGV));
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        final int stepInWorkflowId = 789;
        when(mockStepInWorkflow.getId()).thenReturn(stepInWorkflowId);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepGV.getInteger("stepId")).thenReturn(stepInWorkflowId);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.correct();

        // Check
        assertEquals(EMPTY_LIST, amendments);
    }

    @Test
    public void correctShouldUpdateIdWhenStepBeingValidatedHasWrongId() throws Exception
    {
        // Set up
        final String issueKey = "ABC-123";
        final String statusCode = "knownCode";
        final String statusName = "The Status";
        final long workflowEntryId = 456;
        final GenericValue mockIssue = setUpIssues(10).get(0);
        when(mockIssue.getString("key")).thenReturn(issueKey);
        when(mockIssue.getString("status")).thenReturn(statusCode);
        when(mockIssue.getLong("workflowId")).thenReturn(workflowEntryId);
        final Status mockStatus = mock(Status.class);
        when(mockStatus.getName()).thenReturn(statusName);
        when(mockConstantsManager.getStatusObject(statusCode)).thenReturn(mockStatus);
        when(mockI18nHelper.getText("admin.integrity.check.workflow.current.step.validate.message", issueKey,
                String.valueOf(workflowEntryId)))
                .thenReturn(AMENDMENT_MESSAGE);
        final JiraWorkflow mockWorkflow = mock(JiraWorkflow.class);
        when(mockWorkflowManager.getWorkflow(mockIssue)).thenReturn(mockWorkflow);
        final GenericValue mockStepGV = mock(GenericValue.class);
        when(mockOfBizDelegator.findByAnd("OSCurrentStep", singletonMap("entryId", workflowEntryId)))
                .thenReturn(singletonList(mockStepGV));
        final StepDescriptor mockStepInWorkflow = mock(StepDescriptor.class);
        final int stepInWorkflowId = 789;
        when(mockStepInWorkflow.getId()).thenReturn(stepInWorkflowId);
        when(mockWorkflow.getLinkedStep(mockStatus)).thenReturn(mockStepInWorkflow);
        when(mockStepGV.getInteger("stepId")).thenReturn(stepInWorkflowId + 1);

        // Invoke
        @SuppressWarnings("unchecked")
        final List<CheckAmendment> amendments = checkUnderTest.correct();

        // Check
        assertEquals(1, amendments.size());
        assertAmendment(ERROR, AMENDMENT_MESSAGE, "JRA-4539", amendments.get(0));
        verify(mockStepGV).set("stepId", stepInWorkflowId);
        verify(mockStepGV).store();
    }

    private void assertAmendment(final int expectedType, final String expectedMessage, final String expectedIssueKey,
            final CheckAmendment amendment)
    {
        assertEquals(expectedIssueKey, amendment.getBugId());
        assertEquals(expectedMessage, amendment.getMessage());
        assertEquals(expectedType, amendment.getType());
    }
}
