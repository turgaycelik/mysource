package com.atlassian.jira.issue.views;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.BuildUtilsInfo;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests a the IssueXMLView in a very simple way.
 */
public class TestIssueXMLView
{
    private BuildUtilsInfo buildUtilsInfo;

    @Before
    public void setUp() throws Exception
    {
        buildUtilsInfo = EasyMock.createMock(BuildUtilsInfo.class);
    }

    // All the mocking in this method is just so that we can get the call to see if an XML view
    // exists for the CustomField to return false. In this case we want to make sure we get back
    // and empty string
    @Test
    public void testGetCustomFieldXMLNoXMLTemplateExists()
    {
        MockIssue mockIssue = new MockIssue(1L);
        Mock mockIssueTypeObject = new Mock(IssueType.class);
        mockIssueTypeObject.expectAndReturn("getId", "1");
        mockIssue.setIssueTypeObject((IssueType) mockIssueTypeObject.proxy());

        MockControl ctrlCustomField = MockControl.createControl(CustomField.class);
        MockControl ctrlFieldLayoutMgr = MockControl.createControl(FieldLayoutManager.class);
        MockControl ctrlCustomFieldTypeModuleDescriptor = MockClassControl.createControl(CustomFieldTypeModuleDescriptor.class);

        CustomField mockCustomField = (CustomField) ctrlCustomField.getMock();
        FieldLayoutManager mockFieldLayoutManager = (FieldLayoutManager) ctrlFieldLayoutMgr.getMock();

        mockCustomField.getCustomFieldType();
        Mock mockCustomFieldType = new Mock(CustomFieldType.class);

        CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor = (CustomFieldTypeModuleDescriptor) ctrlCustomFieldTypeModuleDescriptor.getMock();
        customFieldTypeModuleDescriptor.isXMLTemplateExists();
        ctrlCustomFieldTypeModuleDescriptor.setReturnValue(false);
        ctrlCustomFieldTypeModuleDescriptor.replay();

        mockCustomFieldType.expectAndReturn("getDescriptor", customFieldTypeModuleDescriptor);
        ctrlCustomField.setDefaultReturnValue(mockCustomFieldType.proxy());

        ctrlCustomField.replay();
        mockFieldLayoutManager.getFieldLayout(mockIssue);

        Mock mockFieldLayout = new Mock(FieldLayout.class);
        mockFieldLayout.expectAndReturn("getFieldLayoutItem", P.ANY_ARGS, null);
        ctrlFieldLayoutMgr.setReturnValue(mockFieldLayout.proxy());

        ctrlFieldLayoutMgr.replay();
        IssueXMLView issueXMLView = new IssueXMLView(null, null, mockFieldLayoutManager, null, null, null, buildUtilsInfo, null, null);

        assertEquals("", issueXMLView.getCustomFieldXML(mockCustomField, mockIssue));
    }

    // All the mocking in this method is just so that we can get the call to see if an XML view
    // exists for the CustomField to return true. We then want the subsequent call to get the xml
    // view data to return null, and we want to assert that the method returns empty string.
    @Test
    public void testGetCustomFieldXMLTemplateExistsButReturnsNull()
    {
        MockIssue mockIssue = new MockIssue(new Long(1));
        Mock mockIssueTypeObject = new Mock(IssueType.class);
        mockIssueTypeObject.expectAndReturn("getId", "1");
        mockIssue.setIssueTypeObject((IssueType) mockIssueTypeObject.proxy());

        MockControl ctrlCustomField = MockControl.createControl(CustomField.class);
        MockControl ctrlFieldLayoutMgr = MockControl.createControl(FieldLayoutManager.class);

        CustomField mockCustomField = (CustomField) ctrlCustomField.getMock();
        FieldLayoutManager mockFieldLayoutManager = (FieldLayoutManager) ctrlFieldLayoutMgr.getMock();

        mockCustomField.getCustomFieldType();
        Mock mockCustomFieldType = new Mock(CustomFieldType.class);
        MockControl ctrlCustomFieldTypeModuleDescriptor = MockClassControl.createNiceControl(CustomFieldTypeModuleDescriptor.class);
        CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor = (CustomFieldTypeModuleDescriptor) ctrlCustomFieldTypeModuleDescriptor.getMock();
        customFieldTypeModuleDescriptor.isXMLTemplateExists();
        ctrlCustomFieldTypeModuleDescriptor.setReturnValue(true);
        customFieldTypeModuleDescriptor.getViewXML(null, null, null, false);
        ctrlCustomFieldTypeModuleDescriptor.setReturnValue(null);
        ctrlCustomFieldTypeModuleDescriptor.replay();

        mockCustomFieldType.expectAndReturn("getDescriptor", customFieldTypeModuleDescriptor);
        ctrlCustomField.setDefaultReturnValue(mockCustomFieldType.proxy());

        mockCustomField.getId();
        ctrlCustomField.setReturnValue("");
        ctrlCustomField.replay();
        //FieldLayoutManager
        mockFieldLayoutManager.getFieldLayout(mockIssue);

        Mock mockFieldLayout = new Mock(FieldLayout.class);
        mockFieldLayout.expectAndReturn("getFieldLayoutItem", P.ANY_ARGS, null);
        ctrlFieldLayoutMgr.setReturnValue(mockFieldLayout.proxy());

        ctrlFieldLayoutMgr.replay();
        IssueXMLView issueXMLView = new IssueXMLView(null, null, mockFieldLayoutManager, null, null, null, buildUtilsInfo, null, null);

        assertEquals("", issueXMLView.getCustomFieldXML(mockCustomField, mockIssue));

    }

