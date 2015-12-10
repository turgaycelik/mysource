package com.atlassian.jira.issue.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.operation.WorkflowIssueOperation;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.user.MockUserHistoryManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.workflow.JiraWorkflow;

import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestResolutionSystemField extends MockControllerTestCase
{

    private JiraAuthenticationContext authenticationContext;

    @Before
    public void setUp() throws Exception
    {
        authenticationContext = createMock(JiraAuthenticationContext.class);
        expect(authenticationContext.getLoggedInUser()).andStubReturn(new MockUser("userName"));
    }

    @Test
    public void testGetExcludeIncludeResolutionIds()
    {
        Map attributeMap = new HashMap();
        attributeMap.put(JiraWorkflow.JIRA_META_ATTRIBUTE_EXCLUDE_RESOLUTION, "1,3");

        final ActionDescriptor mockActionDescription = DescriptorFactory.getFactory().createActionDescriptor();
        mockActionDescription.getMetaAttributes();
        mockActionDescription.setMetaAttributes(attributeMap);

        final WorkflowIssueOperation mockWorkflowIssueOperation = createMock(WorkflowIssueOperation.class);
        expect(mockWorkflowIssueOperation.getActionDescriptor()).andReturn(mockActionDescription);

        OperationContext mockOperationContext = createMock(OperationContext.class);
        expect(mockOperationContext.getIssueOperation()).andReturn(mockWorkflowIssueOperation);
        expect(mockOperationContext.getIssueOperation()).andReturn(mockWorkflowIssueOperation);

        replay();

        ResolutionSystemField resolutionSystemField = new ResolutionSystemField(null, null, null, null, null, null, null, new MockUserHistoryManager(), null);
        Collection collection = resolutionSystemField.getIncludeResolutionIds(mockOperationContext, JiraWorkflow.JIRA_META_ATTRIBUTE_EXCLUDE_RESOLUTION);
        assertTrue(collection.size() == 2);
        assertTrue(collection.contains("1"));
        assertTrue(collection.contains("3"));
    }

    public OperationContext getOperationContext(String type, String resolutionString)
    {
        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(type, resolutionString);

        final ActionDescriptor mockActionDescription = DescriptorFactory.getFactory().createActionDescriptor();
        mockActionDescription.getMetaAttributes();
        mockActionDescription.setMetaAttributes(attributeMap);

        final WorkflowIssueOperation mockWorkflowIssueOperation = createMock(WorkflowIssueOperation.class);
        expect(mockWorkflowIssueOperation.getActionDescriptor()).andReturn(mockActionDescription);
        expect(mockWorkflowIssueOperation.getActionDescriptor()).andReturn(mockActionDescription);

        OperationContext mockOperationContext = createMock(OperationContext.class);
        expect(mockOperationContext.getIssueOperation()).andStubReturn(mockWorkflowIssueOperation);

        return mockOperationContext;
    }

    public Resolution getResolutionMock(String resolutionId)
    {
        Resolution mockResolution = EasyMock.createMock(Resolution.class);
        expect(mockResolution.getId()).andReturn(resolutionId).atLeastOnce();
        expect(mockResolution.getName()).andStubReturn(resolutionId);
        EasyMock.replay(mockResolution);
        return mockResolution;
    }

    @Test
    public void testRetrieveResolutionsExclude()
    {
        Resolution mockResolution1 = getResolutionMock("1");
        Resolution mockResolution2 = getResolutionMock("2");
        Resolution mockResolution3 = getResolutionMock("3");
        Resolution mockResolution4 = getResolutionMock("4");
        Resolution mockResolution5 = getResolutionMock("5");

        Collection<Resolution> resolutionList = new ArrayList<Resolution>();
        resolutionList.add(mockResolution1);
        resolutionList.add(mockResolution2);
        resolutionList.add(mockResolution3);
        resolutionList.add(mockResolution4);
        resolutionList.add(mockResolution5);

        ConstantsManager mockConstantsManager = createMock(ConstantsManager.class);
        expect(mockConstantsManager.getResolutionObjects()).andReturn(resolutionList);

        OperationContext mockOperationContext = getOperationContext(JiraWorkflow.JIRA_META_ATTRIBUTE_EXCLUDE_RESOLUTION, "1,3");

        replay();

        ResolutionSystemField resolutionSystemField = new ResolutionSystemField(null, null, mockConstantsManager, authenticationContext, null, null, null, new MockUserHistoryManager(), null);
        Collection collection = resolutionSystemField.retrieveResolutions(mockOperationContext, null, Maps.<String, Object>newHashMap());
        assertTrue(collection.size() == 3);
        assertTrue(collection.contains(mockResolution2));
        assertTrue(collection.contains(mockResolution4));
        assertTrue(collection.contains(mockResolution5));
    }

    @Test
    public void testRetrieveResolutionsInclude()
    {
        Resolution mockResolution1 = getResolutionMock("1");
        Resolution mockResolution2 = getResolutionMock("2");
        Resolution mockResolution3 = getResolutionMock("3");
        Resolution mockResolution4 = getResolutionMock("4");
        Resolution mockResolution5 = getResolutionMock("5");

        Collection<Resolution> resolutionList = new ArrayList<Resolution>();
        resolutionList.add(mockResolution1);
        resolutionList.add(mockResolution2);
        resolutionList.add(mockResolution3);
        resolutionList.add(mockResolution4);
        resolutionList.add(mockResolution5);

        ConstantsManager mockConstantsManager = createMock(ConstantsManager.class);
        expect(mockConstantsManager.getResolutionObjects()).andReturn(resolutionList);

        OperationContext mockOperationContext = getOperationContext(JiraWorkflow.JIRA_META_ATTRIBUTE_INCLUDE_RESOLUTION, "1,3");

        replay();

        ResolutionSystemField resolutionSystemField = new ResolutionSystemField(null, null, mockConstantsManager, authenticationContext, null, null, null, new MockUserHistoryManager(), null);
        Collection collection = resolutionSystemField.retrieveResolutions(mockOperationContext, null, Maps.<String, Object>newHashMap());
        assertTrue(collection.size() == 2);
        assertTrue(collection.contains(mockResolution1));
        assertTrue(collection.contains(mockResolution3));
    }
}
