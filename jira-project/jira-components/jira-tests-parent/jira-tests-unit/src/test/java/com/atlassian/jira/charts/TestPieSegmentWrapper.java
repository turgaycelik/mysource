package com.atlassian.jira.charts;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestPieSegmentWrapper extends MockControllerTestCase
{

    @Test
    public void testProject()
    {
        final MockGenericValue mockprojectGV = createProjectMock("homosapien", 10000);

        final MockGenericValue mockprojectGV2 = createProjectMock("monkey", 900);

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);

        mockController.replay();

        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(mockprojectGV, mockI18nBean, "project", null, null);
        assertEquals(mockprojectGV, pieSegmentWrapper.getKey());
        assertEquals("homosapien", pieSegmentWrapper.getName());
        assertEquals("homosapien", pieSegmentWrapper.toString());

        final PieSegmentWrapper pieSegmentWrapper2 = new PieSegmentWrapper(mockprojectGV2, mockI18nBean, "project", null, null);
        assertTrue(pieSegmentWrapper.compareTo(pieSegmentWrapper2) < 0);
        assertEquals(0, pieSegmentWrapper.compareTo(pieSegmentWrapper));
        assertTrue(pieSegmentWrapper2.compareTo(pieSegmentWrapper) > 0);
    }

    @Test
    public void testAssignees()
    {
        final User user = new MockUser("admin", "Administrator", "");

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        mockI18nBean.getText("gadget.filterstats.assignee.unassigned");
        mockController.setReturnValue("Not assigned!");

        mockController.replay();

        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(user, mockI18nBean, "assignees", null, null);

        assertEquals(user, pieSegmentWrapper.getKey());
        assertEquals("Administrator", pieSegmentWrapper.getName());
        assertEquals("Administrator", pieSegmentWrapper.toString());

        final PieSegmentWrapper nullWrapper = new PieSegmentWrapper(null, mockI18nBean, "assignees", null, null);
        assertEquals(null, nullWrapper.getKey());
        assertEquals("Not assigned!", nullWrapper.getName());
        assertEquals("Not assigned!", nullWrapper.toString());
    }

    @Test
    public void testReporter()
    {
        final User user = new MockUser("admin", "Administrator", "");

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        mockI18nBean.getText("gadget.filterstats.reporter.unknown");
        mockController.setReturnValue("No reporter!");

        mockController.replay();

        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(user, mockI18nBean, "reporter", null, null);

        assertEquals(user, pieSegmentWrapper.getKey());
        assertEquals("Administrator", pieSegmentWrapper.getName());
        assertEquals("Administrator", pieSegmentWrapper.toString());

        final PieSegmentWrapper nullWrapper = new PieSegmentWrapper(null, mockI18nBean, "reporter", null, null);
        assertEquals(null, nullWrapper.getKey());
        assertEquals("No reporter!", nullWrapper.getName());
        assertEquals("No reporter!", nullWrapper.toString());
    }

    @Test
    public void testResolution()
    {
        assertConstantPieSegment("resolution", "Resolved", "common.resolution.unresolved", "Unresolved");
    }

    @Test
    public void testPriorities()
    {
        assertConstantPieSegment("priorities", "High", "gadget.filterstats.priority.nopriority", "No Priority!");
    }

    @Test
    public void testIssueType()
    {
        assertConstantPieSegment("issuetype", "Bug", null, null);
    }

    @Test
    public void testStatuses()
    {
        assertConstantPieSegment("statuses", "Open", null, null);
    }

    @Test
    public void testIrrelevant()
    {
        final PieSegmentWrapper irrelevantWrapper = new PieSegmentWrapper(FilterStatisticsValuesGenerator.IRRELEVANT, new MockI18nBean(), "blah", null, null);

        assertFalse(irrelevantWrapper.isGenerateUrl());
        assertEquals("Irrelevant", irrelevantWrapper.getName());
        assertNull(irrelevantWrapper.getKey());
    }

    private void assertConstantPieSegment(final String statisticType, final String nameTranslation, final String i18nNullKey, final String i18nNullValue)
    {

        final IssueConstant mockIssueConstant = mockController.getMock(IssueConstant.class);
        final ConstantsManager mockConstantsManager = mockController.getMock(ConstantsManager.class);
        mockIssueConstant.getNameTranslation();
        mockController.setReturnValue(nameTranslation);

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        //some constants can't be null (issuetype, status)
        if (i18nNullKey != null)
        {
            mockI18nBean.getText(i18nNullKey);
            mockController.setReturnValue(i18nNullValue);
        }

        mockController.replay();

        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(mockIssueConstant, mockI18nBean, statisticType, mockConstantsManager, null);

        assertEquals(mockIssueConstant, pieSegmentWrapper.getKey());
        assertEquals(nameTranslation, pieSegmentWrapper.getName());
        assertEquals(nameTranslation, pieSegmentWrapper.toString());

        //some constants can't be null (issuetype, status)
        if (i18nNullKey != null)
        {
            final PieSegmentWrapper nullWrapper = new PieSegmentWrapper(null, mockI18nBean, statisticType, mockConstantsManager, null);
            assertEquals(null, nullWrapper.getKey());
            assertEquals(i18nNullValue, nullWrapper.getName());
            assertEquals(i18nNullValue, nullWrapper.toString());
        }
    }

    @Test
    public void testComponents()
    {
        final MockGenericValue mockGV = new MockGenericValue("component", EasyMap.build("id", (long) 10000, "name", "Security"));

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        mockI18nBean.getText("gadget.filterstats.component.nocomponent");
        mockController.setReturnValue("No components!");
        mockController.replay();

        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(mockGV, mockI18nBean, "components", null, null);
        assertEquals(mockGV, pieSegmentWrapper.getKey());
        assertEquals("Security", pieSegmentWrapper.getName());
        assertEquals("Security", pieSegmentWrapper.toString());

        final PieSegmentWrapper nullWrapper = new PieSegmentWrapper(null, mockI18nBean, "components", null, null);
        assertEquals(null, nullWrapper.getKey());
        assertEquals("No components!", nullWrapper.getName());
        assertEquals("No components!", nullWrapper.toString());
    }

    @Test
    public void testVersion()
    {
        final Version mockVersion = mockController.getMock(Version.class);
        mockVersion.getName();
        mockController.setReturnValue("Version 1", 2);

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        mockI18nBean.getText("gadget.filterstats.raisedin.unscheduled");
        mockController.setReturnValue("Unscheduled");
        mockController.replay();

        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(mockVersion, mockI18nBean, "version", null, null);
        assertEquals(mockVersion, pieSegmentWrapper.getKey());
        assertEquals("Version 1", pieSegmentWrapper.getName());
        assertEquals("Version 1", pieSegmentWrapper.toString());

        final PieSegmentWrapper allWrapper = new PieSegmentWrapper(mockVersion, mockI18nBean, "allVersion", null, null);
        assertEquals(mockVersion, allWrapper.getKey());
        assertEquals("Version 1", allWrapper.getName());
        assertEquals("Version 1", allWrapper.toString());


        final PieSegmentWrapper nullWrapper = new PieSegmentWrapper(null, mockI18nBean, "version", null, null);
        assertEquals(null, nullWrapper.getKey());
        assertEquals("Unscheduled", nullWrapper.getName());
        assertEquals("Unscheduled", nullWrapper.toString());
    }

    @Test
    public void testFixForVersion()
    {
        final Version mockVersion = mockController.getMock(Version.class);
        mockVersion.getName();
        mockController.setReturnValue("Fix Version 1", 2);

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        mockI18nBean.getText("gadget.filterstats.fixfor.unscheduled");
        mockController.setReturnValue("Unscheduled Fix");
        mockController.replay();

        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(mockVersion, mockI18nBean, "fixfor", null, null);
        assertEquals(mockVersion, pieSegmentWrapper.getKey());
        assertEquals("Fix Version 1", pieSegmentWrapper.getName());
        assertEquals("Fix Version 1", pieSegmentWrapper.toString());

        final PieSegmentWrapper allWrapper = new PieSegmentWrapper(mockVersion, mockI18nBean, "allFixfor", null, null);
        assertEquals(mockVersion, allWrapper.getKey());
        assertEquals("Fix Version 1", allWrapper.getName());
        assertEquals("Fix Version 1", allWrapper.toString());


        final PieSegmentWrapper nullWrapper = new PieSegmentWrapper(null, mockI18nBean, "fixfor", null, null);
        assertEquals(null, nullWrapper.getKey());
        assertEquals("Unscheduled Fix", nullWrapper.getName());
        assertEquals("Unscheduled Fix", nullWrapper.toString());
    }

    @Test
    public void testCustomField()
    {
        final String customFieldValue = "theValue";


        final CustomField mockCustomField = mockController.getMock(CustomField.class);

        final MockControl mockCustomFieldSearcherModuleDescriptorControl = MockClassControl.createControl(CustomFieldSearcherModuleDescriptor.class);
        final CustomFieldSearcherModuleDescriptor mockCustomFieldSearcherModuleDescriptor = (CustomFieldSearcherModuleDescriptor) mockCustomFieldSearcherModuleDescriptorControl.getMock();
        mockCustomFieldSearcherModuleDescriptor.getStatHtml(mockCustomField, customFieldValue, null);
        mockCustomFieldSearcherModuleDescriptorControl.setReturnValue("<html>theValue</html>");
        mockCustomFieldSearcherModuleDescriptorControl.replay();

        final CustomFieldSearcher mockCustomFieldSearcher = mockController.getMock(CustomFieldSearcher.class);
        final CustomFieldManager mockCustomFieldManager = mockController.getMock(CustomFieldManager.class);
        mockCustomFieldManager.getCustomFieldObject("customfield_1025");
        mockController.setReturnValue(mockCustomField);
        mockCustomField.getCustomFieldSearcher();
        mockController.setReturnValue(mockCustomFieldSearcher);
        mockCustomFieldSearcher.getDescriptor();
        mockController.setReturnValue(mockCustomFieldSearcherModuleDescriptor);

        final I18nHelper mockI18nBean = mockController.getMock(I18nHelper.class);
        mockI18nBean.getText("common.words.none");
        mockController.setReturnValue("None");
        mockController.replay();

        final PieSegmentWrapper pieSegmentWrapper = new PieSegmentWrapper(customFieldValue, mockI18nBean, "customfield_1025", null, mockCustomFieldManager);
        assertEquals(customFieldValue, pieSegmentWrapper.getKey());
        assertEquals("<html>theValue</html>", pieSegmentWrapper.getName());
        assertEquals("<html>theValue</html>", pieSegmentWrapper.toString());

        final PieSegmentWrapper nullWrapper = new PieSegmentWrapper(null, mockI18nBean, "customfield_1023", null, mockCustomFieldManager);
        assertEquals(null, nullWrapper.getKey());
        assertEquals("None", nullWrapper.getName());
        assertEquals("None", nullWrapper.toString());

        mockCustomFieldSearcherModuleDescriptorControl.verify();
    }

    private MockGenericValue createProjectMock(final String name, final long id) {
        return new MockGenericValue("project", EasyMap.build("id", id, "name", name));
    }
}