    // All the mocking in this method is just so that we can get the call to see if an XML view
    // exists for the CustomField to return true. We then want the subsequent call to get the xml
    // view data to return SOMETHING, and we want to assert that the method returns that SOMETHING.
    @Test
    public void testGetCustomFieldXMLGivesUsStuff()
    {
        String testReturnValue = "SOMETHING";

        MockIssue mockIssue = new MockIssue(new Long(1));
        Mock mockIssueTypeObject = new Mock(IssueType.class);
        mockIssueTypeObject.expectAndReturn("getId", "1");
        mockIssue.setIssueTypeObject((IssueType) mockIssueTypeObject.proxy());

        MockControl ctrlCustomField = MockControl.createControl(CustomField.class);
        MockControl ctrlFieldLayoutMgr = MockControl.createControl(FieldLayoutManager.class);

        CustomField mockCustomField = (CustomField) ctrlCustomField.getMock();
        FieldLayoutManager mockFieldLayoutManager = (FieldLayoutManager) ctrlFieldLayoutMgr.getMock();

        mockCustomField.getCustomFieldType();
        Mock mockCustomFieldType = new Mock(CustomFieldType.class);
        MockControl ctrlCustomFieldTypeModuleDescriptor = MockClassControl.createNiceControl(CustomFieldTypeModuleDescriptor.class);
        CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor = (CustomFieldTypeModuleDescriptor) ctrlCustomFieldTypeModuleDescriptor.getMock();
        customFieldTypeModuleDescriptor.isXMLTemplateExists();
        ctrlCustomFieldTypeModuleDescriptor.setReturnValue(true);

        customFieldTypeModuleDescriptor.getViewXML(mockCustomField, mockIssue, null, false);
        ctrlCustomFieldTypeModuleDescriptor.setReturnValue(testReturnValue);
        ctrlCustomFieldTypeModuleDescriptor.replay();

        mockCustomFieldType.expectAndReturn("getDescriptor", customFieldTypeModuleDescriptor);
        ctrlCustomField.setDefaultReturnValue(mockCustomFieldType.proxy());

        mockCustomField.getId();
        ctrlCustomField.setReturnValue("");
        ctrlCustomField.replay();
        //FieldLayoutManager
        mockFieldLayoutManager.getFieldLayout(mockIssue);

        Mock mockFieldLayout = new Mock(FieldLayout.class);
        mockFieldLayout.expectAndReturn("getFieldLayoutItem", P.ANY_ARGS, null);
        ctrlFieldLayoutMgr.setReturnValue(mockFieldLayout.proxy());

        ctrlFieldLayoutMgr.replay();
        IssueXMLView issueXMLView = new IssueXMLView(null, null, mockFieldLayoutManager, null, null, null, buildUtilsInfo, null, null);

        assertEquals(testReturnValue, issueXMLView.getCustomFieldXML(mockCustomField, mockIssue));

    }

}
